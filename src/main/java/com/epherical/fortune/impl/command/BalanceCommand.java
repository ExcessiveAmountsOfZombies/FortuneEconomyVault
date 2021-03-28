package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@CommandAlias("bal|money|balance")
public class BalanceCommand extends BaseCommand {

    private Economy economy;
    private EconomyData data;

    public BalanceCommand(Economy economy, EconomyData data) {
        this.economy = economy;
        this.data = data;
    }

    @Default
    @Description("Check your or another players balance.")
    //@CommandPermission("fconomy.command.balance.check")
    @CommandCompletion("@players")
    @Syntax("<player>")
    private void checkBalance(Player source, @Optional String target) {
        try {
            if (target == null) {
                target = source.getName();
            }
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
            if (player != null) {
                double balance = economy.userBalance(player.getUniqueId());
                source.sendMessage(Component.text(player.getName() + " has: " + economy.formatCurrency(balance)));
            }//todo: else send message saying they dont exist


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("add")
    @Description("Add money to a player's account.")
    //@CommandPermission("fconomy.command.balance.add")
    @CommandCompletion("@players 1")
    @Syntax("<player> <amount>")
    private void addMoney(Player source, String target, double amount) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
        if (player != null) {
            economy.depositUser(player.getUniqueId(), amount);
        }

    }

    @Subcommand("remove")
    @Description("remove money to a player's account.")
    //@CommandPermission("fconomy.command.balance.remove")
    @CommandCompletion("@players 1")
    private void removeMoney(Player source, String target, double amount) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
        if (player != null) {
            economy.withdrawUser(player.getUniqueId(), amount);
        }
    }

    @Subcommand("set")
    @Description("sets the money on a player's account.")
    //@CommandPermission("fconomy.command.balance.set")
    @CommandCompletion("@players 1")
    private void setMoney(Player source, String target, double amount) {
        try {
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
            if (player != null) {
                EconomyUser user = data.loadUser(player.getUniqueId());
                user.zeroBalance();
                economy.depositUser(user.uuid(), amount);
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }
}
