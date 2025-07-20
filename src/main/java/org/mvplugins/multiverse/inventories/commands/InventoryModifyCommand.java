package org.mvplugins.multiverse.inventories.commands;

import net.kyori.adventure.text.Component;
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
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.InventoryDataProvider;
import org.mvplugins.multiverse.inventories.view.ModifiableInventoryHolder;

@Service
final class InventoryModifyCommand extends InventoriesCommand {

    private final InventoryDataProvider inventoryDataProvider;
    private final MultiverseInventories inventories;

    @Inject
    InventoryModifyCommand(
            @NotNull InventoryDataProvider inventoryDataProvider,
            @NotNull MultiverseInventories inventories
    ) {
        this.inventoryDataProvider = inventoryDataProvider;
        this.inventories = inventories;
    }

    // This method contains the logic for the /mvinv modify command
    @Subcommand("modify")
    @CommandPermission("multiverse.inventories.view.modify") // Specific permission for modification
    @CommandCompletion("@mvinvplayernames @mvworlds")
    @Syntax("<player> <world>")
    @Description("Modify a player's inventory in a specific world.")
    void onInventoryModifyCommand(
            @NotNull MVCommandIssuer issuer,
            @Syntax("<player>")
            @Description("Online or offline player")
            OfflinePlayer targetPlayer,

            @Syntax("<world>")
            @Description("The world the player's inventory is in")
            MultiverseWorld[] worlds
    ) {
        if (!(issuer.getIssuer() instanceof Player viewer)) {
            issuer.sendError(ChatColor.RED + "Only players can modify inventories.");
            return;
        }
        if (worlds == null || worlds.length == 0 || worlds[0] == null) {
            issuer.sendError(ChatColor.RED + "You must specify a valid world.");
            return;
        }
        if (targetPlayer == null || targetPlayer.getName() == null) {
            issuer.sendError(ChatColor.RED + "You must specify a valid player.");
            return;
        }

        String worldName = worlds[0].getName();
        // Asynchronously load data using InventoryDataProvider
        issuer.sendInfo(ChatColor.YELLOW + "Loading inventory data for " + targetPlayer.getName() + "...");

        inventoryDataProvider.loadPlayerInventoryData(targetPlayer, worldName)
                .thenAccept(playerInventoryData -> {
                    // Ensure GUI operations run on the main thread
                    Bukkit.getScheduler().runTask(inventories, () -> {
                        // Create inventory with ModifiableInventoryHolder
                        // Pass all necessary context to the holder for saving on close.
                        Component title = Component.text("Modifiying " + targetPlayer.getName() + " @ " + worldName);
                        Inventory inv = Bukkit.createInventory(
                                new ModifiableInventoryHolder(
                                        targetPlayer,
                                        worldName,
                                        playerInventoryData.profileTypeUsed, // Use the determined profile type
                                        inventories
                                ),
                                54,
                                title
                        );

                        // Fill inventory
                        if (playerInventoryData.contents != null) {
                            for (int i = 0; i < Math.min(playerInventoryData.contents.length, 36); i++) {
                                inv.setItem(i, playerInventoryData.contents[i]);
                            }
                        }
                        if (playerInventoryData.armor != null && playerInventoryData.armor.length >= 4) {
                            inv.setItem(39, playerInventoryData.armor[0]);
                            inv.setItem(38, playerInventoryData.armor[1]);
                            inv.setItem(37, playerInventoryData.armor[2]);
                            inv.setItem(36, playerInventoryData.armor[3]);
                        }
                        if (playerInventoryData.offHand != null) {
                            inv.setItem(40, playerInventoryData.offHand);
                        }

                        viewer.openInventory(inv);
                        issuer.sendInfo(ChatColor.GREEN + "Opened editable inventory for " + targetPlayer.getName() + " in world " + worldName + ". Changes will save on close.");
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

