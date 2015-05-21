package se.rosenbaum.poppoc.core;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by kalle on 2015-04-11.
 */
public class TransactionDownloaderTest {
    TransactionDownloader sut;

    @Before
    public void setup() {
        String keyId = System.getProperty("chain.key.id");
        String keySecret = System.getProperty("chain.key.secret");
        sut = new TransactionDownloader(null, keyId, keySecret, "https://api.chain.com/v2", NetworkParameters.fromID("org.bitcoin.test"));
    }

 //   @Test
    public void testDownloadTransaction() throws Exception {
        String hash = "052cefc014280d64b950bf278ae6e76b1e3ae187c3efdd80420fbfe489cb40c0";
        Transaction transaction = sut.getBlockchainTransaction(new Sha256Hash(hash));
        assertEquals(hash, transaction.getHashAsString());
    }
}