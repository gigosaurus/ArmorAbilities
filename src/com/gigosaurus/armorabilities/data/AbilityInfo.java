package com.gigosaurus.armorabilities.data;

import com.gigosaurus.armorabilities.utils.ArmorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Extra data for each ability, all taken from the config and stored here so that the config doesn't need to be re-read
 */
public class AbilityInfo {

    private final Ability ability;
    private final String madeWith;
    private final String types;

    /**
     * Create the ability info for an ability
     * @param ability the ability
     * @param madeWith the name of the item the ability is made with
     * @param item the id of the item the ability is made with
     * @param types the
     */
    public AbilityInfo(Ability ability, String madeWith, int item, List<String> types) {
        for (String type : types) {
            for (Material mat : getArmor(type)) {
                ArmorUtils.addArmorRecipe(mat, ability, item);
            }
        }
        this.types = types.toString();
        this.ability = ability;
        this.madeWith = madeWith;
    }

    /**
     * Get the ability that belongs to this info object
     * @return the ability
     */
    public Ability getAbility() {
        return ability;
    }

    /**
     * Message all the information about this ability to the given player
     * @param player the player to inform about this ability
     */
    public void messageInfo(Player player) {
        player.sendMessage(ability.getDescription());
        player.sendMessage(
                "The " + ChatColor.GOLD + ability + ChatColor.WHITE + " ability is made with " + ChatColor.GOLD +
                madeWith);
        player.sendMessage(ChatColor.GRAY + "It can be made on these armor types: " + types);
    }

    /**
     * Get a list of materials belonging to the given armor string
     * @param armorType the armor type
     * @return the materials relating to the given string
     */
    private ArrayList<Material> getArmor(String armorType) {
        ArrayList<Material> armorSet = new ArrayList<>(4);

        if (armorType.equalsIgnoreCase("leather")) {
            armorSet.add(Material.LEATHER_HELMET);
            armorSet.add(Material.LEATHER_CHESTPLATE);
            armorSet.add(Material.LEATHER_LEGGINGS);
            armorSet.add(Material.LEATHER_BOOTS);
        } else if (armorType.equalsIgnoreCase("iron")) {
            armorSet.add(Material.IRON_HELMET);
            armorSet.add(Material.IRON_CHESTPLATE);
            armorSet.add(Material.IRON_LEGGINGS);
            armorSet.add(Material.IRON_BOOTS);
        } else if (armorType.equalsIgnoreCase("gold")) {
            armorSet.add(Material.GOLD_HELMET);
            armorSet.add(Material.GOLD_CHESTPLATE);
            armorSet.add(Material.GOLD_LEGGINGS);
            armorSet.add(Material.GOLD_BOOTS);
        } else if (armorType.equalsIgnoreCase("chainmail")) {
            armorSet.add(Material.CHAINMAIL_HELMET);
            armorSet.add(Material.CHAINMAIL_CHESTPLATE);
            armorSet.add(Material.CHAINMAIL_LEGGINGS);
            armorSet.add(Material.CHAINMAIL_BOOTS);
        } else if (armorType.equalsIgnoreCase("diamond")) {
            armorSet.add(Material.DIAMOND_HELMET);
            armorSet.add(Material.DIAMOND_CHESTPLATE);
            armorSet.add(Material.DIAMOND_LEGGINGS);
            armorSet.add(Material.DIAMOND_BOOTS);
        } else if (armorType.equalsIgnoreCase("pumpkin")) {
            armorSet.add(Material.PUMPKIN);
        } else if (armorType.equalsIgnoreCase("skull")) {
            armorSet.add(Material.SKULL_ITEM);
        } else {

            //something weird has been entered into the config
            Bukkit.getLogger().warning("[ArmorAbilities]: Unknown armor type \"" + armorType + "\" set for " + ability);
        }
        return armorSet;
    }
}
