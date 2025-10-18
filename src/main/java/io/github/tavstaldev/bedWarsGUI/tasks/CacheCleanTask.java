package io.github.tavstaldev.bedWarsGUI.tasks;

import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The CacheCleanTask class is a scheduled task that periodically cleans up
 * player caches marked for removal in the BedWars plugin.
 * It extends BukkitRunnable to allow execution on the server's main thread.
 */
public class CacheCleanTask extends BukkitRunnable {
    /**
     * Executes the cache cleaning task.
     * This method is called periodically to remove player caches that are marked for removal.
     * If the marked-for-removal set is empty, the task exits early.
     */
    @Override
    public void run() {
        // Check if there are any player caches marked for removal
        if (PlayerCacheManager.isMarkedForRemovalEmpty())
            return;

        // Iterate through the set of player IDs marked for removal
        for (var playerId : PlayerCacheManager.getMarkedForRemovalSet()) {
            // Retrieve the player cache for the given player ID
            var playerCache = PlayerCacheManager.get(playerId);
            if (playerCache == null) {
                // If the player cache is null, unmark the player ID for removal and continue
                PlayerCacheManager.unmarkForRemoval(playerId);
                continue;
            }

            // Remove the player cache and unmark the player ID for removal
            PlayerCacheManager.remove(playerId);
            PlayerCacheManager.unmarkForRemoval(playerId);
        }
    }
}
