package com.gigosaurus.armorabilities.utils;

import com.gigosaurus.armorabilities.data.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class ArmorUtils {

    public static final Pattern WORD = Pattern.compile("\\s+");
    private static final Pattern SPACE = Pattern.compile("_", Pattern.LITERAL);

    private ArmorUtils() {
    }

    /**
     * Create the shapeless recipe for the given ability with the given materials
     *
     * @param armor   the armor
     * @param ability the ability
     * @param item    the item to add to the armor to create the ability
     */
    public static void addArmorRecipe(Material armor, Ability ability, int item) {

        String[] newNames = WORD.split(SPACE.matcher(armor.name()).replaceAll(" ").toLowerCase());

        for (int i = 0; i < newNames.length; i++) {
            newNames[i] = newNames[i].substring(0, 1).toUpperCase() + newNames[i].substring(1);
        }

        List<String> nameInOrder = new ArrayList<>(Arrays.asList(newNames));

        if (nameInOrder.size() < 2) {
            nameInOrder.add(newNames[0]);
        }

        ItemStack result = new ItemStack(armor);
        String newName = ability.toString() + ' ' + nameInOrder.get(1);
        ItemMeta im = result.getItemMeta();
        im.setDisplayName(newName);
        result.setItemMeta(im);

        ShapelessRecipe sr = new ShapelessRecipe(new ItemStack(result)).addIngredient(Material.getMaterial(item))
                                                                       .addIngredient(armor);
        Bukkit.getServer().addRecipe(sr);
    }

    /**
     * @param item converts an item id into a lowercase readable item name
     *
     * @return the name
     */
    public static String materialName(int item) {
        return SPACE.matcher(Material.getMaterial(item).name()).replaceAll(" ").toLowerCase();
    }
}
