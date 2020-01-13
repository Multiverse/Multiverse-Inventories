package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.PlayerStats;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;

public class MockPlayer implements Player {

    String name;
    Server server;
    UUID uuid;

    Location compassTarget = null;
    Location bedSpawn = null;
    Location playerLocation = null;
    float exp = PlayerStats.EXPERIENCE;
    int level = PlayerStats.LEVEL;
    int total_exp = PlayerStats.TOTAL_EXPERIENCE;
    float exhaustion = PlayerStats.EXHAUSTION;
    float saturation = PlayerStats.SATURATION;
    int food_level = PlayerStats.FOOD_LEVEL;
    double health = PlayerStats.HEALTH;

    Map<Integer, PotionEffect> potionEffects = new HashMap<Integer, PotionEffect>();

    int max_no_damage_ticks = 0;
    int max_air = 20;
    double last_damage = 0;

    PlayerInventory inventory = new MockPlayerInventory();
    PlayerInventory enderChest = new MockPlayerInventory();

    public MockPlayer(String name, UUID uuid, Server server) {
        this.name = name;
        this.uuid = uuid;
        this.server = server;
    }

    @Override
    public void playSound(Location location, String s, float v, float v2) {

    }

    @Override
    public void sendSignChange(Location location, String[] strings) throws IllegalArgumentException {

    }

    @Override
    public void sendSignChange(@NotNull Location location, @Nullable String[] strings, @NotNull DyeColor dyeColor) throws IllegalArgumentException {

    }

    @Override
    public void removeAchievement(Achievement achievement) {

    }

