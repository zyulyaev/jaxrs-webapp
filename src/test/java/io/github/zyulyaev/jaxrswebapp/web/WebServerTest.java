package io.github.zyulyaev.jaxrswebapp.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.zyulyaev.jaxrswebapp.BankApplication;
import org.hamcrest.Matcher;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebServerTest {
    private static final int UNPROCESSABLE_ENTITY = 422;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ControllableClock clock = new ControllableClock(ZoneOffset.UTC, Instant.now());
    private final JsonNodeFactory json = JsonNodeFactory.instance;
    private final UndertowJaxrsServer httpServer = new UndertowJaxrsServer();
    private final Client httpClient = ResteasyClientBuilder.newClient();
    private final WebTarget target = httpClient.target(TestPortProvider.generateBaseUrl());

    @BeforeEach
    public void setup() {
        httpServer.deploy(new BankApplication(clock));
        httpServer.start();
    }

    @AfterEach
    public void teardown() {
        httpServer.stop();
        httpClient.close();
    }

    @Test
    public void createAccount() {
        Response response = createAccount("John Doe");
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        int accountId = parseAccount(response, is("John Doe"), is(clock.instant()), comparesEqualTo(BigDecimal.ZERO));
        assertEquals(target.path("/accounts/{accountId}").resolveTemplate("accountId", accountId).getUri(),
                response.getLocation());
    }

    @Test
    public void createAccountWithBalance() {
        ObjectNode request = json.objectNode()
                .put("owner", "John Doe")
                .put("balance", new BigDecimal("100.00"));
        Response response = target.path("/accounts")
                .request().post(Entity.json(request));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
    }

    @Test
    public void createAccountWithUnknownProperty() {
        ObjectNode request = json.objectNode()
                .put("owner", "John Doe")
                .put("unknownProperty", "unknown");
        Response response = target.path("/accounts")
                .request().post(Entity.json(request));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
    }

    @Test
    public void lookupAccount() {
        Instant initialInstant = clock.instant();
        int accountsCount = 5;
        int[] accountIds = new int[accountsCount];
        for (int i = 0; i < accountsCount; i++) {
            Instant instantAtCreation = clock.instant();
            Response response = createAccount("John Doe #" + i);
            assertEquals(Response.Status.CREATED, response.getStatusInfo());
            accountIds[i] = parseAccount(response, anything(), is(instantAtCreation), anything());
            clock.advance(Duration.ofSeconds(5));
        }
        for (int i = 0; i < 5; i++) {
            int accountId = accountIds[i];
            Response response = lookupAccount(accountId);
            assertEquals(Response.Status.OK, response.getStatusInfo());
            assertEquals(accountId, parseAccount(response, is("John Doe #" + i),
                    is(initialInstant.plus(Duration.ofSeconds(5 * i))), comparesEqualTo(BigDecimal.ZERO)));
        }
    }

    @Test
    public void lookupUnknownAccount() {
        assertEquals(Response.Status.NOT_FOUND, lookupAccount(0).getStatusInfo());
        assertEquals(Response.Status.NOT_FOUND, lookupAccount(123).getStatusInfo());
        assertEquals(Response.Status.NOT_FOUND, lookupAccount(-1).getStatusInfo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "75.25", "100.00"})
    public void makeDeposit(String amount) {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        Response response = makeTransaction(null, accountId, new BigDecimal(amount));
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        int transactionId = parseTransaction(response, nullValue(), is(accountId),
                comparesEqualTo(new BigDecimal(amount)), is(clock.instant()));
        URI location =
                target.path("/transactions/{transactionId}").resolveTemplate("transactionId", transactionId).getUri();
        assertEquals(location, response.getLocation());
    }

    @Test
    public void makeDepositToUnknownAccount() {
        assertEquals(UNPROCESSABLE_ENTITY, makeTransaction(null, 0, new BigDecimal("100.00")).getStatus());
    }

    @Test
    public void makeNegativeDeposit() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        assertEquals(Response.Status.BAD_REQUEST,
                makeTransaction(null, accountId, new BigDecimal("-100.00")).getStatusInfo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "75.25", "100.00"})
    public void makeWithdrawal(String amount) {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        makeTransaction(null, accountId, new BigDecimal("100.00"));
        clock.advance(Duration.ofSeconds(5));
        Response response = makeTransaction(accountId, null, new BigDecimal(amount));
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        int transactionId = parseTransaction(response, is(accountId), nullValue(),
                comparesEqualTo(new BigDecimal(amount)), is(clock.instant()));
        URI location =
                target.path("/transactions/{transactionId}").resolveTemplate("transactionId", transactionId).getUri();
        assertEquals(location, response.getLocation());
    }

    @Test
    public void makeWithdrawalFromUnknownAccount() {
        assertEquals(UNPROCESSABLE_ENTITY, makeTransaction(0, null, new BigDecimal("100.00")).getStatus());
    }

    @Test
    public void makeNegativeWithdrawal() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        assertEquals(Response.Status.BAD_REQUEST, makeTransaction(accountId, null,
                new BigDecimal("-100.00")).getStatusInfo());
    }

    @Test
    public void makeWithdrawalWithOverdraft() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        assertEquals(UNPROCESSABLE_ENTITY, makeTransaction(accountId, null, new BigDecimal("100.00")).getStatus());
        makeTransaction(null, accountId, new BigDecimal("100.00"));
        assertEquals(UNPROCESSABLE_ENTITY, makeTransaction(accountId, null, new BigDecimal("100.01")).getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "50", "75.25", "100.00"})
    public void makeTransfer(String amount) {
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());
        makeTransaction(null, johnDoeId, new BigDecimal("100.00"));
        clock.advance(Duration.ofSeconds(5));
        Response response = makeTransaction(johnDoeId, janeDoeId, new BigDecimal(amount));
        assertEquals(Response.Status.CREATED, response.getStatusInfo());
        int transactionId = parseTransaction(response, is(johnDoeId), is(janeDoeId),
                comparesEqualTo(new BigDecimal(amount)), is(clock.instant()));
        URI location =
                target.path("/transactions/{transactionId}").resolveTemplate("transactionId", transactionId).getUri();
        assertEquals(location, response.getLocation());
    }

    @Test
    public void makeTransferToUnknownAccount() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        makeTransaction(null, accountId, new BigDecimal("100.00"));
        assertEquals(UNPROCESSABLE_ENTITY,
                makeTransaction(accountId, accountId + 1, new BigDecimal("100.00")).getStatus());
    }

    @Test
    public void makeTransferFromUnknownAccount() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        assertEquals(UNPROCESSABLE_ENTITY,
                makeTransaction(accountId + 1, accountId, new BigDecimal("100.00")).getStatus());
    }

    @Test
    public void makeTransferFromAndToUnknownAccounts() {
        assertEquals(UNPROCESSABLE_ENTITY, makeTransaction(0, 1, new BigDecimal("100.00")).getStatus());
    }

    @Test
    public void makeSelfTransfer() {
        int accountId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        assertEquals(Response.Status.BAD_REQUEST,
                makeTransaction(accountId, accountId, new BigDecimal("100.00")).getStatusInfo());
    }

    @Test
    public void makeNegativeTransfer() {
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());
        assertEquals(Response.Status.BAD_REQUEST,
                makeTransaction(johnDoeId, janeDoeId, new BigDecimal("-100.00")).getStatusInfo());
    }

    @Test
    public void makeTransferWithOverdraft() {
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());
        assertEquals(UNPROCESSABLE_ENTITY,
                makeTransaction(johnDoeId, janeDoeId, new BigDecimal("100.00")).getStatus());
        makeTransaction(null, johnDoeId, new BigDecimal("100.00"));
        assertEquals(UNPROCESSABLE_ENTITY,
                makeTransaction(johnDoeId, janeDoeId, new BigDecimal("100.01")).getStatus());
    }

    @Test
    public void makeTransferWithTransactionId() {
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());
        makeTransaction(null, johnDoeId, new BigDecimal("100.00"));
        JsonNode transaction = json.objectNode()
                .put("sourceAccountId", johnDoeId)
                .put("targetAccountId", janeDoeId)
                .put("amount", new BigDecimal("100.00"))
                .put("transactionId", 1);
        Response response = target.path("/transactions")
                .request().post(Entity.json(transaction));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
    }

    @Test
    public void makeTransferWithUnkonwnProperty() {
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());
        makeTransaction(null, johnDoeId, new BigDecimal("100.00"));
        JsonNode transaction = json.objectNode()
                .put("sourceAccountId", johnDoeId)
                .put("targetAccountId", janeDoeId)
                .put("amount", new BigDecimal("100.00"))
                .put("unknownProperty", "unknown");
        Response response = target.path("/transactions")
                .request().post(Entity.json(transaction));
        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
    }

    @Test
    public void lookupTransactionsAndAccounts() {
        Instant initialInstant = clock.instant();
        int johnDoeId = parseAccount(createAccount("John Doe"), anything(), anything(), anything());
        int janeDoeId = parseAccount(createAccount("Jane Doe"), anything(), anything(), anything());

        int depositId = parseTransaction(makeTransaction(null, johnDoeId, new BigDecimal("100.00")),
                anything(), anything(), anything(), anything());
        parseAccount(lookupAccount(johnDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("100.00")));
        parseAccount(lookupAccount(janeDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("0.00")));
        clock.advance(Duration.ofSeconds(5));

        int transactionId = parseTransaction(makeTransaction(johnDoeId, janeDoeId, new BigDecimal("75.50")),
                anything(), anything(), anything(), anything());
        parseAccount(lookupAccount(johnDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("24.50")));
        parseAccount(lookupAccount(janeDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("75.50")));
        clock.advance(Duration.ofSeconds(5));

        int withdrawalId = parseTransaction(makeTransaction(janeDoeId, null, new BigDecimal("50.50")),
                anything(), anything(), anything(), anything());
        parseAccount(lookupAccount(johnDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("24.50")));
        parseAccount(lookupAccount(janeDoeId), anything(), anything(), comparesEqualTo(new BigDecimal("25.00")));
        clock.advance(Duration.ofSeconds(5));

        Response depositResponse = lookupTransaction(depositId);
        assertEquals(Response.Status.OK, depositResponse.getStatusInfo());
        parseTransaction(depositResponse, nullValue(), is(johnDoeId), comparesEqualTo(new BigDecimal("100.00")),
                is(initialInstant));

        Response transactionResponse = lookupTransaction(transactionId);
        assertEquals(Response.Status.OK, transactionResponse.getStatusInfo());
        parseTransaction(transactionResponse, is(johnDoeId), is(janeDoeId), comparesEqualTo(new BigDecimal("75.50")),
                is(initialInstant.plusSeconds(5)));

        Response withdrawalResponse = lookupTransaction(withdrawalId);
        assertEquals(Response.Status.OK, depositResponse.getStatusInfo());
        parseTransaction(withdrawalResponse, is(janeDoeId), nullValue(), comparesEqualTo(new BigDecimal("50.50")),
                is(initialInstant.plusSeconds(10)));
    }

    @Test
    public void lookupUnknownTransaction() {
        assertEquals(Response.Status.NOT_FOUND, lookupTransaction(0).getStatusInfo());
        assertEquals(Response.Status.NOT_FOUND, lookupTransaction(123).getStatusInfo());
        assertEquals(Response.Status.NOT_FOUND, lookupTransaction(-1).getStatusInfo());
    }

    private Response createAccount(String owner) {
        ObjectNode request = json.objectNode()
                .put("owner", owner);
        return target.path("/accounts")
                .request().post(Entity.json(request));
    }

    private Response lookupAccount(int accountId) {
        return target.path("/accounts/{accountId}").resolveTemplate("accountId", accountId)
                .request().get();
    }

    private int parseAccount(Response response, Matcher<? super String> expectedOwner,
                             Matcher<? super Instant> expectedCreationTime, Matcher<? super BigDecimal> expectedBalance)
    {
        JsonNode account = response.readEntity(JsonNode.class);
        assertTrue(account.get("accountId").isInt());
        assertThat(account.get("owner").textValue(), expectedOwner);
        assertThat(Instant.from(TIME_FORMATTER.parse(account.get("creationTime").textValue())), expectedCreationTime);
        assertThat(account.get("balance").decimalValue(), expectedBalance);
        assertEquals(4, account.size()); // no more fields
        return account.get("accountId").intValue();
    }

    private Response makeTransaction(Integer sourceAccountId, Integer targetAccountId, BigDecimal amount) {
        JsonNode transaction = json.objectNode()
                .put("sourceAccountId", sourceAccountId)
                .put("targetAccountId", targetAccountId)
                .put("amount", amount);
        return target.path("/transactions")
                .request().post(Entity.json(transaction));
    }

    private Response lookupTransaction(int transactionId) {
        return target.path("/transactions/{transactionId}").resolveTemplate("transactionId", transactionId)
                .request().get();
    }

    private int parseTransaction(Response response, Matcher<? super Integer> expectedSourceAccountId,
                                 Matcher<? super Integer> expectedTargetAccountId,
                                 Matcher<? super BigDecimal> expectedAmount,
                                 Matcher<? super Instant> expectedTransactionTime)
    {
        JsonNode transaction = response.readEntity(JsonNode.class);
        assertTrue(transaction.get("transactionId").isInt());
        assertThat(Instant.from(TIME_FORMATTER.parse(transaction.get("transactionTime").textValue())),
                expectedTransactionTime);
        JsonNode sourceAccountId = transaction.get("sourceAccountId");
        assertTrue(sourceAccountId.isNull() || sourceAccountId.isInt());
        assertThat((Integer) sourceAccountId.numberValue(), expectedSourceAccountId);
        JsonNode targetAccountId = transaction.get("targetAccountId");
        assertTrue(targetAccountId.isNull() || targetAccountId.isInt());
        assertThat((Integer) targetAccountId.numberValue(), expectedTargetAccountId);
        assertThat(transaction.get("amount").decimalValue(), expectedAmount);
        assertEquals(5, transaction.size()); // no more fields
        return transaction.get("transactionId").intValue();
    }
}
