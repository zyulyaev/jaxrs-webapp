package io.github.zyulyaev.jaxrswebapp.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Json transaction model
 */
public class TransactionModel {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer transactionId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant transactionTime;
    @JsonProperty
    private Integer sourceAccountId;
    @JsonProperty
    private Integer targetAccountId;
    @JsonProperty
    private BigDecimal amount;

    TransactionModel() {
    }

    public TransactionModel(Integer transactionId, Instant transactionTime, Integer sourceAccountId,
                            Integer targetAccountId, BigDecimal amount)
    {
        this.transactionId = transactionId;
        this.transactionTime = transactionTime;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public Instant getTransactionTime() {
        return transactionTime;
    }

    public Integer getSourceAccountId() {
        return sourceAccountId;
    }

    public Integer getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "TransactionModel{" +
                "transactionId=" + transactionId +
                ", transactionTime=" + transactionTime +
                ", sourceAccountId=" + sourceAccountId +
                ", targetAccountId=" + targetAccountId +
                ", amount=" + amount +
                '}';
    }
}
