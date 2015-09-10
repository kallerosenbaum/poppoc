package se.rosenbaum.poppoc.service;

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
