package com.epherical.fortune;


import co.aikar.commands.PaperCommandManager;
import com.epherical.fortune.impl.FortuneEconomy;
import com.epherical.fortune.impl.command.BalanceCommand;
import com.epherical.fortune.impl.command.BaltopCommand;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.data.EconomyDataFlatFile;
import com.epherical.fortune.impl.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;


public class FortunePlugin extends JavaPlugin {

    private EconomyData economyData;
    private FortuneEconomy economy;
    private PaperCommandManager manager;

    // APi ideas transaction logging, be able to determine who gained/lost what.

    @Override
    public void onEnable() {
        // todo: make the resolve path configurable
        this.economyData = new EconomyDataFlatFile(this.getDataFolder().toPath().resolve("../../../data"));
        this.economy = new FortuneEconomy(this);
        this.manager = new PaperCommandManager(this);

        this.manager.registerCommand(new BaltopCommand(economy, economyData));
        this.manager.registerCommand(new BalanceCommand(economy, economyData));

        //getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        economyData.close();
    }

    public FortuneEconomy economy() {
        return economy;
    }

    public EconomyData economyData() {
        return economyData;
    }
}
