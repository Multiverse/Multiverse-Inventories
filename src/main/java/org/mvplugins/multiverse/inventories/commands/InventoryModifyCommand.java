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
public class InventoryModifyCommand extends InventoriesCommand {

    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final MultiverseInventories inventories;

    @Inject
    InventoryModifyCommand(
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull MultiverseInventories inventories
    ) {
        this.profileContainerStoreProvider = profileContainerStoreProvider;
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
            OfflinePlayer target,

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
        if (target == null || target.getName() == null) {
            issuer.sendError(ChatColor.RED + "You must specify a valid player.");
            return;
        }

        String worldName = worlds[0].getName();
        ProfileContainer container = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName);
        if (container == null) {
            issuer.sendError(ChatColor.RED + "Could not load profile container for world: " + worldName);
            return;
        }

        PlayerProfile tempProfile = container.getPlayerProfileNow(ProfileTypes.SURVIVAL, target);
        ProfileType profileTypeToUse = ProfileTypes.SURVIVAL;
        if (tempProfile == null) {
            for (ProfileType type : ProfileTypes.getTypes()) {
                if (type.equals(ProfileTypes.SURVIVAL)) continue;
                tempProfile = container.getPlayerProfileNow(type, target);
                if (tempProfile != null) {
                    profileTypeToUse = type;
                    break;
                }
            }
        }
        if (tempProfile == null) {
            issuer.sendError(ChatColor.RED + "No player data found for " + target.getName() + " in world " + worldName + ". Cannot modify inventory.");
            return;
        }

        ItemStack[] contents = null;
        ItemStack[] armor = null;
        ItemStack offHand = null;

        try {
            contents = SingleShareReader.of(inventories, target, worldName, profileTypeToUse, Sharables.INVENTORY).read().join();
            armor = SingleShareReader.of(inventories, target, worldName, profileTypeToUse, Sharables.ARMOR).read().join();
            offHand = SingleShareReader.of(inventories, target, worldName, profileTypeToUse, Sharables.OFF_HAND).read().join();
        } catch (CompletionException e) {
            issuer.sendError(ChatColor.RED + "Error loading inventory data: " + e.getCause().getMessage());
            e.printStackTrace();
            return;
        }

        // Create inventory with ModifiableInventoryHolder
        Inventory inv = Bukkit.createInventory(
                new InventoryViewListener.ModifiableInventoryHolder(target, worldName, profileTypeToUse, inventories),
                54,
                "Modifying " + target.getName() + " @ " + worldName
        );

        // Fill inventory
        if (contents != null) {
            for (int i = 0; i < Math.min(contents.length, 36); i++) {
                inv.setItem(i, contents[i]);
            }
        }
        if (armor != null && armor.length >= 4) {
            inv.setItem(39, armor[0]);
            inv.setItem(38, armor[1]);
            inv.setItem(37, armor[2]);
            inv.setItem(36, armor[3]);
        }
        if (offHand != null) {
            inv.setItem(40, offHand);
        }

        viewer.openInventory(inv);
        issuer.sendInfo(ChatColor.GREEN + "Opened editable inventory for " + target.getName() +
                " in world " + ChatColor.YELLOW + worldName + ChatColor.GREEN + ". Changes will save on close.");
    }
}

