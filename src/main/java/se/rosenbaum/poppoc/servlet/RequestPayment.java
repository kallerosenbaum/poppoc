package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.ClientException;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;
import se.rosenbaum.poppoc.service.ServiceType;
import se.rosenbaum.poppoc.service.ServiceTypeFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@WebServlet(urlPatterns = "/RequestPayment/*", name = "RequestPayment")
public class RequestPayment extends BasicServlet {
    Logger logger = LoggerFactory.getLogger(RequestPayment.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String serviceIdString = request.getParameter(JspConst.SERVICE_ID.val());
        if (serviceIdString == null || "".equals(serviceIdString.trim())) {
            throw new ClientException("ServiceId is null or empty");
        }
        int serviceId = Integer.parseInt(serviceIdString);

        Wallet wallet = getWallet();
        Address address = wallet.getNewReceiveAddress();
        logger.debug("Generating address {}", address.toString());

        ServiceType serviceType = new ServiceTypeFactory().createServiceType(serviceId);
        serviceType.useParameters(request.getParameterMap());
        serviceType.setPaymentAddress(address);
        String paymentUri = serviceType.getPaymentUri(address);
        Storage storage = getStorage();
        storage.storePendingPayment(address, serviceType);

        String pollUrl = getConfig().getPopDesitnation() + request.getContextPath() + "/PaymentPoll?" +
                JspConst.RECEIVE_ADDRESS.val() + "=" + address;

        request.setAttribute(JspConst.SERVICE_TYPE.val(), serviceType);
        request.setAttribute(JspConst.PAYMENT_URI.val(), paymentUri);
        request.setAttribute(JspConst.PAYMENT_URI_URL_ENCODED.val(), urlEncode(paymentUri));
        request.setAttribute(JspConst.PAYMENT_POLL_URL.val(), pollUrl);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/requestPayment.jsp");
        requestDispatcher.forward(request, response);
    }

    private String appendParam(HttpServletRequest request, String param, String uri) throws UnsupportedEncodingException {
        String value = request.getParameter(param);
        if (value != null) {
            uri += (uri.contains("?") ? "&" : "?") + param + "=" + urlEncode(value);
        }
        return uri;
    }

}
