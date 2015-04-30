package se.rosenbaum.poppoc.service;

/**
 * User: kalle
 * Date: 4/29/15 4:23 PM
 */
public class ServiceTypeFactory {
    public ServiceType createServiceType(int serviceId) {
        switch (serviceId) {
            case 1:
                return new SimpleService();
            case 2:
                return new PayMessService();
            default:
                throw new IllegalArgumentException("Unknown serviceId " + serviceId);
        }
    }
}
