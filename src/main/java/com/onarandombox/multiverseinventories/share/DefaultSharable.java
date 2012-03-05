package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Enum for specifying the different sharable things.
 */
enum DefaultSharable implements Sharable {

    /**
     * Sharing Inventory.
     */
    INVENTORY("inventory_contents", "inventory", "inv") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setInventoryContents(player.getInventory().getContents());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.getInventory().setContents(profile.getInventoryContents());
        }
    },
    ARMOR("armor_contents", "armor", "inventory", "inv") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setArmorContents(player.getInventory().getArmorContents());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.getInventory().setArmorContents(profile.getArmorContents());

        }
    },
    /**
     * Sharing Health.
     */
    HP("health", "hp", "hitpoints", "hit_points") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setHealth(player.getHealth());

        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setHealth(profile.getHealth());
        }
    },
    /**
     * Sharing Experience.
     */
    EXPERIENCE("exp", "experience", "xp") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setExp(player.getExp());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setExp(profile.getExp());
        }
    },
    /**
     * Sharing Experience.
     */
    LEVEL("level", "experience", "lvl") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setLevel(player.getLevel());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setLevel(profile.getLevel());
        }
    },
    /**
     * Sharing Experience.
     */
    TOTAL_EXPERIENCE("total_exp", "experience", "total_xp", "totalxp") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setTotalExperience(player.getTotalExperience());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setTotalExperience(profile.getTotalExperience());
        }
    },
    /**
     * Sharing Hunger.
     */
    FOOD_LEVEL("food_level", "food", "hunger") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setFoodLevel(player.getFoodLevel());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setFoodLevel(profile.getFoodLevel());
        }
    },
    /**
     * Sharing Hunger.
     */
    EXHAUSTION("exhaustion", "hunger", "exhaust", "exh") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setExhaustion(player.getExhaustion());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setExhaustion(profile.getExhaustion());
        }
    },
    /**
     * Sharing Hunger.
     */
    SATURATION("saturation", "hunger", "sat") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            profile.setSaturation(player.getSaturation());
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            player.setSaturation(profile.getSaturation());
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
    },
    /** TODO: add when there's bukkit api for this.
     * Sharing Effects.
     */
    /*
    EFFECTS("effects", "fx", "potions") {
        @Override
        public void updateProfile(PlayerProfile player, Player player) {
            // NO API FOR EFFECTS AS OF YET
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile player) {
            // NO API FOR EFFECTS AS OF YET
        }
    },*/;

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

    @Override
    public String toString() {
        return this.names[0];
    }
}

