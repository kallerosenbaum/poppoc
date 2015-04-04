package se.rosenbaum.poppoc.servlet;

import net.glxn.qrgen.javase.QRCode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/GenerateQRCode/*", name = "GenerateQRCode")
public class GenerateQRCode extends BasicServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String popRequest = request.getParameter(JspConst.POP_REQUEST.val());

        response.setContentType("image/png");

        QRCode.from(popRequest).withSize(300, 300).writeTo(response.getOutputStream());
    }
}
