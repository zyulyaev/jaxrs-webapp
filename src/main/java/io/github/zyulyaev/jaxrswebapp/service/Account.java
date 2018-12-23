package io.github.zyulyaev.jaxrswebapp.service;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable account snapshot
 */
public interface Account {
    /**
     * Returns unique account identifier.
     *
     * @return unique account identifier
     */
    int getAccountId();

    /**
     * Returns UTC timestamp of account creation.
     *
     * @return UTC timestamp of account creation
     */
    Instant getCreationTime();

    /**
     * Returns account owner assigned at creation.
     *
     * @return account owner assigned at creation
     */
    String getOwner();

    /**
     * Returns current account balance.
     *
     * @return current account balance
     */
    BigDecimal getBalance();
}
