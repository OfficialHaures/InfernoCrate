package nl.inferno.infernoCrate;

import nl.inferno.infernoCrate.commands.CrateCommand;
import nl.inferno.infernoCrate.gui.GuiListener;
import nl.inferno.infernoCrate.listener.CrateInteractListener;
import nl.inferno.infernoCrate.manager.ConfigManager;
import nl.inferno.infernoCrate.manager.CrateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class InfernoCrate extends JavaPlugin {

    private static InfernoCrate instance;
    private CrateManager crateManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);
        this.crateManager = new CrateManager(this);
        getCommand("crate").setExecutor(new CrateCommand(this));
        getServer().getPluginManager().registerEvents(new CrateInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        configManager.loadConfigs();
        crateManager.loadCrates();
    }

    @Override
    public void onDisable() {
        if (crateManager != null) {
            crateManager.saveCrates();
        }
    }

    public static InfernoCrate getInstance() {
        return instance;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
