package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.FortunePlugin;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;


public class BaltopCommand extends BaseCommand {

    private final Economy economy;
    private final EconomyData data;

    public BaltopCommand(Economy economy, EconomyData data) {
        this.economy = economy;
        this.data = data;
    }

    @CommandAlias("baltop|moneytop|balancetop")
    @Description("See who has the most money on the server.")
    @CommandCompletion("1")
    @CommandPermission("fconomy.command.baltop")
    private void baltop(CommandSender source, String[] args) {
        try {
            int page = 1;
            if (args.length >= 1) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {}
            }

            List<EconomyUser> users = data.users();
            users.sort(Comparator.comparingDouble(EconomyUser::currentBalance).reversed());
            page = Math.max(page, 1);
            int maxPage = Math.max(users.size() / 10, 1);
            maxPage = users.size() % 10 != 0 ? maxPage + 1 : maxPage;
            int counter = page == 1 ? 1 : ((page -1) * 10) + 1;

            // -=- Page 1/1 -=- Top Balances -=-=-
            if (FortunePlugin.usingPaper) {
                Component msg = Component.text("-=- ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                        .append(Component.text("Page ").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                        .append(Component.text(page).style(Style.style(TextColor.fromHexString("#545454"))))
                        .append(Component.text("/").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                        .append(Component.text(maxPage).style(Style.style(TextColor.fromHexString("#545454"))))
                        .append(Component.text(" -=- Top Balances -=-=-").style(Style.style(TextColor.fromHexString("#8f8f8f"))));
                source.sendMessage(msg, MessageType.SYSTEM);
            } else {
                String message = ChatColor.of("#8f8f8f") + "-=- ";
                message += ChatColor.of("#8f8f8f") + "Page ";
                message += ChatColor.of("#545454") + "" + page;
                message += ChatColor.of("#8f8f8f") + "/";
                message += ChatColor.of("#545454") + "" + maxPage;
                message += ChatColor.of("#8f8f8f") + " -=- Top Balances -=-=-";
                source.sendMessage(message);
            }

            int begin = page == 1 ? 0 : Math.min(users.size(), ((page -1) * 10));
            int end = page == 1 ? Math.min(users.size(), 10) : Math.min(users.size(), (page * 10));

            for (EconomyUser user : users.subList(begin, end)) {
                String money = economy.format(user.currentBalance());
                if (FortunePlugin.usingPaper) {
                    Component row = Component.text(counter + ". ")
                            .append(Component.text(user.name() + " ").style(Style.style())
                                    .append(Component.text(money).style(Style.style(TextColor.fromHexString("#3d9e00")))));
                    source.sendMessage(row, MessageType.SYSTEM);
                } else {
                    String message = counter + ". " + user.name() + " " + ChatColor.of("#3d9e00") + money;
                    source.sendMessage(message);
                }
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
