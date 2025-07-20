package org.mvplugins.multiverse.inventories.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
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

            @Syntax("<player>")
            @Description("Online or offline player")
            //Player targetPlayer,
            OfflinePlayer targetPlayer,

            @Syntax("<world>")
            @Description("The world the player's inventory is in")
            MultiverseWorld[] worlds
    ) {
        // Check if the command sender is a player
        if (!(issuer.getIssuer() instanceof Player viewer)) {
            issuer.sendError(ChatColor.RED + "Only players can view inventories.");
            return;
        }

        // Validate world argument
        if (worlds == null || worlds.length == 0 || worlds[0] == null) {
            issuer.sendError(ChatColor.RED + "You must specify a valid world.");
            return;
        }

        // Validate targetPlayer player
        if (targetPlayer == null || targetPlayer.getName() == null) {
            issuer.sendError(ChatColor.RED + "You must specify a valid player.");
            return;
        }

        String worldName = worlds[0].getName();

        // Asynchronously load data using InventoryDataProvider
        issuer.sendInfo(ChatColor.YELLOW + "Loading inventory data for " + targetPlayer.getName() + "...");

        inventoryDataProvider.loadPlayerInventoryData(targetPlayer, worldName)
                .thenAccept(playerInventoryData -> {
                    //  Ensure GUI operations run on the main thread
                    Bukkit.getScheduler().runTask(inventories, () -> {
                        // Create an inventory for viewing.
                        Component title = Component.text(targetPlayer.getName() + " @ " + worldName);
                        Inventory inv = Bukkit.createInventory(new ReadOnlyInventoryHolder(), 45, title);

                        // Fill in main inventory slots (0–35)
                        if (playerInventoryData.contents != null) {
                            for (int i = 0; i < Math.min(playerInventoryData.contents.length, 36); i++) {
                                inv.setItem(i, playerInventoryData.contents[i]);
                            }
                        }
                        // Armor slot mapping for display in the GUI and add fillers if empty
                        // Slot 36: Helmet
                        if (playerInventoryData.armor == null || playerInventoryData.armor[3] == null) {
                            inv.setItem(36, inventoryGUIHelper.createFillerItemForSlot(36)); // Use helper
                        } else {
                            inv.setItem(36, playerInventoryData.armor[3]);
                        }
                        // Slot 37: Chestplate
                        if (playerInventoryData.armor == null || playerInventoryData.armor[2] == null) {
                            inv.setItem(37, inventoryGUIHelper.createFillerItemForSlot(37)); // Use helper
                        } else {
                            inv.setItem(37, playerInventoryData.armor[2]);
                        }
                        // Slot 38: Leggings
                        if (playerInventoryData.armor == null || playerInventoryData.armor[1] == null) {
                            inv.setItem(38, inventoryGUIHelper.createFillerItemForSlot(38)); // Use helper
                        } else {
                            inv.setItem(38, playerInventoryData.armor[1]);
                        }
                        // Slot 39: Boots
                        if (playerInventoryData.armor == null || playerInventoryData.armor[0] == null) {
                            inv.setItem(39, inventoryGUIHelper.createFillerItemForSlot(39)); // Use helper
                        } else {
                            inv.setItem(39, playerInventoryData.armor[0]);
                        }

                        // Off-hand slot (40) and add filler if empty
                        if (playerInventoryData.offHand == null || playerInventoryData.offHand.getType() == Material.AIR) {
                            inv.setItem(40, inventoryGUIHelper.createFillerItemForSlot(40)); // Use helper
                        } else {
                            inv.setItem(40, playerInventoryData.offHand);
                        }
                        // Add the remaining slots as non-interactable filler items
                        for (int i = 41; i <= 44; i++) {
                            inv.setItem(i, inventoryGUIHelper.createFillerItemForSlot(i));
                        }

                        viewer.openInventory(inv);
                        issuer.sendInfo(ChatColor.GREEN + playerInventoryData.statusMessage);
                    }); // End of Bukkit.getScheduler().runTask()
                })
                .exceptionally(throwable -> {
                    // This block runs if an exception occurs during data loading
                    issuer.sendError(ChatColor.RED + "Failed to load inventory data: " + throwable.getMessage());
                    inventories.getLogger().severe("Error loading inventory for " + targetPlayer.getName() + ": " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null; // Must return null for CompletableFuture<Void> in exceptionally
                });
    }
}
