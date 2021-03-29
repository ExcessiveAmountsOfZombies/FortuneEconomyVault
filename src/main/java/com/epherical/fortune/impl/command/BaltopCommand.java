package com.epherical.fortune.impl.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.object.EconomyUser;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
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
            int counter = 1;
            for (EconomyUser user : users) {
                source.sendMessage(Component.text(counter + ". " + user.name() + " " + economy.format(user.currentBalance())));
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
