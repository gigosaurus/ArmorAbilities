package com.gigosaurus.armorabilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.gigosaurus.armorabilities.data.AbilityManager;
import com.gigosaurus.armorabilities.data.ConfigData;
import com.gigosaurus.armorabilities.listeners.CombatListeners;
import com.gigosaurus.armorabilities.listeners.InventoryClick;
import com.gigosaurus.armorabilities.listeners.JoinListeners;
import com.gigosaurus.armorabilities.listeners.PlayerMoveListeners;

import java.io.File;

public class ArmorAbilities extends JavaPlugin implements Listener {

    private ConfigData configData;
    private AbilityManager manager;
    private AbilityCheckerTask task;

    private static ArmorAbilities instance;
    public static ArmorAbilities getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        File configFile = new File(getDataFolder() + "/config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        configData = new ConfigData(getConfig());
        manager = new AbilityManager(this);
        task = new AbilityCheckerTask(this);
        task.runTaskTimer(this, 20, 20);

        //init event listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new CombatListeners(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListeners(this), this);
        getServer().getPluginManager().registerEvents(new JoinListeners(this), this);

        //commands
        getCommand("ability").setExecutor(new Commands(this));

        //add all currently online players (if a /reload was triggered or server took a while starting up)
        for (Player player : Bukkit.getOnlinePlayers()) {
            task.addPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        getServer().clearRecipes();
    }

    public ConfigData getData() {
        return configData;
    }

    public AbilityManager getManager() {
        return manager;
    }

    public AbilityCheckerTask getTask() {
        return task;
    }
}
