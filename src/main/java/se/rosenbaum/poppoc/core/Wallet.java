package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class Wallet implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(Wallet.class);
    private WalletAppKit walletAppKit;

    public void start() {
        File walletDirectory = new File(System.getProperty("java.io.tmpdir"), "popWallet");
        walletAppKit = new WalletAppKit(getParams(), walletDirectory, "pop" + getParams().getClass().getSimpleName());
        walletAppKit.startAsync().awaitRunning();
    }

    public void stop() {
        walletAppKit.stopAsync().awaitTerminated();
    }

    public NetworkParameters getParams() {
        return TestNet3Params.get();
    }

    public Address currentReceiveAddress() {
        return walletAppKit.wallet().currentReceiveAddress();
    }

    public Transaction getTransaction(Sha256Hash txid) {
        return walletAppKit.wallet().getTransaction(txid);
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("initialized");
        start();
        servletContextEvent.getServletContext().setAttribute("wallet", this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("destroyed");
        stop();
    }

}
