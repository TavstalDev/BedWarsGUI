package io.github.tavstaldev.bedWarsGUI.models;

import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ArenaMode {
    private final String name;
    private final String description;
    private final String item;
    private final Integer maxPlayers;
    private final Integer slot;

    public ArenaMode(String name, String description, String item, Integer maxPlayers, Integer slot) {
        this.name = name;
        this.description = description;
        this.item = item;
        this.maxPlayers = maxPlayers;
        this.slot = slot;
    }

    public String getName(Player player) {
        return BedWarsGUI.Translator().Localize(player, name);
    }

    public List<String> getDescription(Player player) {
        return BedWarsGUI.Translator().LocalizeList(player, description);
    }

    public Material getItem() {
        Material material = Material.matchMaterial(item);
        if (material == null) {
            material = Material.STONE; // Default material if not found
        }
        return material;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public Integer getSlot() {
        return slot;
    }
}
