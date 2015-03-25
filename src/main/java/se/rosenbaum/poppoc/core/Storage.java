package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Storage implements ServletContextListener {

    private Logger logger = LoggerFactory.getLogger(Storage.class);

    private AtomicInteger id = new AtomicInteger(0);

    CacheContainer cacheManager;
    private Map<Address, String> paymentRequests;
    private Map<Address, String> paidServices;
    private Map<Integer, PopRequest> popRequests;
    private Map<Integer, Boolean> verifiedPops;

    /**
     * Step 1: request a payment and associate the paymentAddress with the serviceId
     */
    public void storePendingPayment(Address paymentAddress, String serviceId) {
        paymentRequests.put(paymentAddress, serviceId);
    }

    /**
     * Step 2: when receiving a payment, get the serviceId for it if available and
     * remove it the association from step 1.
     */
    public String getServiceIdForPendingPayment(Address paymentAddress) {
        return paymentRequests.remove(paymentAddress);
    }

    /**
     * Step 3: record that we have received a payment to an address and associate it with
     * the serviceId
     */
    public void storePayment(String serviceId, Address address) {
        paidServices.put(address, serviceId);
    }

    /**
     * Step 4: Check that we have received a payment to a certain address. This is done
     * to notify the user that the payment is received.
     */
    public String getServiceIdForPayment(Address address) {
        return paidServices.get(address);
    }

    /**
     * Step 5: A PopRequest is created, stored here, and sent to the user. The
     * returned requestId is later used to check the Pop against the PopRequest.
     */
    public int store(PopRequest request) {
        Integer requestId = id.getAndIncrement();
        popRequests.put(requestId, request);
        return requestId;
    }

    /**
     * Step 6: Get the PopRequest in order to validate an incoming Pop.
     */
    public PopRequest getPopRequest(int requestId) {
        return popRequests.get(Integer.valueOf(requestId));
    }

    /**
     * Step 7: If the Pop is valid, the PopRequest is removed
     */
    public PopRequest removePopRequest(int requestId) {
        return popRequests.remove(Integer.valueOf(requestId));
    }

    /**
     * Step 8: Store the information that the Pop has been received and verified
     */
    public void storeVerifiedPop(String requestId) {
        verifiedPops.put(Integer.valueOf(requestId), Boolean.valueOf(true));
    }

    /**
     * Step 9: Check if the PopRequest with the specified requestId has been successfully met.
     */
    public boolean removeVerifiedPop(String requestId) {
        Boolean isVerified = verifiedPops.remove(requestId);
        if (isVerified == null) {
            return false;
        }
        return isVerified;
    }


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("Storage initializing");
        try {
            cacheManager = new DefaultCacheManager("infinispan.xml");
        } catch (IOException e) {
            logger.error("Cannot create cachemanager", e);
        }
        paymentRequests = cacheManager.getCache("paymentRequests");
        paidServices = cacheManager.getCache("paidServices");
        popRequests = cacheManager.getCache("pendingPopRequests");
        verifiedPops = cacheManager.getCache("verifiedPops");
        ServletContext servletContext = servletContextEvent.getServletContext();
        servletContext.setAttribute("storage", this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("Storage shutting down");
        if (cacheManager != null) {
            cacheManager.stop();
        }
    }
}
