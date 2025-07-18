package org.mvplugins.multiverse.inventories.profile.bulkedit;

import com.google.common.collect.Sets;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
final class PlayerProfilesAggregator {

    private final WorldGroupManager worldGroupManager;

    @Inject
    PlayerProfilesAggregator(WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    List<ProfileFileKey> getProfileFileKeys(PlayerProfilesPayload payload) {
        var containerKeys = payload.includeGroupsWorlds()
                ? includeGroupsWorlds(payload.containerKeys())
                : payload.containerKeys();

        List<ProfileFileKey> profileFileKeys = new ArrayList<>(
                payload.globalProfileKeys().length * containerKeys.length);

        for (GlobalProfileKey globalProfileKey : payload.globalProfileKeys()) {
            for (ContainerKey containerKey : containerKeys) {
                profileFileKeys.add(ProfileFileKey.of(
                        containerKey.getContainerType(),
                        containerKey.getDataName(),
                        globalProfileKey.getPlayerUUID(),
                        globalProfileKey.getPlayerName()));
            }
        }
        return profileFileKeys;
    }

    List<ProfileKey> getPlayerProfileKeys(PlayerProfilesPayload payload) {
        var containerKeys = payload.includeGroupsWorlds()
                ? includeGroupsWorlds(payload.containerKeys())
                : payload.containerKeys();

        List<ProfileKey> profileKeys = new ArrayList<>(
                payload.globalProfileKeys().length * containerKeys.length * payload.profileTypes().length);

        for (GlobalProfileKey globalProfileKey : payload.globalProfileKeys()) {
            for (ContainerKey containerKey : containerKeys) {
                for (ProfileType profileType : payload.profileTypes()) {
                    profileKeys.add(ProfileKey.of(
                            containerKey.getContainerType(),
                            containerKey.getDataName(),
                            profileType,
                            globalProfileKey.getPlayerUUID(),
                            globalProfileKey.getPlayerName()));
                }
            }
        }
        return profileKeys;
    }

    private ContainerKey[] includeGroupsWorlds(ContainerKey[] containerKeys) {
        Set<ContainerKey> containerKeyList = Sets.newHashSet(containerKeys);
        for (ContainerKey containerKey : containerKeys) {
            if (containerKey.getContainerType() != ContainerType.GROUP) {
                continue;
            }
            WorldGroup group = worldGroupManager.getGroup(containerKey.getDataName());
            if (group == null) {
                continue;
            }
            containerKeyList.addAll(group.getApplicableWorlds().stream()
                    .map(worldName -> ContainerKey.create(ContainerType.WORLD, worldName))
                    .toList());
        }
        return containerKeyList.toArray(ContainerKey[]::new);
    }
}
