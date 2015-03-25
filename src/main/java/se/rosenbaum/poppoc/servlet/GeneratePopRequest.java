package se.rosenbaum.poppoc.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/GeneratePopRequest/*", name = "GeneratePopRequest")
public class GeneratePopRequest extends PopRequestServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String txid = request.getParameter("txid");
        Long amount = parseLong(request.getParameter("amount"));
        String text = request.getParameter("text");
        createPopRequest(request, response, txid, amount, text);
    }

    private Long parseLong(String value) {
        if (value == null || "".equals(value.trim())) {
            return null;
        }
        return Long.parseLong(value);
    }

}
