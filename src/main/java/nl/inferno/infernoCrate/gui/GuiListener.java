package nl.inferno.infernoCrate.gui;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.gui.CrateEditGUI;
import nl.inferno.infernoCrate.gui.CrateRewardGUI;
import nl.inferno.infernoCrate.models.Crate;
import nl.inferno.infernoCrate.models.CrateReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {
    private final InfernoCrate plugin;
    private final Map<UUID, CrateReward> editingReward = new HashMap<>();
    private final Map<UUID, Crate> editingCrate = new HashMap<>();

    public GuiListener(InfernoCrate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (title.startsWith("Editing: ")) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;

            Crate crate = getCrateFromTitle(title.substring(9));
            if (crate == null) return;

            handleMainEditMenu(player, clicked, crate);
        }

        else if (title.startsWith("Rewards: ")) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;

            Crate crate = getCrateFromTitle(title.substring(9));
            if (crate == null) return;

            handleRewardsMenu(player, clicked, event.getSlot(), event.getClick(), crate);
        }

        else if (title.equals("Edit Reward")) {
            event.setCancelled(true);
            if (clicked == null || !clicked.hasItemMeta()) return;

            CrateReward reward = editingReward.get(player.getUniqueId());
            if (reward == null) return;

            handleRewardEdit(player, clicked, reward);
        }

        else if (title.equals("Add Reward")) {
            if (event.getSlot() != 22) {
                event.setCancelled(true);
                return;
            }

            Crate crate = editingCrate.get(player.getUniqueId());
            if (crate == null) return;

            handleAddReward(player, clicked, crate);
        }
    }

    private void handleMainEditMenu(Player player, ItemStack clicked, Crate crate) {
        switch (clicked.getType()) {
            case CHEST:
                editingCrate.put(player.getUniqueId(), crate);
                new CrateRewardGUI(plugin, crate).openGUI(player);
                break;
            case ENDER_CHEST:
                ItemStack crateBlock = plugin.getCrateManager().createPhysicalCrate(crate);
                player.getInventory().addItem(crateBlock);
                player.sendMessage("§aYou received a physical crate block!");
                break;
        }
    }

    private void handleRewardsMenu(Player player, ItemStack clicked, int slot, ClickType clickType, Crate crate) {
        if (clicked.getType() == Material.EMERALD) {
            new CrateRewardGUI(plugin, crate).openAddRewardGUI(player);
            return;
        }

        if (slot < 45 && slot >= 0) {
            if (clickType == ClickType.LEFT) {
                CrateReward reward = crate.getRewards().get(slot);
                editingReward.put(player.getUniqueId(), reward);
                new CrateRewardGUI(plugin, crate).openRewardEditGUI(player, reward);
            } else if (clickType == ClickType.RIGHT) {
                crate.getRewards().remove(slot);
                plugin.getCrateManager().saveCrates();
                new CrateRewardGUI(plugin, crate).openGUI(player);
            }
        }
    }

    private void handleRewardEdit(Player player, ItemStack clicked, CrateReward reward) {
        Crate crate = editingCrate.get(player.getUniqueId());
        if (crate == null) return;

        switch (clicked.getType()) {
            case REDSTONE:
                reward.setChance(Math.max(0, reward.getChance() - 10));
                break;
            case RED_DYE:
                reward.setChance(Math.max(0, reward.getChance() - 1));
                break;
            case LIME_DYE:
                reward.setChance(Math.min(100, reward.getChance() + 1));
                break;
            case EMERALD:
                reward.setChance(Math.min(100, reward.getChance() + 10));
                break;
            case BELL:
                reward.setBroadcast(!reward.isBroadcast());
                break;
            case EMERALD_BLOCK:
                plugin.getCrateManager().saveCrates();
                player.closeInventory();
                player.sendMessage("§aReward settings saved!");
                editingReward.remove(player.getUniqueId());
                new CrateRewardGUI(plugin, crate).openGUI(player);
                return;
        }

        new CrateRewardGUI(plugin, crate).openRewardEditGUI(player, reward);
    }

    private void handleAddReward(Player player, ItemStack clicked, Crate crate) {
        if (clicked == null || clicked.getType() == Material.AIR) return;

        CrateReward newReward = new CrateReward(
                "reward_" + System.currentTimeMillis(),
                clicked.getItemMeta().hasDisplayName() ? clicked.getItemMeta().getDisplayName() : "Reward",
                clicked.clone(),
                5.0
        );

        crate.getRewards().add(newReward);
        plugin.getCrateManager().saveCrates();

        editingReward.put(player.getUniqueId(), newReward);
        new CrateRewardGUI(plugin, crate).openRewardEditGUI(player, newReward);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        editingReward.remove(player.getUniqueId());
        editingCrate.remove(player.getUniqueId());
    }

    private Crate getCrateFromTitle(String name) {
        return plugin.getCrateManager().getCrates().values().stream()
                .filter(c -> c.getDisplayName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
