package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;

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
    @CommandPermission("fconomy.command.balance.check")
    @CommandCompletion("@players")
    @Syntax("<player>")
    private void checkBalance(Player source, @Optional String target) {
        try {
            if (target == null) {
                target = source.getName();
            }
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
            if (player != null) {
                double balance = economy.getBalance(player);

                Component msg = Component.text(player.getName())
                        .append(Component.text(" has: ").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                        .append(Component.text(economy.format(balance)).style(Style.style(TextColor.fromHexString("#3d9e00"))));

                source.sendMessage(msg, MessageType.SYSTEM);
            } else {
                Component msg = Component.text("That player could not be found!")
                        .style(Style.style(TextColor.fromHexString("#940000")));

                source.sendMessage(msg, MessageType.SYSTEM);
            }


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("add")
    @Description("Add money to a player's account.")
    @CommandPermission("fconomy.command.balance.add")
    @CommandCompletion("@players 1")
    @Syntax("<player> <amount>")
    private void addMoney(Player source, String target, double amount) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
        if (player != null) {
            economy.depositPlayer(player, amount);

            String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();

            Component msg = Component.text("Deposited: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                    .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                    .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));

            source.sendMessage(msg, MessageType.SYSTEM);
        }

    }

    @Subcommand("remove")
    @Description("remove money to a player's account.")
    @CommandPermission("fconomy.command.balance.remove")
    @CommandCompletion("@players 1")
    private void removeMoney(Player source, String target, double amount) {
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
        if (player != null) {
            economy.withdrawPlayer(player, amount);

            String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();

            Component msg = Component.text("Removed: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                    .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                    .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));

            source.sendMessage(msg, MessageType.SYSTEM);
        }
    }

    @Subcommand("set")
    @Description("sets the money on a player's account.")
    @CommandPermission("fconomy.command.balance.set")
    @CommandCompletion("@players 1")
    private void setMoney(Player source, String target, double amount) {
        try {
            OfflinePlayer player = Bukkit.getServer().getOfflinePlayerIfCached(target);
            if (player != null) {
                EconomyUser user = data.loadUser(player.getUniqueId());

                economy.withdrawPlayer(player, user.currentBalance());
                economy.depositPlayer(player, amount);

                String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();

                Component msg = Component.text("Set money to: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                        .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                        .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));

                source.sendMessage(msg, MessageType.SYSTEM);
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }
}
