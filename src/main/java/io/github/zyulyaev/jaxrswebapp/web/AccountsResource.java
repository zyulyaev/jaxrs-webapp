package io.github.zyulyaev.jaxrswebapp.web;

import io.github.zyulyaev.jaxrswebapp.service.Account;
import io.github.zyulyaev.jaxrswebapp.service.BankService;
import io.github.zyulyaev.jaxrswebapp.web.model.AccountModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.apache.logging.log4j.util.Unbox.box;

/**
 * RESTful accounts resource
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountsResource {
    private static final Logger log = LogManager.getLogger(AccountsResource.class);

    private final BankService bankService;

    public AccountsResource(BankService bankService) {
        this.bankService = bankService;
    }

    private static AccountModel convertToModel(Account account) {
        return new AccountModel(account.getAccountId(), account.getOwner(), account.getCreationTime(),
                account.getBalance());
    }

    @POST
    public Response create(AccountModel request, @Context UriInfo uriInfo) {
        Account account = bankService.createAccount(request.getOwner());
        URI location = uriInfo.getRequestUriBuilder().path("{accountId}")
                .build(account.getAccountId());
        return Response.created(location)
                .entity(convertToModel(account))
                .build();
    }

    @GET
    @Path("/{accountId}")
    public Response lookup(@PathParam("accountId") int accountId) {
        Account account = bankService.lookupAccount(accountId);
        if (account == null) {
            log.debug("Account not found: {}", box(accountId));
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }
        return Response.ok()
                .entity(convertToModel(account))
                .build();
    }
}
