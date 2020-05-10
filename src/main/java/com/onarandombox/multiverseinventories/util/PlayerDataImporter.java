package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.PlayerStats;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    private void parseInventoryItems(Tag[] items, ItemStack[][] stacks) {
        // TODO: do we need to fill these with air?
        stacks[0] = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.INVENTORY_SIZE]);
        stacks[1] = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ARMOR_SIZE]);
        stacks[2] = MinecraftTools.fillWithAir(new ItemStack[1]);

        if (items != null) {
            for (Tag item: items) {
                byte inv;
                // this will not cause an NPE (it always appears in playerdata)
                byte slot = (byte) item.findTagByName("Slot").getValue();

                // we need to check this since armor and off hand item are also stored in the inventory
                if (-1 < slot && slot < 36) {
                    inv = 0;
                } else if (99 < slot && slot < 104) {
                    inv = 1;
                    slot -= 100;
                } else if (slot == -106) {
                    inv = 2;
                    slot = 0;
                } else inv = -1;

                if (inv != -1) {
                    // these will not cause NPEs (they always appear in playerdata)
                    String id = (String) item.findTagByName("id").getValue();
                    byte count = (byte) item.findTagByName("Count").getValue();
                    stacks[inv][slot] = new ItemStack(Material.matchMaterial(id), count);

                    // this tag is not always present in playerdata! be mindful of NPEs!
                    Tag tag = item.findTagByName("tag");
                    // TODO: this is currently broken
                    // if (tag != null) parseItem(parsedItems[slot], (Tag) tag.getValue());
                }
            }
        }
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

                Map<String, Tag> tags = new HashMap<>();
                pp = PlayerProfile.createPlayerProfile(ContainerType.GROUP, group.getName(), profileType, player);

                // this is for imports that only need one value. imports that need more than one are dealt with later
                for (Tag tag: (Tag[]) nbt.getValue()) {
                    switch (tag.getName()) {
                        case "FallDistance":
                            pp.set(Sharables.FALL_DISTANCE, (float) tag.getValue());
                            break;
                        case "XpTotal":
                            pp.set(Sharables.TOTAL_EXPERIENCE, (int) tag.getValue());
                            break;
                        case "Health":
                            pp.set(Sharables.HEALTH, (double) (float) tag.getValue());
                            break;
                        case "foodSaturationLevel":
                            pp.set(Sharables.SATURATION, (float) tag.getValue());
                            break;
                        case "Air":
                            pp.set(Sharables.REMAINING_AIR, (int) (short) tag.getValue());
                            break;
                        case "Fire":
                            pp.set(Sharables.FIRE_TICKS, (int) tag.getValue());
                            break;
                        case "foodLevel":
                            pp.set(Sharables.FOOD_LEVEL, (int) tag.getValue());
                            break;
                        case "foodExhaustionLevel":
                            pp.set(Sharables.EXHAUSTION, (float) tag.getValue());
                            break;
                        case "Inventory":
                            ItemStack[][] stacks = new ItemStack[3][];
                            parseInventoryItems((Tag[]) tag.getValue(), stacks);
                            pp.set(Sharables.INVENTORY, stacks[0]);
                            pp.set(Sharables.ARMOR, stacks[1]);
                            pp.set(Sharables.OFF_HAND, stacks[2][0]);
                            break;
                        case "XpLevel":
                            pp.set(Sharables.LEVEL, (int) tag.getValue());
                            break;
                        case "XpP":
                            pp.set(Sharables.EXPERIENCE, (float) tag.getValue());
                            break;
                        case "Pos":
                        case "Rotation":
                        case "SpawnX":
                        case "SpawnY":
                        case "SpawnZ":
                        case "UUIDLeast":
                        case "UUIDMost":
                            tags.put(tag.getName(), tag);
                    }
                } // TODO: max air, and potion effects

                // now we'll deal with the rest of the sharables

                long most, least;
                // these will not cause NPEs (they always appear in playerdata)
                most = (long) tags.get("UUIDMost").getValue();
                least = (long) tags.get("UUIDLeast").getValue();
                // TODO: for some reason world is always null
                World w = this.plugin.getServer().getWorld(new UUID(most, least));

                if (w != null && group.getWorlds().contains(w.getName())) {
                    double x, y, z;
                    float yaw, pitch;
                    Tag[] pos = (Tag[]) tags.get("Pos").getValue();
                    Tag[] rot = (Tag[]) tags.get("Rotation").getValue();

                    x = (double) pos[0].getValue();
                    y = (double) pos[1].getValue();
                    z = (double) pos[2].getValue();
                    yaw = (float) rot[0].getValue();
                    pitch = (float) rot[1].getValue();

                    pp.set(Sharables.LAST_LOCATION, new Location(w, x, y, z, yaw, pitch));
                } else {
                    // TODO: think of a better way to handle this case
                    pp.set(Sharables.LAST_LOCATION,
                            this.plugin.getServer().getWorld(group.getWorlds().toArray(new String[0])[0]).getSpawnLocation());
                }

                Tag bedX, bedY, bedZ;
                bedX = tags.get("SpawnX");
                bedY = tags.get("SpawnY");
                bedZ = tags.get("SpawnZ");

                if (bedX != null && bedY != null && bedZ != null) {
                    // TODO: this might not be the correct world!
                    pp.set(Sharables.BED_SPAWN, new Location(this.plugin.getServer().getWorld(world),
                            (int) bedX.getValue(), (int) bedY.getValue(), (int) bedZ.getValue()));
                }

                group.getGroupProfileContainer().addPlayerData(pp);
                this.plugin.getData().updatePlayerData(pp);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }
}
