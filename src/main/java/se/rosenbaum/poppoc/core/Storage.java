package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.infinispan.Cache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.MortalCacheEntry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.service.ServiceType;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used from various places. The purpose is to keep storage related
 * information hidden from the caller. Right now it uses infinispan caches
 * to store payment requests, received payements, PoP requests and so on. The
 * lifecycles of the elements in the caches are set in infinispan.xml.
 * The usage of this class is outlined in 7 steps as javadoc comments on the methods
 * in this class.
 *
 * Other implementations of Storage is of course possible. It could be an SQL database
 * for example. If more implementations are needed, just make this an interface instead and
 * push down the members to a subclass, InfinispanStorage. Then you can write SQLStorage, or
 * whatever
 */
// TODO: Make this class transactional
public class Storage implements ServletContextListener {

    private Logger logger = LoggerFactory.getLogger(Storage.class);

    private AtomicInteger id = new AtomicInteger(0);

    DefaultCacheManager cacheManager;
    private Cache<Address, ServiceType> paymentRequests; // paymentAddress -> serviceType
    private Cache<Sha256Hash, ServiceType> paidServices; // txid           -> serviceType
    private Cache<Address, Sha256Hash> paidToAddresses;  // paymentAddress -> txid
    private Cache<Integer, PopRequest> popRequests;      // requestId      -> PopRequest
    private Cache<Integer, ServiceType> verifiedPops;    // requestId      -> serviceType

    private Cache<Integer, Long> maxUniqueLong;          // 1 -> maxLongEverUsed

    /**
     * Step 1: request a payment and associate the paymentAddress with the serviceType
     */
    public void storePendingPayment(Address paymentAddress, ServiceType serviceType) {
        paymentRequests.put(paymentAddress, serviceType);
    }

    /**
     * Step 2: Find the serviceType associated with the paymentAddress. If found,
     * move the record to paidServices. Return the serviceType paid for,
     */
    public ServiceType storePayment(Address paymentAddress, Sha256Hash txid, long satoshis) {
        ServiceType serviceType = paymentRequests.get(paymentAddress);
        if (serviceType != null) {
            serviceType.addPayment(satoshis);
            paymentRequests.put(paymentAddress, serviceType); // Replace the object since we've modified it
            if (serviceType.isPaidFor()) {
                paidToAddresses.put(paymentAddress, txid);
                long serviceTime = serviceType.getServiceTime();
                paidServices.put(txid, serviceType, serviceTime, TimeUnit.MILLISECONDS);
                paymentRequests.remove(paymentAddress);
            }
        }
        return serviceType;
    }


    /**
     * Step 3: Check that we have received a payment to a certain address. This is done
     * to notify the user that the payment is received.
     */
    public Sha256Hash getPaymentTransaction(Address address) {
        return paidToAddresses.get(address);
    }

    /**
     * Step 4: Get the paid-for serviceType for a transaction.
     */
    public ServiceType getServiceTypeForPayment(Sha256Hash txid) {
        return paidServices.get(txid);
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
        return popRequests.get(requestId);
    }

    /**
     * Step 7: Store the information that the Pop has been received and verified
     */
    public void storeVerifiedPop(int requestId, Sha256Hash txid) {
        PopRequest popRequest = popRequests.remove(requestId);
        if (popRequest == null && verifiedPops.get(requestId) != null) {
            return; // already verified. Actually an error condition.
        }
        if (popRequest == null && verifiedPops.get(requestId) == null) {
            return; // The pop request was invalidated (too old) while verifying the pop.
        }
        CacheEntry<Sha256Hash, ServiceType> cacheEntry = paidServices.getAdvancedCache().getCacheEntry(txid);
        long lifespan = cacheEntry.getLifespan();
        long created = ((MortalCacheEntry)cacheEntry).getCreated();
        long newLifespan = lifespan - (System.currentTimeMillis() - created);
        ServiceType serviceType = cacheEntry.getValue();
        ServiceType nakedServiceType = popRequest.getServiceType();
        serviceType.update(nakedServiceType);
        paidServices.getAdvancedCache().replace(txid, serviceType, newLifespan, TimeUnit.MILLISECONDS); // Update the cache with new data, preserving metadata, ie lifespan
        verifiedPops.put(requestId, serviceType);
    }

    /**
     * Step 8: Check if the PopRequest with the specified requestId has been successfully fulfilled.
     */
    public ServiceType removeVerifiedPop(int requestId) {
        ServiceType serviceType = verifiedPops.remove(requestId);
        return serviceType;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("Storage initializing");
        ServletContext servletContext = servletContextEvent.getServletContext();
        Config config = (Config)servletContext.getAttribute("config");
        System.setProperty("cache.data.store", config.getCachePersistenceDirectory().getAbsolutePath());
        System.setProperty("cache.manager.name", servletContext.getContextPath());
        try {
            cacheManager = new DefaultCacheManager("infinispan.xml");
        } catch (IOException e) {
            logger.error("Cannot create cachemanager", e);
        }
        paymentRequests = cacheManager.getCache("paymentRequests");
        paidServices = cacheManager.getCache("paidServices");
        paidToAddresses = cacheManager.getCache("paidToAddresses");
        popRequests = cacheManager.getCache("pendingPopRequests");
        verifiedPops = cacheManager.getCache("verifiedPops");
        maxUniqueLong = cacheManager.getCache("maxUniqueLong");

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

    public long getUniqueLong() {
        Long maxLong = maxUniqueLong.get(1);
        if (maxLong == null) {
            maxLong = 0L;
        }
        long result = maxLong + 1;
        maxUniqueLong.put(1, result);
        return result;
    }

    public List<ServiceType> getAllPaidServicesOfId(int serviceId) {
        List<ServiceType> result = new ArrayList<ServiceType>();
        for (ServiceType serviceType : paidServices.values()) {
            if (serviceId == serviceType.getServiceId()) {
                result.add(serviceType);
            }
        }
        return result;
    }
}
