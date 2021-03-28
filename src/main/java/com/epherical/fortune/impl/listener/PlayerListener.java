package com.epherical.fortune.impl.listener;

import com.epherical.fortune.FortunePlugin;
import com.epherical.fortune.impl.FortuneEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {


    private FortunePlugin plugin;

    public PlayerListener(FortunePlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FortuneEconomy economy = plugin.economy();
        Player player = event.getPlayer();
        if (!economy.hasAccount(player)) {
            economy.createPlayerAccount(player);
        }
    }
}
