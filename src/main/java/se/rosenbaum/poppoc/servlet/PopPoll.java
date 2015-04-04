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
        checkSessionPopRequestId(request, requestId);

        Storage storage = getStorage();
        Boolean wasVerified = storage.removeVerifiedPop(requestId);
        response.setContentType("text/plain; charset=US-ASCII");
        if (wasVerified != null && wasVerified) {
            response.getWriter().write(JspConst.VALID_POP_RECEIVED.val());
        } else {
            response.getWriter().write(JspConst.POP_NOT_RECEIVED_YET.val());
        }
    }

    private void checkSessionPopRequestId(HttpServletRequest request, int requestId) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.error("No active session");
            throw new RuntimeException("No active session");
        } else {
            Object sessionRequestId = session.getAttribute(SESSION_POP_REQUEST_ID);
            if (!("" + requestId).equals(sessionRequestId)) {
                logger.error("Wrong or missing requestId in session. Expected {}, got {}", request, sessionRequestId);
                throw new RuntimeException("No active session");
            }
        }
    }
}
