package se.rosenbaum.poppoc.core;

import se.rosenbaum.jpop.PopRequest;
import se.rosenbaum.poppoc.service.ServiceType;

public class PopRequestWithServiceType extends PopRequest {
    private static final long serialVersionUID = 1L;

    private ServiceType serviceType;

    public PopRequestWithServiceType(byte[] nonce, ServiceType serviceType) {
        setNonce(nonce);
        this.serviceType = serviceType;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }
}
