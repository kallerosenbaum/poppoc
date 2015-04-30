package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.*;
import org.bitcoinj.core.Wallet.SendRequest;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.List;

import static org.bitcoinj.core.Wallet.BalanceType.AVAILABLE;

@WebListener
public class Wallet implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(Wallet.class);
    private WalletAppKit walletAppKit;
    private Storage storage;
    private NetworkParameters params;
    private Address addressToMoveIncomingFundsTo;

    public void start(File walletDirectory) {
        walletAppKit = new WalletAppKit(params, walletDirectory, "pop" + params.getClass().getSimpleName());

        WalletEventListener walletEventListener = new AbstractWalletEventListener() {
            @Override
            public void onCoinsReceived(org.bitcoinj.core.Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                List<TransactionOutput> outputs = tx.getOutputs();
                for (TransactionOutput output : outputs) {
                    Address address = output.getAddressFromP2PKHScript(params);
                    logger.info("Payment received. Value: {} Txid: {}", tx.getValueSentToMe(wallet), tx.getHash());
                    storage.storePayment(address, tx.getValueSentToMe(wallet).getValue());
                }
                sendFunds();
            }
        };

        walletAppKit.startAsync().awaitRunning();
        walletAppKit.wallet().addEventListener(walletEventListener);
        sendFunds();
    }

    private void sendFunds() {
        logger.debug("ENTER");
        if (addressToMoveIncomingFundsTo != null) {
            org.bitcoinj.core.Wallet wallet = walletAppKit.wallet();
            Coin balance = wallet.getBalance(AVAILABLE);
            if (Coin.valueOf(1000000).isGreaterThan(balance)) {
                // No use in sending tiny (< 10 mBTC) amounts.
                logger.info("Will not send funds. To small balance: " + balance.toFriendlyString());
                return;
            }
            SendRequest sendRequest = SendRequest.emptyWallet(addressToMoveIncomingFundsTo);
            try {
                logger.info("Sending balance: " + balance.toFriendlyString());
                wallet.sendCoins(sendRequest);
            } catch (InsufficientMoneyException e) {
                logger.info("Could not empty wallet due to insufficient funds.", e);
            } catch (Exception e) {
                logger.info("Could not empty wallet due to exception", e);
            }
        }
    }

    public void stop() {
        if (walletAppKit != null) {
            walletAppKit.stopAsync().awaitTerminated();
        }
    }

    public Address getNewReceiveAddress() {
        return walletAppKit.wallet().freshReceiveAddress();
    }

    public Transaction getTransaction(Sha256Hash txid) {
        walletAppKit.wallet().getTransactions(true);
        return walletAppKit.wallet().getTransaction(txid);
    }

    public boolean isForMe(Transaction transaction) {
        return transaction.getValue(walletAppKit.wallet()).isGreaterThan(Coin.ZERO);
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("Wallet initializing");
        ServletContext context = servletContextEvent.getServletContext();
        this.storage = (Storage) context.getAttribute("storage");
        Config config = (Config)context.getAttribute("config");
        params = config.getNetworkParameters();
        addressToMoveIncomingFundsTo = config.getAddressToSendFundsTo();
        start(config.getWalletDirectory());
        context.setAttribute("wallet", this);
        logger.debug("Wallet initialized");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.debug("Wallet destroying");
        stop();
        logger.debug("Wallet destroyed");
    }

}
