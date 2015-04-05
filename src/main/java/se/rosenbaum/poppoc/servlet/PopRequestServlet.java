package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.core.Storage;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

public class PopRequestServlet extends BasicServlet {

    protected void createPopRequest(HttpServletRequest request, HttpServletResponse response, int serviceId, String txid, Long amount, String text) throws ServletException, IOException {
        PopRequest popRequest = createPopRequest(txid, amount, text, serviceId);

        Storage storage = getStorage();
        int requestId = storage.store(popRequest);


        String contextPath = getServletContext().getContextPath();
        String popRequestUri = createPopRequestUri(popRequest, contextPath, requestId);
        String popPollUrl = craetePopPollUrl(requestId, contextPath);

        request.setAttribute(JspConst.POP_REQUEST.val(), popRequestUri);
        request.setAttribute(JspConst.POP_REQUEST_URL_ENCODED.val(), urlEncode(popRequestUri));
        request.setAttribute(JspConst.POP_POLL_URL.val(), popPollUrl);
        request.setAttribute(JspConst.SERVICE_ID.val(), serviceId);
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_POP_REQUEST_ID, requestId);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/popRequest.jsp");
        requestDispatcher.forward(request, response);
    }

    private String craetePopPollUrl(int requestId, String contextPath) {
        String popPollUrl = getConfig().getPopDesitnation() + contextPath + "/PopPoll/?" + JspConst.REQUEST_ID.val() + "=" + requestId;
        return popPollUrl;
    }

    protected String createPopRequestUri(PopRequest popRequest, String contextPath, int requestId) throws UnsupportedEncodingException {
        String popUrl = getConfig().getPopDesitnation() + contextPath + "/Pop/" + requestId;
        String popRequestUri = "btcpop:?p=" + urlEncode(popUrl);

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

    protected PopRequest createPopRequest(String txid, Long amount, String text, int serviceId) {
        Random random = new SecureRandom();

        byte[] nonceBytes = new byte[8];
        random.nextBytes(nonceBytes);
        nonceBytes[0] = 0;
        nonceBytes[1] = 0;
        nonceBytes[2] = 0;
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        PopRequest popRequest = new PopRequest(nonce, serviceId);
        popRequest.setAmount(amount);
        popRequest.setText(text);
        popRequest.setTxid(txid);
        return popRequest;
    }

    protected boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }


}
