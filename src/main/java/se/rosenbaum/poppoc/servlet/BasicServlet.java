package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;

import javax.servlet.http.HttpServlet;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kalle on 2015-04-02.
 */
public class BasicServlet extends HttpServlet {
    public static final String SESSION_POP_REQUEST_ID = "popRequestId";

    public enum JspConst {

        VALID_POP_RECEIVED("VALID POP RECEIVED"),
        POP_NOT_RECEIVED_YET("POP NOT RECEIVED YET"),
        SERVICE_ID("serviceId"),
        RECEIVE_ADDRESS("receiveAddress"),
        PAYMENT_URI("paymentUri"),
        PAYMENT_URI_URL_ENCODED("paymentUriUrlEncoded"),
        POP_REQUEST("popRequest"),
        POP_REQUEST_URL_ENCODED("popRequestUrlEncoded"),
        POP_POLL_URL("popPollUrl"),
        PAYMENT_RECEIVED("PAYMENT RECEIVED"),
        PAYMENT_NOT_RECEIVED_YET("PAYMENT NOT RECEIVED YET"),
        REQUEST_ID("requestId");

        private String value;

        JspConst(String value) {
            this.value = value;
        }

        public String val() {
            return this.value;
        }

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

    Storage getStorage() {
        return (Storage) getServletContext().getAttribute("storage");
    }

    protected String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}
