package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.util.ItemWrapper;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Enum for specifying the different sharable things.
 */
enum DefaultSharable implements Sharable<PlayerProfile> {

    /**
     * Sharing Inventory.
     */
    INVENTORY("inventory", "inv") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setInventoryContents(player.getInventory().getContents());
            profile.setArmorContents(player.getInventory().getArmorContents());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.getInventory().clear();
            player.getInventory().setContents(profile.getInventoryContents());
            player.getInventory().setArmorContents(profile.getArmorContents());

        }
        
        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Integer i = 0; i < PlayerStats.INVENTORY_SIZE; i++) {
                if (profile.getInventoryContents()[i] != null && profile.getInventoryContents()[i].getTypeId() != 0) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(DataStrings.ITEM_DELIMITER);
                    }
                    builder.append(DataStrings.createEntry(i, new ItemWrapper(profile.getInventoryContents()[i]).toString()));
                }
            }
            playerData.put("inventoryContents", builder.toString());

            builder = new StringBuilder();
            first = true;
            for (Integer i = 0; i < PlayerStats.ARMOR_SIZE; i++) {
                if (profile.getArmorContents()[i] != null && profile.getArmorContents()[i].getTypeId() != 0) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(DataStrings.ITEM_DELIMITER);
                    }
                    builder.append(DataStrings.createEntry(i, new ItemWrapper(profile.getArmorContents()[i]).toString()));
                }
            }
            playerData.put("armorContents", builder.toString());
        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {
            if (playerData.containsKey("inventoryContents")) {
                String[] inventoryArray = playerData.get("inventoryContents")
                        .toString().split(DataStrings.ITEM_DELIMITER);
                ItemStack[] invContents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.INVENTORY_SIZE]);
                for (String itemString : inventoryArray) {
                    String[] itemValues = DataStrings.splitEntry(itemString);
                    try {
                        ItemWrapper itemWrapper = new ItemWrapper(itemValues[1]);
                        invContents[Integer.valueOf(itemValues[0])] = itemWrapper.getItem();
                        //Logging.debug("ItemString '" + itemString + "' unwrapped as: " + itemWrapper.getItem().toString());
                    } catch (Exception e) {
                        if (!itemString.isEmpty()) {
                            Logging.fine("Could not parse item string: " + itemString);
                            Logging.fine(e.getMessage());
                        }
                    }
                }
                profile.setInventoryContents(invContents);
            }
            if (playerData.containsKey("armorContents")) {
                String[] armorArray = playerData.get("armorContents").toString().split(DataStrings.ITEM_DELIMITER);
                ItemStack[] armContents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ARMOR_SIZE]);
                for (String itemString : armorArray) {
                    String[] itemValues = DataStrings.splitEntry(itemString);
                    try {
                        armContents[Integer.valueOf(itemValues[0])] = new ItemWrapper(itemValues[1]).getItem();
                    } catch (Exception e) {
                        if (!itemString.isEmpty()) {
                            Logging.fine("Could not parse armor string: " + itemString);
                            Logging.fine(e.getMessage());
                        }
                    }
                }
                profile.setArmorContents(armContents);
            }
        }
    },
    /**
     * Sharing Health.
     */
    HEALTH("health", "hp") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setHealth(player.getHealth());
            profile.setFallDistance(player.getFallDistance());
            profile.setFireTicks(player.getFireTicks());
            profile.setRemainingAir(player.getRemainingAir());
            profile.setMaximumAir(player.getMaximumAir());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setHealth(profile.getHealth());
            player.setFallDistance(profile.getFallDistance());
            player.setFireTicks(profile.getFireTicks());
            player.setRemainingAir(profile.getRemainingAir());
            player.setMaximumAir(profile.getMaximumAir());
        }

        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {
            Object statsObj = playerData.get("stats");
            if (statsObj == null) {
                statsObj = "";
            }
            StringBuilder builder = new StringBuilder(statsObj.toString());
            if (!statsObj.toString().isEmpty() && !statsObj.toString().endsWith(DataStrings.GENERAL_DELIMITER)) {
                builder.append(DataStrings.GENERAL_DELIMITER);
            }
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_HEALTH, profile.getHealth()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_FALL_DISTANCE, profile.getFallDistance()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_FIRE_TICKS, profile.getFireTicks()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_REMAINING_AIR, profile.getRemainingAir()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_MAX_AIR, profile.getMaximumAir()));
            playerData.put("stats", builder.toString());
        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {
            if (!playerData.containsKey("stats")) {
                return;
            }
            String[] statsArray = playerData.get("stats").toString().split(DataStrings.GENERAL_DELIMITER);
            for (String stat : statsArray) {
                try {
                    String[] statValues = DataStrings.splitEntry(stat);
                    if (statValues[0].equals(DataStrings.PLAYER_HEALTH)) {
                        profile.setHealth(Integer.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_FALL_DISTANCE)) {
                        profile.setFallDistance(Float.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_FIRE_TICKS)) {
                        profile.setFireTicks(Integer.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_REMAINING_AIR)) {
                        profile.setRemainingAir(Integer.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_MAX_AIR)) {
                        profile.setMaximumAir(Integer.valueOf(statValues[1]));
                    }
                } catch (Exception e) {
                    if (!stat.isEmpty()) {
                        Logging.fine("Could not parse stat: '" + stat + "'");
                        Logging.fine(e.getMessage());
                    }
                }
            }
        }
    },
    /**
     * Sharing Experience.
     */
    EXPERIENCE("experience", "exp", "xp") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setExp(player.getExp());
            profile.setLevel(player.getLevel());
            profile.setTotalExperience(player.getTotalExperience());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setExp(profile.getExp());
            player.setLevel(profile.getLevel());
            player.setTotalExperience(profile.getTotalExperience());
        }

        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {
            Object statsObj = playerData.get("stats");
            if (statsObj == null) {
                statsObj = "";
            }
            StringBuilder builder = new StringBuilder(statsObj.toString());
            if (!statsObj.toString().isEmpty() && !statsObj.toString().endsWith(DataStrings.GENERAL_DELIMITER)) {
                builder.append(DataStrings.GENERAL_DELIMITER);
            }
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXPERIENCE, profile.getExp()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_LEVEL, profile.getLevel()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_TOTAL_EXPERIENCE, profile.getTotalExperience()));
            playerData.put("stats", builder.toString());
        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {
            if (!playerData.containsKey("stats")) {
                return;
            }
            String[] statsArray = playerData.get("stats").toString().split(DataStrings.GENERAL_DELIMITER);
            for (String stat : statsArray) {
                try {
                    String[] statValues = DataStrings.splitEntry(stat);
                    if (statValues[0].equals(DataStrings.PLAYER_EXPERIENCE)) {
                        profile.setExp(Float.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_TOTAL_EXPERIENCE)) {
                        profile.setTotalExperience(Integer.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_LEVEL)) {
                        profile.setLevel(Integer.valueOf(statValues[1]));
                    }
                } catch (Exception e) {
                    if (!stat.isEmpty()) {
                        Logging.fine("Could not parse stat: '" + stat + "'");
                        Logging.fine(e.getMessage());
                    }
                }
            }
        }
    },
    /**
     * Sharing Hunger.
     */
    HUNGER("hunger", "food") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setFoodLevel(player.getFoodLevel());
            profile.setExhaustion(player.getExhaustion());
            profile.setSaturation(player.getSaturation());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setFoodLevel(profile.getFoodLevel());
            player.setExhaustion(profile.getExhaustion());
            player.setSaturation(profile.getSaturation());
        }

        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {
            Object statsObj = playerData.get("stats");
            if (statsObj == null) {
                statsObj = "";
            }
            StringBuilder builder = new StringBuilder(statsObj.toString());
            if (!statsObj.toString().isEmpty() && !statsObj.toString().endsWith(DataStrings.GENERAL_DELIMITER)) {
                builder.append(DataStrings.GENERAL_DELIMITER);
            }
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_FOOD_LEVEL, profile.getFoodLevel()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXHAUSTION, profile.getExhaustion()));
            builder.append(DataStrings.GENERAL_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.PLAYER_SATURATION, profile.getSaturation()));
            playerData.put("stats", builder.toString());
        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {
            if (!playerData.containsKey("stats")) {
                return;
            }
            String[] statsArray = playerData.get("stats").toString().split(DataStrings.GENERAL_DELIMITER);
            for (String stat : statsArray) {
                try {
                    String[] statValues = DataStrings.splitEntry(stat);
                    if (statValues[0].equals(DataStrings.PLAYER_FOOD_LEVEL)) {
                        profile.setFoodLevel(Integer.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_EXHAUSTION)) {
                        profile.setExhaustion(Float.valueOf(statValues[1]));
                    } else if (statValues[0].equals(DataStrings.PLAYER_SATURATION)) {
                        profile.setSaturation(Float.valueOf(statValues[1]));
                    }
                } catch (Exception e) {
                    if (!stat.isEmpty()) {
                        Logging.fine("Could not parse stat: '" + stat + "'");
                        Logging.fine(e.getMessage());
                    }
                }
            }
        }
    },
    /**
     * Sharing Hunger.
     */
    BED_SPAWN("bed_spawn", "bedspawn", "bed", "beds") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setBedSpawnLocation(player.getBedSpawnLocation());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            Location loc = profile.getBedSpawnLocation();
            if (loc == null) {
                loc = player.getWorld().getSpawnLocation();
            }
            player.setBedSpawnLocation(loc);
        }

        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {
            if (profile.getBedSpawnLocation() != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_WORLD,
                        profile.getBedSpawnLocation().getWorld().getName()));
                builder.append(DataStrings.GENERAL_DELIMITER);
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_X,
                        profile.getBedSpawnLocation().getX()));
                builder.append(DataStrings.GENERAL_DELIMITER);
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_Y,
                        profile.getBedSpawnLocation().getY()));
                builder.append(DataStrings.GENERAL_DELIMITER);
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_Z,
                        profile.getBedSpawnLocation().getZ()));
                builder.append(DataStrings.GENERAL_DELIMITER);
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_PITCH,
                        profile.getBedSpawnLocation().getPitch()));
                builder.append(DataStrings.GENERAL_DELIMITER);
                builder.append(DataStrings.createEntry(DataStrings.LOCATION_YAW,
                        profile.getBedSpawnLocation().getYaw()));
                playerData.put("bedSpawnLocation", builder.toString());
            }
        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {
            if (!playerData.containsKey("bedSpawnLocation")) {
                return;
            }
            profile.setBedSpawnLocation(DataStrings.parseLocation(
                    playerData.get("bedSpawnLocation").toString().split(DataStrings.GENERAL_DELIMITER)));
        }
    },
    /**
     * Sharing Potions/Effects.
     */
    POTIONS("potions", "pots", "effects", "fx") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {

        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            
        }

        @Override
        public void addToMap(PlayerProfile profile, Map<String, Object> playerData) {

        }

        @Override
        public void addToProfile(Map<String, Object> playerData, PlayerProfile profile) {

        }
    };

    //TODO:
    //player.setCompassTarget();


    private String[] names;

    DefaultSharable(String... names) {
        this.names = names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNames() {
        return this.names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void updateProfile(PlayerProfile profile, Player player);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void updatePlayer(Player player, PlayerProfile profile);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void addToMap(PlayerProfile profile, Map<String, Object> playerData);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void addToProfile(Map<String, Object> playerData, PlayerProfile profile);

    @Override
    public String toString() {
        return this.names[0];
    }
}

