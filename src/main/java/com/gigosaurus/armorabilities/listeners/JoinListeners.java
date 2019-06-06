package com.gigosaurus.armorabilities.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gigosaurus.armorabilities.ArmorAbilities;

public class JoinListeners implements Listener {

    private final ArmorAbilities plugin;

    public JoinListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getTask().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getManager().removeAbilities(event.getPlayer());
    }
}
