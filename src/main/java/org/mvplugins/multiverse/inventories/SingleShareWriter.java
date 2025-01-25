package org.mvplugins.multiverse.inventories;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.share.Sharable;

/**
 * Write a single share to the relevant world and group profiles.
 *
 * @param <T>   The sharable type.
 */
public class SingleShareWriter<T> {

    public static <T> SingleShareWriter<T> of(MultiverseInventories inventories, Player player, Sharable<T> sharable) {
        return new SingleShareWriter<T>(inventories, player, sharable);
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
        if (sharable.isOptional() && !this.inventories.getMVIConfig().getOptionalShares().contains(sharable)) {
            Logging.finer("Skipping write for optional share: " + sharable);
            return;
        }
        Logging.finer("Writing single share: " + sharable.getNames()[0]);
        String worldName = this.player.getWorld().getName();
        this.inventories.getWorldProfileContainerStore()
                .getContainer(worldName)
                .getPlayerData(this.player)
                .set(this.sharable, value);

        this.inventories.getGroupManager().getGroupsForWorld(worldName).forEach(worldGroup -> {
            worldGroup.getGroupProfileContainer().getPlayerData(this.player)
                    .set(this.sharable, value);
        });
    }
}
