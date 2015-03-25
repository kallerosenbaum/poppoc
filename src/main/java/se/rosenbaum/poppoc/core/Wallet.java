package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.List;

@WebListener
public class Wallet implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(Wallet.class);
    private WalletAppKit walletAppKit;
    private Storage storage;

    public void start() {
        File walletDirectory = new File(System.getProperty("java.io.tmpdir"), "popWallet");
        walletAppKit = new WalletAppKit(getParams(), walletDirectory, "pop" + getParams().getClass().getSimpleName());

        WalletEventListener walletEventListener = new AbstractWalletEventListener() {
            @Override
            public void onCoinsReceived(org.bitcoinj.core.Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                List<TransactionOutput> outputs = tx.getOutputs();
                for (TransactionOutput output : outputs) {
                    Address address = output.getAddressFromP2PKHScript(getParams());
                    String serviceId = storage.getServiceIdForPendingPayment(address);
                    if (serviceId != null) {
                        storage.storePayment(serviceId, address);
                    }
                }
            }
        };

        walletAppKit.startAsync().awaitRunning();
        walletAppKit.wallet().addEventListener(walletEventListener);

    }

    public void stop() {
        walletAppKit.stopAsync().awaitTerminated();
    }

    public NetworkParameters getParams() {
        return Config.NETWORK_PARAMETERS;
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
        ServletContext context = servletContextEvent.getServletContext();
        this.storage = (Storage) context.getAttribute("storage");
        start();
        context.setAttribute("wallet", this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("destroyed");
        stop();
    }

}
