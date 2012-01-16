package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for specifying the different sharable things.
 */
public enum Sharable {

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
     * Sharing Effects.
     */
    EFFECTS("effects", "fx", "potions") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            // NO API FOR EFFECTS AS OF YET
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            // NO API FOR EFFECTS AS OF YET
        }
    },

    /**
     * Indicates ALL of the above.
     */
    ALL("*", "all", "everything") {
        @Override
        public void updateProfile(PlayerProfile profile, Player player) {
            EnumSet<Sharable> sharables = EnumSet.allOf(Sharable.class);
            sharables.remove(ALL);
            for (Sharable sharable : sharables) {
                sharable.updateProfile(profile, player);
            }
        }

        @Override
        public void updatePlayer(Player player, PlayerProfile profile) {
            EnumSet<Sharable> sharables = EnumSet.allOf(Sharable.class);
            sharables.remove(ALL);
            for (Sharable sharable : sharables) {
                sharable.updatePlayer(player, profile);
            }
        }
    };

    private String name;

    private static Map<String, Sharable> lookup = new HashMap<String, Sharable>();

    Sharable(String name, String...extraNames) {
        this.addLookup(name);
        for (String extraName : extraNames) {
            this.addLookup(extraName);
        }
    }

    private void addLookup(String name) {
        Sharable.lookup.put(name, this);
    }

    /**
     * @param profile Updates the data of this profile according to the Sharable
     *                with the values of the player.
     * @param player The player whose values will be used to update the given profile.
     */
    public abstract void updateProfile(PlayerProfile profile, Player player);

    /**
     * @param player Updates the data of this player according to the Sharable
     *               with the values of the given profile.
     * @param profile The profile whose values will be used to update the give player.
     */
    public abstract void updatePlayer(Player player, PlayerProfile profile);

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Looks up a sharable by one of the acceptable names.
     *
     * @param name Name to look up by.
     * @return Sharable by that name or null if none by that name.
     */
    public static Sharable lookup(String name) {
        return Sharable.lookup.get(name);
    }
}
