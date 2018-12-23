package io.github.zyulyaev.jaxrswebapp.service.inmemory;

import io.github.zyulyaev.jaxrswebapp.service.Account;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable {@link Account} snapshot class
 */
final class AccountSnapshot implements Account {
    private final int accountId;
    private final String owner;
    private final Instant creationTime;
    private final BigDecimal balance;

    AccountSnapshot(int accountId, String owner, Instant creationTime, BigDecimal balance) {
        this.accountId = accountId;
        this.owner = owner;
        this.creationTime = creationTime;
        this.balance = balance;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "AccountSnapshot{" +
                "accountId=" + accountId +
                ", owner='" + owner + '\'' +
                ", creationTime=" + creationTime +
                ", balance=" + balance +
                '}';
    }
}
