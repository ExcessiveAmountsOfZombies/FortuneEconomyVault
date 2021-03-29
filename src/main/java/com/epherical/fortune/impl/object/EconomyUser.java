package com.epherical.fortune.impl.object;

import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.exception.EconomyException;
import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class EconomyUser {
    private final UUID uuid;
    private final String name;
    private double balance;
    private final List<Transaction> transactions;

    private ScheduledFuture<?> future;

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

    public void applyFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public void cancelFuture() {
        this.future.cancel(false);
        this.future = null;
    }

    public Runnable scheduleSave(EconomyData data) {
        return () -> {
            try {
                if (transactions.size() == 0) {
                    return;
                }
                data.saveUser(this);
                this.balance = currentBalance();
                transactions.clear();
                System.out.println("Transactions cleared for: " + this.name);
            } catch (EconomyException e) {
                e.printStackTrace();
            }
        };
    }

    public void add(double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        transactions.add(new Transaction(amount, Transaction.Type.ADD));
    }

    public void subtract(double amount) {
        Validate.isTrue(amount >= 0.0d, "Values are required to be positive, %.2f was given.", amount);
        transactions.add(new Transaction(amount, Transaction.Type.SUBTRACT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EconomyUser that = (EconomyUser) o;

        if (!uuid.equals(that.uuid)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
