package nl.inferno.infernoCrate.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateReward {
    private final String id;
    private String displayName;
    private ItemStack item;
    private double chance;
    private List<String> commands;
    private boolean broadcast;

    public CrateReward(String id, String displayName, ItemStack item, double chance) {
        this.id = id;
        this.displayName = displayName;
        this.item = item;
        this.chance = chance;
        this.commands = new ArrayList<>();
        this.broadcast = false;
    }

    public void addCommand(String command) {
        commands.add(command);
    }

    public void removeCommand(String command) {
        commands.remove(command);
    }


    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public ItemStack getItem() { return item; }
    public void setItem(ItemStack item) { this.item = item; }
    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }
    public List<String> getCommands() { return commands; }
    public boolean isBroadcast() { return broadcast; }
    public void setBroadcast(boolean broadcast) { this.broadcast = broadcast; }
}
