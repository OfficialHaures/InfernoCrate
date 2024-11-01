package nl.inferno.infernoCrate.models;

import org.bukkit.ChatColor;

public enum CrateType {
    COMMON(ChatColor.GREEN + "Common"),
    RARE(ChatColor.BLUE + "Rare"),
    EPIC(ChatColor.LIGHT_PURPLE + "Epic"),
    LEGENDARY(ChatColor.GOLD + "Legendary");

    private final String displayName;

    CrateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
