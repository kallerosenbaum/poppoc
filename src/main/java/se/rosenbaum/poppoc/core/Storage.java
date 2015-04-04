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
     * Step 2: Find the serviceId associated with the paymentAddress. If found,
     * move the record to paidServices. Return the serviceId paid for,
     */
    public String storePayment(Address paymentAddress) {
        String serviceId = paymentRequests.get(paymentAddress);
        if (serviceId != null) {
            paidServices.put(paymentAddress, serviceId);
            paymentRequests.remove(paymentAddress);
        }
        return serviceId;
    }

    /**
     * Step 3: Check that we have received a payment to a certain address. This is done
     * to notify the user that the payment is received.
     */
    public String getServiceIdForPayment(Address address) {
        return paidServices.get(address);
    }

    /**
     * Step 4: A PopRequest is created, stored here, and sent to the user. The
     * returned requestId is later used to check the Pop against the PopRequest.
     */
    public int store(PopRequest request) {
        Integer requestId = id.getAndIncrement();
        popRequests.put(requestId, request);
        return requestId;
    }

    /**
     * Step 5: Get the PopRequest in order to validate an incoming Pop.
     */
    public PopRequest getPopRequest(int requestId) {
        return popRequests.get(Integer.valueOf(requestId));
    }

    /**
     * Step 6: Store the information that the Pop has been received and verified
     */
    public void storeVerifiedPop(int requestId) {
        verifiedPops.put(requestId, Boolean.valueOf(true));
        popRequests.remove(Integer.valueOf(requestId));
    }

    /**
     * Step 7: Check if the PopRequest with the specified requestId has been successfully fulfilled.
     */
    public boolean removeVerifiedPop(int requestId) {
        Boolean isVerified = verifiedPops.remove(requestId);
        if (isVerified == null) {
            return false;
        }
        return isVerified;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("Storage initializing");
        ServletContext servletContext = servletContextEvent.getServletContext();
        Config config = (Config)servletContext.getAttribute("config");
        System.setProperty("cache.data.store", config.getCachePersistenceDirectory().getAbsolutePath());
        try {
            cacheManager = new DefaultCacheManager("infinispan.xml");
        } catch (IOException e) {
            logger.error("Cannot create cachemanager", e);
        }
        paymentRequests = cacheManager.getCache("paymentRequests");
        paidServices = cacheManager.getCache("paidServices");
        popRequests = cacheManager.getCache("pendingPopRequests");
        verifiedPops = cacheManager.getCache("verifiedPops");

        // Make sure we don't reuse requestIds that are still in use.
        // This is a very clumsy way of doing it that possibly never will
        // start over from 0. We want these numbers to be short since they
        // are used in pop-urls and we want pop-urls to be short.
        id.set(getMaxRequestId() + 1);

        servletContext.setAttribute("storage", this);
    }

    private int getMaxRequestId() {
        int maxRequestId = -1;
        for (Integer requestId : popRequests.keySet()) {
            maxRequestId = Math.max(maxRequestId, requestId);
        }
        for (Integer requestId : verifiedPops.keySet()) {
            maxRequestId = Math.max(maxRequestId, requestId);
        }
        return maxRequestId;
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("Storage shutting down");
        if (cacheManager != null) {
            cacheManager.stop();
        }
    }
}
