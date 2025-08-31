package io.github.tavstaldev.bedWarsGUI.commands;

import io.github.tavstaldev.bedWarsGUI.BedWarsGUI;
import io.github.tavstaldev.bedWarsGUI.gui.MainGUI;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.BedwarsAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandGUI implements CommandExecutor {
    private final PluginLogger _logger = BedWarsGUI.Logger().WithModule(CommandGUI.class);
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "bedwarsgui.commands.help", Map.of(
                "syntax", "",
                "description", "Commands.Help.Desc"
            )));
            // VERSION
            add(new SubCommandData("version", "bedwarsgui.commands.version", Map.of(
                "syntax", "",
                "description", "Commands.Version.Desc"
            )));
            // RELOAD
            add(new SubCommandData("reload", "bedwarsgui.commands.reload", Map.of(
               "syntax", "",
               "description", "Commands.Reload.Desc"
            )));
            // OPEN
            add(new SubCommandData("", "bedwarsgui.commands.gui", Map.of(
                "syntax", "",
                "description", "Commands.Gui.Desc"
            )));
        }
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.Info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("bedwarsgui.commands.gui")) {
            BedWarsGUI.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("version", BedWarsGUI.Instance.getVersion());
                    BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Version.Current", parameters);

                    BedWarsGUI.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        } else {
                            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Version.Outdated", Map.of("link", BedWarsGUI.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.Error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("bedwarsgui.commands.reload")) {
                        BedWarsGUI.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    BedWarsGUI.Instance.reload();
                    BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
            }

            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
            return true;
        }

        if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player))
        {
            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.InGame");
            return true;
        }

        MainGUI.open(player);
        return true;
    }

    private void help(Player player, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(player)) {
                i--;
                continue;
            }

            subCommand.send(BedWarsGUI.Instance, player);
        }

        // Bottom message
        String previousBtn = BedWarsGUI.Instance.Localize(player, "Commands.Help.PrevBtn");
        String nextBtn = BedWarsGUI.Instance.Localize(player, "Commands.Help.NextBtn");
        String bottomMsg = BedWarsGUI.Instance.Localize(player, "Commands.Help.Bottom")
                .replace("%current_page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage));

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true).clickEvent(ClickEvent.runCommand("/bwgui help " + (page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true).clickEvent(ClickEvent.runCommand("/bwgui help " + (page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        player.sendMessage(bottomComp);
    }
}
