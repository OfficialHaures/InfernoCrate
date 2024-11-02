package nl.inferno.infernoCrate.manager;

import nl.inferno.infernoCrate.InfernoCrate;
import nl.inferno.infernoCrate.models.Crate;
import nl.inferno.infernoCrate.models.CrateReward;
import nl.inferno.infernoCrate.models.CrateType;
import nl.inferno.infernoCrate.utils.HologramUtils;
import nl.inferno.infernoCrate.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrateManager {
    private final InfernoCrate plugin;
    private final Map<String, Crate> crates;

    public CrateManager(InfernoCrate plugin) {
        this.plugin = plugin;
        this.crates = new HashMap<>();
    }

    public void loadCrates() {
        FileConfiguration config = plugin.getConfigManager().getCratesConfig();
        ConfigurationSection cratesSection = config.getConfigurationSection("crates");

        if (cratesSection == null) return;

        for (String crateId : cratesSection.getKeys(false)) {
            ConfigurationSection crateSection = cratesSection.getConfigurationSection(crateId);
            if (crateSection == null) continue;

            String displayName = crateSection.getString("displayName");
            CrateType type = CrateType.valueOf(crateSection.getString("type", "COMMON"));

            Crate crate = new Crate(crateId, displayName, type);

            // Load location
            if (crateSection.contains("location")) {
                ConfigurationSection locSection = crateSection.getConfigurationSection("location");
                if (locSection != null) {
                    String worldName = locSection.getString("world");
                    double x = locSection.getDouble("x");
                    double y = locSection.getDouble("y");
                    double z = locSection.getDouble("z");
                    float yaw = (float) locSection.getDouble("yaw", 0.0);
                    float pitch = (float) locSection.getDouble("pitch", 0.0);

                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location loc = new Location(world, x, y, z, yaw, pitch);
                        crate.setLocation(loc);
                        setupHolograms(crate);
                    }
                }
            }

            // Load rewards
            loadRewards(crate, crateSection.getConfigurationSection("rewards"));

            crates.put(crateId, crate);
        }
    }

    public void saveCrates() {
        FileConfiguration config = plugin.getConfigManager().getCratesConfig();
        config.set("crates", null);

        for (Crate crate : crates.values()) {
            String path = "crates." + crate.getId();
            config.set(path + ".displayName", crate.getDisplayName());
            config.set(path + ".type", crate.getType().name());

            if (crate.getLocation() != null) {
                Location loc = crate.getLocation();
                config.set(path + ".location.world", loc.getWorld().getName());
                config.set(path + ".location.x", loc.getX());
                config.set(path + ".location.y", loc.getY());
                config.set(path + ".location.z", loc.getZ());
                config.set(path + ".location.yaw", loc.getYaw());
                config.set(path + ".location.pitch", loc.getPitch());
            }

            saveRewards(crate, config, path + ".rewards");
        }

        plugin.getConfigManager().saveCratesConfig();
    }

    private void loadRewards(Crate crate, ConfigurationSection rewardsSection) {
        if (rewardsSection == null) return;

        for (String rewardId : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardId);
            if (rewardSection == null) continue;

            String displayName = rewardSection.getString("displayName");
            ItemStack item = rewardSection.getItemStack("item");
            double chance = rewardSection.getDouble("chance", 1.0);

            CrateReward reward = new CrateReward(rewardId, displayName, item, chance);
            reward.setBroadcast(rewardSection.getBoolean("broadcast", false));

            List<String> commands = rewardSection.getStringList("commands");
            commands.forEach(reward::addCommand);

            crate.addReward(reward);
        }
    }

    private void saveRewards(Crate crate, FileConfiguration config, String path) {
        for (CrateReward reward : crate.getRewards()) {
            String rewardPath = path + "." + reward.getId();
            config.set(rewardPath + ".displayName", reward.getDisplayName());
            config.set(rewardPath + ".item", reward.getItem());
            config.set(rewardPath + ".chance", reward.getChance());
            config.set(rewardPath + ".broadcast", reward.isBroadcast());
            config.set(rewardPath + ".commands", reward.getCommands());
        }
    }

    public void setupHolograms(Crate crate) {
        if (crate.getLocation() == null) return;

        if (!crate.getHolograms().isEmpty()) {
            HologramUtils.removeHologram(crate.getHolograms());
        }

        List<String> lines = List.of(
                crate.getType().getDisplayName(),
                crate.getDisplayName(),
                "§7Right-click with key to open!"
        );

        Location holoLoc = crate.getLocation().clone().add(0.5, 2, 0.5);
        crate.setHolograms(HologramUtils.createHologram(holoLoc, lines));

        ParticleUtils.playIdleEffect(crate.getLocation().clone().add(0.5, 0.5, 0.5));
    }

    public void openCrate(Player player, Crate crate) {
        CrateReward reward = crate.getRandomReward();
        if (reward == null) {
            player.sendMessage("§cThis crate has no rewards configured!");
            return;
        }

        player.getInventory().addItem(reward.getItem().clone());

        if (reward.isBroadcast()) {
            Bukkit.broadcastMessage("§6" + player.getName() + " §7has won " + reward.getDisplayName() + " §7from a " + crate.getDisplayName() + "§7!");
        }

        for (String command : reward.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", player.getName()));
        }
    }


    public Crate getCrate(String id) {
        return crates.get(id);
    }

    public Map<String, Crate> getCrates() {
        return crates;
    }

    public ItemStack createPhysicalCrate(Crate crate) {
        ItemStack crateItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = crateItem.getItemMeta();
        meta.setDisplayName(crate.getDisplayName());
        meta.setLore(Arrays.asList(
                "§7Place this crate anywhere",
                "§7in the world!",
                "",
                "§8ID: " + crate.getId()
        ));
        crateItem.setItemMeta(meta);
        return crateItem;
    }

}
