package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.core.PendingPopRequests;
import se.rosenbaum.poppoc.core.PopRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

@WebServlet(urlPatterns = "/GeneratePopRequest/*", name = "GeneratePopRequest")
public class GeneratePopRequest extends HttpServlet {
    public static final String POP_DESTINATION="http://localhost:9090/Pop";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String txid = request.getParameter("txid");
        Long amount = parseLong(request.getParameter("amount"));
        String text = request.getParameter("text");

        Random random = new SecureRandom();

        byte[] nonceBytes = new byte[8];
        random.nextBytes(nonceBytes);
        nonceBytes[0] = 0;
        nonceBytes[1] = 0;
        nonceBytes[2] = 0;
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        PopRequest popRequest = new PopRequest(nonce);
        popRequest.setAmount(amount);
        popRequest.setText(text);
        popRequest.setTxid(txid);

        PendingPopRequests pendingPopRequests = (PendingPopRequests) request.getServletContext().getAttribute("pendingPopRequests");
        int requestId = pendingPopRequests.store(popRequest);

        String popRequestUri = "bitcoin:?p=" + URLEncoder.encode(POP_DESTINATION, "UTF-8") + "/" + requestId;

        popRequestUri += "&nonce=" + nonce;

        if (isSet(txid)) {
            popRequestUri += "&txid=" + urlEncode(txid);
        }

        if (amount != null) {
            popRequestUri += "&amount=" + amount;
        }

        if (isSet(text)) {
            popRequestUri += "&text=" + urlEncode(text);
        }

        request.setAttribute("popRequest", popRequestUri);
        request.setAttribute("popRequestUrlEncoded", urlEncode(popRequestUri));
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/popRequest.jsp");
        requestDispatcher.forward(request, response);
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
