package com.epherical.fortune.impl.object;

import java.util.function.Function;

public class Transaction {

    private double amount;
    private Type type;

    public Transaction(double amount, Type type) {
        this.type = type;
        this.amount = amount;
    }

    public double amount() {
        return amount;
    }

    public Type type() {
        return type;
    }

    public double applyTransactionModifier() {
        return type.modifier.apply(amount);
    }


    public enum Type {
        ADD(input -> input),
        SUBTRACT(input -> -input);

        Function<Double, Double> modifier;

        Type(Function<Double, Double> modifier) {
            this.modifier = modifier;
        }
    }
}
