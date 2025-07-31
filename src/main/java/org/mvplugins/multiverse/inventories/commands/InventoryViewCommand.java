package org.mvplugins.multiverse.inventories.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
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
import org.mvplugins.multiverse.inventories.view.InventoryGUIHelper;
import org.mvplugins.multiverse.inventories.view.PlayerInventoryData;
import org.mvplugins.multiverse.inventories.view.ReadOnlyInventoryHolder;
import org.mvplugins.multiverse.inventories.view.InventoryDataProvider;

@Service
final class InventoryViewCommand extends InventoriesCommand {

    private final InventoryDataProvider inventoryDataProvider;
    private final MultiverseInventories inventories;
    private final InventoryGUIHelper inventoryGUIHelper;

    @Inject
    InventoryViewCommand(
            @NotNull InventoryDataProvider inventoryDataProvider,
            @NotNull MultiverseInventories inventories,
            @NotNull InventoryGUIHelper inventoryGUIHelper
    ) {
        this.inventories = inventories;
        this.inventoryDataProvider = inventoryDataProvider;
        this.inventoryGUIHelper = inventoryGUIHelper;
    }

    @Subcommand("view")
    @CommandPermission("multiverse.inventories.view")
    @CommandCompletion("@mvinvplayernames @mvworlds")
    @Syntax("<player> <world>")
    @Description("View a player's inventory in a specific world.")
    void onInventoryViewCommand(
            @NotNull MVCommandIssuer issuer,

            // Ensure the command is run by a player
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

        // Asynchronously load data using InventoryDataProvider
        issuer.sendInfo(ChatColor.YELLOW + "Loading inventory data for " + targetPlayer.getName() + "...");
        handleInventoryLoadAndDisplay(issuer, player, targetPlayer, worldName);
    }

    /**
     * Handles the asynchronous loading of player inventory data and the display of the GUI.
     *
     * @param issuer The command issuer.
     * @param player The player who will view the inventory.
     * @param targetPlayer The offline player whose inventory data is being loaded.
     * @param worldName The name of the world for which inventory data is loaded.
     */
    private void handleInventoryLoadAndDisplay(
            @NotNull MVCommandIssuer issuer,
            @NotNull Player player,
            @NotNull OfflinePlayer targetPlayer,
            @NotNull String worldName) {
    inventoryDataProvider.loadPlayerInventoryData(targetPlayer, worldName)
            .thenAccept(playerInventoryData -> {
                //  Ensure GUI operations run on the main thread
                Bukkit.getScheduler().runTask(inventories, () -> {
                    createAndOpenGUI(issuer, player, targetPlayer, worldName, playerInventoryData);
                }); // End of Bukkit.getScheduler().runTask()
            })
            .exceptionally(throwable -> {
                // This block runs if an exception occurs during data loading
                issuer.sendError(ChatColor.RED + "Failed to load inventory data: " + throwable.getMessage());
                Logging.severe("Error loading inventory for " + targetPlayer.getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
                return null; // Must return null for CompletableFuture<Void> in exceptionally
            });
    }

    /**
     * Creates and opens the custom inventory GUI for viewing.
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
        String title = targetPlayer.getName() + " @ " + worldName;
        Inventory inv = Bukkit.createInventory(new ReadOnlyInventoryHolder(),
                45, // 5 rows for 36 main + 4 armor + 1 off-hand + 4 fillers
                title
        );

        // Call the helper method to populate the GUI
        inventoryGUIHelper.populateInventoryGUI(inv, playerInventoryData, false);
        player.openInventory(inv);
        issuer.sendInfo(ChatColor.GREEN + playerInventoryData.status.getFormattedMessage(targetPlayer.getName(), worldName) + ". Changes will save on close.");
    }
}
