package com.epherical.fortune.impl;

import com.epherical.fortune.FortunePlugin;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class FortuneEconomy implements Economy {

    private final FortunePlugin plugin;

    public FortuneEconomy(FortunePlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean hasAccount(String player) {
        try {
            return plugin.economyData().userExists(player);
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player != null) {
            try {
                return plugin.economyData().userExists(player.getUniqueId());
            } catch (EconomyException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean hasAccount(String player, String world) {
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String player) {
        try {
            return plugin.economyData().loadUser(player).currentBalance();
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return 0.0d;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player != null) {
            return plugin.economyData().getUser(player.getUniqueId()).currentBalance();
        }
        return 0;
    }

    @Override
    public double getBalance(String player, String world) {
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String player, double amount) {
        try {
            return plugin.economyData().loadUser(player).currentBalance() > amount;
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return player != null && plugin.economyData().getUser(player.getUniqueId()).currentBalance() > amount;
    }

    @Override
    public boolean has(String player, String world, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        try {
            return plugin.economyData().userWithdraw(player, amount);
        } catch (EconomyException e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player was null");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        if (player != null) {
            return plugin.economyData().userWithdraw(player.getUniqueId(), amount);
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player was null");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String player, String world, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String player, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        try {
            return plugin.economyData().userDeposit(player, amount);
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player was null");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        if (player != null) {
            return plugin.economyData().userDeposit(player.getUniqueId(), amount);
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player was null");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String player, String world, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        Validate.isTrue(amount >= 0, "Values are required to be positive, %.2f was given.", amount);
        return depositPlayer(player, amount);
    }


    @Override
    public boolean createPlayerAccount(String player) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(player));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        try {
            if (player != null) {
                return plugin.economyData().saveUser(new EconomyUser(player.getUniqueId(), player.getName(), 0));
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(String player, String world) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(player));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String world) {
        return createPlayerAccount(player);
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
    public String currencyNameSingular() {
        return "Dollar";
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
