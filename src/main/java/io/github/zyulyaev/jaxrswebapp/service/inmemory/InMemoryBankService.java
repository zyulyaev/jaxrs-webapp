package io.github.zyulyaev.jaxrswebapp.service.inmemory;

import io.github.zyulyaev.jaxrswebapp.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of {@link BankService}
 */
public final class InMemoryBankService implements BankService {
    private static final Logger log = LogManager.getLogger(InMemoryBankService.class);

    private final Clock clock;
    private final List<InMemoryAccount> accounts = new ArrayList<>();
    private final List<InMemoryTransaction> transactions = new ArrayList<>();

    /**
     * <p>Create new in-memory bank service.</p>
     * <p>Passed clock instance is used to assign timestamps to events like account or transaction creation.</p>
     *
     * @param clock clock to use for assigning timestamps
     */
    public InMemoryBankService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Account createAccount(String owner) {
        synchronized (accounts) {
            InMemoryAccount account = new InMemoryAccount(accounts.size(), owner, clock.instant());
            accounts.add(account);
            log.info("Account created: {}", account);
            return account.toSnapshot();
        }
    }

    @Override
    public Account lookupAccount(int accountId) {
        synchronized (accounts) {
            if (accountId < 0 || accountId >= accounts.size()) {
                return null;
            }
            return accounts.get(accountId).toSnapshot();
        }
    }

    private InMemoryAccount getAccount(int accountId) throws AccountNotFoundException {
        synchronized (accounts) {
            if (accountId < 0 || accountId >= accounts.size()) {
                throw new AccountNotFoundException(accountId);
            }
            return accounts.get(accountId);
        }
    }

    private void ensurePositiveAmount(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Negative amount: " + amount);
        }
    }

    @Override
    public Transaction deposit(int targetAccountId, BigDecimal amount) throws AccountNotFoundException {
        ensurePositiveAmount(amount);
        InMemoryAccount target = getAccount(targetAccountId);
        synchronized (target) {
            target.balance = target.balance.add(amount);
            synchronized (transactions) {
                InMemoryTransaction transaction =
                        new InMemoryTransaction(transactions.size(), clock.instant(), null, target, amount);
                transactions.add(transaction);
                log.info("Transaction created: {}", transaction);
                return transaction;
            }
        }
    }

    @Override
    public Transaction withdraw(int sourceAccountId, BigDecimal amount) throws BankOperationException {
        ensurePositiveAmount(amount);
        InMemoryAccount source = getAccount(sourceAccountId);
        synchronized (source) {
            if (source.balance.compareTo(amount) < 0) {
                throw new OverdraftException(sourceAccountId);
            }
            source.balance = source.balance.subtract(amount);
            synchronized (transactions) {
                InMemoryTransaction transaction =
                        new InMemoryTransaction(transactions.size(), clock.instant(), source, null, amount);
                transactions.add(transaction);
                log.info("Transaction created: {}", transaction);
                return transaction;
            }
        }
    }

    @Override
    public Transaction transfer(int sourceAccountId, int targetAccountId, BigDecimal amount)
            throws BankOperationException
    {
        if (sourceAccountId == targetAccountId) {
            throw new IllegalArgumentException("Self transfer: " + sourceAccountId);
        }
        ensurePositiveAmount(amount);
        InMemoryAccount source = getAccount(sourceAccountId);
        InMemoryAccount target = getAccount(targetAccountId);
        InMemoryAccount firstToLock = source.getAccountId() < target.getAccountId() ? source : target;
        InMemoryAccount secondToLock = source.getAccountId() < target.getAccountId() ? target : source;
        synchronized (firstToLock) {
            synchronized (secondToLock) {
                if (source.balance.compareTo(amount) < 0) {
                    throw new OverdraftException(sourceAccountId);
                }
                source.balance = source.balance.subtract(amount);
                target.balance = target.balance.add(amount);
                synchronized (transactions) {
                    InMemoryTransaction transaction =
                            new InMemoryTransaction(transactions.size(), clock.instant(), source, target, amount);
                    transactions.add(transaction);
                    log.info("Transaction created: {}", transaction);
                    return transaction;
                }
            }
        }
    }

    @Override
    public Transaction lookupTransaction(int transactionId) {
        synchronized (transactions) {
            if (transactionId < 0 || transactionId >= transactions.size()) {
                return null;
            }
            return transactions.get(transactionId);
        }
    }
}
