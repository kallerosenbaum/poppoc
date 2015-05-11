package se.rosenbaum.poppoc.core;

import com.google.gson.Gson;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

public class TransactionDownloader {
    private Logger logger = LoggerFactory.getLogger(TransactionDownloader.class);

    private String keyId;
    private String keySecret;
    private NetworkParameters networkParameters;
    private OkHttpClient httpClient = new OkHttpClient();
    private String chainUrl;

    public TransactionDownloader(String keyId, String keySecret, String chainUrl, NetworkParameters networkParameters) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.networkParameters = networkParameters;
        this.chainUrl = chainUrl;
    }

    public Transaction downloadTransaction(Sha256Hash hash) {
        String url = getUrl("/transactions/" + hash.toString() + "/hex");
        try {
            Response response = get(url);
            Gson gson = new Gson();
            BinaryTransactionResponse binaryTransactionResponse = gson.fromJson(response.body().charStream(), BinaryTransactionResponse.class);
            byte[] transactionBytes = DatatypeConverter.parseHexBinary(binaryTransactionResponse.hex);
            Transaction t = new Transaction(networkParameters, transactionBytes);
            if (!hash.equals(t.getHash())) {
                throw new RuntimeException("Got an unexpected transaction from online API. Expected " + hash + " but got " + t.getHash());
            }
            return t;
        } catch (Exception e) {
            logger.error("Error downloading tx " + hash, e);
            throw new RuntimeException(e);
        }
    }

    public String getUrl(String pathWithoutNetwork) {
        //org.bitcoin.production or org.bitcoin.test
        String network;
        if (networkParameters.getId().equals("org.bitcoin.production")) {
            network = "bitcoin";
        } else {
            network = "testnet3";
        }
        return chainUrl + "/" + network + pathWithoutNetwork;
    }

    private Response get(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(keyId, keySecret))
                .build();
        Response response = this.httpClient.newCall(request).execute();
        if (response.code() != 200) {
            throw new IOException("Error code " + response.code() + " received for url " + url +
                    ". Response message: " + response.message());
        }
        return response;
    }

    private class BinaryTransactionResponse {
        private String transactionHash;
        private String hex;
    }
}
