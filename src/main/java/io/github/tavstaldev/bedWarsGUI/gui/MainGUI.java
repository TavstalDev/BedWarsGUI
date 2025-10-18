package io.github.tavstaldev.bedWarsGUI.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.bedWarsGUI.BWGConfiguration;
import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
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
 * The MainGUI class is responsible for creating, opening, closing, and refreshing
 * the main GUI for players in the BedWars plugin.
 */
public class MainGUI {
    private static final PluginLogger _logger = BedWarsGUI.Logger().withModule(MainGUI.class);
    private static final PluginTranslator _translator = BedWarsGUI.Instance.getTranslator();
    private static final Integer itemsPerPage = 14; // Maximum number of items per page
    private static final Integer rows = 4; // Number of rows in the GUI

    /**
     * Creates the main GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance representing the main GUI.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = BedWarsGUI.GUI().create(_translator.localize(player, "GUI.Main.Title"), rows);
            BWGConfiguration config = BedWarsGUI.Config();

            // Create placeholders for empty slots
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, config.guiPlaceholderItem, " "));
            int slots = rows * 9;
            for (int i = 0; i < slots; i++) {
                menu.setButton(0, i, placeholderButton);
            }

            // Close button to exit the GUI
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, config.guiCloseItem, _translator.localize(player, "GUI.Close"))
            ).withListener(event -> close(player));
            menu.setButton(0, 27, closeButton);

            // Page indicator button
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            BedWarsGUI.Instance,
                            config.guiCurrentPageItem,
                            _translator.localize(player, "GUI.Page", Map.of("page", "1"))
                    )
            );
            menu.setButton(0, 31, pageButton);

            // AutoJoin button to join an arena automatically
            SGButton autoJoinButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, config.guiAutoJoinItem, _translator.localize(player, "GUI.AutoJoin"))
            ).withListener(event -> {
                final String command = config.arenaAutoJoinCommand;
                close(player);
                Bukkit.dispatchCommand(player, command);
            });
            menu.setButton(0, 35, autoJoinButton);
            return menu;
        } catch (Exception ex) {
            _logger.error("An error occurred while creating the main GUI.");
            _logger.error(ex);
            return null;
        }
    }

    /**
     * Opens the main GUI for the specified player.
     *
     * @param player The player for whom the GUI is being opened.
     */
    public static void open(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI
        playerCache.setGuiOpened(true);
        playerCache.setMainPage(1);
        player.openInventory(playerCache.getMainMenu().getInventory());
        refresh(player);
    }

    /**
     * Closes the main GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        player.closeInventory();
        playerCache.setGuiOpened(false);
    }

    /**
     * Refreshes the main GUI for the specified player, updating its contents.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        try {
            var playerId = player.getUniqueId();
            var playerCache = PlayerCacheManager.get(playerId);
            var menu = playerCache.getMainMenu();
            BWGConfiguration config = BedWarsGUI.Config();

            var arenas = BedWarsGUI.ArenaModes();
            int page = playerCache.getMainPage();
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
                if (playerCache_.getMainPage() > 1) {
                    playerCache_.setMainPage(playerCache_.getMainPage() - 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 30, prevPageButton);

            // Page indicator button
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, config.guiCurrentPageItem, _translator.localize(player, "GUI.Page", Map.of(
                            "page", String.valueOf(page)))
                    )
            );
            menu.setButton(0, 31, pageButton);

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
                if (playerCache_.getMainPage() < maxPage) {
                    playerCache_.setMainPage(playerCache_.getMainPage() + 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 32, nextPageButton);

            // Populate the GUI with arena modes
            for (var arena : arenas) {
                var material = arena.getItem();
                String displayName = BedWarsGUI.Translator().localize(player, "GUI.Main.Name", Map.of("mode", arena.getName(player)));
                var rawLore = BedWarsGUI.Translator().localizeList(player, "GUI.Main.Lore");
                var lore = new ArrayList<Component>();
                for (var desc : rawLore) {
                    if (desc.contains("%description%")) {
                        var description = arena.getDescription(player);
                        if (description == null || description.isEmpty()) {
                            continue;
                        }
                        for (var line : description) {
                            lore.add(ChatUtils.translateColors(line, true));
                        }
                        continue;
                    }

                    desc = desc.replace("%mode%", arena.getName(player));
                    lore.add(ChatUtils.translateColors(desc, true));
                }
                var item = GuiUtils.createItem(BedWarsGUI.Instance, material, displayName, lore);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
                menu.setButton(arena.getSlot(), new SGButton(item).withListener((InventoryClickEvent event) -> {
                    var data = PlayerCacheManager.get(playerId);
                    close(player);
                    data.setArenaPage(1);
                    data.setSelectedMode(arena);
                    var games = BedWarsGUI.BedwarsApi().getGames();
                    data.setAvailableArenas(games.stream().filter(g -> g.getMaxPlayers() == arena.getMaxPlayers()).toList());
                    ArenaGUI.open(player);
                }));
            }
            player.openInventory(menu.getInventory());
        } catch (Exception ex) {
            _logger.error("An error occurred while refreshing the main GUI.");
            _logger.error(ex);
        }
    }
}
