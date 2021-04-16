package com.epherical.fortune;


import co.aikar.commands.BukkitCommandManager;
import com.epherical.fortune.impl.FortuneEconomy;
import com.epherical.fortune.impl.command.BalanceCommand;
import com.epherical.fortune.impl.command.BaltopCommand;
import com.epherical.fortune.impl.config.FortuneConfig;
import com.epherical.fortune.impl.data.EconomyData;
import com.epherical.fortune.impl.data.EconomyDataFlatFile;
import com.epherical.fortune.impl.data.EconomyDataMySQL;
import com.epherical.fortune.impl.listener.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;


public class FortunePlugin extends JavaPlugin {

    private EconomyData economyData;
    private FortuneEconomy economy;
    private BukkitCommandManager manager;
    private FortuneConfig config;

    private Metrics metrics;

    public static boolean usingPaper;

    @Override
    public void onEnable() {
        this.config = new FortuneConfig(this.getDataFolder(), "config.yml");
        this.config.loadConfig();

        this.metrics = new Metrics(this, 11055);

        if (this.config.usingDatabase()) {
            this.economyData = new EconomyDataMySQL(this.config);
        } else {
            this.economyData = new EconomyDataFlatFile(this.getDataFolder().toPath().resolve(this.config.dataPath()));
        }

        this.economy = new FortuneEconomy(this);
        this.manager = new BukkitCommandManager(this);

        this.manager.registerCommand(new BaltopCommand(economy, economyData));
        this.manager.registerCommand(new BalanceCommand(economy, economyData));

        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        try {
            Class.forName("net.kyori.adventure.text.Component");
            usingPaper = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            usingPaper = false;
        }
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
