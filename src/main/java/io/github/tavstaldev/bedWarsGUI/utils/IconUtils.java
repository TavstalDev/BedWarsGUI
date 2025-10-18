package io.github.tavstaldev.bedWarsGUI.utils;

import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import org.bukkit.Material;

/**
 * The IconUtils class provides utility methods for retrieving Material instances
 * based on material names or configuration keys. It includes methods to handle
 * default materials and materials specific to arenas.
 */
public class IconUtils {

    /**
     * Retrieves a Material instance based on the provided material name.
     * If the material name is null, empty, or invalid, it defaults to Material.STONE.
     *
     * @param materialName The name of the material to retrieve.
     * @return The corresponding Material instance, or Material.STONE if not found.
     */
    public static Material getMaterial(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return Material.STONE; // Default material if name is null or empty
        }
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.STONE; // Default material if not found
        }
        return material;
    }

    /**
     * Retrieves a Material instance based on a configuration key.
     * The configuration key is used to fetch the material name from the plugin's configuration.
     *
     * @param configKey The configuration key to look up the material name.
     * @return The corresponding Material instance, or Material.STONE if not found.
     */
    public static Material getMaterialFromConfig(String configKey) {
        // Assuming you have a method to get the material name from your config
        String materialName = BedWarsGUI.Config().getString(configKey);
        return getMaterial(materialName);
    }

    /**
     * Retrieves a Material instance for a specific arena based on its name.
     * If the arena-specific material is not found, it defaults to a material
     * specified by the "arenas.defaultArenaItem" configuration key.
     *
     * @param arenaName The name of the arena to retrieve the material for.
     * @return The corresponding Material instance, or the default arena material if not found.
     */
    public static Material getArenaMaterial(String arenaName) {
        String materialName = BedWarsGUI.Config().getString(String.format("arenas.%s", arenaName));
        if (materialName == null || materialName.isEmpty()) {
            return getMaterialFromConfig("arenas.defaultArenaItem"); // Default material for arenas
        }

        return getMaterial(materialName);
    }
}
