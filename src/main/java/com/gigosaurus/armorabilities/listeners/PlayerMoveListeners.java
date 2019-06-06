package com.gigosaurus.armorabilities.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.data.Ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerMoveListeners implements Listener {

    private final Map<String, ArrayList<Block>> vineMap = new HashMap<>(0);

    private final ArmorAbilities plugin;

    public PlayerMoveListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
    }

    private static BlockFace yawToFace(float yaw) {
        BlockFace[] axis = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
        return axis[Math.round(yaw / 90.0F) & 0x3];
    }

    @EventHandler
    public void sneakToggle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        //make assassins invisible
        if (player.hasPermission("armorabilities.assassin") && abilities.containsKey(Ability.ASSASSIN)) {
            if (event.isSneaking()) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.hidePlayer(ArmorAbilities.getInstance(), player);
                }
            } else {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.showPlayer(ArmorAbilities.getInstance(), player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location headLoc = player.getEyeLocation();
        Map<Ability, Integer> abilities = plugin.getManager().getAbilities(player);

        //add potion effects if the player is underwater
        if (abilities.containsKey(Ability.SCUBA)) {
            if (player.hasPermission("armorabilities.scuba")) {
                if (headLoc.getBlock().getType() == Material.WATER) {
                    if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING) &&
                        !plugin.getManager().isScuba(player)) {
                        plugin.getManager().addScuba(player);
                        int scubaAmt = abilities.get(Ability.SCUBA);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                                                                plugin.getData().getScubaTime() * scubaAmt * scubaAmt *
                                                                20, 1));
                    }
                } else {
                    player.removePotionEffect(PotionEffectType.WATER_BREATHING);
                    plugin.getManager().removeScuba(player);
                }
            }

            if ((abilities.get(Ability.SCUBA) == 4) && player.hasPermission("armorabilities.scubaextras")) {
                if (headLoc.getBlock().getType() == Material.WATER) {
                    int fastDig = plugin.getData().getScubaHasteNum();
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, fastDig));

                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                } else {
                    player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }

        //add potion effects if the player is not underwater
        if (abilities.containsKey(Ability.MINER) && player.hasPermission("armorabilities.miner")) {
            if (headLoc.getBlock().getType() == Material.WATER) {
                player.removePotionEffect(PotionEffectType.FAST_DIGGING);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            } else {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.FAST_DIGGING, 20000, plugin.getData().getMinerHasteNum()));

                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
            }
        }

        //allow the player to move up walls
        if (abilities.containsKey(Ability.SPIDER) && player.hasPermission("armorabilities.spider")) {
            BlockFace bf = yawToFace(player.getLocation().getYaw());
            Block block = player.getLocation().getBlock().getRelative(bf);
            if (block.getType() == Material.AIR) {
                for (int i = 0; i < getVines(player).size(); i++) {
                    player.sendBlockChange(getVines(player).get(i).getLocation(), Material.AIR.createBlockData());
                }
                getVines(player).clear();
            } else {
                Location wall = block.getLocation();
                Location air = player.getLocation();
                while (wall.getY() < 256) {
                    if (!wall.getBlock().getType().isSolid()) {
                        break;
                    }

                    if (air.getBlock().getType() == Material.AIR) {

                        MultipleFacing blockData = (MultipleFacing) Material.VINE.createBlockData();
                        blockData.setFace(bf, true);
                        player.sendBlockChange(air, blockData);
                        addVines(player, air.getBlock());
                    }

                    player.setFallDistance(0);

                    wall.add(0, 1, 0);
                    air.add(0, 1, 0);

                }
            }
        }

        //add potion effects if the player is in lava
        if (abilities.containsKey(Ability.LAVA) && player.hasPermission("armorabilities.lavaswim")) {
            int lavaAmt = abilities.get(Ability.LAVA);
            if ((event.getTo() != null) && (event.getTo().getBlock().getType() == Material.LAVA)) {
                if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) && !plugin.getManager().isLava(player)) {
                    plugin.getManager().addLava(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
                                                            plugin.getData().getLavaTime() * lavaAmt * lavaAmt * 20,
                                                            1));
                }
            } else if (event.getFrom().getBlock().getType() != Material.LAVA) {

                if (lavaAmt == 4) {
                    player.setFireTicks(-20);
                }

                if (player.getFireTicks() <= 0) {
                    player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    plugin.getManager().removeLava(player);
                }
            }
        }
    }

    private ArrayList<Block> getVines(Player player) {
        if (vineMap.containsKey(player.getName())) {
            return vineMap.get(player.getName());
        }
        return new ArrayList<>(1);
    }

    private void setVines(Player player, ArrayList<Block> vines) {
        vineMap.put(player.getName(), vines);
    }

    private void addVines(Player player, Block vine) {
        ArrayList<Block> updated = getVines(player);
        updated.add(vine);
        setVines(player, updated);
    }
}
