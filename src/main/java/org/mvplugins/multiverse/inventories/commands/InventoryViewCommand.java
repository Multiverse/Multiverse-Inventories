package org.mvplugins.multiverse.inventories.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
import org.mvplugins.multiverse.inventories.handleshare.SingleShareReader;
import org.mvplugins.multiverse.inventories.listeners.InventoryViewListener;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.util.concurrent.CompletionException;

@Service
public class InventoryViewCommand extends InventoriesCommand {

    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final MultiverseInventories inventories;

    @Inject
    InventoryViewCommand(
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull MultiverseInventories inventories
    ) {
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.inventories = inventories;
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

        // Load the container for this world
        ProfileContainer container = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName);

        if (container == null) {
            issuer.sendError("Could not load profile container for world: " + worldName);
            return;
        }

        // Load the targetPlayer's profile key from the container
        //var profile = container.getPlayerProfileNow(targetPlayer);
        PlayerProfile tempProfile = container.getPlayerProfileNow(ProfileTypes.SURVIVAL, targetPlayer);
        ProfileType profileTypeToUse = ProfileTypes.SURVIVAL; // Default to SURVIVAL

        if (tempProfile == null) {
            // If SURVIVAL profile not found, iterate through other known types as a fallback
            // to find ANY PlayerProfile and use its type.
            for (ProfileType type : ProfileTypes.getTypes()) {
                if (type.equals(ProfileTypes.SURVIVAL)) {
                    continue; // Skip SURVIVAL as we already tried it
                }
                tempProfile = container.getPlayerProfileNow(type, targetPlayer);
                if (tempProfile != null) {
                    profileTypeToUse = type; // Use the type of the found profile
                    break;
                }
            }
        }

        if (tempProfile == null) {
            issuer.sendError("No inventory data found for " + targetPlayer.getName() + " in world " + worldName);
            return;
        }

        ItemStack[] contents = null;
        ItemStack[] armor = null;
        ItemStack offHand = null;

        try {
            // Read main inventory contents
            contents = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.INVENTORY)
                    .read()
                    .join(); // Use .join() to block until the future completes

            // Read armor contents
            armor = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.ARMOR)
                    .read()
                    .join();

            // Read off-hand item
            offHand = SingleShareReader.of(inventories, targetPlayer, worldName, profileTypeToUse, Sharables.OFF_HAND)
                    .read()
                    .join();

        } catch (CompletionException e) {
            issuer.sendError(ChatColor.RED + "Error loading inventory data: " + e.getCause().getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return;
        }

        // Create an inventory for viewing. Size 54 (6 rows) is good for main inventory + armor + offhand.
        // This links the inventory to our listener, making it read-only.
        Inventory inv = Bukkit.createInventory(new InventoryViewListener.ReadOnlyInventoryHolder(), 54, targetPlayer.getName() + " @ " + worldName);

        // Fill in main inventory slots (0–35)
        // Ensure we don't go out of bounds if contents is smaller than expected
        if (contents != null) {
            for (int i = 0; i < Math.min(contents.length, 36); i++) {
                inv.setItem(i, contents[i]);
            }
        }

        if (armor != null && armor.length >= 4) {
            inv.setItem(39, armor[0]); // Helmet (from profile) -> Slot 39 (viewing inv)
            inv.setItem(38, armor[1]); // Chestplate (from profile) -> Slot 38 (viewing inv)
            inv.setItem(37, armor[2]); // Leggings (from profile) -> Slot 37 (viewing inv)
            inv.setItem(36, armor[3]); // Boots (from profile) -> Slot 36 (viewing inv)
        }

        // Fill in offhand slot (40)
        if (offHand != null) {
            inv.setItem(40, offHand);
        }

        // Open the GUI for the viewer
        viewer.openInventory(inv);
    }
}