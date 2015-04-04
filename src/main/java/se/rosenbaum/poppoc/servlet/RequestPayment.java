package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;

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
        String serviceId = request.getParameter(JspConst.SERVICE_ID.val());
        if (serviceId == null || "".equals(serviceId.trim())) {
            logger.error("serviceId is empty or null");
            throw new RuntimeException("ServiceId is null or empty");
        }

        Wallet wallet = getWallet();
        Address address = wallet.currentReceiveAddress();
        logger.debug("Generating address {}", address.toString());

        String paymentUri = "bitcoin:" + urlEncode(address.toString());

        paymentUri = appendParam(request, "amount", paymentUri);
        paymentUri = appendParam(request, "label", paymentUri);
        paymentUri = appendParam(request, "message", paymentUri);

        Storage storage = getStorage();
        storage.storePendingPayment(address, serviceId);

        request.setAttribute(JspConst.RECEIVE_ADDRESS.val(), address);
        request.setAttribute(JspConst.SERVICE_ID.val(), serviceId);
        request.setAttribute(JspConst.PAYMENT_URI.val(), paymentUri);
        request.setAttribute(JspConst.PAYMENT_URI_URL_ENCODED.val(), urlEncode(paymentUri));
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/requestPayment.jsp");
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
