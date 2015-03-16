package se.rosenbaum.poppoc.core;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import se.rosenbaum.poppoc.core.PopRequest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class PendingPopRequests implements ServletContextListener {

    private AtomicInteger id = new AtomicInteger(0);
    private Cache cache;

    public int store(PopRequest request) {
        int requestId = id.getAndIncrement();
        cache.put(new Element(requestId, request));
        return requestId;
    }

    public PopRequest get(int requestId) {
        Element element = cache.removeAndReturnElement(Integer.valueOf(requestId));
        if (element != null) {
            return (PopRequest)element.getObjectValue();
        }
        return null;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        cache = CacheManager.getInstance().getCache("pendingPopRequests");
        servletContextEvent.getServletContext().setAttribute("pendingPopRequests", this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        CacheManager.getInstance().shutdown();
    }
}
