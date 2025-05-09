package org.mvplugins.multiverse.inventories.share;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.collect.Sets;
import org.bukkit.advancement.AdvancementProgress;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.data.ProfileData;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.DataStrings;
import org.mvplugins.multiverse.inventories.util.MinecraftTools;
import org.mvplugins.multiverse.inventories.util.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findBedFromRespawnLocation;

/**
 * The Sharables class is where all the default Sharable instances are located as constants as well as a factory class
 * for generating Shares.
 */
public final class Sharables implements Shares {

    private static final Shares ALL_SHARABLES = new Sharables(new LinkedHashSet<>());
    private static final Shares STANDARD_SHARABLES = new Sharables(new LinkedHashSet<>());
    private static final Shares OPTIONAL_SHARABLES = new Sharables(new LinkedHashSet<>());

    /**
     * The map used to lookup a Sharable or set of Sharables by their name.
     */
    static final Map<String, Shares> LOOKUP_MAP = new HashMap<String, Shares>();

    private static Shares enabledShares = noneOf();

    private static MultiverseInventories inventories = null;
    private static InventoriesConfig inventoriesConfig = null;
    private static WorldGroupManager worldGroupManager = null;
    private static MVEconomist economist = null;
    private static AsyncSafetyTeleporter safetyTeleporter = null;

    private static Attribute maxHealthAttr = null;

    /**
     * Initialize this class with the instance of Inventories.
     *
     * @param inventories the instance of Inventories.
     */
    public static void init(MultiverseInventories inventories) {
        Sharables.inventories = inventories;
        Sharables.economist = inventories.getServiceLocator().getService(MVEconomist.class);
        Sharables.safetyTeleporter = inventories.getServiceLocator().getService(AsyncSafetyTeleporter.class);
        Sharables.inventoriesConfig = inventories.getServiceLocator().getService(InventoriesConfig.class);
        Sharables.worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        initMaxHealthAttr();
    }

    private static void initMaxHealthAttr() {
        Sharables.maxHealthAttr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
        if (Sharables.maxHealthAttr == null) {
            // Old key for older minecraft version (<1.21)
            Sharables.maxHealthAttr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
        }
        if (Sharables.maxHealthAttr == null) {
            Logging.warning("Could not find max_health attribute. Health related sharables may not work as expected.");
        }
    }

    /**
     * Sharing Enderchest Inventory.
     */
    public static final Sharable<ItemStack[]> ENDER_CHEST = new Sharable.Builder<ItemStack[]>("ender_chest",
            ItemStack[].class, new SharableHandler<ItemStack[]>() {
        @Override
        public void updateProfile(ProfileData profile, Player player) {
            profile.set(ENDER_CHEST, player.getEnderChest().getContents());
        }

        @Override
        public boolean updatePlayer(Player player, ProfileData profile) {
            ItemStack[] value = profile.get(ENDER_CHEST);
            if (value == null) {
                player.getEnderChest().setContents(MinecraftTools.fillWithAir(
                        new ItemStack[PlayerStats.ENDER_CHEST_SIZE]));
                return false;
            }
            player.getEnderChest().setContents(value);
            return true;
        }
    }).serializer(new ProfileEntry(false, DataStrings.ENDER_CHEST_CONTENTS),
            new InventorySerializer(PlayerStats.ENDER_CHEST_SIZE)).altName("ender").build();

