package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Coin;
import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.service.ServiceType;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

public class PopRequestServlet extends BasicServlet {

    protected void createPopRequest(HttpServletRequest request, HttpServletResponse response, ServiceType nakedServiceType) throws ServletException, IOException {
        PopRequest popRequest = nakedServiceType.getPopRequest();

        Storage storage = getStorage();
        int requestId = storage.store(popRequest);

        String contextPath = getServletContext().getContextPath();
        String popRequestUri = createPopRequestUri(popRequest, contextPath, requestId);
        String popPollUrl = craetePopPollUrl(requestId, contextPath);

        request.setAttribute(JspConst.POP_REQUEST.val(), popRequestUri);
        request.setAttribute(JspConst.POP_REQUEST_URL_ENCODED.val(), urlEncode(popRequestUri));
        request.setAttribute(JspConst.POP_POLL_URL.val(), popPollUrl);
        request.setAttribute(JspConst.SERVICE_TYPE.val(), nakedServiceType);
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_POP_REQUEST_ID, requestId);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/popRequest.jsp");
        requestDispatcher.forward(request, response);
    }

    private String craetePopPollUrl(int requestId, String contextPath) {
        String popPollUrl = getConfig().getPopDesitnation() + contextPath + "/PopPoll/?" + JspConst.REQUEST_ID.val() + "=" + requestId;
        return popPollUrl;
    }

    protected String createPopRequestUri(PopRequest popRequest, String contextPath, int requestId) throws UnsupportedEncodingException {
        String popUrl = getConfig().getPopDesitnation() + contextPath + "/Pop/" + requestId;
        return popRequest.createPopRequestUri(popUrl);
    }
}
