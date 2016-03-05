package se.rosenbaum.poppoc.core;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import se.rosenbaum.jpop.validate.TransactionStore;

import javax.xml.bind.DatatypeConverter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

public class BitcoinRPCTransactionStore implements TransactionStore {
    String rpcURL;
    String rpcuser;
    String rpcpassword;
    NetworkParameters params;

    public BitcoinRPCTransactionStore(NetworkParameters params, String rpcURL, String rpcuser, String rpcpassword) {
        this.params = params;
        this.rpcpassword = rpcpassword;
        this.rpcURL = rpcURL;
        this.rpcuser = rpcuser;
    }

    public Transaction getTransaction(Sha256Hash txid) {
        try {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication (rpcuser, rpcpassword.toCharArray());
                }
            });
            URL serviceUrl = new URL(rpcURL);
            JsonRpcHttpClient client = new JsonRpcHttpClient(serviceUrl);
            long now = System.currentTimeMillis();
            String txHex = client.invoke("getrawtransaction", new Object[] { txid.toString() }, String.class);
            System.out.println("getrawtransaction took " + (System.currentTimeMillis() - now) + " ms");
            byte[] transactionBytes = DatatypeConverter.parseHexBinary(txHex);
            Transaction t = new Transaction(params, transactionBytes);
            if (!txid.equals(t.getHash())) {
                throw new RuntimeException("Got an unexpected transaction from online API. Expected " + txid + " but got " + t.getHash());
            }
            return t;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}

