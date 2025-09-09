package io.github.tavstaldev.bedWarsGUI;

import io.github.tavstaldev.bedWarsGUI.utils.IconUtils;
import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BWGConfiguration extends ConfigurationBase {
    public BWGConfiguration() {
        super(BedWarsGUI.Instance, "config.yml", null);
    }

    public String prefix;
    public boolean checkForUpdates, debug;

    public String arenaJoinCommand, arenaAutoJoinCommand;


    public Material guiPlaceholderItem, guiNoPreviousPageItem, guiPreviousPageItem, guiCurrentPageItem, guiNoNextPageItem, guiNextPageItem,
            guiCloseItem, guiBackItem, guiAutoJoinItem;

    public Material defaultArenaItem;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "eng");
        resolve("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&cBed&fWarsGUI &8»");

        arenaJoinCommand = resolveGet("commands.arenaJoin", "bw join %s");
        arenaAutoJoinCommand = resolveGet("commands.arenaAutoJoin", "bw autojoin");

        // modes
        if (get("modes") == null) {
            resolve("modes", Arrays.asList(
                    Map.of(
                            "name", "Modes.Solo.Name",
                            "description", "Modes.Solo.Description",
                            "maxPlayers", 4,
                            "slot", 11,
                            "item", "WOODEN_SWORD"
                    ),
                    Map.of(
                            "name", "Modes.Duo.Name",
                            "description", "Modes.Duo.Description",
                            "maxPlayers", 8,
                            "slot", 13,
                            "item", "STONE_SWORD"
                    ),
                    Map.of(
                            "name", "Modes.Thrio.Name",
                            "description", "Modes.Thrio.Description",
                            "maxPlayers", 12,
                            "slot", 15,
                            "item", "IRON_SWORD"
                    ),
                    Map.of(
                            "name", "Modes.Squad.Name",
                            "description", "Modes.Squad.Description",
                            "maxPlayers", 16,
                            "slot", 22,
                            "item", "DIAMOND_SWORD"
                    )
            ));
            resolveComment("modes", List.of(
                    "Arena mode settings",
                    "You can add/remove modes, but make sure to also update the lang file accordingly",
                    "The maxPlayers is used for checking if an arena is suitable for the mode",
                    "The item is used as the icon in the GUI"
            ));
        }

        // gui
        String material = resolveGet("gui.placeholderItem", "BLACK_STAINED_GLASS_PANE");
        guiPlaceholderItem = IconUtils.getMaterial(material);
        material = resolveGet("gui.noPreviousPageItem", "BLACK_STAINED_GLASS_PANE");
        guiNoPreviousPageItem = IconUtils.getMaterial(material);
        material = resolveGet("gui.previousPageItem", "ARROW");
        guiPreviousPageItem = IconUtils.getMaterial(material);
        material = resolveGet("gui.currentPageItem", "PAPER");
        guiCurrentPageItem =  IconUtils.getMaterial(material);
        material = resolveGet("gui.noNextPageItem", "BLACK_STAINED_GLASS_PANE");
        guiNoNextPageItem =  IconUtils.getMaterial(material);
        material = resolveGet("gui.nextPageItem", "ARROW");
        guiNextPageItem =  IconUtils.getMaterial(material);
        material = resolveGet("gui.closeItem", "BARRIER");
        guiCloseItem = IconUtils.getMaterial(material);
        material = resolveGet("gui.backItem", "SPRUCE_DOOR");
        guiBackItem =  IconUtils.getMaterial(material);
        material = resolveGet("gui.autoJoinItem", "ENDER_EYE");
        guiAutoJoinItem =  IconUtils.getMaterial(material);

        // arenas
        if (get("arenas") == null) {
            material = resolveGet("arenas.defaultArenaItem", "BARRIER");
            defaultArenaItem =  IconUtils.getMaterial(material);

            resolve("arenas.ArenaOne", "GRASS_BLOCK");
            resolve("arenas.ArenaTwo", "SAND");
        } else {
            material = resolveGet("arenas.defaultArenaItem", "BARRIER");
            defaultArenaItem =  IconUtils.getMaterial(material);
        }
        resolveComment("arenas", List.of(
                "For setting arena item icons in the GUI",
                "The arena names here must match with the arena names in ScreamingBedWars.",
                "If an arena is not listed here, the defaultArenaItem will be used"
        ));
    }
}
