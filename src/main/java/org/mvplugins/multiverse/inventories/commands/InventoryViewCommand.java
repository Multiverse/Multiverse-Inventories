package org.mvplugins.multiverse.inventories.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

@Service
public class InventoryViewCommand extends InventoriesCommand{

    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    @Inject
    InventoryViewCommand(
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider
    ) {
        this.profileContainerStoreProvider = profileContainerStoreProvider;
    }

    @Subcommand("view")
    @CommandPermission("multiverse.inventories.view")
    @CommandCompletion("@players @mvworlds")
    @Syntax("<player> <world>")
    @Description("View a player's inventory in a specific world.")

    void onInventoryViewCommand(
            @NotNull MVCommandIssuer issuer,
        // TODO add capability for offline players
            @Syntax("<player>")
            @Description("Online player")
            Player target,

            @Syntax("<world>")
            @Description("The world the player's inventory is in")
            MultiverseWorld[] worlds
    ) {
        if (!(issuer.getIssuer() instanceof Player viewer)) {
            issuer.sendMessage(ChatColor.RED + "Only players can view inventories.");
            return;
        }

        if (worlds == null || worlds.length == 0 || worlds[0] == null) {
            issuer.sendMessage(ChatColor.RED + "You must specify a valid world.");
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

        // Load the target's profile key from the container
        var profile = container.getPlayerProfileNow(target);
        if (profile == null) {
            issuer.sendError("No inventory data found for " + target.getName() + " in world " + worldName);
            return;
        }

        // Get the contents of the player's inventory from the profile
        ItemStack[] contents = target.getInventory().getContents();     // This is the main inventory (0–35)
        ItemStack[] armor = target.getInventory().getArmorContents();   // Armor contents (helmet, chest, etc.)
        ItemStack offHand = target.getInventory().getItemInOffHand();   // Offhand slot

        // Create a new inventory with enough space (you can customize this layout)
        Inventory inv = Bukkit.createInventory(null, 54, target.getName() + " @ " + worldName);

        // Copy inventory contents into the first 36 slots
        for (int i = 0; i < contents.length && i < 36; i++) {
            inv.setItem(i, contents[i]);
        }

        // Optionally add armor and offhand to specific GUI slots
        inv.setItem(36, armor.length > 3 ? armor[3] : null); // Boots
        inv.setItem(37, armor.length > 2 ? armor[2] : null); // Leggings
        inv.setItem(38, armor.length > 1 ? armor[1] : null); // Chestplate
        inv.setItem(39, armor.length > 0 ? armor[0] : null); // Helmet
        inv.setItem(40, offHand);                            // Offhand (shield)

        // Open the GUI for the viewer
        viewer.openInventory(inv);
    }
}
