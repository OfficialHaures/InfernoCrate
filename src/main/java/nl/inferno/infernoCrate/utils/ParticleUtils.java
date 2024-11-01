package nl.inferno.infernoCrate.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import nl.inferno.infernoCrate.InfernoCrate;

public class ParticleUtils {

    public static void playCrateOpenEffect(Location location) {
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {
                phi += Math.PI/8;
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/16) {
                    double r = 1.5;
                    double x = r * Math.cos(theta) * Math.sin(phi);
                    double y = r * Math.cos(phi) + 1.5;
                    double z = r * Math.sin(theta) * Math.sin(phi);

                    location.add(x, y, z);
                    location.getWorld().spawnParticle(Particle.SPELL_WITCH, location, 1, 0, 0, 0, 0);
                    location.subtract(x, y, z);
                }

                if (phi > 2*Math.PI) {
                    this.cancel();
                }
            }
        }.runTaskTimer(InfernoCrate.getInstance(), 0, 1);
    }

    public static void playWinningEffect(Location location) {
        new BukkitRunnable() {
            double t = 0;

            @Override
            public void run() {
                t += 0.5;
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/8) {
                    double x = 2 * Math.cos(theta) * Math.cos(t);
                    double y = 0.2 * t;
                    double z = 2 * Math.sin(theta) * Math.cos(t);

                    location.add(x, y, z);
                    location.getWorld().spawnParticle(Particle.TOTEM, location, 1, 0, 0, 0, 0);
                    location.subtract(x, y, z);
                }

                if (t > 6) {
                    this.cancel();
                }
            }
        }.runTaskTimer(InfernoCrate.getInstance(), 0, 1);
    }

    public static void playIdleEffect(Location location) {
        new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                angle += Math.PI/16;
                double x = 0.8 * Math.cos(angle);
                double z = 0.8 * Math.sin(angle);

                location.add(x, 0.5, z);
                location.getWorld().spawnParticle(Particle.END_ROD, location, 1, 0, 0, 0, 0);
                location.subtract(x, 0.5, z);

                if (angle > 2*Math.PI) {
                    angle = 0;
                }
            }
        }.runTaskTimer(InfernoCrate.getInstance(), 0, 2);
    }
}