    /**
     * Sharing Inventory.
     */
    public static final Sharable<ItemStack[]> INVENTORY = new Sharable.Builder<ItemStack[]>("inventory_contents",
            ItemStack[].class, new SharableHandler<ItemStack[]>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(INVENTORY, player.getInventory().getContents());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    ItemStack[] value = profile.get(INVENTORY);
                    if (value == null) {
                        player.getInventory().setContents(MinecraftTools.fillWithAir(
                                new ItemStack[PlayerStats.INVENTORY_SIZE]));
                        return false;
                    }
                    player.getInventory().setContents(value);
                    return true;
                }
            }).serializer(new ProfileEntry(false, DataStrings.PLAYER_INVENTORY_CONTENTS),
                    new InventorySerializer(PlayerStats.INVENTORY_SIZE)).build();

    /**
     * Sharing Armor.
     */
    public static final Sharable<ItemStack[]> ARMOR = new Sharable.Builder<ItemStack[]>("armor_contents",
            ItemStack[].class, new SharableHandler<ItemStack[]>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(ARMOR, player.getInventory().getArmorContents());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    ItemStack[] value = profile.get(ARMOR);
                    if (value == null) {
                        player.getInventory().setArmorContents(MinecraftTools.fillWithAir(
                                new ItemStack[PlayerStats.ARMOR_SIZE]));
                        return false;
                    }
                    player.getInventory().setArmorContents(value);
                    return true;
                }
            }).serializer(new ProfileEntry(false, DataStrings.PLAYER_ARMOR_CONTENTS),
                    new InventorySerializer(PlayerStats.ARMOR_SIZE)).altName("armor").build();

    /**
     * Sharing Offhand.
     */
    public static final Sharable<ItemStack> OFF_HAND = new Sharable.Builder<ItemStack>("off_hand",
            ItemStack.class, new SharableHandler<ItemStack>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(OFF_HAND, player.getInventory().getItemInOffHand());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    ItemStack value = profile.get(OFF_HAND);
                    if (value == null) {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                        return false;
                    }
                    player.getInventory().setItemInOffHand(value);
                    return true;
                }
            }).serializer(new ProfileEntry(false, DataStrings.PLAYER_OFF_HAND_ITEM),
                    new ItemStackSerializer()).altName("shield").build();

    /**
     * Sharing Max Health.
     */
    public static final Sharable<Double> MAX_HEALTH = new Sharable.Builder<>("max_hit_points", Double.class,
            new SharableHandler<Double>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(MAX_HEALTH, getMaxHealth(player));
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Double value = profile.get(MAX_HEALTH);
                    if (value == null) {
                        Option.of(maxHealthAttr).map(player::getAttribute)
                                .peek(attr -> attr.setBaseValue(attr.getDefaultValue()));
                        return false;
                    }
                    Option.of(maxHealthAttr).map(player::getAttribute)
                            .peek(attr -> attr.setBaseValue(value));
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_MAX_HEALTH))
            .altName("maxhealth").altName("maxhp").altName("maxhitpoints").build();

    /**
     * Sharing Health.
     */
    public static final Sharable<Double> HEALTH = new Sharable.Builder<Double>("hit_points", Double.class,
            new SharableHandler<Double>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    double health = player.getHealth();
                    // Player is dead, so health should be regained to full.
                    if (health <= 0) {
                        health = getMaxHealth(player);
                    }
                    profile.set(HEALTH, health);
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Double value = profile.get(HEALTH);
                    if (value == null) {
                        player.setHealth(PlayerStats.HEALTH);
                        return false;
                    }
                    try {
                        double maxHealth = getMaxHealth(player);
                        // This share may handled before MAX_HEALTH.
                        // Thus this is needed to ensure there is no loss in health stored
                        if (value > maxHealth) {
                            Option.of(maxHealthAttr).map(player::getAttribute)
                                    .peek(attr -> attr.setBaseValue(maxHealth));
                        }
                        player.setHealth(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setHealth(PlayerStats.HEALTH);
                        return false;
                    }
                    return true;
                }

            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_HEALTH))
            .altName("health").altName("hp").altName("hitpoints").build();

    private static double getMaxHealth(Player player) {
        return Option.of(maxHealthAttr).map(player::getAttribute)
                .map(AttributeInstance::getValue)
                .getOrElse(PlayerStats.MAX_HEALTH);
    }

    /**
     * Sharing Remaining Air.
     */
    public static final Sharable<Integer> REMAINING_AIR = new Sharable.Builder<Integer>("remaining_air", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(REMAINING_AIR, player.getRemainingAir());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(REMAINING_AIR);
                    if (value == null) {
                        player.setRemainingAir(PlayerStats.REMAINING_AIR);
                        return false;
                    }
                    try {
                        player.setRemainingAir(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setRemainingAir(PlayerStats.REMAINING_AIR);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_REMAINING_AIR)).build();

    /**
     * Sharing Maximum Air.
     */
    public static final Sharable<Integer> MAXIMUM_AIR = new Sharable.Builder<Integer>("maximum_air", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(MAXIMUM_AIR, player.getMaximumAir());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(MAXIMUM_AIR);
                    if (value == null) {
                        player.setMaximumAir(PlayerStats.MAXIMUM_AIR);
                        return false;
                    }
                    try {
                        player.setMaximumAir(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setMaximumAir(PlayerStats.MAXIMUM_AIR);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_MAX_AIR)).build();

    /**
     * Sharing Fall Distance.
     */
    public static final Sharable<Float> FALL_DISTANCE = new Sharable.Builder<Float>("fall_distance", Float.class,
            new SharableHandler<Float>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(FALL_DISTANCE, player.getFallDistance());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Float value = profile.get(FALL_DISTANCE);
                    if (value == null) {
                        player.setFallDistance(PlayerStats.FALL_DISTANCE);
                        return false;
                    }
                    try {
                        player.setFallDistance(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setFallDistance(PlayerStats.FALL_DISTANCE);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_FALL_DISTANCE))
            .altName("falling").build();

    /**
     * Sharing Fire Ticks.
     */
    public static final Sharable<Integer> FIRE_TICKS = new Sharable.Builder<Integer>("fire_ticks", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(FIRE_TICKS, player.getFireTicks());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(FIRE_TICKS);
                    if (value == null) {
                        player.setFireTicks(PlayerStats.FIRE_TICKS);
                        return false;
                    }
                    try {
                        player.setFireTicks(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setFireTicks(PlayerStats.FIRE_TICKS);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_FIRE_TICKS))
            .altName("fire").altName("burning").build();


    /**
     * Sharing Experience.
     */
    public static final Sharable<Float> EXPERIENCE = new Sharable.Builder<Float>("xp", Float.class,
            new SharableHandler<Float>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(EXPERIENCE, player.getExp());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Float value = profile.get(EXPERIENCE);
                    if (value == null) {
                        player.setExp(PlayerStats.EXPERIENCE);
                        return false;
                    }
                    try {
                        player.setExp(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setExp(PlayerStats.EXPERIENCE);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_EXPERIENCE)).build();

    /**
     * Sharing Level.
     */
    public static final Sharable<Integer> LEVEL = new Sharable.Builder<Integer>("lvl", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(LEVEL, player.getLevel());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(LEVEL);
                    if (value == null) {
                        player.setLevel(PlayerStats.LEVEL);
                        return false;
                    }
                    try {
                        player.setLevel(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setLevel(PlayerStats.LEVEL);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_LEVEL)).build();

    /**
     * Sharing Total Experience.
     */
    public static final Sharable<Integer> TOTAL_EXPERIENCE = new Sharable.Builder<Integer>("total_xp", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(TOTAL_EXPERIENCE, player.getTotalExperience());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(TOTAL_EXPERIENCE);
                    if (value == null) {
                        player.setTotalExperience(PlayerStats.TOTAL_EXPERIENCE);
                        return false;
                    }
                    try {
                        player.setTotalExperience(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setTotalExperience(PlayerStats.TOTAL_EXPERIENCE);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_TOTAL_EXPERIENCE)).build();

    /**
     * Sharing Hunger.
     */
    public static final Sharable<Integer> FOOD_LEVEL = new Sharable.Builder<Integer>("food_level", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(FOOD_LEVEL, player.getFoodLevel());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Integer value = profile.get(FOOD_LEVEL);
                    if (value == null) {
                        player.setFoodLevel(PlayerStats.FOOD_LEVEL);
                        return false;
                    }
                    try {
                        player.setFoodLevel(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setFoodLevel(PlayerStats.FOOD_LEVEL);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_FOOD_LEVEL))
            .altName("food").build();

    /**
     * Sharing Exhaustion.
     */
    public static final Sharable<Float> EXHAUSTION = new Sharable.Builder<Float>("exhaustion", Float.class,
            new SharableHandler<Float>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(EXHAUSTION, player.getExhaustion());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Float value = profile.get(EXHAUSTION);
                    if (value == null) {
                        player.setExhaustion(PlayerStats.EXHAUSTION);
                        return false;
                    }
                    try {
                        player.setExhaustion(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setExhaustion(PlayerStats.EXHAUSTION);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_EXHAUSTION))
            .altName("exhaust").altName("exh").build();

    /**
     * Sharing Saturation.
     */
    public static final Sharable<Float> SATURATION = new Sharable.Builder<Float>("saturation", Float.class,
            new SharableHandler<Float>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    profile.set(SATURATION, player.getSaturation());
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Float value = profile.get(SATURATION);
                    if (value == null) {
                        player.setSaturation(PlayerStats.SATURATION);
                        return false;
                    }
                    try {
                        player.setSaturation(value);
                    } catch (IllegalArgumentException e) {
                        Logging.fine("Invalid value '" + value + "': " + e.getMessage());
                        player.setSaturation(PlayerStats.SATURATION);
                        return false;
                    }
                    return true;
                }
            }).stringSerializer(new ProfileEntry(true, DataStrings.PLAYER_SATURATION))
            .altName("sat").build();

    /**
     * Sharing Bed Spawn.
     */
    public static final Sharable<Location> BED_SPAWN = new Sharable.Builder<Location>("bed_spawn", Location.class,
            new SharableHandler<Location>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    if (hasSetSpawnEvent) {
                        // Bed spawn location already updated during PlayerSpawnChangeEvent
                        return;
                    }
                    Location bedSpawnLocation = null;
                    try {
                        Logging.finer("profile bed: " + player.getBedSpawnLocation());
                        bedSpawnLocation = findBedFromRespawnLocation(player.getBedSpawnLocation());
                    } catch (NullPointerException e) {
                        // TODO this is a temporary fix for the bug occurring in 1.16.X CB/Spigot/Paper
                        StackTraceElement[] stackTrace = e.getStackTrace();
                        String error;
                        if (stackTrace.length > 1) {
                            error = stackTrace[0].toString() + stackTrace[1];
                        } else {
                            error = "NullPointerException thrown by Player#getBedSpawnLocation";
                        }
                        Logging.warning(error + " - See https://github.com/Multiverse/Multiverse-Inventories/issues/374 for more details.");
                    }
                    profile.set(BED_SPAWN, bedSpawnLocation);
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Location loc = profile.get(BED_SPAWN);
                    if (loc == null) {
                        Logging.finer("No bed location saved");
                        ignoreSpawnListener.add(player.getUniqueId());
                        player.setBedSpawnLocation(player.getWorld().getSpawnLocation(), true);
                        ignoreSpawnListener.remove(player.getUniqueId());
                        return false;
                    }
                    ignoreSpawnListener.add(player.getUniqueId());
                    player.setBedSpawnLocation(loc, true);
                    ignoreSpawnListener.remove(player.getUniqueId());
                    Logging.finer("updating bed: " + player.getBedSpawnLocation());
                    return true;
                }
            }).serializer(new ProfileEntry(false, DataStrings.PLAYER_BED_SPAWN_LOCATION), new LocationSerializer())
            .altName("bedspawn").altName("bed").altName("beds").altName("bedspawns").build();

    // todo: handle this somewhere better
    private static List<UUID> ignoreSpawnListener = new ArrayList<>();
    private static boolean hasSetSpawnEvent = ReflectHelper.hasClass("org.bukkit.event.player.PlayerSpawnChangeEvent")
            || ReflectHelper.hasClass("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent");

    @ApiStatus.Internal
    public static boolean isIgnoringSpawnListener(Player player) {
        return ignoreSpawnListener.contains(player.getUniqueId());
    }

    /**
     * Sharing Last Location.
     */
    public static final Sharable<Location> LAST_LOCATION = new Sharable.Builder<Location>("last_location", Location.class,
            new SharableHandler<Location>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    /* It's too late to update the profile for last location here because the world change has already
                       happened. The update occurs in the PlayerTeleportEvent handler in InventoriesListener. */
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Location loc = profile.get(LAST_LOCATION);
                    if (loc == null || loc.getWorld() == null || loc.equals(player.getLocation())) {
                        return false;
                    }
                    safetyTeleporter.to(loc).checkSafety(false).teleport(player);
                    return true;
                }
            }).serializer(new ProfileEntry(false, DataStrings.PLAYER_LAST_LOCATION), new LocationSerializer())
            .altName("loc").altName("location").altName("pos").altName("position").optional().build();

    /**
     * Sharing Economy.
     */
    public static final Sharable<Double> ECONOMY = new Sharable.Builder<Double>("economy", Double.class,
            new SharableHandler<Double>() {
                private boolean hasValidEconomyHandler() {
                    if (economist.isUsingEconomyPlugin()) {
                        return true;
                    }
                    Logging.warning("You do not have an an economy plugin with Vault. Economy sharable will not work!");
                    Logging.warning("Check that your economy and vault plugin are both working correctly.");
                    Logging.warning("If you enabled the sharable by mistake, run '/mvinv toggle economy' to resolve this.");
                    return false;
                }

                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    if (!hasValidEconomyHandler()) {
                        return;
                    }
                    profile.set(ECONOMY, economist.getBalance(player));
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    if (!hasValidEconomyHandler()) {
                        return false;
                    }
                    Double money = profile.get(ECONOMY);
                    if (money == null) {
                        economist.setBalance(player, 0);
                        return false;
                    }
                    economist.setBalance(player, money);
                    return true;
                }
            }).stringSerializer(new ProfileEntry(false, "balance")).optional()
            .altName("money").altName("econ").altName("cash").altName("balance").build();

    /**
     * Sharing Potions.
     */
    public static final Sharable<PotionEffect[]> POTIONS = new Sharable.Builder<PotionEffect[]>("potion_effects", PotionEffect[].class,
            new SharableHandler<PotionEffect[]>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
                    profile.set(POTIONS, potionEffects.toArray(new PotionEffect[potionEffects.size()]));
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    PotionEffect[] effects = profile.get(POTIONS);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }
                    if (effects == null) {
                        return false;
                    }
                    for (PotionEffect effect : effects) {
                        player.addPotionEffect(effect);
                    }
                    return true;
                }
            }).serializer(new ProfileEntry(false, "potions"), new PotionEffectSerializer())
            .altName("potion").altName("potions").build();

    /**
     * Sharing Advancements.
     */
    public static final Sharable<List> ADVANCEMENTS = new Sharable.Builder<>("advancements", List.class,
            new SharableHandler<>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    Set<String> completedAdvancements = new HashSet<>();
                    Bukkit.advancementIterator().forEachRemaining(advancement -> {
                        Collection<String> awardedCriteria = player.getAdvancementProgress(advancement).getAwardedCriteria();
                        completedAdvancements.addAll(awardedCriteria);
                    });
                    profile.set(ADVANCEMENTS, new ArrayList<>(completedAdvancements));
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    List<String> advancements = profile.get(ADVANCEMENTS);
                    Set<String> processedCriteria = new HashSet<>();
                    Set<String> completedCriteria = (advancements != null) ? new HashSet<>(advancements) : new HashSet<>();

                    // Advancements may cause the player to level up, which we don't want to happen
                    int totalExperience = player.getTotalExperience();
                    int level = player.getLevel();
                    float exp = player.getExp();

                    Bukkit.advancementIterator().forEachRemaining(advancement -> {
                        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
                        for (String criteria : advancement.getCriteria()) {
                            if (processedCriteria.contains(criteria)) {
                                continue;
                            } else if (completedCriteria.contains(criteria)) {
                                advancementProgress.awardCriteria(criteria);
                            } else {
                                advancementProgress.revokeCriteria(criteria);
                            }
                            processedCriteria.add(criteria);
                        }
                    });

                    // Set back the level from before applying the advancements
                    player.setExp(exp);
                    player.setLevel(level);
                    player.setTotalExperience(totalExperience);

                    return advancements != null;
                }
            }).defaultSerializer(new ProfileEntry(false, "advancements")).altName("achievements").optional().build();

    /**
     * Sharing Statistics.
     */
    public static final Sharable<Map> GAME_STATISTICS = new Sharable.Builder<>("game_statistics", Map.class,
            new SharableHandler<>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                    Map<String, Integer> playerStats = new HashMap<>();
                    for (Statistic stat: Statistic.values()) {
                        if (stat.getType() == Statistic.Type.UNTYPED) {
                            int val = player.getStatistic(stat);
                            // no need to save values of 0, that's the default!
                            if (val != 0) {
                                playerStats.put(stat.name(), val);
                            }
                        }
                    }
                    profile.set(GAME_STATISTICS, playerStats);
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    Map<String, Integer> playerStats = profile.get(GAME_STATISTICS);
                    if (playerStats == null) {
                        // Set all to 0
                        for (Statistic stat : Statistic.values()) {
                            if (stat.getType() == Statistic.Type.UNTYPED) {
                                player.setStatistic(stat, 0);
                            }
                        }
                        return false;
                    }

                    for (Statistic stat : Statistic.values()) {
                        if (stat.getType() == Statistic.Type.UNTYPED) {
                            player.setStatistic(stat, playerStats.getOrDefault(stat.name(), 0));
                        }
                    }

                    return true;
                }
            }).defaultSerializer(new ProfileEntry(false, "game_statistics")).altName("game_stats").optional().build();

    /**
     * Sharing Recipes.
     */
    public static final Sharable<List> RECIPES = new Sharable.Builder<>("recipes", List.class,
            new SharableHandler<>() {
                @Override
                public void updateProfile(ProfileData profile, Player player) {
                   List<String> recipes = player.getDiscoveredRecipes().stream()
                           // Save space by removing the namespace if its default minecraft
                           .map(key -> NamespacedKey.MINECRAFT.equals(key.getNamespace())
                                   ? key.getKey() : key.toString())
                           .toList();
                    profile.set(RECIPES, recipes);
                }

                @Override
                public boolean updatePlayer(Player player, ProfileData profile) {
                    List<String> recipes = profile.get(RECIPES);
                    if (recipes == null) {
                        player.undiscoverRecipes(player.getDiscoveredRecipes());
                        return false;
                    }

                    Set<NamespacedKey> discoveredRecipes = player.getDiscoveredRecipes();
                    Set<NamespacedKey> toDiscover = recipes.stream().map(NamespacedKey::fromString)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    player.undiscoverRecipes(Sets.difference(discoveredRecipes, toDiscover));
                    player.discoverRecipes(Sets.difference(toDiscover, discoveredRecipes));

                    return true;
                }
            }).defaultSerializer(new ProfileEntry(false, "recipes")).optional().build();

    /**
     * Grouping for inventory sharables.
     */
    public static final Shares ALL_INVENTORY = new SharableGroup("inventory",
            fromSharables(INVENTORY, ARMOR, ENDER_CHEST, OFF_HAND), "inv", "inventories");

    /**
     * Grouping for experience sharables.
     */
    public static final Shares ALL_EXPERIENCE = new SharableGroup("experience",
            fromSharables(EXPERIENCE, TOTAL_EXPERIENCE, LEVEL), "exp", "level");

    /**
     * Grouping for air/breath related sharables.
     */
    public static final Shares AIR = new SharableGroup("air",
            fromSharables(REMAINING_AIR, MAXIMUM_AIR), "breath");

    /**
     * Grouping for hunger related sharables.
     */
    public static final SharableGroup HUNGER = new SharableGroup("hunger",
            fromSharables(FOOD_LEVEL, SATURATION, EXHAUSTION));

    /**
     * Grouping for player health related sharables.
     */
    public static final SharableGroup ALL_HEALTH = new SharableGroup("health",
            fromSharables(HEALTH, MAX_HEALTH, REMAINING_AIR, MAXIMUM_AIR, FALL_DISTANCE, FIRE_TICKS));

    /**
     * Grouping for player stat related sharables not including inventory.
     */
    public static final SharableGroup STATS = new SharableGroup("stats",
            fromSharables(HEALTH, MAX_HEALTH, FOOD_LEVEL, SATURATION, EXHAUSTION, EXPERIENCE, TOTAL_EXPERIENCE, LEVEL,
                    REMAINING_AIR, MAXIMUM_AIR, FALL_DISTANCE, FIRE_TICKS, POTIONS, GAME_STATISTICS, ADVANCEMENTS));

    /**
     * Grouping for ALL default sharables.
     * TODO: make this really mean all, including 3rd party.
     */
    public static final SharableGroup ALL_DEFAULT = new SharableGroup("all", fromSharables(HEALTH, MAX_HEALTH,
            ECONOMY, FOOD_LEVEL, SATURATION, EXHAUSTION, EXPERIENCE, TOTAL_EXPERIENCE, LEVEL, INVENTORY, ARMOR, BED_SPAWN,
            MAXIMUM_AIR, REMAINING_AIR, FALL_DISTANCE, FIRE_TICKS, POTIONS, LAST_LOCATION, ENDER_CHEST, OFF_HAND,
            GAME_STATISTICS, ADVANCEMENTS, RECIPES),
            "*", "everything");


    /**
     * Registers a Sharable, which is required for it to function properly. This method is called automatically when
     * using the {@link Sharable.Builder} class and should not be called manually.
     *
     * @param sharable The sharable to register.
     * @return True if the sharable is not already registered.
     */
    static boolean register(Sharable sharable) {
        if (!ALL_SHARABLES.contains(sharable)) {
            // If the plugin has been enabled, we need to add this sharable to the existing groups with all sharables.
            if (inventories != null) {
                var worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
                for (WorldGroup group : worldGroupManager.getGroups()) {
                    if (group.getShares().isSharing(Sharables.all())) {
                        group.getShares().setSharing(sharable, true);
                    }
                }
            }
        }
        if (ALL_SHARABLES.add(sharable)) {
            if (sharable.isOptional()) {
                OPTIONAL_SHARABLES.add(sharable);
            } else {
                STANDARD_SHARABLES.add(sharable);
            }
            for (String name : sharable.getNames()) {
                String key = name.toLowerCase();
                Shares shares = LOOKUP_MAP.get(key);
                if (shares == null) {
                    shares = noneOf();
                    LOOKUP_MAP.put(key, shares);
                }
                shares.add(sharable);
            }
            return true;
        }
        return false;
    }

    /**
     * Looks up a sharable by one of the acceptable names.
     *
     * @param name Name to look up by.
     * @return Sharable by that name or null if none by that name.
     */
    public static Shares lookup(String name) {
        return LOOKUP_MAP.get(name.toLowerCase());
    }

    /**
     * @return A collection of all registered {@link Shares}. This is NOT to be modified and serves only as a reference.
     */
    public static Collection<String> getShareNames() {
        return LOOKUP_MAP.keySet();
    }

    /**
     * @return A {@link Shares} collection containing ALL registered {@link Sharable}s. This is NOT to be modified and
     * serves only as a reference. For a version you can do what you want with, see {@link #allOf()}.
     */
    public static Shares all() {
        return ALL_SHARABLES;
    }

    /**
     * @return A {@link Shares} collection containing ALL registered standard {@link Sharable}s. This is NOT to be modified and
     * serves only as a reference. For a version you can do what you want with, see {@link #allOf()}.
     */
    public static Shares standard() {
        return STANDARD_SHARABLES;
    }

    /**
     * @return A {@link Shares} collection containing ALL registered enabled {@link Sharable}s. This is NOT to be modified and
     * serves only as a reference. For a version you can do what you want with, see {@link #allOf()}.
     */
    public static Shares enabled() {
        return enabledShares;
    }

    /**
     * @return A {@link Shares} collection containing ALL registered optional {@link Sharable}s. This is NOT to be modified and
     * serves only as a reference. For a version you can do what you want with, see {@link #optionalOf()}.
     */
    public static Shares optional() {
        return OPTIONAL_SHARABLES;
    }

    /**
     * @return A new {@link Shares} instance containing ALL registered {@link Sharable}s for your own devices.
     */
    public static Shares allOf() {
        return new Sharables(new LinkedHashSet<>(ALL_SHARABLES));
    }

    /**
     * @return A new {@link Shares} instance containing ALL enabled optional {@link Sharable}s for your own devices.
     */
    public static Shares enabledOf() {
        return new Sharables(new LinkedHashSet<>(enabledShares));
    }

    public static Shares standardOf() {
        return new Sharables(new LinkedHashSet<>(STANDARD_SHARABLES));
    }

    /**
     * @return A new {@link Shares} instance containing ALL registered optional {@link Sharable}s for your own devices.
     */
    public static Shares optionalOf() {
        return new Sharables(new LinkedHashSet<>(OPTIONAL_SHARABLES));
    }

    /**
     * @return A new empty {@link Shares} instance for your own devices.
     */
    public static Shares noneOf() {
        return new Sharables(new LinkedHashSet<>(ALL_SHARABLES.size()));
    }

    /**
     * @param shares Shares to compare against.
     * @return A new {@link Shares} instance containing all {@link Sharable}s present in {@link #all()} that are NOT
     * contained in the shares argument.
     */
    public static Shares complimentOf(Shares shares) {
        Shares compliment = Sharables.allOf();
        compliment.removeAll(shares);
        return compliment;
    }

    /**
     * Creates a new instance of {@link Shares} containing all Sharables contained in shares.
     *
     * @param shares Shares instance to effectively copy.
     * @return A new Shares instance containing all Sharables in shares.
     */
    public static Shares fromShares(Shares shares) {
        return new Sharables(shares);
    }

    /**
     * Creates a new instance of {@link Shares} containing all Sharables contained in sharesCollection.
     *
     * @param sharesCollection A collection of Sharables to effectively copy.
     * @return A new Shares instance containing all Sharables in sharesCollection.
     */
    public static Shares fromCollection(Collection<Sharable> sharesCollection) {
        Shares shares = noneOf();
        shares.addAll(sharesCollection);
        return shares;
    }

    /**
     * Creates a new instance of {@link Shares} containing the listed {@link Sharable}s.
     *
     * @param sharables Sharables to fill new Shares with.
     * @return A new Shares instance containing all Sharables passed in.
     */
    public static Shares fromSharables(Sharable... sharables) {
        Shares shares = noneOf();
        shares.addAll(Arrays.asList(sharables));
        return shares;
    }

    /**
     * Creates a new instance of {@link Shares} containing all shares negated in the given list.
     *
     * @param sharesList A list whose elements are to be strings which will be parsed with {@link #lookup(String)} if
     *                   they start with "-" indicating they are "negated" shares.
     * @return A new instance of {@link Shares} containing all shares negated in the given list.
     */
    public static Shares negativeFromList(List sharesList) {
        Shares shares = noneOf();
        for (Object shareStringObj : sharesList) {
            String shareString = shareStringObj.toString();
            if (!shareString.startsWith("-") || shareString.length() <= 1) {
                continue;
            }
            shareString = shareString.substring(1);
            Shares sharables = Sharables.lookup(shareString);
            if (sharables != null) {
                shares.mergeShares(sharables);
            } else {
                if (shareString.equals("*") || shareString.equalsIgnoreCase("all")
                        || shareString.equalsIgnoreCase("everything")) {
                    shares = allOf();
                    break;
                }
            }
        }
        return shares;
    }

    /**
     * Creates a new instance of {@link Shares} containing all shares not negated in the given list.
     *
     * @param sharesList A list whose elements are to be strings which will be parsed with {@link #lookup(String)} if
     *                   they do not start with "-" indicating they are normal shares.
     * @return A new instance of {@link Shares} containing all shares non-negated in the given list.
     */
    public static Shares fromList(List sharesList) {
        Shares shares = noneOf();
        for (Object shareStringObj : sharesList) {
            String shareString = shareStringObj.toString();
            Shares sharables = Sharables.lookup(shareString);
            if (sharables != null) {
                shares.mergeShares(sharables);
            } else {
                if (shareString.equals("*") || shareString.equalsIgnoreCase("all")
                        || shareString.equalsIgnoreCase("everything")) {
                    shares = allOf();
                    break;
                }
            }
        }
        return shares;
    }

    public static void recalculateEnabledShares() {
        Logging.finer("Recalculating enabled shares...");
        enabledShares = standardOf();
        enabledShares.addAll(inventoriesConfig.getActiveOptionalShares());
        worldGroupManager.recalculateApplicableShares();
    }

    private Set<Sharable> sharables;

    private Sharables(Set<Sharable> sharableSet) {
        this.sharables = sharableSet;
    }

    private Sharables(Shares shares) {
        this.sharables = new LinkedHashSet<Sharable>(ALL_SHARABLES.size());
        this.sharables.addAll(shares);
    }

    @Override
    public int size() {
        return this.sharables.size();
    }

    @Override
    public boolean isEmpty() {
        return this.sharables.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.sharables.contains(o);
    }

    @Override
    public Iterator<Sharable> iterator() {
        return this.sharables.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.sharables.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.sharables.toArray(a);
    }

    @Override
    public boolean add(Sharable sharable) {
        return this.sharables.add(sharable);
    }

    @Override
    public boolean remove(Object o) {
        return this.sharables.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.sharables.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Sharable> c) {
        return this.sharables.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.sharables.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.sharables.removeAll(c);
    }

    @Override
    public void clear() {
        this.sharables.clear();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Shares && ((Shares) o).isSharing(this);
    }

    @Override
    public int hashCode() {
        return this.sharables.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeShares(Shares newShares) {
        this.addAll(newShares);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares setSharing(Sharable sharable, boolean sharing) {
        if (sharing) {
            this.add(sharable);
        } else {
            this.remove(sharable);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares setSharing(Shares sharables, boolean sharing) {
        for (Sharable sharable : sharables) {
            if (sharing) {
                this.add(sharable);
            } else {
                this.remove(sharable);
            }
        }
        return this;
    }

    @Override
    public Shares compare(Shares shares) {
        Shares bothSharing = Sharables.noneOf();
        for (Sharable sharable : shares) {
            if (this.contains(sharable)) {
                bothSharing.add(sharable);
            }
        }
        return bothSharing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharing(Sharable sharable) {
        return this.contains(sharable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharing(Shares shares) {
        boolean isSharing = this.sharables.equals(shares);
        if (!isSharing) {
            for (Sharable sharable : shares) {
                if (!this.isSharing(sharable)) {
                    return false;
                }
            }
            isSharing = true;
        }
        return isSharing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new LinkedList<String>();
        if (this.isSharing(Sharables.allOf())) {
            list.add("all");
        } else {
            for (Sharable sharable : this) {
                list.add(sharable.toString());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Sharable sharable : this) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(sharable);
        }
        return stringBuilder.toString();
    }
}
