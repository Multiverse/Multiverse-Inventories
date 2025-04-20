package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public record BulkProfilesPayload(@NotNull GlobalProfileKey[] globalProfileKeys,
                                  @NotNull ContainerKey[] containerKeys,
                                  @NotNull ProfileType[] profileTypes,
                                  boolean includeGroupsWorlds) {

    public Map<String, List<String>> getSummary() {
        return Map.of(
                "Players", Arrays.stream(globalProfileKeys)
                        .map(GlobalProfileKey::getPlayerName)
                        .toList(),
                "Worlds", Arrays.stream(containerKeys)
                        .filter(c -> c.getContainerType() == ContainerType.WORLD)
                        .map(ContainerKey::getDataName)
                        .toList(),
                "Groups", Arrays.stream(containerKeys)
                        .filter(c -> c.getContainerType() == ContainerType.GROUP)
                        .map(ContainerKey::getDataName)
                        .toList(),
                "Profile Types", Arrays.stream(profileTypes)
                        .map(ProfileType::getName)
                        .map(String::toLowerCase)
                        .toList()
        );
    }
}
