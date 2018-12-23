package io.github.zyulyaev.jaxrswebapp.service.inmemory;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mutable in-memory account state class
 */
final class InMemoryAccount {
    private final int accountId;
    private final String owner;
    private final Instant creationTime;
    volatile BigDecimal balance = BigDecimal.ZERO;

    InMemoryAccount(int accountId, String owner, Instant creationTime) {
        this.accountId = accountId;
        this.owner = owner;
        this.creationTime = creationTime;
    }

    int getAccountId() {
        return accountId;
    }

    AccountSnapshot toSnapshot() {
        return new AccountSnapshot(accountId, owner, creationTime, balance);
    }

    @Override
    public String toString() {
        return "InMemoryAccount{" +
                "accountId=" + accountId +
                ", owner='" + owner + '\'' +
                ", creationTime=" + creationTime +
                ", balance=" + balance +
                '}';
    }
}
