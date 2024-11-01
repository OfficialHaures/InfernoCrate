package nl.inferno.infernoCrate.utils;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import java.util.List;

public class HologramUtils {

    private static final double LINE_HEIGHT = 0.25;

    public static List<ArmorStand> createHologram(Location location, List<String> lines) {
        List<ArmorStand> holograms = new ArrayList<>();
        Location currentLocation = location.clone().add(0, (lines.size() - 1) * LINE_HEIGHT, 0);

        for (String line : lines) {
            ArmorStand hologram = (ArmorStand) location.getWorld().spawnEntity(currentLocation, EntityType.ARMOR_STAND);

            hologram.setVisible(false);
            hologram.setGravity(false);
            hologram.setCanPickupItems(false);
            hologram.setCustomNameVisible(true);
            hologram.setCustomName(line);
            hologram.setMarker(true);

            holograms.add(hologram);
            currentLocation.subtract(0, LINE_HEIGHT, 0);
        }

        return holograms;
    }

    public static void updateHologram(List<ArmorStand> holograms, List<String> newLines) {
        int minSize = Math.min(holograms.size(), newLines.size());

        for (int i = 0; i < minSize; i++) {
            holograms.get(i).setCustomName(newLines.get(i));
        }
    }

    public static void removeHologram(List<ArmorStand> holograms) {
        holograms.forEach(ArmorStand::remove);
    }

    public static void createAnimatedHologram(Location location, String text) {
        for (ArmorStand armorStand : new ArmorStand[]{
                createFloatingText(location.clone().add(0, 0.5, 0), text)
        }) {

        }

    }

    private static ArmorStand createFloatingText(Location location, String text) {
        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setCustomNameVisible(true);
        as.setCustomName(text);
        as.setMarker(true);
        return as;
    }
}
