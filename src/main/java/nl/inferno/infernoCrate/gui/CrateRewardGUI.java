package nl.inferno.infernoCrate.gui;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.models.Crate;
import nl.inferno.infernoCrate.models.CrateReward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrateRewardGUI {
    private final InfernoCrate plugin;
    private final Crate crate;
    private final int page;

    public CrateRewardGUI(InfernoCrate plugin, Crate crate) {
        this(plugin, crate, 0);
    }

    public CrateRewardGUI(InfernoCrate plugin, Crate crate, int page) {
        this.plugin = plugin;
        this.crate = crate;
        this.page = page;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Rewards: " + crate.getDisplayName());

        // Add rewards
        List<CrateReward> rewards = crate.getRewards();
        int startIndex = page * 45;
        for (int i = 0; i < 45 && startIndex + i < rewards.size(); i++) {
            CrateReward reward = rewards.get(startIndex + i);
            ItemStack display = reward.getItem().clone();
            ItemMeta meta = display.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("§7Chance: §f" + reward.getChance() + "%");
            lore.add("§7Broadcast: §f" + (reward.isBroadcast() ? "Yes" : "No"));
            lore.add("");
            lore.add("§eLeft-click to edit");
            lore.add("§eRight-click to remove");

            meta.setLore(lore);
            display.setItemMeta(meta);

            inv.setItem(i, display);
        }

        // Navigation buttons
        if (page > 0) {
            inv.setItem(45, createGuiItem(Material.ARROW, "§6Previous Page"));
        }

        if ((page + 1) * 45 < rewards.size()) {
            inv.setItem(53, createGuiItem(Material.ARROW, "§6Next Page"));
        }

        // Add new reward button
        inv.setItem(49, createGuiItem(Material.EMERALD, "§6Add New Reward",
                "§7Click to add a new reward"));

        player.openInventory(inv);
    }

    public void openRewardEditGUI(Player player, CrateReward reward) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Reward");

        // Display current item
        inv.setItem(13, reward.getItem());

        // Chance buttons
        inv.setItem(11, createGuiItem(Material.REDSTONE, "§c-10% Chance",
                "§7Current: " + reward.getChance() + "%"));
        inv.setItem(12, createGuiItem(Material.RED_DYE, "§c-1% Chance",
                "§7Current: " + reward.getChance() + "%"));
        inv.setItem(14, createGuiItem(Material.LIME_DYE, "§a+1% Chance",
                "§7Current: " + reward.getChance() + "%"));
        inv.setItem(15, createGuiItem(Material.EMERALD, "§a+10% Chance",
                "§7Current: " + reward.getChance() + "%"));

        // Toggle broadcast
        inv.setItem(21, createGuiItem(Material.BELL,
                "§6Broadcast: " + (reward.isBroadcast() ? "§aYes" : "§cNo"),
                "§7Click to toggle"));

        inv.setItem(26, createGuiItem(Material.EMERALD_BLOCK, "§aSave Changes"));

        player.openInventory(inv);
    }

    public void openAddRewardGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Add Reward");

        inv.setItem(13, createGuiItem(Material.PAPER, "§6Add New Reward",
                "§7Place your item in the slot below",
                "§7to add it as a reward"));

        inv.setItem(22, createGuiItem(Material.BARRIER, "§ePlace Item Here"));

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
