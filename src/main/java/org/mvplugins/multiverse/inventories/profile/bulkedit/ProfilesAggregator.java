package org.mvplugins.multiverse.inventories.profile.bulkedit;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public final class ProfilesAggregator {

    private final WorldGroupManager worldGroupManager;

    @Inject
    ProfilesAggregator(WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    public List<ProfileFileKey> getProfileFileKeys(
            @NotNull GlobalProfileKey[] globalProfileKeys,
            @NotNull ContainerKey[] containerKeys,
            boolean includeGroupsWorlds
    ) {
        if (includeGroupsWorlds) {
            containerKeys = includeGroupsWorlds(containerKeys);
        }
        List<ProfileFileKey> profileFileKeys = new ArrayList<>(globalProfileKeys.length * containerKeys.length);
        for (GlobalProfileKey globalProfileKey : globalProfileKeys) {
            for (ContainerKey containerKey : containerKeys) {
                profileFileKeys.add(ProfileFileKey.create(
                        containerKey.getContainerType(),
                        containerKey.getDataName(),
                        globalProfileKey.getPlayerUUID()));
            }
        }
        return profileFileKeys;
    }

    public List<ProfileKey> getPlayerProfileKeys(
            @NotNull GlobalProfileKey[] globalProfileKeys,
            @NotNull ContainerKey[] containerKeys,
            @NotNull ProfileType[] profileTypes,
            boolean includeGroupsWorlds
    ) {
        if (includeGroupsWorlds) {
            containerKeys = includeGroupsWorlds(containerKeys);
        }
        List<ProfileKey> profileKeys = new ArrayList<>(globalProfileKeys.length * containerKeys.length * profileTypes.length);
        for (GlobalProfileKey globalProfileKey : globalProfileKeys) {
            for (ContainerKey containerKey : containerKeys) {
                for (ProfileType profileType : profileTypes) {
                    profileKeys.add(ProfileKey.create(
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
            containerKeyList.addAll(group.getWorlds().stream()
                    .map(worldName -> ContainerKey.create(ContainerType.WORLD, worldName))
                    .toList());
        }
        return containerKeyList.toArray(ContainerKey[]::new);
    }
}
