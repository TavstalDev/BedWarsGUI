package io.github.tavstaldev.bedWarsGUI;

import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
import io.github.tavstaldev.bedWarsGUI.models.PlayerCache;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for handling player-related events in the BedWarsGUI plugin.
 * This class listens for player join and quit events and manages player data accordingly.
 */
public class EventListener implements Listener
{
    // Logger instance for logging messages related to this event listener.
    private static final PluginLogger _logger = BedWarsGUI.Logger().withModule(EventListener.class);

    /**
     * Initializes the event listener by registering it with the Bukkit plugin manager.
     * Logs debug messages during the registration process.
     */
    public static void init() {
        _logger.debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), BedWarsGUI.Instance);
        _logger.debug("Event listener registered.");
    }

    /**
     * Handles the PlayerJoinEvent.
     * Creates a new PlayerCache object for the joining player and adds it to the PlayerCacheManager.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerCache playerData = new PlayerCache(player);
        PlayerCacheManager.add(player.getUniqueId(), playerData);
    }

    /**
     * Handles the PlayerQuitEvent.
     * Removes the player's data from the PlayerCacheManager when they leave the server.
     *
     * @param event The PlayerQuitEvent triggered when a player quits the server.
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerCacheManager.remove(player.getUniqueId());
    }
}