package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.NetworkParameters;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class Config implements ServletContextListener {
    public enum Param {
        NETWORK("network"), POP_DESTINATION("popDesitnation"), WALLET_DIR("walletDir"), CACHE_PERSISTENCE_DIR("cachePersistenceDir");

        private String paramName;

        Param(String paramName) {
            this.paramName = paramName;
        }

        private String getParamName() {
            return paramName;
        }
    }

    private NetworkParameters networkParameters;
    private File walletDirectory;
    private File cachePersistenceDirectory;
    private String popDesitnation;

    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    public File getWalletDirectory() {
        return walletDirectory;
    }

    public File getCachePersistenceDirectory() {
        return cachePersistenceDirectory;
    }

    public String getPopDesitnation() {
        return popDesitnation;
    }

    private String getConfigParameter(ServletContext context, Param param) {
        return context.getInitParameter(param.getParamName());
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        Config config = new Config();
        config.networkParameters = NetworkParameters.fromID(getConfigParameter(context, Param.NETWORK));
        config.walletDirectory = new File(getConfigParameter(context, Param.WALLET_DIR));
        config.popDesitnation = getConfigParameter(context, Param.POP_DESTINATION);
        config.cachePersistenceDirectory = new File(getConfigParameter(context, Param.CACHE_PERSISTENCE_DIR));
        context.setAttribute("config", config);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
