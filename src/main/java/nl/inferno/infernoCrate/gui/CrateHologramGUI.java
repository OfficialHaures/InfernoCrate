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
import java.util.List;

public class CrateHologramGUI {
    private final InfernoCrate plugin;
    private final Crate crate;

    public CrateHologramGUI(InfernoCrate plugin, Crate crate) {
        this.plugin = plugin;
        this.crate = crate;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Hologram: " + crate.getDisplayName());

        List<String> currentLines = Arrays.asList(
                crate.getType().getDisplayName(),
                crate.getDisplayName(),
                "§7Right-click with key to open!"
        );

        for (int i = 0; i < currentLines.size(); i++) {
            inv.setItem(10 + i, createGuiItem(Material.NAME_TAG,
                    "§6Line " + (i + 1),
                    "§7Current: " + currentLines.get(i),
                    "",
                    "§eClick to edit"));
        }

        // Save button
        inv.setItem(22, createGuiItem(Material.EMERALD, "§aSave Changes",
                "§7Click to save hologram changes"));

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
