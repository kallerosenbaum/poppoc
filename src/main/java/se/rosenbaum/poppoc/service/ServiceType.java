package se.rosenbaum.poppoc.service;

import se.rosenbaum.poppoc.core.PopRequest;

public interface ServiceType {
    int getServiceId();
    PopRequest getPopRequest();
    String getPaymentUriParameters();
}
