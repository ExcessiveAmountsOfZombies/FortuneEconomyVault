package com.epherical.fortune.impl.object;

import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.UUID;

public class EconomyUser {
    private final UUID uuid;
    private final String name;
    private final double balance;
    private final List<Transaction> transactions;

    public EconomyUser(UUID uuid, String name, double balance) {
        this.uuid = uuid;
        this.name = name;
        this.balance = balance;
        this.transactions = Lists.newArrayList();
    }

    public EconomyUser(EconomyUser user, double delta) {
        this(user.uuid, user.name, user.balance + delta);
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public double currentBalance() {
        double curBalance = balance;
        for (Transaction transaction : transactions) {
            curBalance += transaction.applyTransactionModifier();
        }

        return curBalance;
    }

    public void zeroBalance() {
        transactions.add(new Transaction(balance, Transaction.Type.SUBTRACT));
    }

    public void addTransaction(Transaction transaction) {
        Validate.isTrue(transaction.amount() >= 0, "Values are required to be positive, %.2f was given.", transaction.amount());
        transactions.add(transaction);
    }

    public void add(double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        transactions.add(new Transaction(amount, Transaction.Type.ADD));
    }

    public void subtract(double amount) {
        Validate.isTrue(amount >= 0.0d, "Values are required to be positive, %.2f was given.", amount);
        transactions.add(new Transaction(amount, Transaction.Type.SUBTRACT));
    }


}
