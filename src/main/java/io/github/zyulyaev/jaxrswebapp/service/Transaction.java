package io.github.zyulyaev.jaxrswebapp.service;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * <p>Immutable transaction snapshot.</p>
 * <p>May represent 3 types of operations: deposit, withdrawal or transfer. Operation types differ by
 * {@link #getSourceAccountId()} and {@link #getTargetAccountId()} values:
 * <dl>
 * <dt>Deposit</dt>
 * <dd>{@code source account id == null} and {@code target account id != null}</dd>
 * <dt>Withdrawal</dt>
 * <dd>{@code source account id != null} and {@code target account id == null}</dd>
 * <dt>Transfer</dt>
 * <dd>{@code source account id != null} and {@code target account id != null}</dd>
 * </dl>
 * <p>Source and target account ids are never null at the same time.</p>
 * </p>
 */
public interface Transaction {
    /**
     * Returns unique transaction identifier.
     *
     * @return unique transaction identifier
     */
    int getTransactionId();

    /**
     * Returns UTC timestamp of transaction creation.
     *
     * @return UTC timestamp of transaction creation
     */
    Instant getTransactionTime();

    /**
     * Returns source account identifier. May be {@code null} in case of deposit operation.
     *
     * @return source account identifier, or {@code null} in case of deposit
     */
    Integer getSourceAccountId();

    /**
     * Returns target account identifier. May be {@code null} in case of withdraw operation.
     *
     * @return target account identifier, or {@code null} in case of withdrawal
     */
    Integer getTargetAccountId();

    /**
     * Returns amount of deposited, withdrawn or transferred money. Always positive.
     *
     * @return amount of deposited, withdrawn or transferred money
     */
    BigDecimal getAmount();
}
