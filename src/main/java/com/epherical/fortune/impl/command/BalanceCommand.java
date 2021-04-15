package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.FortunePlugin;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.exception.EconomyException;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
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
        long beginNano = System.nanoTime();
        try {
            if (target == null) {
                target = source.getName();
            }
            EconomyUser player = data.loadUser(target);
            if (player != null) {
                double balance = player.currentBalance();
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text(player.name())
                            .append(Component.text(" has: ").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                            .append(Component.text(economy.format(balance)).style(Style.style(TextColor.fromHexString("#3d9e00"))));

                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(
                            player.name() + ChatColor.of("#8f8f8f") + " has " + ChatColor.of("#3d9e00") + economy.format(balance));
                }


            } else {
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text("That player could not be found!")
                            .style(Style.style(TextColor.fromHexString("#940000")));

                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(ChatColor.of("#940000") + "That player could not be found!");
                }
            }

            long endTime = System.nanoTime();
            System.out.println((endTime - beginNano) / 1000000);
        } catch (IllegalArgumentException | EconomyException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("add")
    @Description("Add money to a player's account.")
    @CommandPermission("fconomy.command.balance.add")
    @CommandCompletion("@players 1")
    @Syntax("<player> <amount>")
    private void addMoney(Player source, String target, double amount) {
        try {
            EconomyUser player = data.loadUser(target);
            if (player != null) {
                data.userDeposit(player.uuid(), amount);

                String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text("Deposited: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                            .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                            .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));

                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(ChatColor.of("#8f8f8f") + "Deposited: " + ChatColor.of("#8f8f8f") + economy.format(amount) + " " + ChatColor.of("#8f8f8f"));
                }
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }

    }

    @Subcommand("remove")
    @Description("remove money to a player's account.")
    @CommandPermission("fconomy.command.balance.remove")
    @CommandCompletion("@players 1")
    private void removeMoney(Player source, String target, double amount) {
        try {
            EconomyUser player = data.loadUser(target);

            if (player != null) {
                data.userWithdraw(player.uuid(), amount);

                String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text("Removed: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                            .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                            .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));
                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(ChatColor.of("#8f8f8f") + "Removed: " + ChatColor.of("#8f8f8f") + economy.format(amount) + " " + ChatColor.of("#8f8f8f"));
                }
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("set")
    @Description("sets the money on a player's account.")
    @CommandPermission("fconomy.command.balance.set")
    @CommandCompletion("@players 1")
    private void setMoney(Player source, String target, double amount) {
        try {
            EconomyUser player = data.loadUser(target);
            if (player != null) {

                data.userWithdraw(player.uuid(), player.currentBalance());
                data.userDeposit(player.uuid(), amount);

                String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text("Set money to: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                            .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                            .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f"))));
                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(ChatColor.of("#8f8f8f") + "Set money to: " + ChatColor.of("#8f8f8f") + economy.format(amount) + " " + ChatColor.of("#8f8f8f"));
                }
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("pay")
    @Description("pays money to another account holder")
    @CommandPermission("fconomy.command.balance.pay")
    @CommandCompletion("@players 1")
    public void payMoney(Player source, String target, double amount) {
        try {
            EconomyUser sourceUser = data.loadUser(source.getUniqueId());
            EconomyUser targetUser = data.loadUser(target);
            if (sourceUser != null && targetUser != null) {
                if (sourceUser.equals(targetUser)) {
                    source.sendMessage(ChatColor.of("#940000") + "You can not send money to yourself!");
                    return;
                }
                data.userWithdraw(sourceUser.uuid(), amount);
                data.userDeposit(targetUser.uuid(), amount);

                String amt = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
                if (FortunePlugin.usingPaper) {
                    Component msg = Component.text("Sent: ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                            .append(Component.text(economy.format(amount)).style(Style.style(TextColor.fromHexString("#3d9e00"))))
                            .append(Component.text(" " + amt.toLowerCase(Locale.ROOT)).style(Style.style(TextColor.fromHexString("#8f8f8f")))
                            .append(Component.text(" to ").style(Style.style(TextColor.fromHexString("#8f8f8f")))))
                            .append(Component.text(targetUser.name()).style(Style.style(TextColor.fromHexString("#8f8f8f"))));
                    source.sendMessage(msg, MessageType.SYSTEM);
                } else {
                    source.sendMessage(
                            ChatColor.of("#8f8f8f") + "Sent: "
                                    + ChatColor.of("#3d9e00") + economy.format(amount)
                                    + ChatColor.of("#8f8f8f") + " to " + targetUser.name());
                }
            }
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }
}
