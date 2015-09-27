package se.rosenbaum.poppoc.servlet;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import se.rosenbaum.jpop.Pop;
import se.rosenbaum.jpop.PopRequest;
import se.rosenbaum.jpop.validate.InvalidPopException;
import se.rosenbaum.jpop.validate.PopValidator;
import se.rosenbaum.jpop.validate.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rosenbaum.poppoc.core.Config;
import se.rosenbaum.poppoc.core.PopRequestWithServiceType;
import se.rosenbaum.poppoc.core.Storage;
import se.rosenbaum.poppoc.core.TransactionDownloader;
import se.rosenbaum.poppoc.service.ServiceType;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

        if (!"application/bitcoin-pop".equals(request.getContentType())) {
            replyError("Unexpected content type. Expected " + CONTENT_TYPE, response, null);
        }

        Storage storage = getStorage();
        PopRequestWithServiceType popRequest = storage.getPopRequest(requestId);
        if (popRequest == null) {
            replyError("No PoP request associated with requestId " + requestId, response, null);
            return;
        }

        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) > -1 && out.size() < Transaction.MAX_STANDARD_TX_SIZE) {
            out.write(buffer, 0, count);
        }
        Pop pop = new Pop(getConfig().getNetworkParameters(), out.toByteArray());

        Config config = getConfig();
        TransactionDownloader transactionDownloader = new TransactionDownloader(getWallet(), config.getChainKeyId(), config.getChainKeySecret(), config.getChainUrl(), config.getNetworkParameters());

        try {
            Sha256Hash txid = validatePop(transactionDownloader, pop, popRequest);
            replySuccess(response);
            storage.storeVerifiedPop(requestId, txid);
        } catch (InvalidPopException e) {
            replyError(e.getMessage(), response, e);
        } catch (Exception e) {
            replySystemError(response, e);
        }

    }

    private void replySuccess(HttpServletResponse response) throws IOException {
        response.getOutputStream().print("valid");
    }

    private void replySystemError(HttpServletResponse response, Exception e) throws IOException {
        logger.debug("Internal system error", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getOutputStream().print("Internal server error");
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

    Sha256Hash validatePop(TransactionStore transactionDownloader, Pop pop, PopRequestWithServiceType popRequest) throws InvalidPopException {

        PopValidator validator = new PopValidator(transactionDownloader);

        Transaction provenTransaction = validator.validatePop(pop, popRequest.getNonce());

        // 7 Check proven transaction
        checkPaysForCorrectService(popRequest, provenTransaction);

        // If specific txid is requested, Check that the pop proves that tx.
        Sha256Hash provenTxid = provenTransaction.getHash();
        if (popRequest.getTxid() != null) {
            if (!provenTxid.toString().equals(popRequest.getTxid())) {
                throw new InvalidPopException("Wrong transaction");
            }
        }

        // Here we should check more hints, for example that the hint amount
        // actually equals the amount in txid.
        // Not imlemented here right now.

        // No exceptions, means PoP valid.
        logger.info("Valid PoP for txid {} received.", provenTxid);
        return provenTxid;
    }

    private void checkPaysForCorrectService(PopRequestWithServiceType popRequest, Transaction blockchainTx) throws InvalidPopException {
        boolean paysForCorrectService = false;
        ServiceType serviceTypeForPayment = getStorage().getServiceTypeForPayment(blockchainTx.getHash());
        if (serviceTypeForPayment != null && serviceTypeForPayment.isSameServiceType(popRequest.getServiceType())) {
            paysForCorrectService = true;
        }
        if (!paysForCorrectService) {
            throw new InvalidPopException("Proven transaction does not pay for service " + popRequest.getServiceType().getClass().getSimpleName());
        }
    }
}
