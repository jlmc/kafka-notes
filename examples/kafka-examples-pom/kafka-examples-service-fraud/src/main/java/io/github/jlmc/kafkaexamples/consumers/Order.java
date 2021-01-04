package io.github.jlmc.kafkaexamples.consumers;

import java.math.BigDecimal;

public class Order {

    private final String id;
    private final String userId;
    private final BigDecimal amount;

    public Order(final String id, final String userId, final BigDecimal amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
