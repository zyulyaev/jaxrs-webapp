package io.github.zyulyaev.jaxrswebapp.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Json account model
 */
public class AccountModel {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer accountId;
    @JsonProperty
    private String owner;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant creationTime;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal balance;

    AccountModel() {
    }

    public AccountModel(Integer accountId, String owner, Instant creationTime, BigDecimal balance) {
        this.accountId = accountId;
        this.owner = owner;
        this.creationTime = creationTime;
        this.balance = balance;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "accountId=" + accountId +
                ", owner='" + owner + '\'' +
                ", creationTime=" + creationTime +
                ", balance=" + balance +
                '}';
    }
}
