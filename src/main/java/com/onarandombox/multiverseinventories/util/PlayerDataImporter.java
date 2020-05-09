package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.PlayerStats;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataImporter {
    private final MultiverseInventories plugin;
    private final String world;
    private final WorldGroup group;

    public PlayerDataImporter(MultiverseInventories plugin, String world, WorldGroup group) {
        this.plugin = plugin;
        this.world = world;
        this.group = group;
    }

    private File getPlayerDataFolder() {
        return new File(this.plugin.getServer().getWorldContainer() + File.separator + this.world + File.separator + "playerdata");
    }

    private void parseItem(ItemStack item, Tag tag) {
        // TODO: implement this method. it will handle enchantments, durability, etc...
    }

    private ItemStack[] parseInventoryItems(Tag[] items, int size) {
        // TODO: do we need to fill this with air?
        ItemStack[] parsedItems = MinecraftTools.fillWithAir(new ItemStack[size]);

        if (items != null) {
            for (Tag item: items) {
                // this will not cause an NPE (it always appears in playerdata)
                byte slot = (byte) item.findTagByName("Slot").getValue();

                // we need to check this since armor and off hand item are also stored in the inventory
                if (-1 < slot && slot < 36) {
                    // these will not cause NPEs (they always appear in playerdata)
                    String id = (String) item.findTagByName("id").getValue();
                    byte count = (byte) item.findTagByName("Count").getValue();
                    // this tag is not always present in playerdata! be mindful of NPEs!
                    Tag tag = item.findTagByName("tag");

                    parsedItems[slot] = new ItemStack(Material.matchMaterial(id), count);
                    // TODO: this is currently broken
                    //if (tag != null) parseItem(parsedItems[slot], (Tag) tag.getValue());
                }
            }
        }

        return parsedItems;
    }

    public boolean doImport() {
        File playerDataFolder = getPlayerDataFolder();

        for (File playerData: playerDataFolder.listFiles()) {
            try {
                Tag nbt = Tag.readFrom(new FileInputStream(playerData));

                PlayerProfile pp;
                UUID uuid = UUID.fromString(playerData.getName().substring(0, playerData.getName().indexOf('.')));
                OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(uuid);
                ProfileType profileType;

                // this will not cause an NPE (it always appears in playerdata)
                switch ((int) nbt.findTagByName("playerGameType").getValue()) {
                    default:
                        profileType = ProfileTypes.SURVIVAL;
                        break;
                    case 1:
                        profileType = ProfileTypes.CREATIVE;
                        break;
                    case 2:
                        profileType = ProfileTypes.ADVENTURE;
                        break;
                }

                pp = PlayerProfile.createPlayerProfile(ContainerType.GROUP, group.getName(), profileType, player);

                for (Sharable sharable: Sharables.ALL_DEFAULT) {
                    switch (sharable.getNames()[0]) {
                        case "ender_chest":
                            // this will not cause an NPE (it always appears in playerdata)
                            Tag[] enderItems = (Tag[]) nbt.findTagByName("EnderItems").getValue();
                            ItemStack[] enderInv = parseInventoryItems(enderItems, PlayerStats.ENDER_CHEST_SIZE);
                            pp.set(Sharables.ENDER_CHEST, enderInv);
                            break;
                        case "inventory_contents":
                            // this will not cause an NPE (it always appears in playerdata)
                            Tag[] invItems = (Tag[]) nbt.findTagByName("Inventory").getValue();
                            ItemStack[] playerInv = parseInventoryItems(invItems, PlayerStats.INVENTORY_SIZE);
                            pp.set(Sharables.INVENTORY, playerInv);
                            break;
                        case "armor_contents":
                            // TODO: there's probably a more efficient way to do this (combine with inventory_contents)
                            // this will not cause an NPE (it always appears in playerdata)
                            Tag[] invItems2 = (Tag[]) nbt.findTagByName("Inventory").getValue();
                            // TODO: do we need to fill this with air?
                            ItemStack[] armorContents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ARMOR_SIZE]);

                            for (Tag item: invItems2) {
                                // this will not cause an NPE (it always appears in playerdata)
                                byte slot = (byte) item.findTagByName("Slot").getValue();

                                // for some reason the armor slots are 100, 101, 102, and 103?
                                // corroborated here: https://minecraft.gamepedia.com/Player.dat_format#Inventory_slot_numbers
                                if (99 < slot && slot < 104) {
                                    slot -= 100;

                                    // these will not cause NPEs (they always appear in playerdata)
                                    String id = (String) item.findTagByName("id").getValue();
                                    byte count = (byte) item.findTagByName("Count").getValue();
                                    // this tag is not always present in playerdata! be mindful of NPEs!
                                    Tag tag = item.findTagByName("tag");

                                    armorContents[slot] = new ItemStack(Material.matchMaterial(id), count);
                                    // TODO: this is currently broken
                                    //if (tag != null) parseItem(armorContents[slot], (Tag) tag.getValue());
                                }
                            }

                            pp.set(Sharables.ARMOR, armorContents);
                            break;
                        case "off_hand":
                            // TODO: there's probably a more efficient way to do this (combine with inventory_contents)
                            // this will not cause an NPE (it always appears in playerdata)
                            Tag[] invItems3 = (Tag[]) nbt.findTagByName("Inventory").getValue();
                            // TODO: should this be air?
                            ItemStack offHand = null;

                            for (Tag item: invItems3) {
                                // this will not cause an NPE (it always appears in playerdata)
                                byte slot = (byte) item.findTagByName("Slot").getValue();

                                // for some reason off hand item is in slot -106?
                                // corroborated here: https://minecraft.gamepedia.com/Player.dat_format#Inventory_slot_numbers
                                if (slot == -106) {
                                    // these will not cause NPEs (they always appear in playerdata)
                                    String id = (String) item.findTagByName("id").getValue();
                                    byte count = (byte) item.findTagByName("Count").getValue();
                                    // this tag is not always present in playerdata! be mindful of NPEs!
                                    Tag tag = item.findTagByName("tag");

                                    offHand = new ItemStack(Material.matchMaterial(id), count);
                                    // TODO: this is currently broken
                                    //if (tag != null) parseItem(offHand, (Tag) tag.getValue());
                                }
                            }

                            pp.set(Sharables.OFF_HAND, offHand);
                            break;
                        case "hit_points":
                            // this will not cause an NPE (it always appears in playerdata)
                            // also, the casting to float is not a mistake! this is its type in playerdata
                            double hp = (float) nbt.findTagByName("Health").getValue();
                            pp.set(Sharables.HEALTH, hp);
                            break;
                        case "remaining_air":
                            // this will not cause an NPE (it always appears in playerdata)
                            // also, the casting to short is not a mistake! this is its type in playerdata
                            int air = (short) nbt.findTagByName("Air").getValue();
                            pp.set(Sharables.REMAINING_AIR, air);
                            break;
                        case "maximum_air":
                            break;
                        case "fall_distance":
                            break;
                        case "fire_ticks":
                            break;
                        case "xp":
                            break;
                        case "lvl":
                            break;
                        case "total_xp":
                            break;
                        case "food_level":
                            // this will not cause an NPE (it always appears in playerdata)
                            int foodLevel = (int) nbt.findTagByName("foodLevel").getValue();
                            pp.set(Sharables.FOOD_LEVEL, foodLevel);
                            break;
                        case "exhaustion":
                            // this will not cause an NPE (it always appears in playerdata)
                            float exhaustion = (float) nbt.findTagByName("foodExhaustionLevel").getValue();
                            pp.set(Sharables.EXHAUSTION, exhaustion);
                            break;
                        case "saturation":
                            // this will not cause an NPE (it always appears in playerdata)
                            float saturation = (float) nbt.findTagByName("foodSaturationLevel").getValue();
                            pp.set(Sharables.SATURATION, saturation);
                            break;
                        case "bed_spawn":
                            // this tag is not always present in playerdata! be mindful of NPEs!
                            break;
                        case "last_location":
                            long most, least;
                            // these will not cause NPEs (they always appear in playerdata)
                            most = (long) nbt.findTagByName("UUIDMost").getValue();
                            least = (long) nbt.findTagByName("UUIDLeast").getValue();
                            // TODO: for some reason this is always null
                            World world = this.plugin.getServer().getWorld(new UUID(most, least));

                            if (world != null && group.getWorlds().contains(world.getName())) {
                                double x, y, z;
                                float yaw, pitch;
                                // these will not cause NPEs (they always appear in playerdata)
                                Tag[] pos = (Tag[]) nbt.findTagByName("Pos").getValue();
                                Tag[] rot = (Tag[]) nbt.findTagByName("Rotation").getValue();

                                x = (double) pos[0].getValue();
                                y = (double) pos[1].getValue();
                                z = (double) pos[2].getValue();
                                yaw = (float) rot[0].getValue();
                                pitch = (float) rot[1].getValue();

                                pp.set(Sharables.LAST_LOCATION, new Location(world, x, y, z, yaw, pitch));
                            } else {
                                // TODO: think of a better way to handle this case
                                pp.set(Sharables.LAST_LOCATION,
                                        this.plugin.getServer().getWorld(group.getWorlds().toArray(new String[0])[0]).getSpawnLocation());
                            }
                            break;
                        case "potion_effects":
                            break;
                    }
                }

                group.getGroupProfileContainer().addPlayerData(pp);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }
}
