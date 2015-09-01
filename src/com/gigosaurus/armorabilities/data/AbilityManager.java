package com.gigosaurus.armorabilities.data;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.utils.ArmorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.Map.Entry;

public class AbilityManager {

    private final Map<String, Map<Ability, Integer>> abilities = new HashMap<>(16);
    private final Set<String> scubaActive = new HashSet<>(5);
    private final Set<String> lavaActive = new HashSet<>(5);
    private final ArmorAbilities plugin;

    public AbilityManager(ArmorAbilities plugin) {
        this.plugin = plugin;
    }

    /**
     * Update which abilities the given player has, and change their passive effects accordingly
     * @param player the player to update
     */
    public void updateAbilityAmounts(Player player) {

        Map<Ability, Integer> playerAbilities = getAbilities(player);

        //remove active ability effects
        if (playerAbilities.containsKey(Ability.MOON)) {
            player.removePotionEffect(PotionEffectType.JUMP);
        }

        if (playerAbilities.containsKey(Ability.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        }

        if (playerAbilities.containsKey(Ability.SCUBA)) {
            player.removePotionEffect(PotionEffectType.WATER_BREATHING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            scubaActive.remove(player.getName());
        }

        if (playerAbilities.containsKey(Ability.MINER)) {
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        if (playerAbilities.containsKey(Ability.LAVA)) {
            lavaActive.remove(player.getName());
        }

        String[] armorNames = new String[4];
        int i = 0;

        //check which ability effects the player should have
        ItemStack head = player.getInventory().getHelmet();
        if ((head != null) && (head.getItemMeta().getDisplayName() != null)) {
            armorNames[i] = ArmorUtils.WORD.split(head.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack chest = player.getInventory().getChestplate();
        if ((chest != null) && (chest.getItemMeta().getDisplayName() != null)) {
            armorNames[i] = ArmorUtils.WORD.split(chest.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack legs = player.getInventory().getLeggings();
        if ((legs != null) && (legs.getItemMeta().getDisplayName() != null)) {
            armorNames[i] = ArmorUtils.WORD.split(legs.getItemMeta().getDisplayName())[0];
            i++;
        }

        ItemStack feet = player.getInventory().getBoots();
        if ((feet != null) && (feet.getItemMeta().getDisplayName() != null)) {
            armorNames[i] = ArmorUtils.WORD.split(feet.getItemMeta().getDisplayName())[0];
        }

        playerAbilities = getAbilityAmounts(armorNames);
        abilities.put(player.getName(), playerAbilities);

        //give the player the ability effects
        if (player.hasPermission("armorabilities.speed") && playerAbilities.containsKey(Ability.SPEED)) {
            int speedAmt = playerAbilities.get(Ability.SPEED);

            PotionEffect speedAbility = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, plugin.getData().getSpeedNum() * speedAmt);
            speedAbility.apply(player);
        }

        if (player.hasPermission("armorabilities.haste") && playerAbilities.containsKey(Ability.SPEED)) {
            int fastDig = plugin.getData().getSpeedHasteNum();
            if (playerAbilities.get(Ability.SPEED) == 4) {
                PotionEffect speedHaste = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, fastDig);
                speedHaste.apply(player);
            }
        }

        if (player.hasPermission("armorabilities.jump") && playerAbilities.containsKey(Ability.MOON)) {
            int jumpAmt = playerAbilities.get(Ability.MOON);

            PotionEffect jumpAbility = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, plugin.getData().getJumpNum() * jumpAmt);
            jumpAbility.apply(player);
        }

        if (player.hasPermission("armorabilities.miner") && playerAbilities.containsKey(Ability.MINER)) {
            int hasteNum = plugin.getData().getMinerHasteNum();

            PotionEffect fastDig = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, hasteNum);
            fastDig.apply(player);

            PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1);
            nightVision.apply(player);
        }

        if (player.hasPermission("armorabilities.scuba") && playerAbilities.containsKey(Ability.SCUBA)) {
            if ((player.getEyeLocation().getBlock().getType() == Material.WATER) ||
                (player.getEyeLocation().getBlock().getType() == Material.STATIONARY_WATER)) {
                int scubaAmt = playerAbilities.get(Ability.SCUBA);
                PotionEffect waterSurvival = new PotionEffect(PotionEffectType.WATER_BREATHING, plugin.getData().getScubaTime() * scubaAmt * scubaAmt * 20, 1);
                waterSurvival.apply(player);

                if (playerAbilities.get(Ability.SCUBA) == 4) {
                    int fastDig = plugin.getData().getScubaHasteNum();
                    PotionEffect scubaHaste = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, fastDig);
                    scubaHaste.apply(player);

                    PotionEffect waterSee = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1);
                    waterSee.apply(player);
                }
            }
        }
    }

    /**
     * turn the provided names into a map containing the abilities and their strengths
     * @param names the name(s) of the ability items
     * @return the map
     */
    public Map<Ability, Integer> getAbilityAmounts(String... names) {

        //check the strength of each ability effect
        Map<Ability, Integer> abilityAmounts = new EnumMap<>(Ability.class);

        for (String name : names) {
            if (name != null) {
                Ability ability = getAbility(name);
                if (ability != null) {
                    abilityAmounts.put(ability,
                                       abilityAmounts.containsKey(ability) ? (abilityAmounts.get(ability) + 1) : 1);
                }
            }
        }

        //remove any which don't have the full set which require it
        Iterator<Entry<Ability, Integer>> itr = abilityAmounts.entrySet().iterator();

        while (itr.hasNext()) {
            Entry<Ability, Integer> entry = itr.next();
            if (entry.getKey().requiresFour() && (entry.getValue() != 4)) {
                itr.remove();
            }
        }

        return abilityAmounts;
    }

    /**
     * Silently obtain the ability with the given name
     * @param name the name of the ability
     * @return the ability
     */
    public static Ability getAbility(String name) {
        try {
            return Ability.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Get all the abilities currently active on a player
     * @param player the player
     * @return the abilities
     */
    public Map<Ability, Integer> getAbilities(Player player) {
        if (abilities.containsKey(player.getName())) {
            return abilities.get(player.getName());
        }

        return new EnumMap<>(Ability.class);
    }

    /**
     * Sets the player as currently scuba diving
     * @param player the player
     */
    public void addScuba(Player player) {
        scubaActive.add(player.getName());
    }

    /**
     * Gets if the player is currently scuba diving
     * @param player the player
     * @return if they are scuba diving
     */
    public boolean isScuba(Player player) {
        return scubaActive.contains(player.getName());
    }

    /**
     * Removes a player from scuba diving
     * @param player the player
     */
    public void removeScuba(Player player) {
        scubaActive.remove(player.getName());
    }

    /**
     * Sets the player as swimming in lava
     * @param player the player
     */
    public void addLava(Player player) {
        lavaActive.add(player.getName());
    }

    /**
     * Gets if the player is currently swimming in lava
     * @param player the player
     * @return if they are swimming in lava
     */
    public boolean isLava(Player player) {
        return lavaActive.contains(player.getName());
    }

    /**
     * Removes a player from swimming in lava
     * @param player the player
     */
    public void removeLava(Player player) {
        lavaActive.remove(player.getName());
    }

    /**
     * Remove the abilities from a player because they have left the server
     * @param player the player
     */
    public void removeAbilities(Player player) {
        abilities.remove(player.getName());
        removeScuba(player);
        removeLava(player);
    }

}
