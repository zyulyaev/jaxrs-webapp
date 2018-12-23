package io.github.zyulyaev.jaxrswebapp.service;

/**
 * Thrown when an operation which requires an account fails due to absence of specified account.
 */
public class AccountNotFoundException extends BankOperationException {
    public AccountNotFoundException(int accountId) {
        super("Account #" + accountId + " not found");
    }
}
