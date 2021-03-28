package com.epherical.fortune.impl.listener;

import com.epherical.fortune.FortunePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.ServiceRegisterEvent;

public class ServiceListener implements Listener {

    private FortunePlugin plugin;

    private boolean override = false;

    public ServiceListener(FortunePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService().equals(Economy.class) && !event.getProvider().getPlugin().equals(plugin)) {
            override = true;
        }
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.STARTUP) {
            if (!override) {
                plugin.registerCommandsAfterLoading();
            }
        }
    }
}
