package nl.inferno.infernoCrate.gui;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.models.Crate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CrateEditGUI {
    private final InfernoCrate plugin;
    private final Crate crate;

    public CrateEditGUI(InfernoCrate plugin, Crate crate) {
        this.plugin = plugin;
        this.crate = crate;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Editing: " + crate.getDisplayName());
        inv.setItem(11, createGuiItem(Material.CHEST, "§6Edit Rewards",
                "§7Click to edit crate rewards"));

        inv.setItem(13, createGuiItem(Material.ENDER_CHEST, "§6Get Crate Block",
                "§7Click to get a placeable crate"));

        inv.setItem(15, createGuiItem(Material.NAME_TAG, "§6Edit Hologram",
                "§7Click to edit hologram text"));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