    @Override
    public boolean hasAchievement(Achievement achievement) {
        return false;
    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, int i) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int i) throws IllegalArgumentException {

    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {

    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) {

    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int i) {

    }

    @Override
    public void setPlayerWeather(WeatherType weatherType) {

    }

    @Override
    public WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {

    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public void setResourcePack(String s) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public boolean isHealthScaled() {
        return false;
    }

    @Override
    public void setHealthScaled(boolean b) {

    }

    @Override
    public void setHealthScale(double v) throws IllegalArgumentException {

    }

    @Override
    public double getHealthScale() {
        return 0;
    }

    @Override
    public void setCustomName(String s) {

    }

    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean b) {

    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean isLeashed() {
        return false;
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return null;
    }

    @Override
    public boolean setLeashHolder(Entity entity) {
        return false;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) {
        return null;
    }

    @Override
    public void setCompassTarget(Location location) {
        this.compassTarget = location;
    }

    @Override
    public Location getCompassTarget() {
        return this.compassTarget;
    }

    @Override
    public void giveExp(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void giveExpLevels(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMaxHealth(final double i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetMaxHealth() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getExp() {
        return this.exp;
    }

    @Override
    public void setExp(float v) {
        this.exp = v;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int i) {
        this.level = i;
    }

    @Override
    public int getTotalExperience() {
        return this.total_exp;
    }

    @Override
    public void setTotalExperience(int i) {
        this.total_exp = i;
    }

    @Override
    public float getExhaustion() {
        return this.exhaustion;
    }

    @Override
    public void setExhaustion(float v) {
        this.exhaustion = v;
    }

    @Override
    public float getSaturation() {
        return this.saturation;
    }

    @Override
    public void setSaturation(float v) {
        this.saturation = v;
    }

    @Override
    public int getFoodLevel() {
        return this.food_level;
    }

    @Override
    public void setFoodLevel(int i) {
        this.food_level = i;
    }

    @Override
    public Location getBedSpawnLocation() {
        return this.bedSpawn;
    }

    @Override
    public void setBedSpawnLocation(Location location) {
        this.bedSpawn = location;
    }

    @Override
    public void setBedSpawnLocation(Location location, boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PlayerInventory getInventory() {
        return this.inventory;
    }

    @Override
    public Inventory getEnderChest() {
        return this.enderChest;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public String getPlayerListName() {
        return this.name;
    }


    @Override
    public double getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(double i) {
        this.health = i;
    }

    @Override
    public double getAbsorptionAmount() {
        return 0;
    }

    @Override
    public void setAbsorptionAmount(double v) {

    }

    @Override
    public int getMaximumNoDamageTicks() {
        return this.max_no_damage_ticks;
    }

    @Override
    public void setMaximumNoDamageTicks(int i) {
        this.max_no_damage_ticks = i;
    }

    @Override
    public double getLastDamage() {
        return this.last_damage;
    }

    @Override
    public void setLastDamage(double i) {
        this.last_damage = i;
    }

    @Override
    public double getMaxHealth() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRemainingAir() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRemainingAir(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMaximumAir() {
        return max_air;
    }

    @Override
    public void setMaximumAir(int i) {
        this.max_air = i;
    }

    @Override
    public boolean addPotionEffect(PotionEffect potionEffect) {
        potionEffects.put(potionEffect.getType().getId(), potionEffect);
        return true;
    }

    @Override
    public boolean addPotionEffect(PotionEffect potionEffect, boolean b) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> potionEffects) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType potionEffectType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removePotionEffect(PotionEffectType potionEffectType) {
        potionEffects.remove(potionEffectType.getId());
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        return new ArrayList<PotionEffect>(this.potionEffects.values());
    }

    @Override
    public Location getLocation() {
        return this.playerLocation;
    }

    @Override
    public Location getLocation(Location location) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public World getWorld() {
        return this.playerLocation.getWorld();
    }

    @Override
    public int getFireTicks() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMaxFireTicks() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFireTicks(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getFallDistance() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFallDistance(float v) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getTicksLived() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTicksLived(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getExpToLevel() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRemoveWhenFarAway(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isValid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public boolean teleport(Location location) {
        return teleport(location, TeleportCause.UNKNOWN);
    }

    @Override
    public boolean teleport(Location location, TeleportCause teleportCause) {
        this.playerLocation = location;

        return true;
    }

    @Override
    public Server getServer() {
        return this.server;
    }


    @Override
    public void setDisplayName(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerListName(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendRawMessage(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void kickPlayer(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void chat(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean performCommand(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSneaking() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSneaking(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSprinting() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSprinting(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void loadData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSleepingIgnored(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSleepingIgnored() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void playNote(Location location, byte b, byte b1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void playNote(Location location, Instrument instrument, Note note) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void playEffect(Location location, Effect effect, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T t) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendBlockChange(Location location, Material material, byte b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sendChunkChange(Location location, int i, int i1, int i2, byte[] bytes) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMap(MapView mapView) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateInventory() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void awardAchievement(Achievement achievement) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void incrementStatistic(Statistic statistic) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void incrementStatistic(Statistic statistic, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPlayerTime(long l, boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getPlayerTime() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resetPlayerTime() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getAllowFlight() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAllowFlight(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void hidePlayer(Player player) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void showPlayer(Player player) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canSee(Player player) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String[] strings) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, Object> serialize() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isConversing() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void acceptConversationInput(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean setWindowProperty(Property property, int i) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InventoryView getOpenInventory() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InventoryView openInventory(Inventory itemStacks) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void openInventory(InventoryView inventoryView) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void closeInventory() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getItemInHand() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setItemInHand(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ItemStack getItemOnCursor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setItemOnCursor(ItemStack itemStack) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSleeping() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getSleepTicks() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.SURVIVAL;
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public double getEyeHeight() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double getEyeHeight(boolean b) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Location getEyeLocation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean isFlying() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFlying(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBlocking() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void damage(double i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void damage(double i, Entity entity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNoDamageTicks() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNoDamageTicks(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Player getKiller() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void setVelocity(Vector vector) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Vector getVelocity() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean teleport(Entity entity) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean teleport(Entity entity, TeleportCause teleportCause) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Entity> getNearbyEntities(double v, double v1, double v2) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getEntityId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void remove() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDead() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Entity getPassenger() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setPassenger(Entity entity) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEmpty() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean eject() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void setLastDamageCause(EntityDamageEvent entityDamageEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }


    @Override
    public void playEffect(EntityEffect entityEffect) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EntityType getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInsideVehicle() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean leaveVehicle() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Entity getVehicle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMetadata(String s, MetadataValue metadataValue) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<MetadataValue> getMetadata(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasMetadata(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeMetadata(String s, Plugin plugin) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOnline() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBanned() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWhitelisted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setWhitelisted(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public long getFirstPlayed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getLastPlayed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPlayedBefore() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPermissionSet(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(String s) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void recalculatePermissions() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOp() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setOp(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setFlySpeed(float v) throws IllegalArgumentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setWalkSpeed(float v) throws IllegalArgumentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getFlySpeed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getWalkSpeed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void setTexturePack(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void playSound(Location location, Sound sound, float v, float v1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EntityEquipment getEquipment() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCanPickupItems(boolean b) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getCanPickupItems() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(Location location, String s, SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void stopSound(Sound sound) {

    }

    @Override
    public void stopSound(String s) {

    }

    @Override
    public void stopSound(Sound sound, SoundCategory soundCategory) {

    }

    @Override
    public void stopSound(String s, SoundCategory soundCategory) {

    }

    @Override
    public void setResourcePack(String s, byte[] bytes) {

    }

    @Override
    public Entity getSpectatorTarget() {
        return null;
    }

    @Override
    public void setSpectatorTarget(Entity entity) {

    }

    @Override
    public void sendTitle(String s, String s1) {

    }

    @Override
    public void sendTitle(String s, String s1, int i, int i1, int i2) {

    }

    @Override
    public void resetTitle() {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, T t) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, T t) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, T t) {

    }

    @Override
    public MainHand getMainHand() {
        return null;
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean b) {
        return null;
    }

    @Override
    public InventoryView openMerchant(Merchant merchant, boolean b) {
        return null;
    }

    @Override
    public boolean hasCooldown(Material material) {
        return false;
    }

    @Override
    public int getCooldown(Material material) {
        return 0;
    }

    @Override
    public void setCooldown(Material material, int i) {

    }

    @Override
    public boolean isHandRaised() {
        return false;
    }

    @Override
    public List<Block> getLineOfSight(Set<Material> set, int i) {
        return null;
    }

    @Override
    public Block getTargetBlock(Set<Material> set, int i) {
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) {
        return null;
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType potionEffectType) {
        return null;
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public void setGliding(boolean b) {

    }

    @Override
    public void setAI(boolean b) {

    }

    @Override
    public boolean hasAI() {
        return false;
    }

    @Override
    public void setCollidable(boolean b) {

    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public <T> @Nullable T getMemory(@NotNull MemoryKey<T> memoryKey) {
        return null;
    }

    @Override
    public <T> void setMemory(@NotNull MemoryKey<T> memoryKey, @Nullable T t) {

    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        return null;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean addPassenger(Entity entity) {
        return false;
    }

    @Override
    public boolean removePassenger(Entity entity) {
        return false;
    }

    @Override
    public void setGlowing(boolean b) {

    }

    @Override
    public boolean isGlowing() {
        return false;
    }

    @Override
    public void setInvulnerable(boolean b) {

    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void setSilent(boolean b) {

    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(boolean b) {

    }

    @Override
    public int getPortalCooldown() {
        return 0;
    }

    @Override
    public void setPortalCooldown(int i) {

    }

    @Override
    public Set<String> getScoreboardTags() {
        return null;
    }

    @Override
    public boolean addScoreboardTag(String s) {
        return false;
    }

    @Override
    public boolean removeScoreboardTag(String s) {
        return false;
    }

    @Override
    public AdvancementProgress getAdvancementProgress(Advancement advancement) {
        return null;
    }

    @Override
    public String getLocale() {
        return null;
    }

    @Override
    public Entity getShoulderEntityLeft() {
        return null;
    }

    @Override
    public void setShoulderEntityLeft(Entity entity) {

    }

    @Override
    public Entity getShoulderEntityRight() {
        return null;
    }

    @Override
    public void setShoulderEntityRight(Entity entity) {

    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @Override
    public String getPlayerListHeader() {
        return null;
    }

    @Override
    public String getPlayerListFooter() {
        return null;
    }

    @Override
    public void setPlayerListHeader(String s) {

    }

    @Override
    public void setPlayerListFooter(String s) {

    }

    @Override
    public void setPlayerListHeaderFooter(String s, String s1) {

    }

    @Override
    public void sendBlockChange(Location location, BlockData blockData) {

    }

    @Override
    public void hidePlayer(Plugin plugin, Player player) {

    }

    @Override
    public void showPlayer(Plugin plugin, Player player) {

    }

    @Override
    public int getClientViewDistance() {
        return 0;
    }

    @Override
    public void updateCommands() {

    }

    @Override
    public void openBook(@NotNull ItemStack itemStack) {

    }

    @Override
    public boolean sleep(Location location, boolean b) {
        return false;
    }

    @Override
    public void wakeup(boolean b) {

    }

    @Override
    public Location getBedLocation() {
        return null;
    }

    @Override
    public boolean discoverRecipe(NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> collection) {
        return 0;
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> collection) {
        return 0;
    }

    @Override
    public Block getTargetBlockExact(int i) {
        return null;
    }

    @Override
    public Block getTargetBlockExact(int i, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(double v) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(double v, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public boolean isSwimming() {
        return false;
    }

    @Override
    public void setSwimming(boolean b) {

    }

    @Override
    public boolean isRiptiding() {
        return false;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void setPersistent(boolean b) {

    }

    @Override
    public BlockFace getFacing() {
        return null;
    }

    @Override
    public @NotNull Pose getPose() {
        return null;
    }

    @Override
    public void setRotation(float v, float v1) {

    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return null;
    }
}
