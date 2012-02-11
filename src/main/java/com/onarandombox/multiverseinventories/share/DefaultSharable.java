package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for specifying the different sharable things.
 */
public enum DefaultSharable implements Sharable {

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
    },
    /**
     * Sharing Health.
     */
    HEALTH("health", "hp") {
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

    private static Map<String, DefaultSharable> lookup = new HashMap<String, DefaultSharable>();

    static {
        for (DefaultSharable sharable : EnumSet.allOf(DefaultSharable.class)) {
            for (String name : sharable.getNames()) {
                lookup.put(name, sharable);
            }
        }
    }

    DefaultSharable(String... names) {
        this.names = names;
    }

    private String[] getNames() {
        return this.names;
    }

    private void addLookup(String name) {
        DefaultSharable.lookup.put(name, this);
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

    /**
     * TODO Move to {@link Sharables}
     * Looks up a sharable by one of the acceptable names.
     *
     * @param name Name to look up by.
     * @return Sharable by that name or null if none by that name.
     */
    public static Sharable lookup(String name) {
        return DefaultSharable.lookup.get(name);
    }
}

