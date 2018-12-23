package io.github.zyulyaev.jaxrswebapp.service;

import java.math.BigDecimal;

/**
 * Bank operations service.
 */
public interface BankService {
    /**
     * Create new account with specified owner.
     *
     * @param owner owner of the created account
     * @return created account
     */
    Account createAccount(String owner);

    /**
     * Lookup account by id.
     *
     * @param accountId account id to look for
     * @return found account, or null if not found
     */
    Account lookupAccount(int accountId);

    /**
     * Create transaction to deposit specified amount of money to the account specified by id.
     *
     * @param targetAccountId account id to deposit money to
     * @param amount          amount of deposited money
     * @return created transaction
     * @throws AccountNotFoundException if specified account not found
     * @throws IllegalArgumentException if deposited amount is not positive
     */
    Transaction deposit(int targetAccountId, BigDecimal amount) throws BankOperationException;

    /**
     * Create transaction to withdraw specified amount of money from the account specified by id.
     *
     * @param sourceAccountId account id to withdraw money from
     * @param amount          amount of withdrawn money
     * @return created transaction
     * @throws AccountNotFoundException if specified account not found
     * @throws OverdraftException if specified account does not have enough money
     * @throws IllegalArgumentException if withdrawn amount is not positive
     */
    Transaction withdraw(int sourceAccountId, BigDecimal amount) throws BankOperationException;

    /**
     * Create transaction to transfer specified amount of money from the source account to the target account
     * specified by ids.
     *
     * @param sourceAccountId account id to withdraw money from
     * @param targetAccountId account id to deposit money to
     * @param amount          amount of transferred money
     * @return created transaction
     * @throws AccountNotFoundException if any of specified accounts is not found
     * @throws OverdraftException if specified source account does not have enough money
     * @throws IllegalArgumentException if transferred amount is not positive
     */
    Transaction transfer(int sourceAccountId, int targetAccountId, BigDecimal amount) throws BankOperationException;

    /**
     * Lookup transaction by id.
     *
     * @param transactionId transaction id to look for
     * @return found transaction, or null if not found
     */
    Transaction lookupTransaction(int transactionId);
}
