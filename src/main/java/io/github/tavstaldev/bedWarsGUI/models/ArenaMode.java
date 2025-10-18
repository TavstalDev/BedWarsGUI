package io.github.tavstaldev.bedWarsGUI.models;

import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The ArenaMode class represents a game mode in the BedWars plugin.
 * It contains information about the mode's name, description, associated item,
 * maximum number of players, and its slot in the GUI.
 */
@SuppressWarnings("ClassCanBeRecord")
public class ArenaMode {
    private final String name; // The localized name key for the arena mode
    private final String description; // The localized description key for the arena mode
    private final String item; // The material name for the item representing the mode
    private final Integer maxPlayers; // The maximum number of players allowed in this mode
    private final Integer slot; // The slot in the GUI where this mode is displayed

    /**
     * Constructs an ArenaMode instance with the specified properties.
     *
     * @param name        The localized name key for the arena mode.
     * @param description The localized description key for the arena mode.
     * @param item        The material name for the item representing the mode.
     * @param maxPlayers  The maximum number of players allowed in this mode.
     * @param slot        The slot in the GUI where this mode is displayed.
     */
    public ArenaMode(String name, String description, String item, Integer maxPlayers, Integer slot) {
        this.name = name;
        this.description = description;
        this.item = item;
        this.maxPlayers = maxPlayers;
        this.slot = slot;
    }

    /**
     * Retrieves the localized name of the arena mode for the specified player.
     *
     * @param player The player for whom the name should be localized.
     * @return The localized name of the arena mode.
     */
    public String getName(Player player) {
        return BedWarsGUI.Translator().localize(player, name);
    }

    /**
     * Retrieves the localized description of the arena mode for the specified player.
     *
     * @param player The player for whom the description should be localized.
     * @return A list of localized description strings for the arena mode.
     */
    public List<String> getDescription(Player player) {
        return BedWarsGUI.Translator().localizeList(player, description);
    }

    /**
     * Retrieves the Material representing the arena mode.
     * If the material name is invalid, it defaults to STONE.
     *
     * @return The Material representing the arena mode.
     */
    public Material getItem() {
        Material material = Material.matchMaterial(item);
        if (material == null) {
            material = Material.STONE; // Default material if not found
        }
        return material;
    }

    /**
     * Retrieves the maximum number of players allowed in this arena mode.
     *
     * @return The maximum number of players.
     */
    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Retrieves the slot in the GUI where this arena mode is displayed.
     *
     * @return The slot number.
     */
    public Integer getSlot() {
        return slot;
    }
}
