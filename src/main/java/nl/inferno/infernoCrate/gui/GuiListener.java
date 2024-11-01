package nl.inferno.infernoCrate.gui;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.models.Crate;
import nl.inferno.infernoCrate.models.CrateReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {

    private final InfernoCrate plugin;
    private final Map<UUID, EditSession> editingSessions = new HashMap<>();

    public GuiListener(InfernoCrate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("Editing: ") && !title.startsWith("Rewards: ") && !title.startsWith("Hologram: ")) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        if (title.startsWith("Editing: ")) {
            handleEditMenu(player, clicked, title.substring(9));
        } else if (title.startsWith("Rewards: ")) {
            handleRewardMenu(player, clicked, title.substring(9), event.getSlot(), event.getClick());
        } else if (title.startsWith("Hologram: ")) {
            handleHologramMenu(player, clicked, title.substring(9), event.getSlot());
        }
    }

    private void handleEditMenu(Player player, ItemStack clicked, String crateName) {
        Crate crate = getCrateByName(crateName);
        if (crate == null) return;

        switch (clicked.getItemMeta().getDisplayName()) {
            case "§6Edit Rewards":
                new CrateRewardGUI(plugin, crate).openGUI(player);
                break;

            case "§6Get Crate Block":
                ItemStack crateBlock = plugin.getCrateManager().createPhysicalCrate(crate);
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), crateBlock);
                } else {
                    player.getInventory().addItem(crateBlock);
                }
                player.sendMessage("§aYou received a physical crate block!");
                break;

            case "§6Edit Hologram":
                if (crate.getLocation() == null) {
                    player.sendMessage("§cPlace the crate first before editing holograms!");
                    return;
                }
                new CrateHologramGUI(plugin, crate).openGUI(player);
                break;
        }
    }

    private void handleRewardMenu(Player player, ItemStack clicked, String crateName, int slot, ClickType clickType) {
        Crate crate = getCrateByName(crateName);
        if (crate == null) return;

        // Navigation buttons
        if (clicked.getType() == Material.ARROW) {
            if (clicked.getItemMeta().getDisplayName().equals("§6Previous Page")) {
                new CrateRewardGUI(plugin, crate, getCurrentPage(player) - 1).openGUI(player);
            } else if (clicked.getItemMeta().getDisplayName().equals("§6Next Page")) {
                new CrateRewardGUI(plugin, crate, getCurrentPage(player) + 1).openGUI(player);
            }
            return;
        }

        // Add new reward button
        if (clicked.getType() == Material.EMERALD) {
            startRewardCreation(player, crate);
            return;
        }

        // Existing reward actions
        if (slot < 45) {
            CrateReward reward = crate.getRewards().get(slot + (getCurrentPage(player) * 45));

            if (clickType == ClickType.LEFT) {
                // Edit reward
                startRewardEdit(player, crate, reward);
            } else if (clickType == ClickType.RIGHT) {
                // Remove reward
                crate.removeReward(reward);
                plugin.getCrateManager().saveCrates();
                new CrateRewardGUI(plugin, crate, getCurrentPage(player)).openGUI(player);
            }
        }
    }

    private void handleHologramMenu(Player player, ItemStack clicked, String crateName, int slot) {
        Crate crate = getCrateByName(crateName);
        if (crate == null) return;

        if (clicked.getType() == Material.NAME_TAG) {
            int line = slot - 10;
            if (line >= 0 && line < 3) {
                startHologramEdit(player, crate, line);
            }
        } else if (clicked.getType() == Material.EMERALD) {
            saveHologramChanges(player, crate);
        }
    }

    private void startRewardCreation(Player player, Crate crate) {
        player.closeInventory();
        EditSession session = new EditSession(crate, EditType.NEW_REWARD);
        editingSessions.put(player.getUniqueId(), session);

        player.sendMessage("§aPlace the reward item in your hand and type its chance (0-100)");
        player.sendMessage("§7Type 'cancel' to cancel");
    }

    private void startRewardEdit(Player player, Crate crate, CrateReward reward) {
        player.closeInventory();
        EditSession session = new EditSession(crate, EditType.EDIT_REWARD);
        session.setReward(reward);
        editingSessions.put(player.getUniqueId(), session);

        player.sendMessage("§aEditing reward. Available commands:");
        player.sendMessage("§7- chance <number> : Set the chance");
        player.sendMessage("§7- broadcast <true/false> : Toggle broadcast");
        player.sendMessage("§7- command add <command> : Add command");
        player.sendMessage("§7- command remove <index> : Remove command");
        player.sendMessage("§7- done : Save changes");
        player.sendMessage("§7- cancel : Cancel editing");
    }

    private void startHologramEdit(Player player, Crate crate, int line) {
        player.closeInventory();
        EditSession session = new EditSession(crate, EditType.EDIT_HOLOGRAM);
        session.setHologramLine(line);
        editingSessions.put(player.getUniqueId(), session);

        player.sendMessage("§aType the new text for line " + (line + 1));
        player.sendMessage("§7Type 'cancel' to cancel");
    }

    private void saveHologramChanges(Player player, Crate crate) {
        plugin.getCrateManager().setupHolograms(crate);
        plugin.getCrateManager().saveCrates();
        player.sendMessage("§aHologram updated successfully!");
        player.closeInventory();
    }

    private Crate getCrateByName(String name) {
        return plugin.getCrateManager().getCrates().values().stream()
                .filter(c -> c.getDisplayName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private int getCurrentPage(Player player) {
        return editingSessions.containsKey(player.getUniqueId()) ?
                editingSessions.get(player.getUniqueId()).getPage() : 0;
    }

    private static class EditSession {
        private final Crate crate;
        private final EditType type;
        private CrateReward reward;
        private int hologramLine;
        private int page;

        public EditSession(Crate crate, EditType type) {
            this.crate = crate;
            this.type = type;
        }

        public void setReward(CrateReward reward) {
            this.reward = reward;
        }

        public void setHologramLine(int line) {
            this.hologramLine = line;
        }
        public int getPage() {
            return page;
        }

        // Getters and setters
    }

    private enum EditType {
        NEW_REWARD,
        EDIT_REWARD,
        EDIT_HOLOGRAM
    }
}
