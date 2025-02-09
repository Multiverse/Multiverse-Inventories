package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharable;

/**
 * Write a single share to the relevant world and group profiles.
 *
 * @param <T>   The sharable type.
 */
final class SingleShareWriter<T> {

    public static <T> SingleShareWriter<T> of(MultiverseInventories inventories, Player player, Sharable<T> sharable) {
        return new SingleShareWriter<>(inventories, player, sharable);
    }

    private final MultiverseInventories inventories;
    private final Player player;
    private final Sharable<T> sharable;

    private SingleShareWriter(MultiverseInventories inventories, Player player, Sharable<T> sharable) {
        this.inventories = inventories;
        this.player = player;
        this.sharable = sharable;
    }

    public void write(T value) {
        if (sharable.isOptional() &&
                !inventories.getServiceLocator().getService(InventoriesConfig.class).getOptionalShares().contains(sharable)) {
            Logging.finer("Skipping write for optional share: " + sharable);
            return;
        }
        Logging.finer("Writing single share: " + sharable.getNames()[0]);
        String worldName = this.player.getWorld().getName();
        var profileContainerStoreProvider = this.inventories.getServiceLocator().getService(ProfileContainerStoreProvider.class);
        profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName)
                .getPlayerData(this.player)
                .set(this.sharable, value);

        this.inventories.getServiceLocator().getService(WorldGroupManager.class)
                .getGroupsForWorld(worldName)
                .forEach(worldGroup ->
                        worldGroup.getGroupProfileContainer().getPlayerData(this.player)
                                .set(this.sharable, value));
    }
}
