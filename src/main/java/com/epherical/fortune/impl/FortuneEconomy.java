package com.epherical.fortune.impl;

import com.epherical.fortune.FortunePlugin;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class FortuneEconomy implements Economy {



    private FortunePlugin plugin;

    public FortuneEconomy(FortunePlugin plugin) {
        this.plugin = plugin;
    }



    @Override
    public String currencyNameSingular() {
        return "Dollar";
    }

    @Override
    public boolean hasAccount(String player) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean hasAccount(String player, String world) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return false;
    }

    @Override
    public double getBalance(String player) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return 0;
    }

    @Override
    public double getBalance(String player, String world) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return 0;
    }

    @Override
    public boolean has(String player, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return false;
    }

    @Override
    public boolean has(String player, String world, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, String world, double amount) {
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String player, String world, double amount) {
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        return null;
    }


    @Override
    public boolean createPlayerAccount(String player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String player, String world) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String world) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("$%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
        return "Dollars";

    }

    @Override
    public String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }

    @Override
    public boolean hasAccount(UUID userID) {
        return plugin.economyData().userExists(userID);
    }

    @Override
    public boolean hasAmount(UUID userID, double amount) {
        try {
            return plugin.economyData().loadUser(userID).balance() > amount;
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Future<Boolean> createAccount(UUID userID, String name) {
        try {
            return plugin.economyData().saveUser(new EconomyUser(userID, name, 0));
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public double userBalance(UUID userID) {
        try {
            return plugin.economyData().loadUser(userID).balance();
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return 0.0d;
    }

    @Override
    public EconomyQuery withdrawUser(UUID uuid, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        try {
            EconomyUser user = plugin.economyData().loadUser(uuid);
            return plugin.economyData().userWithdraw(user, amount);
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public EconomyQuery depositUser(UUID uuid, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        try {
            EconomyUser user = plugin.economyData().loadUser(uuid);
            return plugin.economyData().userDeposit(user, amount);
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        throw new NotImplementedException();
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getBanks() {
        throw new NotImplementedException();
    }
}
