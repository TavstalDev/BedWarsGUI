package io.github.tavstaldev.bedWarsGUI;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.bedWarsGUI.commands.CommandGUI;
import io.github.tavstaldev.bedWarsGUI.metrics.Metrics;
import io.github.tavstaldev.bedWarsGUI.models.ArenaMode;
import io.github.tavstaldev.bedWarsGUI.tasks.CacheCleanTask;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.screamingsandals.bedwars.api.BedwarsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The BedWarsGUI class is the main plugin class for the BedWarsGUI plugin.
 * It extends the PluginBase class and provides initialization, configuration,
 * and management of the plugin's features, such as arena modes, GUI handling,
 * and integration with the BedWars plugin.
 */
public class BedWarsGUI extends PluginBase {
    public static BedWarsGUI Instance; // Singleton instance of the plugin
    private SpiGUI _spiGUI; // SpiGUI instance for managing GUIs
    private BedwarsAPI _bedwarsApi; // API instance for interacting with the BedWars plugin
    private List<ArenaMode> _arenaModes; // List of available arena modes
    private CacheCleanTask cacheCleanTask; // Task for cleaning player caches

    /**
     * Retrieves the plugin's logger instance.
     *
     * @return The PluginLogger instance for logging messages.
     */
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }

    /**
     * Retrieves the plugin's translator instance.
     *
     * @return The PluginTranslator instance for localization.
     */
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }

    /**
     * Retrieves the plugin's configuration instance.
     *
     * @return The BWGConfiguration instance for accessing configuration values.
     */
    public static BWGConfiguration Config() {
        return (BWGConfiguration) Instance.getConfig();
    }

    /**
     * Retrieves the SpiGUI instance for managing GUIs.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI GUI() {
        return Instance._spiGUI;
    }

    /**
     * Retrieves the BedWars API instance.
     *
     * @return The BedwarsAPI instance.
     */
    public static BedwarsAPI BedwarsApi() {
        return Instance._bedwarsApi;
    }

    /**
     * Retrieves the list of available arena modes.
     *
     * @return A list of ArenaMode instances.
     */
    public static List<ArenaMode> ArenaModes() {
        return Instance._arenaModes;
    }

    /**
     * Constructs a BedWarsGUI instance and sets the plugin's update URL.
     */
    public BedWarsGUI() {
        super(true, "https://github.com/TavstalDev/BedWarsGUI/releases/latest");
    }

    /**
     * Called when the plugin is enabled. Initializes the plugin, loads configurations,
     * hooks into the BedWars plugin, and registers commands and tasks.
     */
    @Override
    public void onEnable() {
        Instance = this;
        super.onEnable();
        _config = new BWGConfiguration();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.info(String.format("Loading %s...", getProjectName()));

        if (VersionUtils.isLegacy()) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!_translator.load()) {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Check BedWars Plugin
        _logger.debug("Hooking into BedWars...");
        if (Bukkit.getPluginManager().isPluginEnabled("BedWars") || Bukkit.getPluginManager().isPluginEnabled("ScreamingBedWars")) {
            _bedwarsApi = BedwarsAPI.getInstance();
            _logger.info("BedWars found and hooked into it.");
        } else {
            _logger.warn("BedWars not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize SpiGUI
        _logger.debug("Initializing SpiGUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        _logger.debug("Registering commands...");
        new CommandGUI();

        // Load Arena Modes
        _logger.debug("Loading arena modes...");
        _arenaModes = new ArrayList<>();
        List<?> modesList = getConfig().getList("modes");
        if (modesList == null) {
            _logger.error("Failed to load arena modes... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (Object item : modesList) {
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

        // Register cache cleanup task
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask();
        cacheCleanTask.runTaskTimer(this, 0, 5 * 60 * 20);

        // Metrics
        try {
            @SuppressWarnings("unused") Metrics metrics = new Metrics(this, 27758);
        }
        catch (Exception ex)
        {
            _logger.error("Failed to start Metrics: " + ex.getMessage());
        }

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (Config().checkForUpdates) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.ok("Plugin is up to date!");
                } else {
                    _logger.warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    /**
     * Called when the plugin is disabled. Cleans up resources and logs the unload event.
     */
    @Override
    public void onDisable() {
        super.onDisable();
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Reloads the plugin's configuration and arena modes.
     */
    public void reload() {
        _logger.info(String.format("Reloading %s...", getProjectName()));
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        this._config.load();
        _logger.debug("Configuration reloaded.");

        _arenaModes.clear();
        _logger.debug("Loading arena modes...");
        List<?> modesList = getConfig().getList("modes");
        if (modesList == null) {
            _logger.error("Failed to load arena modes... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (Object item : modesList) {
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
        _logger.debug("Loaded arena modes.");
    }
}