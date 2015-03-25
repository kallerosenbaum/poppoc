package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@WebServlet(urlPatterns = "/RequestPayment/*", name = "RequestPayment")
public class RequestPayment extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(RequestPayment.class);

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String serviceId = request.getParameter("serviceId");
        if (serviceId == null || "".equals(serviceId.trim())) {
            logger.error("serviceId is empty or null");
            throw new RuntimeException("ServiceId is null or empty");
        }

        Wallet wallet = (Wallet) request.getServletContext().getAttribute("wallet");
        Address address = wallet.currentReceiveAddress();
        logger.debug("Generating address {}", address.toString());

        String paymentUri = "bitcoin:" + URLEncoder.encode(address.toString(), "UTF-8");

        paymentUri = appendParam(request, "amount", paymentUri);
        paymentUri = appendParam(request, "label", paymentUri);
        paymentUri = appendParam(request, "message", paymentUri);

        Storage storage = (Storage)request.getServletContext().getAttribute("storage");
        storage.storePendingPayment(address, serviceId);

        request.setAttribute("receiveAddress", address);
        request.setAttribute("serviceId", serviceId);
        request.setAttribute("paymentUri", paymentUri);
        request.setAttribute("paymentUriUrlEncoded", urlEncode(paymentUri));
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

    private String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Long parseLong(String value) {
        if (!isSet(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

}
