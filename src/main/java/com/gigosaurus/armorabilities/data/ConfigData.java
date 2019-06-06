package com.gigosaurus.armorabilities.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigData {

    private final ArrayList<AbilityInfo> abilities = new ArrayList<>(Ability.values().length);
    private int jumpNum;
    private int speedNum;
    private int speedHasteNum;
    private int scubaTime;
    private int scubaHasteNum;
    private int lavaTime;
    private int rageLightningDamage;
    private int rageFireTime;
    private int creeperAbilityExplosion;
    private boolean creeperBlockDamage = true;
    private int minerHasteNum;
    private int assassinDamage;
    private double vampirePercent;

    public ConfigData(FileConfiguration config) {
        for (Ability ability : Ability.values()) {
            ConfigurationSection conf = config.getConfigurationSection(ability.name());

            if (conf != null) {
                String item = conf.getString("Item");
                List<String> types = (List<String>) conf.getList("ArmorTypes");
                if ((item != null) && (types != null) && !types.isEmpty()) {
                    abilities.add(new AbilityInfo(ability, item, item, types));

                    switch (ability) {
                        case MOON:
                            jumpNum = conf.getInt("JumpBoost");
                            break;
                        case SCUBA:
                            scubaHasteNum = conf.getInt("Haste");
                            scubaTime = conf.getInt("ScubaTime");
                            break;
                        case SPEED:
                            speedNum = conf.getInt("SpeedBoost");
                            speedHasteNum = conf.getInt("Haste");
                            break;
                        case LAVA:
                            lavaTime = conf.getInt("LavaTime");
                            break;
                        case RAGE:
                            rageLightningDamage = conf.getInt("LightningDamage");
                            rageFireTime = conf.getInt("FireTime");
                            break;
                        case CREEPER:
                            creeperAbilityExplosion = conf.getInt("ExplosionSize");
                            creeperBlockDamage = conf.getBoolean("BlockDamage");
                            break;
                        case MINER:
                            minerHasteNum = conf.getInt("Haste");
                            break;
                        case ASSASSIN:
                            assassinDamage = conf.getInt("SneakDamage");
                            break;
                        case VAMPIRE:
                            vampirePercent = conf.getDouble("VampirePercent", 25);
                            break;
                    }
                }
            }
        }
    }

    @Nullable
    public AbilityInfo getInfo(String name) {
        for (AbilityInfo info : abilities) {
            if (name.equalsIgnoreCase(info.getAbility().name())) {
                return info;
            }
        }
        return null;
    }

    public ArrayList<AbilityInfo> getInfo() {
        return abilities;
    }

    public int getCreeperAbilityExplosion() {
        return creeperAbilityExplosion;
    }

    public int getRageLightningDamage() {
        return rageLightningDamage;
    }

    public int getRageFireTime() {
        return rageFireTime;
    }

    public int getAssassinDamage() {
        return assassinDamage;
    }

    public boolean isCreeperBlockDamage() {
        return creeperBlockDamage;
    }

    public int getSpeedNum() {
        return speedNum;
    }

    public int getSpeedHasteNum() {
        return speedHasteNum;
    }

    public int getJumpNum() {
        return jumpNum;
    }

    public int getMinerHasteNum() {
        return minerHasteNum;
    }

    public int getScubaHasteNum() {
        return scubaHasteNum;
    }

    public int getLavaTime() {
        return lavaTime;
    }

    public int getScubaTime() {
        return scubaTime;
    }

    public double getVampirePercent() {
        return vampirePercent;
    }
}
