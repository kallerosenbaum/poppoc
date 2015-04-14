package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.NetworkParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class Config implements ServletContextListener {
    Logger logger = LoggerFactory.getLogger(Config.class);

    public enum Param {
        NETWORK("network"),
        POP_DESTINATION("popDesitnation"),
        WALLET_DIR("walletDir"),
        CACHE_PERSISTENCE_DIR("cachePersistenceDir"),
        SEND_FUNDS_TO("sendFundsTo"),
        CHAIN_URL("chainUrl"),
        CHAIN_KEY_ID("chainKeyId"),
        CHAIN_KEY_SECRET("chainKeySecret");

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
    private Address addressToSendFundsTo = null;
    private String chainUrl;
    private String chainKeyId;
    private String chainKeySecret;

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

    public Address getAddressToSendFundsTo() {
        return addressToSendFundsTo;
    }

    public String getChainUrl() {
        return chainUrl;
    }

    public String getChainKeyId() {
        return chainKeyId;
    }

    public String getChainKeySecret() {
        return chainKeySecret;
    }

    private String getConfigParameter(ServletContext context, Param param) {
        return context.getInitParameter(param.getParamName());
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        networkParameters = NetworkParameters.fromID(getConfigParameter(context, Param.NETWORK));
        walletDirectory = new File(getConfigParameter(context, Param.WALLET_DIR));
        popDesitnation = getConfigParameter(context, Param.POP_DESTINATION);
        cachePersistenceDirectory = new File(getConfigParameter(context, Param.CACHE_PERSISTENCE_DIR));
        String addressString = getConfigParameter(context, Param.SEND_FUNDS_TO);
        if (addressString != null) {
            try {
                addressToSendFundsTo = new Address(networkParameters, addressString);
            } catch (AddressFormatException e) {
                throw new RuntimeException("Invalid address: " + addressString, e);
            }
        }
        chainUrl = getConfigParameter(context, Param.CHAIN_URL);
        chainKeyId = getConfigParameter(context, Param.CHAIN_KEY_ID);
        chainKeySecret = getConfigParameter(context, Param.CHAIN_KEY_SECRET);
        logger.info(toString());
        context.setAttribute("config", this);
    }

    public String toString() {
        return "NetworkParameters: " + getNetworkParameters() +
                ", walletDirectory: " + getWalletDirectory().getAbsolutePath() +
                ", popDestination: " + getPopDesitnation() +
                ", cachePersistenceDirectory: " + getCachePersistenceDirectory().getAbsolutePath() +
                ", addressToSendFundsTo: " + getAddressToSendFundsTo() +
                ", chainUrl: " + chainUrl +
                ", chainKeyId: " + chainKeyId +
                ", chainKeySecret: " + (chainKeySecret != null ? "set but undisclosed" : null);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
