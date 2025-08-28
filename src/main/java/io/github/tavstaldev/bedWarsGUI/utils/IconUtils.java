package io.github.tavstaldev.bedWarsGUI.utils;

import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import org.bukkit.Material;

public class IconUtils {

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

    public static Material getMaterialFromConfig(String configKey) {
        // Assuming you have a method to get the material name from your config
        String materialName = BedWarsGUI.Config().getString(configKey);
        return getMaterial(materialName);
    }

    public static Material getArenaMaterial(String arenaName) {
        String materialName = BedWarsGUI.Config().getString(String.format("arenas.%s", arenaName));
        if (materialName == null || materialName.isEmpty()) {
            return getMaterialFromConfig("arenas.defaultArenaItem"); // Default material for arenas
        }

        return getMaterial(materialName);
    }
}
