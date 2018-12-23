package io.github.zyulyaev.jaxrswebapp.web;

import io.github.zyulyaev.jaxrswebapp.service.*;
import io.github.zyulyaev.jaxrswebapp.web.model.TransactionModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * RESTful transactions resource
 */
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionsResource {
    private static final Logger log = LogManager.getLogger(TransactionsResource.class);

    private final BankService bankService;

    public TransactionsResource(BankService bankService) {
        this.bankService = bankService;
    }

    private static TransactionModel convertToModel(Transaction transaction) {
        return new TransactionModel(transaction.getTransactionId(), transaction.getTransactionTime(),
                transaction.getSourceAccountId(), transaction.getTargetAccountId(),
                transaction.getAmount());
    }

    @POST
    public Response create(TransactionModel request, @Context UriInfo uriInfo) {
        if (Objects.equals(request.getSourceAccountId(), request.getTargetAccountId())) {
            log.debug("Self transfer attempt: {}", request);
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        }
        if (request.getAmount().signum() <= 0) {
            log.debug("Negative transfer attempt: {}", request);
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        }
        Transaction transaction;
        try {
            if (request.getSourceAccountId() == null) {
                transaction = bankService.deposit(request.getTargetAccountId(), request.getAmount());
            } else if (request.getTargetAccountId() == null) {
                transaction = bankService.withdraw(request.getSourceAccountId(), request.getAmount());
            } else {
                transaction = bankService.transfer(request.getSourceAccountId(), request.getTargetAccountId(),
                        request.getAmount());
            }
        } catch (BankOperationException ex) {
            log.debug("Failed to create transaction: {}", ex.getMessage());
            return Response.status(ResponseStatusCode.UNPROCESSABLE_ENTITY)
                    .build();
        }
        URI location = uriInfo.getRequestUriBuilder().path("{transactionId}")
                .build(transaction.getTransactionId());
        return Response.created(location)
                .entity(convertToModel(transaction))
                .build();
    }

    @GET
    @Path("/{transactionId}")
    public Response lookup(@PathParam("transactionId") int transactionId) {
        Transaction transaction = bankService.lookupTransaction(transactionId);
        if (transaction == null) {
            log.debug("Transaction not found: {}", box(transactionId));
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.ok()
                .entity(convertToModel(transaction))
                .build();
    }
}
