package io.github.tavstaldev.bedWarsGUI.tasks;

import io.github.tavstaldev.bedWarsGUI.managers.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class CacheCleanTask extends BukkitRunnable {
    @Override
    public void run() {
        if (PlayerCacheManager.isMarkedForRemovalEmpty())
            return;

        for (var playerId : PlayerCacheManager.getMarkedForRemovalSet()) {
            var playerCache = PlayerCacheManager.get(playerId);
            if (playerCache == null)
            {
                PlayerCacheManager.unmarkForRemoval(playerId);
                continue;
            }

            PlayerCacheManager.remove(playerId);
            PlayerCacheManager.unmarkForRemoval(playerId);
        }
    }
}
