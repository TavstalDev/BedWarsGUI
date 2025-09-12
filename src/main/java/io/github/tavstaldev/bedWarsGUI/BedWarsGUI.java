package io.github.tavstaldev.bedWarsGUI;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.bedWarsGUI.commands.CommandGUI;
import io.github.tavstaldev.bedWarsGUI.models.ArenaMode;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.screamingsandals.bedwars.api.BedwarsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedWarsGUI extends PluginBase {
    public static BedWarsGUI Instance;
    private SpiGUI _spiGUI;
    private BedwarsAPI _bedwarsApi;
    private List<ArenaMode> _arenaModes;

    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }
    public static BWGConfiguration Config(){
        return (BWGConfiguration) Instance.getConfig();
    }
    public static SpiGUI GUI() {
        return Instance._spiGUI;
    }
    public static BedwarsAPI BedwarsApi() {return Instance._bedwarsApi;}
    public static List<ArenaMode> ArenaModes() {return Instance._arenaModes;}

    public BedWarsGUI() {
        super(true, "https://github.com/TavstalDev/BedWarsGUI/releases/latest");
    }

    @Override
    public void onEnable() {
        Instance = this;
        _config = new BWGConfiguration();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.Info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.Error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.Load())
        {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Check BedWars Plugin
        _logger.Debug("Hooking into BedWars...");
        if (Bukkit.getPluginManager().isPluginEnabled("BedWars") || Bukkit.getPluginManager().isPluginEnabled("ScreamingBedWars"))
        {
            _bedwarsApi = BedwarsAPI.getInstance();
            _logger.Info("BedWars found and hooked into it.");
        }
        else
        {
            _logger.Warn("BedWars not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize SpiGUI
        _logger.Debug("Initializing SpiGUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        _logger.Debug("Registering commands...");
        var command = getCommand("bwgui");
        if (command != null) {
            command.setExecutor(new CommandGUI());
        }

        // Load Arena Modes
        _logger.Debug("Loading arena modes...");
        _arenaModes = new ArrayList<>();
        List<?> modesList = getConfig().getList("modes");
        if (modesList == null) {
            _logger.Error("Failed to load arena modes... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (Object item : modesList) {
            // Each 'item' in the list is a LinkedHashMap.
            if (item instanceof Map) {
                Map<String, Object> modeMap = (Map<String, Object>) item;

                String name = (String) modeMap.get("name");
                String description = (String) modeMap.get("description");
                String itemString = (String) modeMap.get("item");
                int maxPlayers = (Integer) modeMap.get("maxPlayers");
                int slot = (Integer) modeMap.get("slot");

                ArenaMode arenaMode = new ArenaMode(name, description, itemString, maxPlayers, slot);
                _arenaModes.add(arenaMode);
            }
        }


        _logger.Ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (Config().checkForUpdates) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.Ok("Plugin is up to date!");
                } else {
                    _logger.Warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.Error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    @Override
    public void onDisable() {
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    public void reload() {
        _logger.Info(String.format("Reloading %s...", getProjectName()));
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this._config.load();
        _logger.Debug("Configuration reloaded.");

        _arenaModes.clear();
        _logger.Debug("Loading arena modes...");
        List<?> modesList = getConfig().getList("modes");
        if (modesList == null) {
            _logger.Error("Failed to load arena modes... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (Object item : modesList) {
            // Each 'item' in the list is a LinkedHashMap.
            if (item instanceof Map) {
                Map<String, Object> modeMap = (Map<String, Object>) item;

                String name = (String) modeMap.get("name");
                String description = (String) modeMap.get("description");
                String itemString = (String) modeMap.get("item");
                int maxPlayers = (Integer) modeMap.get("maxPlayers");
                int slot = (Integer) modeMap.get("slot");

                ArenaMode arenaMode = new ArenaMode(name, description, itemString, maxPlayers, slot);
                _arenaModes.add(arenaMode);
            }
        }
        _logger.Debug("Loaded arena modes.");
    }
}
