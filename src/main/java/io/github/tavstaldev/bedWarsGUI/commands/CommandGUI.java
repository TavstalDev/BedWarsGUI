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

/**
 * CommandGUI class handles the `/bwgui` command and its subcommands.
 * It implements the CommandExecutor interface to process commands issued by players or the console.
 */
public class CommandGUI implements CommandExecutor {
    private final PluginLogger _logger = BedWarsGUI.Logger().withModule(CommandGUI.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String baseCommand = "bwgui";
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP subcommand
            add(new SubCommandData("help", "bedwarsgui.commands.help", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )));
            // VERSION subcommand
            add(new SubCommandData("version", "bedwarsgui.commands.version", Map.of(
                    "syntax", "",
                    "description", "Commands.Version.Desc"
            )));
            // RELOAD subcommand
            add(new SubCommandData("reload", "bedwarsgui.commands.reload", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )));
            // OPEN GUI subcommand
            add(new SubCommandData("", "bedwarsgui.commands.gui", Map.of(
                    "syntax", "",
                    "description", "Commands.Gui.Desc"
            )));
        }
    };

    /**
     * Constructor for CommandGUI.
     * Registers the `/bwgui` command and sets this class as its executor.
     * Logs an error if the command is not found in `plugin.yml`.
     */
    public CommandGUI() {
        var command = BedWarsGUI.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the `/bwgui` command and its subcommands.
     *
     * @param sender  The entity (player or console) that issued the command.
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments passed with the command.
     * @return true if the command was processed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Handle console sender
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has permission to use the GUI command
        if (!player.hasPermission("bedwarsgui.commands.gui")) {
            BedWarsGUI.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        // Handle subcommands
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    // Display help menu
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
                    // Display plugin version and update status
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
                        _logger.error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    // Reload the plugin configuration
                    if (!player.hasPermission("bedwarsgui.commands.reload")) {
                        BedWarsGUI.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    BedWarsGUI.Instance.reload();
                    BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
            }

            // Invalid subcommand
            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.InvalidArguments");
            return true;
        }

        // Open the GUI if the player is not in a game
        if (BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) {
            BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.InGame");
            return true;
        }

        MainGUI.open(player);
        return true;
    }

    /**
     * Displays the help menu for the `/bwgui` command.
     *
     * @param player The player requesting the help menu.
     * @param page   The page number of the help menu to display.
     */
    private void help(Player player, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        BedWarsGUI.Instance.sendLocalizedMsg(player, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
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

            subCommand.send(BedWarsGUI.Instance, player, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn = BedWarsGUI.Instance.localize(player, "Commands.Help.PrevBtn");
        String nextBtn = BedWarsGUI.Instance.localize(player, "Commands.Help.NextBtn");
        String bottomMsg = BedWarsGUI.Instance.localize(player, "Commands.Help.Bottom")
                .replace("%current_page%", String.valueOf(page))
                .replace("%max_page%", String.valueOf(maxPage));

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        player.sendMessage(bottomComp);
    }
}
