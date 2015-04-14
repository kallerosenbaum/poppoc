package se.rosenbaum.poppoc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.ClientException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by kalle on 2015-04-04.
 */
@WebServlet(urlPatterns = "/ErrorHandler/*", name = "ErrorHandler")
public class ErrorHandler extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Analyze the servlet exception
        Exception exception = (Exception)request.getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        String servletName = (String)request.getAttribute("javax.servlet.error.servlet_name");
        String requestUri = (String)request.getAttribute("javax.servlet.error.request_uri");

        if (exception instanceof ClientException) {
            // No error logging.
            String message = String.format("StatusCode: %s, servletUri: %s, requestIri: %s", statusCode, servletName, requestUri);
            logger.debug(message, exception);
        } else {
            String message = String.format("StatusCode: %s, servletUri: %s, requestIri: %s", statusCode, servletName, requestUri);
            logger.error(message, exception);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
