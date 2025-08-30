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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class ArenaGUI {
    private static final PluginLogger _logger = BedWarsGUI.Logger().WithModule(ArenaGUI.class);
    private static final PluginTranslator _translator = BedWarsGUI.Instance.getTranslator();
    private static final Integer ItemsPerPage = 28;
    private static final  Integer Rows = 6;

    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = BedWarsGUI.GUI().create("...", Rows);

            // Create Placeholders
            Material placeholderMaterial = IconUtils.getMaterialFromConfig("gui.placeholderItem");
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, placeholderMaterial, " "));
            int slots = Rows * 9;
            for (int i = 0; i < slots; i++) {
                menu.setButton(0, i, placeholderButton);
            }

            // Back Button
            Material closeMaterial = IconUtils.getMaterialFromConfig("gui.backItem");
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, closeMaterial, _translator.Localize(player, "GUI.Back"))
            ).withListener(event -> {
                close(player);
                MainGUI.open(player);
            });
            menu.setButton(0, 45, closeButton);

            // Page Indicator
            Material pageMaterial = IconUtils.getMaterialFromConfig("gui.currentPageItem");
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(
                            BedWarsGUI.Instance,
                            pageMaterial,
                            _translator.Localize(player, "GUI.Page", Map.of("page", "1"))
                    )
            );
            menu.setButton(0, 49, pageButton);
            return menu;
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while creating the arena GUI.");
            _logger.Error(ex);
            return null;
        }
    }

    public static void open(@NotNull Player player) {
        var playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI
        playerCache.setGuiOpened(true);
        var menu = playerCache.getArenaMenu();
        menu.setName(BedWarsGUI.Translator().Localize(player, "GUI.Arena.Title", Map.of("mode", playerCache.getSelectedMode().getName(player))));
        player.openInventory(menu.getInventory());
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
            var menu = playerCache.getArenaMenu();

            var arenas = playerCache.getAvailableArenas();
            int page = playerCache.getArenaPage();
            boolean hasPrevious = page > 1;
            boolean hasNext = arenas.size() > page * ItemsPerPage;

            //#region Previous Page Button
            Material prevMaterial = hasPrevious ?
                    IconUtils.getMaterialFromConfig("gui.previousPageItem")
                    :
                    IconUtils.getMaterialFromConfig("gui.noPreviousPageItem");
            String prevName = hasPrevious ? _translator.Localize(player, "GUI.PreviousPage") : " ";
            SGButton prevPageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, prevMaterial, prevName )
            ).withListener(event -> {
                var playerCache_ = PlayerCacheManager.get(playerId);
                if (playerCache_.getArenaPage() > 1) {
                    playerCache_.setArenaPage(playerCache_.getArenaPage() - 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 48, prevPageButton);
            //#endregion

            //#region Page Indicator
            Material pageMaterial = IconUtils.getMaterialFromConfig("gui.currentPageItem");
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(BedWarsGUI.Instance, pageMaterial, _translator.Localize(player, "GUI.Page", Map.of(
                            "page", String.valueOf(page)))
                    )
            );
            menu.setButton(0, 49, pageButton);
            //#endregion

            //#region Next Page Button
            Material nextMaterial = hasNext ?
                    IconUtils.getMaterialFromConfig("gui.nextPageItem")
                    :
                    IconUtils.getMaterialFromConfig("gui.noNextPageItem");
            String nextName = hasNext ? _translator.Localize(player, "GUI.NextPage") : " ";
            SGButton nextPageButton = new SGButton(GuiUtils.createItem(BedWarsGUI.Instance, nextMaterial,nextName)
            ).withListener(event -> {
                var playerCache_ = PlayerCacheManager.get(playerId);
                int maxPage = 1 + arenas.size() / ItemsPerPage;
                if (playerCache_.getArenaPage() < maxPage) {
                    playerCache_.setArenaPage(playerCache_.getArenaPage() + 1);
                    refresh(player);
                }
            });
            menu.setButton(0, 50, nextPageButton);
            //#endregion

            String commandConfigValue = BedWarsGUI.Config().getString("arenaJoinCommand");
            final String bwJoinCommand = commandConfigValue != null ? commandConfigValue : "bw join %s";

            for (int i = 0; i < ItemsPerPage; i++) {
                int index = i + (page - 1) * ItemsPerPage;
                int slot = i + 10 + (2 * (i / 7));
                if (index >= arenas.size()) {
                    menu.removeButton(0, slot);
                    continue;
                }

                var arena = arenas.get(index);
                Material material = IconUtils.getArenaMaterial(arena.getName());

                String displayName = BedWarsGUI.Translator().Localize(player, "GUI.Arena.Name", Map.of("arena", arena.getName()));
                var rawLore = BedWarsGUI.Translator().LocalizeList(player, "GUI.Arena.Lore");
                var lore = new ArrayList<Component>();
                for (var desc : rawLore) {
                    if (desc.contains("%description%")) {
                        var description = BedWarsGUI.Translator().LocalizeList(player, String.format("ArenaDescriptions.%s", arena.getName()));
                        if (description == null || description.isEmpty()) {
                            description = BedWarsGUI.Translator().LocalizeList(player, "ArenaDescriptions.Unknown");
                        }
                        for (var line : description) {
                            lore.add(ChatUtils.translateColors(line, true));
                        }
                        continue;
                    }

                    desc = desc.replace("%status%", BedWarsGUI.Translator().Localize(String.format("Status.%s", arena.getStatus().name())))
                                .replace("%players%", String.valueOf(arena.getConnectedPlayers().size()))
                                .replace("%max_players%", String.valueOf(arena.getMaxPlayers()));
                            ;
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
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while refreshing the arena GUI.");
            _logger.Error(ex);
        }
    }
}