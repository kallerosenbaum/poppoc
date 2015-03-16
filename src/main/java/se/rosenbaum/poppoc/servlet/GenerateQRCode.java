package se.rosenbaum.poppoc.servlet;

import net.glxn.qrgen.javase.QRCode;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@WebServlet(urlPatterns = "/GenerateQRCode/*", name = "GenerateQRCode")
public class GenerateQRCode extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String popRequest = request.getParameter("popRequest");

        response.setContentType("image/png");

        QRCode.from(popRequest).writeTo(response.getOutputStream());
    }

    private String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
