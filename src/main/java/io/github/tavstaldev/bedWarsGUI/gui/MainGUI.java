package io.github.tavstaldev.bedWarsGUI.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
import io.github.tavstaldev.bedWarsGUI.utils.IconUtils;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class MainGUI {
    private static final PluginLogger _logger = BedWarsGUI.Logger().WithModule(MainGUI.class);
    private static final PluginTranslator _translator = BedWarsGUI.Instance.getTranslator();
    private static final Integer ItemsPerPage = 14;
    private static final  Integer Rows = 4;

    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = BedWarsGUI.GUI().create(_translator.Localize(player, "GUI.Main.Title"), Rows);

            // Create Placeholders
            Material placeholderMaterial = IconUtils.getMaterialFromConfig("gui.placeholderItem");
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, placeholderMaterial, " "));
            int slots = Rows * 9;
            for (int i = 0; i < slots; i++) {
                menu.setButton(0, i, placeholderButton);
            }

            // Close Button
            Material closeMaterial = IconUtils.getMaterialFromConfig("gui.closeItem");
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, closeMaterial, _translator.Localize(player, "GUI.Close"))
            ).withListener(event -> close(player));
            menu.setButton(0, 27, closeButton);

            // Page Indicator
            Material pageMaterial = IconUtils.getMaterialFromConfig("gui.currentPageItem");
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            BedWarsGUI.Instance,
                            pageMaterial,
                            _translator.Localize(player, "GUI.Page", Map.of("page", "1"))
                    )
            );
            menu.setButton(0, 31, pageButton);
            return menu;
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while creating the main GUI.");
            _logger.Error(ex);
            return null;
        }
    }

    public static void open(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI
        playerCache.setGuiOpened(true);
        playerCache.setMainPage(1);
        player.openInventory(playerCache.getMainMenu().getInventory());
        refresh(player);
    }

    public static void close(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        player.closeInventory();
        playerCache.setGuiOpened(false);
    }

    public static void refresh(@NotNull Player player) {
        try {
            var playerId = player.getUniqueId();
            var playerCache = PlayerCacheManager.get(playerId);
            var menu = playerCache.getMainMenu();

            //var games = BedWarsGUI.BedwarsApi().getGames();
            var arenas = BedWarsGUI.ArenaModes();
            int page = playerCache.getMainPage();
            boolean hasPrevious = page > 1;
            boolean hasNext = arenas.size() > page * ItemsPerPage;

            //#region Previous Page Button
            // Material
            Material prevMaterial = hasPrevious ?
                    IconUtils.getMaterialFromConfig("gui.previousPageItem")
                    :
                    IconUtils.getMaterialFromConfig("gui.noPreviousPageItem");
            // Name
            String prevName = hasPrevious ? _translator.Localize(player, "GUI.PreviousPage") : " ";
            // Button
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
            //#endregion

            //#region Page Indicator
            Material pageMaterial = IconUtils.getMaterialFromConfig("gui.currentPageItem");
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, pageMaterial, _translator.Localize(player, "GUI.Page", Map.of(
                            "page", String.valueOf(page)))
                    )
            );
            menu.setButton(0, 31, pageButton);
            //#endregion

            //#region Next Page Button
            Material nextMaterial = hasNext ?
                    IconUtils.getMaterialFromConfig("gui.nextPageItem")
                    :
                    IconUtils.getMaterialFromConfig("gui.noNextPageItem");

            String nextName = hasNext ? _translator.Localize(player, "GUI.NextPage") : " ";
            SGButton nextPageButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, nextMaterial, nextName)
            ).withListener(event -> {
                var playerCache_ = PlayerCacheManager.get(playerId);
                int maxPage = 1 + arenas.size() / ItemsPerPage;
                if (playerCache_.getMainPage() < maxPage) {
                    playerCache_.setMainPage(playerCache_.getMainPage() + 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 32, nextPageButton);
            //#endregion

            // TODO: Pagination for arenas
            // At the moment this is not needed as there are not that many arena modes
            for (var arena : arenas) {
                var material = arena.getItem();
                String displayName = BedWarsGUI.Translator().Localize(player, "GUI.Main.Name", Map.of("mode", arena.getName(player)));
                var rawLore = BedWarsGUI.Translator().LocalizeList(player, "GUI.Main.Lore");
                var lore = new ArrayList<Component>();
                for (var desc : rawLore) {
                    if (desc.contains("%description%")) {
                        var description = arena.getDescription(player);
                        if (description.isEmpty()) {
                            continue;
                        }
                        if (description.contains("\n")) {
                            String[] descLines = description.split("\n");
                            for (var line : descLines) {
                                lore.add(ChatUtils.translateColors(line, true));
                            }
                            continue;
                        }
                        lore.add(ChatUtils.translateColors(description, true));
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
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while refreshing the main GUI.");
            _logger.Error(ex);
        }
    }
}
