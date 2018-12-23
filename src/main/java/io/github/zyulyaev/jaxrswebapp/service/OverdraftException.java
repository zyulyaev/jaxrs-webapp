package io.github.zyulyaev.jaxrswebapp.service;

/**
 * Thrown when an operation which includes withdrawal from an account fails due to insufficient money on that account.
 */
public class OverdraftException extends BankOperationException {
    public OverdraftException(int accountId) {
        super("Account #" + accountId + " overdraft");
    }
}
