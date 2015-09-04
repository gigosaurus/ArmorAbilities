package com.gigosaurus.armorabilities.listeners;

import com.gigosaurus.armorabilities.ArmorAbilities;
import com.gigosaurus.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerMoveListeners implements Listener {

    private final Map<String, ArrayList<Block>> vineMap = new HashMap<>(0);
    private final ArrayList<Integer> noVine = new ArrayList<>(38);

    private final ArmorAbilities plugin;

    public PlayerMoveListeners(ArmorAbilities armorAbilities) {
        plugin = armorAbilities;
        defineNoVineBlocks();
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
                    players.hidePlayer(player);
                }
            } else {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    players.showPlayer(player);
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
                if ((headLoc.getBlock().getType() == Material.WATER) ||
                    (headLoc.getBlock().getType() == Material.STATIONARY_WATER)) {
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
                if ((headLoc.getBlock().getType() == Material.WATER) ||
                    (headLoc.getBlock().getType() == Material.STATIONARY_WATER)) {
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
            if ((headLoc.getBlock().getType() == Material.WATER) ||
                (headLoc.getBlock().getType() == Material.STATIONARY_WATER)) {
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
                    player.sendBlockChange(getVines(player).get(i).getLocation(), Material.AIR, (byte) 0);
                }
                getVines(player).clear();
            } else {
                for (int i = 0; i < 300; i++) {
                    Block temp = block.getLocation().add(0.0D, i, 0.0D).getBlock();
                    Block opp = player.getLocation().add(0.0D, i, 0.0D).getBlock();
                    Block aboveOpp = opp.getLocation().add(0.0D, 1.0D, 0.0D).getBlock();
                    int counter = 0;
                    for (Integer id : noVine) {
                        if ((temp.getType() != Material.AIR) && (temp.getTypeId() != id)) {
                            counter++;
                        }
                    }
                    if ((counter != noVine.size()) ||
                        ((opp.getType() != Material.AIR) && (opp.getType() != Material.LONG_GRASS) &&
                         (opp.getType() != Material.YELLOW_FLOWER) && (opp.getType() != Material.RED_ROSE))) {
                        break;
                    }
                    if (aboveOpp.getType() == Material.AIR) {
                        player.sendBlockChange(opp.getLocation(), Material.VINE, (byte) 0);
                        addVines(player, opp);
                    }
                    player.setFallDistance(0.0F);
                }
            }
        }

        //add potion effects if the player is in lava
        if (abilities.containsKey(Ability.LAVA) && player.hasPermission("armorabilities.lavaswim")) {
            int lavaAmt = abilities.get(Ability.LAVA);
            if ((event.getTo().getBlock().getType() == Material.LAVA) ||
                (event.getTo().getBlock().getType() == Material.STATIONARY_LAVA)) {
                if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE) && !plugin.getManager().isLava(player)) {
                    plugin.getManager().addLava(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
                                                            plugin.getData().getLavaTime() * lavaAmt * lavaAmt * 20,
                                                            1));
                }
            } else if ((event.getFrom().getBlock().getType() != Material.LAVA) &&
                       (event.getFrom().getBlock().getType() != Material.STATIONARY_LAVA)) {

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

    private void defineNoVineBlocks() {
        noVine.add(Material.THIN_GLASS.getId());
        noVine.add(Material.STEP.getId());
        noVine.add(Material.WOOD_STEP.getId());
        noVine.add(Material.WOOD_STAIRS.getId());
        noVine.add(Material.JUNGLE_WOOD_STAIRS.getId());
        noVine.add(Material.BIRCH_WOOD_STAIRS.getId());
        noVine.add(Material.SPRUCE_WOOD_STAIRS.getId());
        noVine.add(Material.COBBLESTONE_STAIRS.getId());
        noVine.add(Material.BRICK_STAIRS.getId());
        noVine.add(Material.WOOD_STAIRS.getId());
        noVine.add(Material.SMOOTH_STAIRS.getId());
        noVine.add(Material.NETHER_BRICK_STAIRS.getId());
        noVine.add(Material.SANDSTONE_STAIRS.getId());
        noVine.add(Material.FENCE.getId());
        noVine.add(Material.FENCE_GATE.getId());
        noVine.add(Material.NETHER_FENCE.getId());
        noVine.add(Material.LADDER.getId());
        noVine.add(Material.VINE.getId());
        noVine.add(Material.BED.getId());
        noVine.add(Material.BED_BLOCK.getId());
        noVine.add(Material.IRON_FENCE.getId());
        noVine.add(Material.SNOW.getId());
        noVine.add(Material.SIGN.getId());
        noVine.add(Material.LEVER.getId());
        noVine.add(Material.TRAP_DOOR.getId());
        noVine.add(Material.PISTON_EXTENSION.getId());
        noVine.add(Material.PISTON_MOVING_PIECE.getId());
        noVine.add(Material.TRIPWIRE_HOOK.getId());
        noVine.add(Material.DIODE_BLOCK_OFF.getId());
        noVine.add(Material.DIODE_BLOCK_ON.getId());
        noVine.add(Material.BOAT.getId());
        noVine.add(Material.MINECART.getId());
        noVine.add(Material.CAKE.getId());
        noVine.add(Material.CAKE_BLOCK.getId());
        noVine.add(Material.WATER.getId());
        noVine.add(Material.STATIONARY_WATER.getId());
        noVine.add(Material.LAVA.getId());
        noVine.add(Material.STATIONARY_LAVA.getId());
    }
}
