package nl.inferno.infernoCrate.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Crate {
    private final String id;
    private String displayName;
    private Material crateBlock;
    private Location location;
    private List<CrateReward> rewards;
    private List<ArmorStand> holograms;
    private CrateType type;
    private ItemStack key;

    public Crate(String id, String displayName, CrateType type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rewards = new ArrayList<>();
        this.holograms = new ArrayList<>();
        this.crateBlock = Material.ENDER_CHEST;
    }

    public void addReward(CrateReward reward) {
        rewards.add(reward);
    }

    public CrateReward getRandomReward() {
        if (rewards.isEmpty()) {
            return null;
        }

        double totalWeight = rewards.stream()
                .mapToDouble(CrateReward::getChance)
                .sum();

        double random = Math.random() * totalWeight;
        double currentWeight = 0;

        for (CrateReward reward : rewards) {
            currentWeight += reward.getChance();
            if (random <= currentWeight) {
                return reward;
            }
        }

        return rewards.get(0);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Material getCrateBlock() { return crateBlock; }
    public void setCrateBlock(Material crateBlock) { this.crateBlock = crateBlock; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public List<CrateReward> getRewards() { return rewards; }
    public List<ArmorStand> getHolograms() { return holograms; }
    public void setHolograms(List<ArmorStand> holograms) { this.holograms = holograms; }
    public CrateType getType() { return type; }
    public void setType(CrateType type) { this.type = type; }
    public ItemStack getKey() { return key; }
    public void setKey(ItemStack key) { this.key = key; }

    public void removeReward(CrateReward reward) {
        rewards.remove(reward);
    }
}
