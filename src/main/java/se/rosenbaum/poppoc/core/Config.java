package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Context;
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
        TX_SERVICE_URL("txServiceUrl"),
        TX_SERVICE_USER("txServiceUser"),
        TX_SERVICE_PASSWORD("txServicePassword");

        private String paramName;

        Param(String paramName) {
            this.paramName = paramName;
        }

        private String getParamName() {
            return paramName;
        }
    }

    private Context context;
    private File walletDirectory;
    private File cachePersistenceDirectory;
    private String popDesitnation;
    private Address addressToSendFundsTo = null;
    private String txServiceUrl;
    private String txServiceUser;
    private String txServicePassword;

    public NetworkParameters getNetworkParameters() {
        return getContext().getParams();
    }

    public Context getContext() {
        return context;
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

    public String getTxServiceUrl() {
        return txServiceUrl;
    }

    public String getTxServiceUser() {
        return txServiceUser;
    }

    public String getTxServicePassword() {
        return txServicePassword;
    }

    private String getConfigParameter(ServletContext context, Param param) {
        return context.getInitParameter(param.getParamName());
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        NetworkParameters networkParameters = NetworkParameters.fromID(getConfigParameter(servletContext, Param.NETWORK));
        context = new Context(networkParameters);
        walletDirectory = new File(getConfigParameter(servletContext, Param.WALLET_DIR));
        popDesitnation = getConfigParameter(servletContext, Param.POP_DESTINATION);
        cachePersistenceDirectory = new File(getConfigParameter(servletContext, Param.CACHE_PERSISTENCE_DIR));
        String addressString = getConfigParameter(servletContext, Param.SEND_FUNDS_TO);
        if (addressString != null) {
            try {
                addressToSendFundsTo = new Address(networkParameters, addressString);
            } catch (AddressFormatException e) {
                throw new RuntimeException("Invalid address: " + addressString, e);
            }
        }
        txServiceUrl = getConfigParameter(servletContext, Param.TX_SERVICE_URL);
        txServiceUser = getConfigParameter(servletContext, Param.TX_SERVICE_USER);
        txServicePassword = getConfigParameter(servletContext, Param.TX_SERVICE_PASSWORD);
        logger.info(toString());
        servletContext.setAttribute("config", this);
    }

    public String toString() {
        return "NetworkParameters: " + getContext().getParams() +
                ", walletDirectory: " + getWalletDirectory().getAbsolutePath() +
                ", popDestination: " + getPopDesitnation() +
                ", cachePersistenceDirectory: " + getCachePersistenceDirectory().getAbsolutePath() +
                ", addressToSendFundsTo: " + getAddressToSendFundsTo() +
                ", txServiceUrl: " + txServiceUrl +
                ", txServiceUser: " + txServiceUser +
                ", txServicePassword: " + (txServicePassword != null ? "set but undisclosed" : null);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
