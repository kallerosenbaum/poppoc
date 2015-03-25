package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.InvalidPopException;
import se.rosenbaum.poppoc.core.Pop;
import se.rosenbaum.poppoc.core.PopRequest;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.Wallet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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
public class PopServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(PopServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Storage storage = (Storage) request.getServletContext().getAttribute("storage");
        int requestId = getRequestId(request.getPathInfo());
        if (requestId < 0) {
            throw new ServletException("Invalid requestId " + requestId);
        }

        PopRequest popRequest = storage.getPopRequest(requestId);
        if (popRequest == null) {
            throw new ServletException("No PoP request associated with requestId " + requestId);
        }

        if (request.getParts().size() != 1) {
            throw new ServletException("Wrong number of parts in request. Expected 1");
        }

        Part part = request.getParts().iterator().next();
        InputStream in = part.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = in.read(buffer)) > -1 && out.size() < Transaction.MAX_STANDARD_TX_SIZE) {
            out.write(buffer, 0, count);
        }

        Pop pop = new Pop(Config.NETWORK_PARAMETERS, out.toByteArray());

        String responseString;
        try {
            Wallet wallet = (Wallet)request.getServletContext().getAttribute("wallet");
            validatePop(wallet, pop, popRequest);
            responseString = "valid";
        } catch (InvalidPopException e) {
            logger.debug("Invalid pop exception", e);
            responseString = "invalid\n" + e.getMessage();
        }

        response.getOutputStream().print(responseString);
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

        Sha256Hash txid = new Sha256Hash(ByteBuffer.wrap(data, 3, 32).array());

        byte[] nonceBytes = new byte[8];
        System.arraycopy(data, 35, nonceBytes, 3, 5);
        long nonce = ByteBuffer.wrap(nonceBytes).getLong();

        if (nonce != popRequest.getNonce()) {
            throw new InvalidPopException("Wrong nonce");
        }

        if (popRequest.getTxid() != null) {
            if (!txid.toString().equals(popRequest.getTxid())) {
                throw new InvalidPopException("Wrong transaction");
            }
        } else {
            // Check if txid actually paid for the service requested.
            // A bit hard to implement, so skipping for now.
        }

        checkInputs(wallet, pop, txid);
    }

    private void checkInputs(Wallet wallet, Pop pop, Sha256Hash txid) throws InvalidPopException {
        Transaction blockchainTx = wallet.getTransaction(txid);
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
                popInput.verify();
            } catch (Exception e) {
                logger.debug("Failed to verify input", e);
                throw new InvalidPopException("Signature verification failed", e);
            }
        }
    }

    private byte[] checkOutput(Pop pop) throws InvalidPopException {
        List<TransactionOutput> outputs = pop.getOutputs();
        if (outputs == null || outputs.size() != 1) {
            throw new InvalidPopException("Wrong number of outputs");
        }
        TransactionOutput output = outputs.get(0);

        List<ScriptChunk> chunks = output.getScriptPubKey().getChunks();
        if (chunks.size() != 2) {
            throw new InvalidPopException("Misformed output");
        }
        ScriptChunk opReturn = chunks.get(0);
        if (!opReturn.equalsOpCode(ScriptOpCodes.OP_RETURN)) {
            throw new InvalidPopException("Wrong opcode");
        }

        ScriptChunk txidAndNonce = chunks.get(1);
        byte[] data = txidAndNonce.data;
        if (data == null || data.length != 40) {
            throw new InvalidPopException("Invalid data length. Expected 40");
        }

        String popLiteral;
        try {
            popLiteral = new String(data, 0, 3, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new InvalidPopException("Could not decode \"PoP\" literal");
        }
        if (!"PoP".equals(popLiteral)) {
            throw new InvalidPopException("Invalid \"PoP\" literal. Got " + popLiteral);
        }

        return data;
    }

}
