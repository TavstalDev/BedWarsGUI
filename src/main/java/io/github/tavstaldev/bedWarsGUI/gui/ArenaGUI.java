package io.github.tavstaldev.bedWarsGUI.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.bedWarsGUI.BWGConfiguration;
import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
import io.github.tavstaldev.bedWarsGUI.utils.IconUtils;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

/**
 * The ArenaGUI class is responsible for creating, opening, closing, and refreshing
 * the arena selection GUI for players in the BedWars plugin.
 */
public class ArenaGUI {
    private static final PluginLogger _logger = BedWarsGUI.Logger().withModule(ArenaGUI.class);
    private static final PluginTranslator _translator = BedWarsGUI.Instance.getTranslator();
    private static final Integer itemsPerPage = 28; // Maximum number of items per page
    private static final Integer rows = 6; // Number of rows in the GUI

    /**
     * Creates the arena selection GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance representing the arena GUI.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = BedWarsGUI.GUI().create("...", rows);
            BWGConfiguration config = BedWarsGUI.Config();

            // Create placeholders for empty slots
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, config.guiPlaceholderItem, " "));
            int slots = rows * 9;
            for (int i = 0; i < slots; i++) {
                menu.setButton(0, i, placeholderButton);
            }

            // Back button to return to the main GUI
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, config.guiBackItem, _translator.localize(player, "GUI.Back"))
            ).withListener(event -> {
                close(player);
                MainGUI.open(player);
            });
            menu.setButton(0, 45, closeButton);

            // Page indicator button
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            BedWarsGUI.Instance,
                            config.guiCurrentPageItem,
                            _translator.localize(player, "GUI.Page", Map.of("page", "1"))
                    )
            );
            menu.setButton(0, 49, pageButton);
            return menu;
        } catch (Exception ex) {
            _logger.error("An error occurred while creating the arena GUI.");
            _logger.error(ex);
            return null;
        }
    }

    /**
     * Opens the arena selection GUI for the specified player.
     *
     * @param player The player for whom the GUI is being opened.
     */
    public static void open(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI
        playerCache.setGuiOpened(true);
        var menu = playerCache.getArenaMenu();
        menu.setName(BedWarsGUI.Translator().localize(player, "GUI.Arena.Title", Map.of("mode", playerCache.getSelectedMode().getName(player))));
        player.openInventory(menu.getInventory());
        refresh(player);
    }

    /**
     * Closes the arena selection GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        player.closeInventory();
        playerCache.setGuiOpened(false);
    }

    /**
     * Refreshes the arena selection GUI for the specified player, updating its contents.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        try {
            var playerId = player.getUniqueId();
            var playerCache = PlayerCacheManager.get(playerId);
            var menu = playerCache.getArenaMenu();
            BWGConfiguration config = BedWarsGUI.Config();

            var arenas = playerCache.getAvailableArenas();
            int page = playerCache.getArenaPage();
            boolean hasPrevious = page > 1;
            boolean hasNext = arenas.size() > page * itemsPerPage;

            // Previous page button
            Material prevMaterial = hasPrevious ?
                    config.guiPreviousPageItem
                    :
                    config.guiNoPreviousPageItem;
            String prevName = hasPrevious ? _translator.localize(player, "GUI.PreviousPage") : " ";
            SGButton prevPageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, prevMaterial, prevName)
            ).withListener(event -> {
                var playerCache_ = PlayerCacheManager.get(playerId);
                if (playerCache_.getArenaPage() > 1) {
                    playerCache_.setArenaPage(playerCache_.getArenaPage() - 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 48, prevPageButton);

            // Page indicator button
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, config.guiCurrentPageItem, _translator.localize(player, "GUI.Page", Map.of(
                            "page", String.valueOf(page)))
                    )
            );
            menu.setButton(0, 49, pageButton);

            // Next page button
            Material nextMaterial = hasNext ?
                    config.guiNextPageItem
                    :
                    config.guiNoNextPageItem;
            String nextName = hasNext ? _translator.localize(player, "GUI.NextPage") : " ";
            SGButton nextPageButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, nextMaterial, nextName)
            ).withListener(event -> {
                var playerCache_ = PlayerCacheManager.get(playerId);
                int maxPage = 1 + arenas.size() / itemsPerPage;
                if (playerCache_.getArenaPage() < maxPage) {
                    playerCache_.setArenaPage(playerCache_.getArenaPage() + 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 50, nextPageButton);

            final String bwJoinCommand = config.arenaJoinCommand;

            // Populate the GUI with arena items
            for (int i = 0; i < itemsPerPage; i++) {
                int index = i + (page - 1) * itemsPerPage;
                int slot = i + 10 + (2 * (i / 7));
                if (index >= arenas.size()) {
                    menu.removeButton(0, slot);
                    continue;
                }

                var arena = arenas.get(index);
                Material material = IconUtils.getArenaMaterial(arena.getName());

                String displayName = BedWarsGUI.Translator().localize(player, "GUI.Arena.Name", Map.of("arena", arena.getName()));
                var rawLore = BedWarsGUI.Translator().localizeList(player, "GUI.Arena.Lore");
                var lore = new ArrayList<Component>();
                for (var desc : rawLore) {
                    if (desc.contains("%description%")) {
                        var description = BedWarsGUI.Translator().localizeList(player, String.format("ArenaDescriptions.%s", arena.getName()));
                        if (description == null || description.isEmpty()) {
                            description = BedWarsGUI.Translator().localizeList(player, "ArenaDescriptions.Unknown");
                        }
                        for (var line : description) {
                            lore.add(ChatUtils.translateColors(line, true));
                        }
                        continue;
                    }

                    desc = desc.replace("%status%", BedWarsGUI.Translator().localize(String.format("Status.%s", arena.getStatus().name())))
                            .replace("%players%", String.valueOf(arena.getConnectedPlayers().size()))
                            .replace("%max_players%", String.valueOf(arena.getMaxPlayers()));
                    lore.add(ChatUtils.translateColors(desc, true));
                }

                var item = GuiUtils.createItem(BedWarsGUI.Instance, material, displayName, lore);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
                menu.setButton(0, slot, new SGButton(item).withListener((InventoryClickEvent event) -> {
                    close(player);
                    Bukkit.dispatchCommand(player, String.format(bwJoinCommand, arena.getName()));
                }));
            }
            player.openInventory(menu.getInventory());
        } catch (Exception ex) {
            _logger.error("An error occurred while refreshing the arena GUI.");
            _logger.error(ex);
        }
    }
}
