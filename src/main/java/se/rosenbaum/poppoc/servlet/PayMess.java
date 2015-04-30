package se.rosenbaum.poppoc.servlet;

import se.rosenbaum.poppoc.paymess.Message;
import se.rosenbaum.poppoc.paymess.MessageSpace;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = "/PayMess/*", name = "PayMess")
public class PayMess extends BasicServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<MessageSpace> messageSpaces = new ArrayList<MessageSpace>();
        messageSpaces.add(new MessageSpace(56L, new Message("Message 56")));
        //messageSpaces.add(new MessageSpace(6L, new Message("Message<script type=\"text/javascript\">alert('apa')</script> 6")));
        messageSpaces.add(new MessageSpace(5677777L, new Message("Message 5677777")));
        request.setAttribute("messageSpaces", messageSpaces);

        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/payMess.jsp");
        requestDispatcher.forward(request, response);
    }

}
