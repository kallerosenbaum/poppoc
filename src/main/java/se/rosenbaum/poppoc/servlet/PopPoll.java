package se.rosenbaum.poppoc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Storage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = "/PopPoll/*", name = "PopPoll")
public class PopPoll extends BasicServlet {
    private Logger logger = LoggerFactory.getLogger(PopPoll.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestIdString = request.getParameter(JspConst.REQUEST_ID.val());
        if (requestIdString == null) {
            logger.error("No requestId");
            throw new RuntimeException("No requestId");
        }
        int requestId;
        try {
            requestId = Integer.parseInt(requestIdString);
        } catch (NumberFormatException e) {
            logger.error("Malformed requestId {}", requestIdString);
            throw new RuntimeException("Malformed requestId: " + requestIdString, e);
        }
        HttpSession session = getSession(request);
        checkSessionPopRequestId(session, requestId);

        Storage storage = getStorage();
        Integer serviceId = storage.removeVerifiedPop(requestId);

        response.setContentType("text/plain; charset=US-ASCII");
        if (serviceId != null) {
            session.removeAttribute(SESSION_POP_REQUEST_ID);
            addServiceToSession(session, serviceId);
            response.getWriter().write(JspConst.VALID_POP_RECEIVED.val());
        } else {
            response.getWriter().write(JspConst.POP_NOT_RECEIVED_YET.val());
        }
    }

    private HttpSession getSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.error("No active session");
            throw new RuntimeException("No active session");
        }
        return session;
    }

    private void checkSessionPopRequestId(HttpSession session, int requestId) {
        Integer sessionRequestId = (Integer)session.getAttribute(SESSION_POP_REQUEST_ID);
        if (sessionRequestId == null || sessionRequestId != requestId) {
            String message = String.format("Wrong or missing requestId in session. Expected %s, got %s", requestId, sessionRequestId);
            logger.error(message);
            throw new RuntimeException(message);
        }
    }
}
