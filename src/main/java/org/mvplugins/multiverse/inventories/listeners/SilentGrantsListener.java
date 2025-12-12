package org.mvplugins.multiverse.inventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AchievementMessagePreProcessEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.handleshare.PlayerShareHandlingState;
import org.mvplugins.multiverse.inventories.share.Sharables;

@Service
final class SilentGrantsListener implements MVInvListener  {

    private final PlayerShareHandlingState playerShareHandlingState;

    private final boolean hasShouldShowNotificationMethod;
    private final boolean hasPlayerAdvancementDoneMessageMethod;

    @Inject
    SilentGrantsListener(PlayerShareHandlingState playerShareHandlingState) {
        this.playerShareHandlingState = playerShareHandlingState;

        this.hasShouldShowNotificationMethod = ReflectHelper.getMethod(
                PlayerRecipeDiscoverEvent.class,"shouldShowNotification") != null;
        this.hasPlayerAdvancementDoneMessageMethod = ReflectHelper.getMethod(
                PlayerAdvancementDoneEvent.class,"message") != null;

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Logging.fine("Registering DiscordSRV advancement grant hook.");
            DiscordSRV.api.subscribe(new DiscordSrvHook());
        }
    }

    @EventMethod
    void onPlayerRecipeDiscover(PlayerRecipeDiscoverEvent event) {
        if (!this.hasShouldShowNotificationMethod) {
            // spigot does not have the method to suppress notifications
            return;
        }
        if (playerShareHandlingState.isHandlingSharable(event.getPlayer(), Sharables.RECIPES)) {
            Logging.finest("Suppressing recipe discover notification for player %s due to share handling.",
                    event.getPlayer().getName());
            event.shouldShowNotification(false);
        }
    }

    @EventMethod
    void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        if (!this.hasPlayerAdvancementDoneMessageMethod) {
            // paper does not have the method to suppress notifications
            return;
        }
        if (playerShareHandlingState.isHandlingSharable(event.getPlayer(), Sharables.ADVANCEMENTS)) {
            Logging.finest("Suppressing advancement done message for player %s due to share handling.",
                    event.getPlayer().getName());
            event.message(null);
        }
    }

    @EventMethod
    void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("DiscordSRV")) {
            Logging.fine("Registering DiscordSRV advancement grant hook.");
            DiscordSRV.api.subscribe(new DiscordSrvHook());
        }
    }

    private class DiscordSrvHook {
        @Subscribe
        public void onAchievementMessage(AchievementMessagePreProcessEvent event) {
            if (playerShareHandlingState.isHandlingSharable(event.getPlayer(), Sharables.ADVANCEMENTS)) {
                Logging.finest("Suppressing DiscordSRV advancement grant message for player %s due to share handling.",
                        event.getPlayer().getName());
                event.setCancelled(true);
            }
        }
    }
}
