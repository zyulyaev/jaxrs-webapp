package io.github.zyulyaev.jaxrswebapp.service.inmemory;

import io.github.zyulyaev.jaxrswebapp.service.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable in-memory transaction class
 */
final class InMemoryTransaction implements Transaction {
    private final int transactionId;
    private final Instant transactionTime;
    private final InMemoryAccount source;
    private final InMemoryAccount target;
    private final BigDecimal amount;

    InMemoryTransaction(int transactionId, Instant transactionTime, InMemoryAccount source, InMemoryAccount target,
                        BigDecimal amount)
    {
        this.transactionId = transactionId;
        this.transactionTime = Objects.requireNonNull(transactionTime, "transactionTime");
        if (source == null && target == null) {
            throw new IllegalArgumentException("At least one of source and target must not be null");
        }
        this.source = source;
        this.target = target;
        this.amount = Objects.requireNonNull(amount, "amount");
    }

    @Override
    public int getTransactionId() {
        return transactionId;
    }

    @Override
    public Instant getTransactionTime() {
        return transactionTime;
    }

    @Override
    public Integer getSourceAccountId() {
        return source == null ? null : source.getAccountId();
    }

    @Override
    public Integer getTargetAccountId() {
        return target == null ? null : target.getAccountId();
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "InMemoryTransaction{" +
                "transactionId=" + transactionId +
                ", transactionTime=" + transactionTime +
                ", source=" + source +
                ", target=" + target +
                ", amount=" + amount +
                '}';
    }
}
