package io.github.zyulyaev.jaxrswebapp.service;

/**
 * Thrown when a {@link BankService} operation fails.
 */
public class BankOperationException extends Exception {
    public BankOperationException(String message) {
        super(message);
    }
}
