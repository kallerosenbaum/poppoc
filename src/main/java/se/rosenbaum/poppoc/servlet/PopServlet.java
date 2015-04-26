package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.*;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.*;
import se.rosenbaum.poppoc.core.Wallet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = "/Pop/*", name = "Pop")
@MultipartConfig
public class PopServlet extends BasicServlet {
    Logger logger = LoggerFactory.getLogger(PopServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int requestId = getRequestId(request.getRequestURI());
        if (requestId < 0) {
            replyError("Invalid requestId " + requestId, response, null);
            return;
        }

        Storage storage = getStorage();
        PopRequest popRequest = storage.getPopRequest(requestId);
        if (popRequest == null) {
            replyError("No PoP request associated with requestId " + requestId, response, null);
            return;
        }

        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = in.read(buffer)) > -1 && out.size() < Transaction.MAX_STANDARD_TX_SIZE) {
            out.write(buffer, 0, count);
        }
        Pop pop = new Pop(getConfig().getNetworkParameters(), out.toByteArray());

        try {
            validatePop(getWallet(), pop, popRequest);
            replySuccess(response);
            storage.storeVerifiedPop(requestId);
        } catch (InvalidPopException e) {
            replyError(e.getMessage(), response, e);
        }

    }

    private void replySuccess(HttpServletResponse response) throws IOException {
        response.getOutputStream().print("valid");
    }

    private void replyError(String message, HttpServletResponse response, Exception e) throws IOException {
        logger.debug("Invalid pop: " + message, e);
        response.getOutputStream().print("invalid\n" + message);
    }

    int getRequestId(String path) {
        Pattern compile = Pattern.compile(".*/Pop/[0-9]+/?$");
        Matcher matcher = compile.matcher(path);
        if (!matcher.matches()) {
            return -1;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int indexOfLastSlash = path.lastIndexOf("/");
        String requestIdString = path.substring(indexOfLastSlash + 1);
        try {
            return Integer.parseInt(requestIdString);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    void validatePop(Wallet wallet, Pop pop, PopRequest popRequest) throws InvalidPopException {
        try {
            pop.verify();
        } catch (VerifyError e) {
            throw new InvalidPopException("Basic verification failed.", e);
        }

        byte[] data = checkOutput(pop);

        byte[] txidBytes = new byte[32];
        System.arraycopy(data, 4, txidBytes, 0, 32);
        Sha256Hash txid = new Sha256Hash(txidBytes);

        byte[] nonceBytes = new byte[8];
        System.arraycopy(data, 36, nonceBytes, 3, 5);
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        if (nonce != popRequest.getNonce()) {
            throw new InvalidPopException("Wrong nonce");
        }

        if (popRequest.getTxid() != null) {
            if (!txid.toString().equals(popRequest.getTxid())) {
                throw new InvalidPopException("Wrong transaction");
            }
        }

        // Here we should check more hints, for example that the hint amount
        // actually equals the amount in txid.
        // Not imlemented here right now.

        checkProvedTransaction(wallet, pop, popRequest, txid);
        // No exceptions, means PoP valid.
        logger.info("Valid PoP for txid {} received.", txid);
    }

    private void checkProvedTransaction(Wallet wallet, Pop pop, PopRequest popRequest, Sha256Hash txid) throws InvalidPopException {
        Transaction blockchainTx = wallet.getTransaction(txid);
        if (blockchainTx == null) {
            throw new InvalidPopException("Unknown transaction");
        }

        checkPaysForCorrectService(popRequest, blockchainTx);

        List<TransactionInput> popInputs = pop.getInputs();
        List<TransactionInput> blockchainTxInputs = blockchainTx.getInputs();
        if (popInputs.size() != blockchainTxInputs.size()) {
            throw new InvalidPopException("Wrong number of inputs");
        }

        for (int i = 0; i < blockchainTxInputs.size(); i++) {
            // Here I assume the inputs of the pop are in the same order
            // as in the payment transaction. Maybe we should allow
            // any order? I do think that strict checks are less error prone, though.
            TransactionInput popInput = popInputs.get(i);
            TransactionInput bcInput = blockchainTxInputs.get(i);
            if (!popInput.getOutpoint().equals(bcInput.getOutpoint())) {
                throw new InvalidPopException("Wrong set of input outpoints");
            }
            // Check signature
            try {
                // connect the input to the right transaction:

                Sha256Hash hash = bcInput.getOutpoint().getHash();
                Transaction inputTx = wallet.getTransaction(hash);
                if (inputTx == null) {
                    // Transaction i unknown to the wallet. Download it from a blockchain api service
                    Config config = getConfig();
                    TransactionDownloader downloader = new TransactionDownloader(config.getChainKeyId(), config.getChainKeySecret(), config.getChainUrl(), config.getNetworkParameters());
                    inputTx = downloader.downloadTransaction(hash);
                }
                if (inputTx == null) {
                    String message = "Could not find input tx: " + hash;
                    logger.debug(message);
                    throw new InvalidPopException(message);
                }
                popInput.connect(inputTx, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);

                popInput.verify();
            } catch (Exception e) {
                logger.debug("Failed to verify input", e);
                throw new InvalidPopException("Signature verification failed", e);
            }
        }
    }

    private void checkPaysForCorrectService(PopRequest popRequest, Transaction blockchainTx) throws InvalidPopException {
        if (popRequest.getServiceId() == 1000) {
            // Special serviceId 1000. Any payment that is for me will do.
            if (!getWallet().isForMe(blockchainTx)) {
                throw new InvalidPopException("Transaction " + blockchainTx.getHashAsString() + " is not for me");
            }
            return;
        }
        NetworkParameters networkParameters = getConfig().getNetworkParameters();
        boolean paysForCorrectService = false;
        for (TransactionOutput transactionOutput : blockchainTx.getOutputs()) {
            Address outputAddress = transactionOutput.getAddressFromP2PKHScript(networkParameters);
            if (outputAddress == null) {
                outputAddress = transactionOutput.getAddressFromP2SH(networkParameters);
                if (outputAddress == null) {
                    continue; // No address found here, try the next output
                }
            }
            Integer serviceIdForPayment = getStorage().getServiceIdForPayment(outputAddress);
            if (serviceIdForPayment != null && serviceIdForPayment.equals(popRequest.getServiceId())) {
                paysForCorrectService = true;
                break;
            }
        }
        if (!paysForCorrectService) {
            throw new InvalidPopException("Proven transaction does not pay for serviceId " + popRequest.getServiceId());
        }
    }

    private byte[] checkOutput(Pop pop) throws InvalidPopException {
        List<TransactionOutput> outputs = pop.getOutputs();
        if (outputs == null || outputs.size() != 1) {
            throw new InvalidPopException("Wrong number of outputs");
        }
        TransactionOutput output = outputs.get(0);

        byte[] scriptBytes = output.getScriptBytes();
        if (scriptBytes == null || scriptBytes.length != 41) {
            throw new InvalidPopException("Invalid script length. Expected 41");
        }
        if (scriptBytes[0] != ScriptOpCodes.OP_RETURN) {
            throw new InvalidPopException("Wrong opcode: " + scriptBytes[0]);
        }

        String popLiteral;
        try {
            popLiteral = new String(scriptBytes, 1, 3, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidPopException("Could not decode \"PoP\" literal");
        }
        if (!"PoP".equals(popLiteral)) {
            throw new InvalidPopException("Invalid \"PoP\" literal. Got " + popLiteral);
        }

        return scriptBytes;
    }

}
