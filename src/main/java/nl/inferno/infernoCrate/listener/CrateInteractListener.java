package nl.inferno.infernoCrate.listener;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.models.Crate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CrateInteractListener implements Listener {

    private final InfernoCrate plugin;

    public CrateInteractListener(InfernoCrate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCrateInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Location blockLoc = block.getLocation();

        for (Crate crate : plugin.getCrateManager().getCrates().values()) {
            if (crate.getLocation() != null && crate.getLocation().equals(blockLoc)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                if (itemInHand != null && itemInHand.isSimilar(crate.getKey())) {
                    if (itemInHand.getAmount() > 1) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }

                    plugin.getCrateManager().openCrate(player, crate);
                } else {
                    player.sendMessage("§cYou need a " + crate.getType().getDisplayName() + " §ckey to open this crate!");
                }
                return;
            }
        }
    }

    @EventHandler
    public void onCrateBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        for (Crate crate : plugin.getCrateManager().getCrates().values()) {
            if (crate.getLocation() != null && crate.getLocation().equals(blockLoc)) {
                Player player = event.getPlayer();

                if (!player.hasPermission("infernocrate.admin")) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou don't have permission to break crates!");
                    return;
                }

                if (!crate.getHolograms().isEmpty()) {
                    crate.getHolograms().forEach(hologram -> hologram.remove());
                    crate.getHolograms().clear();
                }
                crate.setLocation(null);
                plugin.getCrateManager().saveCrates();

                player.sendMessage("§aCrate successfully removed!");
                return;
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() == Material.ENDER_CHEST && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            String id = getCrateIdFromLore(item);
            if (id != null) {
                Player player = (Player) event.getWhoClicked();
                if (!player.hasPermission("infernocrate.admin")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private String getCrateIdFromLore(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                return line.substring(6);
            }
        }
        return null;
    }


    @EventHandler
    public void onCratePlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item.getType() == Material.ENDER_CHEST && item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().hasLore()) {

            String crateName = item.getItemMeta().getDisplayName();

            for (Crate crate : plugin.getCrateManager().getCrates().values()) {
                if (crateName.equals(crate.getDisplayName())) {
                    if (!player.hasPermission("infernocrate.admin")) {
                        event.setCancelled(true);
                        player.sendMessage("§cYou don't have permission to place crates!");
                        return;
                    }

                    crate.setLocation(event.getBlock().getLocation());
                    plugin.getCrateManager().setupHolograms(crate);
                    plugin.getCrateManager().saveCrates();

                    player.sendMessage("§aCrate successfully placed!");
                    return;
                }
            }
        }
    }
}
