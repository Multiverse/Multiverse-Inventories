package org.mvplugins.multiverse.inventories.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareReader;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.PlayerStats;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
final class GiveCommand extends InventoriesCommand {

    private final MultiverseInventories inventories;
    private final ProfileDataSource profileDataSource;

    @Inject
    GiveCommand(
            @NotNull MultiverseInventories inventories,
            @NotNull ProfileDataSource profileDataSource
    ) {
        this.inventories = inventories;
        this.profileDataSource = profileDataSource;
    }

    // TODO Better offline player parsing
    @Subcommand("give")
    @CommandPermission("multiverse.inventories.give")
    @CommandCompletion("@players " +
            "@mvworlds:scope=both " +
            "@mvinvprofiletypes:checkPermissions=@mvinv-gamemode-profile-true|@materials:checkPermissions=@mvinv-gamemode-profile-false " +
            "@materials:checkPermissions=@mvinv-gamemode-profile-true|@range:64,checkPermissions=@mvinv-gamemode-profile-false " +
            "@range:64,checkPermissions=@mvinv-gamemode-profile-true|@empty " +
            "@empty")
    @Syntax("<player> <world> [gamemode] <item> [amount]")
    @Description("World and Group Information")
    void onGiveCommand(
            MVCommandIssuer issuer,

            @Syntax("<player>")
            OfflinePlayer player,

            @Syntax("<world>")
            MultiverseWorld world,

            @Syntax("[gamemode]")
            ProfileType profileType,

            @Syntax("<item> [amount]")
            String item
    ) {
        ItemStack itemStack = parseItemFromString(issuer, item);
        if (itemStack == null) {
            return;
        }
        Logging.finer("Giving player " + player.getName() + " item: " + itemStack);

        // Giving online player in same world
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null
                && world.getName().equals(onlinePlayer.getWorld().getName())
                && ProfileTypes.forPlayer(onlinePlayer).equals(profileType)) {
            onlinePlayer.getInventory().addItem(itemStack);
            issuer.sendInfo("Gave player %s %s %s in world %s."
                    .formatted(player.getName(), itemStack.getAmount(), itemStack, world.getName()));
            return;
        }

        SingleShareReader.of(inventories, player, world.getName(), profileType, Sharables.INVENTORY)
                .read()
                .thenCompose(inventory -> updatePlayerInventory(issuer, player, world, profileType, inventory, itemStack))
                .exceptionally(throwable -> {
                    issuer.sendError(throwable.getMessage());
                    return null;
                });
    }

    private @Nullable ItemStack parseItemFromString(MVCommandIssuer issuer, String item) {
        // Get amount
        int amount = 1;
        AtomicBoolean endIsAmount = new AtomicBoolean(false);
        int lastSpace = item.lastIndexOf(' ');
        if (lastSpace != -1) {
            String amountString = item.substring(lastSpace + 1);
            amount = Try.of(() -> Integer.parseInt(amountString))
                    .peek(ignore -> endIsAmount.set(true))
                    .getOrElse(1);
        }
        if (amount < 1) {
            issuer.sendError("You have to give at least 1 item.");
            return null;
        }
        if (amount > 6400) {
            issuer.sendError("Cannot give more than 6400 items at once.");
            return null;
        }
        // Remove amount string from item
        if (endIsAmount.get()) {
            item = item.substring(0, lastSpace);
        }

        // Get material
        String[] split = REPatterns.get("\\[").split(item, 2);
        String itemName = split[0];
        Material material = Material.matchMaterial(itemName);
        if (material == null) {
            issuer.sendError("Invalid Material: " + split[0]);
            return null;
        }

        // Create item and parse additional vanilla component data
        ItemStack itemStack = new ItemStack(material, amount);
        if (split.length < 2) {
            return itemStack;
        }
        String additionalData = split[1];
        return Try.of(() -> Bukkit.getUnsafe().modifyItemStack(itemStack, itemStack.getType().getKey() + "[" + additionalData))
                .onFailure(throwable -> issuer.sendError(throwable.getMessage()))
                .getOrNull();
    }

    private CompletableFuture<Void> updatePlayerInventory(
            MVCommandIssuer issuer,
            OfflinePlayer player,
            MultiverseWorld world,
            ProfileType profileType,
            @Nullable ItemStack[] inventory,
            @NotNull ItemStack itemStack
    ) {
        putItemInInventory(inventory, itemStack);
        return SingleShareWriter.of(inventories, player, world.getName(), profileType, Sharables.INVENTORY)
                .write(inventory, true)
                .thenCompose(ignore -> player.isOnline()
                        ? CompletableFuture.completedFuture(null)
                        : profileDataSource.modifyGlobalProfile(GlobalProfileKey.of(player), profile -> profile.setLoadOnLogin(true)))
                .thenRun(() -> issuer.sendInfo("Gave player %s %s %s in world %s."
                        .formatted(player.getName(), itemStack.getAmount(), itemStack.getI18NDisplayName(), world.getName())));
    }

    private void putItemInInventory(@Nullable ItemStack[] inventory, @NotNull ItemStack itemStack) {
        if (inventory == null) {
            inventory = new ItemStack[PlayerStats.INVENTORY_SIZE];
        }
        int amountLeft = itemStack.getAmount();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null || inventory[i].getType() == Material.AIR) {
                int amountToGive = Math.min(amountLeft, itemStack.getMaxStackSize());
                inventory[i] = itemStack.clone();
                inventory[i].setAmount(amountToGive);
                amountLeft -= amountToGive;
            } else if (inventory[i].isSimilar(itemStack)) {
                int amountToGive = Math.min(amountLeft, itemStack.getMaxStackSize() - inventory[i].getAmount());
                inventory[i].setAmount(inventory[i].getAmount() + amountToGive);
                amountLeft -= amountToGive;
            }

            if (amountLeft == 0) {
                break;
            }
        }
    }
}
