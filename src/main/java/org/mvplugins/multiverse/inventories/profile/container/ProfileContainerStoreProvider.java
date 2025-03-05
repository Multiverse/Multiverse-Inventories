package org.mvplugins.multiverse.inventories.profile.container;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;

import java.util.EnumMap;
import java.util.Map;

/**
 * A provider for ProfileContainerStore instances based on ContainerType.
 */
@Service
public class ProfileContainerStoreProvider {

    private final Map<ContainerType, ProfileContainerStore> stores;
    private final MultiverseInventories inventories;

    @Inject
    ProfileContainerStoreProvider(@NotNull MultiverseInventories inventories) {
        this.inventories = inventories;
        stores = new EnumMap<>(ContainerType.class);
    }

    /**
     * Gets the store for a given container type.
     *
     * @param type  the container type
     * @return the store
     */
    public ProfileContainerStore getStore(ContainerType type) {
        return stores.computeIfAbsent(type, t -> new ProfileContainerStore(inventories, t));
    }

    public void clearCache() {
        stores.clear();
    }
}
