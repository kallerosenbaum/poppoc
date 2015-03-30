package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.core.Storage;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

public class PopRequestServlet extends HttpServlet {
    protected void createPopRequest(HttpServletRequest request, HttpServletResponse response, String txid, Long amount, String text) throws ServletException, IOException {
        PopRequest popRequest = createPopRequest(txid, amount, text);

        Storage pendingPopRequests = (Storage)getServletContext().getAttribute("storage");
        int requestId = pendingPopRequests.store(popRequest);


        String popRequestUri = createPopRequestUri(popRequest, getServletContext().getContextPath(), requestId);

        request.setAttribute("popRequest", popRequestUri);
        request.setAttribute("popRequestUrlEncoded", urlEncode(popRequestUri));
        request.setAttribute("requestId", requestId);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/popRequest.jsp");
        requestDispatcher.forward(request, response);
    }

    protected String createPopRequestUri(PopRequest popRequest, String contextPath, int requestId) throws UnsupportedEncodingException {
        String popUrl = Config.POP_DESTINATION + contextPath + "/Pop/" + requestId;
        String popRequestUri = "btcpop:?p=" + URLEncoder.encode(popUrl, "UTF-8");

        popRequestUri += "&nonce=" + popRequest.getNonce();

        if (isSet(popRequest.getTxid())) {
            popRequestUri += "&txid=" + urlEncode(popRequest.getTxid());
        }

        if (popRequest.getAmount() != null) {
            popRequestUri += "&amount=" + popRequest.getAmount();
        }

        if (isSet(popRequest.getText())) {
            popRequestUri += "&text=" + urlEncode(popRequest.getText());
        }
        return popRequestUri;
    }

    protected PopRequest createPopRequest(String txid, Long amount, String text) {
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
        return popRequest;
    }

    protected String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    protected boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }


}
