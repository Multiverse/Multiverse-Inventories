package org.mvplugins.multiverse.inventories.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.view.InventoryDataProvider;
import org.mvplugins.multiverse.inventories.view.InventoryGUIHelper;
import org.mvplugins.multiverse.inventories.view.InventoryStatus;
import org.mvplugins.multiverse.inventories.view.ModifiableInventoryHolder;
import org.mvplugins.multiverse.inventories.view.PlayerInventoryData;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class InventoryModifyCommand extends InventoriesCommand {

    private final InventoryDataProvider inventoryDataProvider;
    private final MultiverseInventories inventories;
    private final InventoryGUIHelper inventoryGUIHelper;

    @Inject
    InventoryModifyCommand(
            @NotNull InventoryDataProvider inventoryDataProvider,
            @NotNull MultiverseInventories inventories,
            @NotNull InventoryGUIHelper inventoryGUIHelper
    ) {
        this.inventoryDataProvider = inventoryDataProvider;
        this.inventories = inventories;
        this.inventoryGUIHelper = inventoryGUIHelper;
    }

    // This method contains the logic for the /mvinv modify command
    @Subcommand("modify")
    @CommandPermission("multiverse.inventories.view.modify") // Specific permission for modification
    @CommandCompletion("@mvinvplayernames @mvworlds")
    @Syntax("<player> <world>")
    @Description("Modify a player's inventory in a specific world.")
    void onInventoryModifyCommand(
            @NotNull MVCommandIssuer issuer,

            // to make sure the command is only run by players
            @Flags("resolve=issuerOnly")
            @NotNull Player player,

            @Syntax("<player>")
            @Description("Online or offline player")
            OfflinePlayer targetPlayer,

            @Syntax("<world>")
            @Description("The world the player's inventory is in")
            MultiverseWorld world
    ) {
        String worldName = world.getName();
        issuer.sendInfo(MVInvi18n.INVENTORY_LOADING, replace("{player}").with(targetPlayer.getName()));
        handleInventoryLoadAndDisplay(issuer, player, targetPlayer, worldName);
    }

    /**
     * Handles the asynchronous loading of player inventory data and the display of the GUI.
     *
     * @param issuer The command issuer.
     * @param player The player who will view/modify the inventory.
     * @param targetPlayer The offline or online player whose inventory data is being loaded.
     * @param worldName The name of the world for which inventory data is loaded.
     */
    private void handleInventoryLoadAndDisplay(
            @NotNull MVCommandIssuer issuer,
            @NotNull Player player,
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName
    ) {
        inventoryDataProvider.loadPlayerInventoryData(targetPlayer, worldName)
                .thenAccept(playerInventoryData -> {
                    // Ensure GUI operations run on the main thread
                    Bukkit.getScheduler().runTask(inventories, () -> {

                        // If the player tries to modify their own live inventory, stop
                        if (player.getUniqueId().equals(targetPlayer.getUniqueId())
                            && player.getWorld().getName().equalsIgnoreCase(worldName)) {
                            issuer.sendError(MVInvi18n.INVENTORY_MODIFYLIVESELF);
                            return; // Stop here if it's a live self-inventory
                        }
                        createAndOpenGUI(issuer, player, targetPlayer, worldName, playerInventoryData);
                    });
                })


                .exceptionally(throwable -> {
                    // This block runs if an exception occurs during data loading
                    String errorMessage = throwable.getLocalizedMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        issuer.sendError(MVInvi18n.INVENTORY_LOADFAILED,
                                replace("{error}").with(Message.of(MVInvi18n.INVENTORY_UNKNOWNLOADERROR)));
                    } else {
                        issuer.sendError(MVInvi18n.INVENTORY_LOADFAILED, replace("{error}").with(throwable));
                    }
                    Logging.fine("Error loading inventory for " + targetPlayer.getName() + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }

    /**
     * Creates and opens the custom inventory GUI for modification.
     * This method must be called on the main server thread.
     *
     * @param issuer The command issuer.
     * @param player The player who will view/modify the inventory.
     * @param targetPlayer The offline player whose inventory is being displayed.
     * @param worldName The name of the world for which inventory data is displayed.
     * @param playerInventoryData The loaded inventory data.
     */
    private void createAndOpenGUI(
            @NotNull MVCommandIssuer issuer,
            @NotNull Player player,
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName,
            @NotNull PlayerInventoryData playerInventoryData
    ) {
        String title = "Modify " + targetPlayer.getName() + " @ " + worldName;
        Inventory inv = Bukkit.createInventory(
                new ModifiableInventoryHolder(
                        targetPlayer,
                        worldName,
                        playerInventoryData.profileTypeUsed // Use the determined profile type
                ),
                45, // 5 rows for 36 main + 4 armor + 1 off-hand + 4 fillers
                title
        );

        // Call the helper method to populate the GUI
        inventoryGUIHelper.populateInventoryGUI(inv, playerInventoryData, true);
        player.openInventory(inv);
        issuer.sendInfo(MVInvi18n.INVENTORY_OPENED,
                replace("{status}").with(getStatusMessage(playerInventoryData.status, targetPlayer.getName(), worldName)));
    }

    private Message getStatusMessage(InventoryStatus status, String playerName, String worldName) {
        MVInvi18n messageKey = switch (status) {
            case LIVE_INVENTORY -> MVInvi18n.INVENTORY_LIVEINVENTORY;
            case STORED_INVENTORY -> MVInvi18n.INVENTORY_STOREDINVENTORY;
            case NO_DATA_FOUND -> MVInvi18n.INVENTORY_NODATAFOUND;
        };
        return Message.of(messageKey,
                replace("{player}").with(playerName),
                replace("{world}").with(worldName));
    }
}
