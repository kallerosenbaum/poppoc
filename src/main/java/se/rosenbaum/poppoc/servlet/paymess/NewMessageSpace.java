package se.rosenbaum.poppoc.servlet.paymess;

import se.rosenbaum.poppoc.servlet.BasicServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = "/NewMessageSpace/*", name = "NewMessageSpace")
public class NewMessageSpace extends BasicServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String messageText = request.getParameter("messageSpaceText");
        Long messageSpaceId = getStorage().getUniqueLong();
        response.sendRedirect(request.getContextPath() + "/RequestPayment?serviceId=2&messageSpaceText=" +
                urlEncode(messageText) + "&messageSpaceId=" + messageSpaceId);
    }
}
