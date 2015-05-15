package se.rosenbaum.poppoc.servlet.paymess;

import se.rosenbaum.poppoc.paymess.Message;
import se.rosenbaum.poppoc.paymess.MessageSpace;
import se.rosenbaum.poppoc.service.PayMessService;
import se.rosenbaum.poppoc.service.ServiceType;
import se.rosenbaum.poppoc.servlet.BasicServlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebServlet(urlPatterns = "/PayMess/*", name = "PayMess")
public class PayMess extends BasicServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setAttribute("messageSpaces", getMessageSpaces());

        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/payMess.jsp");
        requestDispatcher.forward(request, response);
    }

    private List<MessageSpace> getMessageSpaces() {
        List<ServiceType> allPayMessServiceTypes = getStorage().getAllPaidServicesOfId(new PayMessService().getServiceId());
        Collections.sort(allPayMessServiceTypes, new Comparator<ServiceType>() {
            public int compare(ServiceType o1, ServiceType o2) {
                if (o1.paidDate() == null && o2.paidDate() == null) {
                    return 0;
                }
                if (o1.paidDate() == null) {
                    return 1;
                }
                if (o2.paidDate() == null) {
                    return -1;
                }
                return o2.paidDate().compareTo(o1.paidDate());
            }
        });
        List<MessageSpace> messageSpaces = new ArrayList<MessageSpace>(allPayMessServiceTypes.size());
        for (ServiceType serviceType : allPayMessServiceTypes) {
            messageSpaces.add(((PayMessService)serviceType).getMessageSpace());
        }
        return messageSpaces;
    }
}
