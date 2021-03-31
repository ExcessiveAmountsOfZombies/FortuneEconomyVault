package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;


public class BaltopCommand extends BaseCommand {

    private Economy economy;
    private EconomyData data;

    public BaltopCommand(Economy economy, EconomyData data) {
        this.economy = economy;
        this.data = data;
    }

    public void register() {

    }


    @CommandAlias("fortune help")
    private int helpMenu(CommandSender source) {
        // todo: fu
        System.out.println("todo:");
        return 0;
    }

    @CommandAlias("baltop|moneytop|balancetop")
    @Description("See who has the most money on the server.")
    //@CommandPermission("fconomy.command.baltop")
    private void baltop(CommandSender source) {
        try {
            List<EconomyUser> users = data.users();
            users.sort(Comparator.comparingDouble(EconomyUser::currentBalance).reversed());

            int page = 1;

            int counter = 1;

            // -=- Page 1/1 -=- Top Balances -=-=-
            Component msg = Component.text("-=- ").style(Style.style(TextColor.fromHexString("#8f8f8f")))
                    .append(Component.text("Page ").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                    .append(Component.text(page).style(Style.style(TextColor.fromHexString("#545454"))))
                    .append(Component.text("/").style(Style.style(TextColor.fromHexString("#8f8f8f"))))
                    .append(Component.text(page).style(Style.style(TextColor.fromHexString("#545454"))))
                    .append(Component.text(" -=- Top Balances -=-=-").style(Style.style(TextColor.fromHexString("#8f8f8f"))));

            source.sendMessage(msg, MessageType.SYSTEM);


            for (EconomyUser user : users) {
                String money = economy.format(user.currentBalance());



                Component row = Component.text(counter + ". ")
                        .append(Component.text(user.name() + " ").style(Style.style())
                        .append(Component.text(money).style(Style.style(TextColor.fromHexString("#3d9e00")))));


                source.sendMessage(row, MessageType.SYSTEM);
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
