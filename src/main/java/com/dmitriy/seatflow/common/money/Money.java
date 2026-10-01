package com.dmitriy.seatflow.common.money;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Денежная сумма вместе с кодом валюты.
 */
@Embeddable
public class Money {

    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private String currency;

    /**
     * Конструктор для JPA.
     */
    protected Money() {
    }

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount must not be null"
            );
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Amount must not be negative"
            );
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount must have no more than two decimal places"
            );
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency must not be blank"
            );
        }

        String currencyCode = currency
                .trim()
                .toUpperCase(Locale.ROOT);

        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currencyCode,
                    exception
            );
        }

        this.amount = amount.setScale(2);
        this.currency = currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Money money = (Money) object;

        return Objects.equals(amount, money.amount)
                && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}