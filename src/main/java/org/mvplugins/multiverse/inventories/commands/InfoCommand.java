package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.bukkit.Bukkit;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.List;
import java.util.Set;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class InfoCommand extends InventoriesCommand {

    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final WorldGroupManager worldGroupManager;

    @Inject
    InfoCommand(
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull WorldGroupManager worldGroupManager
    ) {
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("info")
    @CommandPermission("multiverse.inventories.info")
    @CommandCompletion("@mvworlds")
    @Syntax("<world|group>")
    @Description("World and Group Information")
    void onInfoCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Single
            @Syntax("<world|group>")
            @Description("World or Group")
            @NotNull String name
    ) {
        if (name == null) {
            if (!issuer.isPlayer()) {
                issuer.sendError(MVInvi18n.INFO_ZEROARG);
                return;
            }
            name = issuer.getPlayer().getWorld().getName();
        }

        ProfileContainer worldProfileContainer = profileContainerStoreProvider.getStore(ContainerType.WORLD).getContainer(name);
        issuer.sendInfo(MVInvi18n.INFO_WORLD, replace("{world}").with(name));
        if (worldProfileContainer != null && Bukkit.getWorld(worldProfileContainer.getContainerName()) != null) {
            worldInfo(issuer, worldProfileContainer);
        } else {
            issuer.sendError(MVInvi18n.ERROR_NOWORLDPROFILE, replace("{world}").with(name));
        }
        WorldGroup worldGroup = worldGroupManager.getGroup(name);
        issuer.sendInfo(MVInvi18n.INFO_GROUP, replace("{group}").with(name));
        if (worldGroup != null) {
            this.groupInfo(issuer, worldGroup);
        } else {
            issuer.sendError(MVInvi18n.ERROR_NOGROUP, replace("{group}").with(name));
        }
    }

    private void groupInfo(MVCommandIssuer issuer, WorldGroup worldGroup) {
        StringBuilder worldsString = new StringBuilder();
        Set<String> worlds = worldGroup.getWorlds();
        if (worlds.isEmpty()) {
            worldsString.append("N/A");
        } else {
            for (String world : worlds) {
                if (!worldsString.toString().isEmpty()) {
                    worldsString.append(", ");
                }
                worldsString.append(world);
            }
        }
        issuer.sendInfo(MVInvi18n.INFO_GROUP_INFO, replace("{worlds}").with(worldsString));
        issuer.sendInfo(MVInvi18n.INFO_GROUP_INFOSHARES, replace("{shares}").with(worldGroup.getShares()));
    }

    private void worldInfo(MVCommandIssuer issuer, ProfileContainer worldProfileContainer) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroup> worldGroups = worldGroupManager.getGroupsForWorld(worldProfileContainer.getContainerName());

        if (worldGroups.isEmpty()) {
            groupsString.append("N/A");
        } else {
            for (WorldGroup worldGroup : worldGroups) {
                if (!groupsString.toString().isEmpty()) {
                    groupsString.append(", ");
                }
                groupsString.append(worldGroup.getName());
            }
        }
        issuer.sendInfo(MVInvi18n.INFO_WORLD_INFO, replace("{groups}").with(groupsString));
    }

    @Service
    private final static class LegacyAlias extends InfoCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(ProfileContainerStoreProvider profileContainerStoreProvider, WorldGroupManager worldGroupManager) {
            super(profileContainerStoreProvider, worldGroupManager);
        }

        @Override
        @CommandAlias("mvinvinfo|mvinvi")
        void onInfoCommand(MVCommandIssuer issuer, String name) {
            super.onInfoCommand(issuer, name);
        }
    }
}
