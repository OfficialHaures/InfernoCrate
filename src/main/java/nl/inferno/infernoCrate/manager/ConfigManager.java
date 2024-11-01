package nl.inferno.infernoCrate.manager;

import nl.inferno.infernoCrate.InfernoCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final InfernoCrate plugin;
    private FileConfiguration cratesConfig;
    private File cratesFile;

    public ConfigManager(InfernoCrate plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        cratesFile = new File(plugin.getDataFolder(), "crates.yml");

        if (!cratesFile.exists()) {
            plugin.saveResource("crates.yml", false);
        }

        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
    }

    public FileConfiguration getCratesConfig() {
        return cratesConfig;
    }

    public void saveCratesConfig() {
        try {
            cratesConfig.save(cratesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save crates.yml!");
            e.printStackTrace();
        }
    }

    public void reloadConfigs() {
        cratesConfig = YamlConfiguration.loadConfiguration(cratesFile);
    }
}
