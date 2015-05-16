package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.*;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.*;
import se.rosenbaum.poppoc.service.ServiceType;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = "/Pop/*", name = "Pop")
@MultipartConfig
public class PopServlet extends BasicServlet {
    public static final String CONTENT_TYPE = "application/bitcoin-pop";
    Logger logger = LoggerFactory.getLogger(PopServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int requestId = getRequestId(request.getRequestURI());
        if (requestId < 0) {
            replyError("Invalid requestId " + requestId, response, null);
            return;
        }

        if ("application/bitcoin-pop".equals(request.getContentType())) {
            replyError("Unexpected content type. Expected " + CONTENT_TYPE, response, null);
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
            Sha256Hash txid = validatePop(getWallet(), pop, popRequest);
            replySuccess(response);
            storage.storeVerifiedPop(requestId, txid);
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


    /**
     * This will check the PoP according to specification at
     * {@link https://github.com/kallerosenbaum/poppoc/wiki/Proof-of-Payment}
     */
    Sha256Hash validatePop(se.rosenbaum.poppoc.core.Wallet wallet, Pop pop, PopRequest popRequest) throws InvalidPopException {
        // 1 Basic checks
        try {
            pop.verify();
        } catch (VerifyError e) {
            throw new InvalidPopException("Basic verification failed.", e);
        }

        // 2 Check OP_RETURN output
        byte[] data = checkOutput(pop);

        Sha256Hash txid = new Sha256Hash(ByteBuffer.wrap(data, 3, 32).array());
        Transaction provenTransaction = getBlockchainTransaction(wallet, txid);

        // 3 Check other outputs
        checkOutputs(pop, provenTransaction);

        // 4 Check nonce
        checkNonce(popRequest, data);

        // 5 Check inputs
        // 6 Check signatures
        checkInputsAndSignatures(wallet, pop, popRequest, provenTransaction);

        // 7 Check proven transaction
        checkPaysForCorrectService(popRequest, provenTransaction);

        // If specific txid is requested, Check that the pop proves that tx.
        if (popRequest.getTxid() != null) {
            if (!txid.toString().equals(popRequest.getTxid())) {
                throw new InvalidPopException("Wrong transaction");
            }
        }

        // Here we should check more hints, for example that the hint amount
        // actually equals the amount in txid.
        // Not imlemented here right now.

        // No exceptions, means PoP valid.
        logger.info("Valid PoP for txid {} received.", txid);
        return txid;
    }

    private Transaction getBlockchainTransaction(se.rosenbaum.poppoc.core.Wallet wallet, Sha256Hash txid) throws InvalidPopException {
        Transaction blockchainTx = wallet.getTransaction(txid);
        if (blockchainTx == null) {
            throw new InvalidPopException("Unknown transaction");
        }
        return blockchainTx;
    }

    private void checkOutputs(Pop pop, Transaction provenTransaction) throws InvalidPopException {
        List<TransactionOutput> popOutputs = pop.getOutputs();
        List<TransactionOutput> provenTxOutputs = provenTransaction.getOutputs();
        Coin expectedPopOutputValue = Coin.ZERO;
        int popOutputIndex = 1;
        for (int provenTxIndex = 0; provenTxIndex < provenTxOutputs.size(); provenTxIndex++) {
            TransactionOutput provenTxOutput = provenTxOutputs.get(provenTxIndex);
            byte[] scriptBytes = provenTxOutput.getScriptBytes();
            if (scriptBytes != null && scriptBytes.length > 0 && scriptBytes[0] == ScriptOpCodes.OP_RETURN) {
                expectedPopOutputValue.add(provenTxOutput.getValue());
                continue; // OP_RETURN outputs from proven tx should not appear in the PoP.
            }
            if (popOutputIndex < popOutputs.size()) {
                TransactionOutput popOutput = popOutputs.get(popOutputIndex);
                popOutputIndex++;
                if (!popOutput.getValue().equals(provenTxOutput.getValue())) {
                    throw new InvalidPopException("Mismatching value of output " + (popOutputIndex - 1) +
                            ". Expected " + provenTxOutput.getValue() + ", got " + popOutput.getValue());
                }
                if (!Arrays.equals(popOutput.getScriptBytes(), provenTxOutput.getScriptBytes())) {
                    throw new InvalidPopException("Mismatching script of output " + (popOutputIndex - 1));
                }
            }
        }
        Coin popOutputValue = pop.getOutput(0).getValue();
        if (!popOutputValue.equals(expectedPopOutputValue)) {
            throw new InvalidPopException("Unexpected value of PoP output. Expected " + expectedPopOutputValue +
                    ". Got " + popOutputValue);
        }
    }

    private void checkNonce(PopRequest popRequest, byte[] data) throws InvalidPopException {
        byte[] nonceBytes = new byte[8];
        System.arraycopy(data, 36, nonceBytes, 3, 5);
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        if (nonce != popRequest.getNonce()) {
            throw new InvalidPopException("Wrong nonce");
        }
    }

    private void checkInputsAndSignatures(se.rosenbaum.poppoc.core.Wallet wallet, Pop pop, PopRequest popRequest, Transaction provenTransaction) throws InvalidPopException {


        List<TransactionInput> popInputs = pop.getInputs();
        List<TransactionInput> blockchainTxInputs = provenTransaction.getInputs();
        if (popInputs.size() != blockchainTxInputs.size()) {
            throw new InvalidPopException("Wrong number of inputs");
        }

        for (int i = 0; i < blockchainTxInputs.size(); i++) {
            // Here I check that the inputs of the pop are in the same order
            // as in the payment transaction.
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
        boolean paysForCorrectService = false;
        ServiceType serviceTypeForPayment = getStorage().getServiceTypeForPayment(blockchainTx.getHash());
        if (serviceTypeForPayment != null && serviceTypeForPayment.isSameServiceType(popRequest.getServiceType())) {
            paysForCorrectService = true;
        }
        if (!paysForCorrectService) {
            throw new InvalidPopException("Proven transaction does not pay for service " + popRequest.getServiceType().getClass().getSimpleName());
        }
    }

    private byte[] checkOutput(Pop pop) throws InvalidPopException {
        List<TransactionOutput> outputs = pop.getOutputs();
        if (outputs == null || outputs.size() < 2) {
            throw new InvalidPopException("Wrong number of outputs. Expected at least 2.");
        }
        TransactionOutput output = outputs.get(0);

        byte[] scriptBytes = output.getScriptBytes();
        if (scriptBytes == null || scriptBytes.length != 41) {
            throw new InvalidPopException("Invalid script length. Expected 41");
        }
        if (scriptBytes[0] != ScriptOpCodes.OP_RETURN) {
            throw new InvalidPopException("Wrong opcode: " + scriptBytes[0]);
        }

        short version = ByteBuffer.wrap(scriptBytes, 1, 2).getShort();
        if (version != 1) {
            throw new InvalidPopException("Wrong version: " + version + " expected 1");
        }

        return scriptBytes;
    }

}
