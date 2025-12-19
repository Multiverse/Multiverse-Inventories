package org.mvplugins.multiverse.inventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AchievementMessagePreProcessEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * Spigot is old and outdated, so we have this separate class to handle Paper-specific
 * advancement done event features that uses adventure text components.
 */
final class AdvancementDonePaperHelper {

    private static AdvancementDonePaperHelper instance;

    static AdvancementDonePaperHelper getInstance() {
        if (instance == null) {
            instance = new AdvancementDonePaperHelper();
        }
        return instance;
    }

    private AdvancementDonePaperHelper() {
    }

    void registerDiscordSrvHook() {
        Logging.fine("Registering DiscordSRV advancement grant hook.");
        DiscordSRV.api.subscribe(new DiscordSrvHook());
    }

    void handleAdvancementDoneEventSuppression(PlayerAdvancementDoneEvent event) {
        Logging.finest("Suppressing advancement done message for player %s due to share handling.",
                event.getPlayer().getName());
        event.message(net.kyori.adventure.text.Component.text("mvinv:suppressed"));
    }

    private static class DiscordSrvHook {
        @Subscribe
        public void onAchievementMessage(AchievementMessagePreProcessEvent event) {
            if (!(event.getTriggeringBukkitEvent() instanceof PlayerAdvancementDoneEvent advancementEvent)) {
                return;
            }
            if (net.kyori.adventure.text.Component.EQUALS.test(
                    advancementEvent.message(),
                    net.kyori.adventure.text.Component.text("mvinv:suppressed"))) {
                Logging.finest("Suppressing DiscordSRV advancement message for player %s due to share handling.",
                        advancementEvent.getPlayer().getName());
                event.setCancelled(true);
            }
        }
    }
}
