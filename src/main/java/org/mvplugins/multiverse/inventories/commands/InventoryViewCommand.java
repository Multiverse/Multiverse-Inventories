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
import org.mvplugins.multiverse.inventories.view.ReadOnlyInventoryHolder;
import org.mvplugins.multiverse.inventories.profile.InventoryDataProvider;

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

        inventoryDataProvider.loadPlayerInventoryData(targetPlayer, worldName)
                .thenAccept(playerInventoryData -> {
                    //  Ensure GUI operations run on the main thread
                    Bukkit.getScheduler().runTask(inventories, () -> {
                        // Create an inventory for viewing.
                        String title = targetPlayer.getName() + " @ " + worldName;
                        Inventory inv = Bukkit.createInventory(new ReadOnlyInventoryHolder(), 45, title);

                        // Call the helper method to populate the GUI
                        inventoryGUIHelper.populateInventoryGUI(inv, playerInventoryData, false);
                        player.openInventory(inv);
                        issuer.sendInfo(playerInventoryData.status.getFormattedMessage(targetPlayer.getName(),worldName));
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
}
