package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;
import se.rosenbaum.poppoc.core.ClientException;
import se.rosenbaum.poppoc.core.Config;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalle on 2015-04-02.
 */
public class BasicServlet extends HttpServlet {
    public static final String SESSION_POP_REQUEST_ID = "popRequestId";
    private static final String SESSION_AUTHORIZED_FOR_SERVICE = "authorizedForService";

    protected boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected int getServiceId(HttpServletRequest request) {
        String serviceIdString = request.getParameter(JspConst.SERVICE_ID.val());
        if (!isSet(serviceIdString)) {
            throw new ClientException("ServiceId is null or empty");
        }
        return Integer.parseInt(serviceIdString);
    }

    public enum JspConst {

        VALID_POP_RECEIVED("VALID POP RECEIVED"),
        POP_NOT_RECEIVED_YET("POP NOT RECEIVED YET"),
        SERVICE_ID("serviceId"),
        RECEIVE_ADDRESS("receiveAddress"),
        PAYMENT_URI("paymentUri"),
        PAYMENT_URI_URL_ENCODED("paymentUriUrlEncoded"),
        PAYMENT_POLL_URL("paymentPollUrl"),
        POP_REQUEST("popRequest"),
        POP_REQUEST_URL_ENCODED("popRequestUrlEncoded"),
        POP_POLL_URL("popPollUrl"),
        PAYMENT_RECEIVED("PAYMENT RECEIVED"),
        PAYMENT_NOT_RECEIVED_YET("PAYMENT NOT RECEIVED YET"),
        REQUEST_ID("requestId"),
        SERVICE_TYPE("serviceType"),
        CALLBACK_URL("callbackUrl");

        private String value;

        JspConst(String value) {
            this.value = value;
        }

        public String val() {
            return this.value;
        }

    }


    protected void addServiceToSession(HttpSession session, int serviceId) {
        session.setAttribute(SESSION_AUTHORIZED_FOR_SERVICE + serviceId, new Date());
    }

    protected void removeServiceFromSession(HttpSession session, int serviceId) {
        if (session == null) {
            return;
        }
        session.removeAttribute(SESSION_AUTHORIZED_FOR_SERVICE + serviceId);
    }

    protected boolean isAuthorized(HttpSession session, int serviceId) {
        Date timeOfAuthentication = (Date)session.getAttribute(SESSION_AUTHORIZED_FOR_SERVICE + serviceId);
        if (timeOfAuthentication == null) {
            return false;
        }
        // We could check limit the time an authentication is valid here. But for now
        // we authorize indefinately or until logout.
        return true;
    }

    @Override
    public void init() {
        Map<String, String> constants = new HashMap<String, String>();
        for (JspConst jspConst : JspConst.values()) {
            constants.put(jspConst.name(), jspConst.val());
        }
        getServletContext().setAttribute("constants", constants);
    }

    Wallet getWallet() {
        return (Wallet) getServletContext().getAttribute("wallet");
    }

    Config getConfig() {
        return (Config) getServletContext().getAttribute("config");
    }

    protected Storage getStorage() {
        return (Storage) getServletContext().getAttribute("storage");
    }

    protected String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}
