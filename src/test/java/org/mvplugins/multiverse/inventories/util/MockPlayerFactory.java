/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package org.mvplugins.multiverse.inventories.util;

import org.mvplugins.multiverse.inventories.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyFloat;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockPlayerFactory {

    private static final Map<String, Player> createdPlayers = new HashMap<>();
    private static final Map<UUID, Player> playerUIDs = new HashMap<>();

    public static Player makeMockPlayer(String name, Server server) {
        Player player = new MockPlayerFactory(name, UUID.randomUUID(), server).getMockPlayer();
        registerPlayer(player);
        return player;
    }

    public static Player makeMockPlayer(UUID uuid, Server server) {
        Player player = new MockPlayerFactory(uuid.toString(), uuid, server).getMockPlayer();
        registerPlayer(player);
        return player;
    }

    public static Player getMockPlayer(String name) {
        return createdPlayers.get(name);
    }

    public static Player getOrCreateMockPlayer(String name, Server server) {
        Player player = getMockPlayer(name);
        if (player == null) {
            player = makeMockPlayer(name, server);
        }
        return player;
    }

    public static Player getMockPlayer(UUID uuid) {
        return playerUIDs.get(uuid);
    }

    public static Player getOrCreateMockPlayer(UUID uuid, Server server) {
        Player player = getMockPlayer(uuid);
        if (player == null) {
            player = makeMockPlayer(uuid, server);
        }
        return player;
    }

    public static void clearAllPlayers() {
        createdPlayers.clear();
        playerUIDs.clear();
    }

    public static Collection<Player> getAllPlayers() {
        return createdPlayers.values();
    }

    public static Player changeName(Player player, String newName) {
        createdPlayers.remove(player.getName());
        when(player.getName()).thenReturn(newName);

        registerPlayer(player);
        return player;
    }

    private static void registerPlayer(Player player) {
        createdPlayers.put(player.getName(), player);
        playerUIDs.put(player.getUniqueId(), player);
    }

    private final Player player;
    private final PlayerData data = new PlayerData();

    private MockPlayerFactory(String name, UUID uuid, Server server) {
        player = mock(Player.class);
        mockName(name);
        mockUid(uuid);
        mockServer(server);
        mockPlayerData();
    }

    private Player getMockPlayer() {
        return player;
    }

    private void mockName(String name) {
        when(player.getName()).thenReturn(name);
        when(player.getDisplayName()).thenReturn(name);
        when(player.getPlayerListName()).thenReturn(name);
    }

    private void mockUid(UUID uuid) {
        when(player.getUniqueId()).thenReturn(uuid);
    }

    private void mockServer(Server server) {
        when(player.getServer()).thenReturn(server);
    }

    private void mockPlayerData() {
        mockCompassTarget();
        mockExp();
        mockLevel();
        mockTotalExperience();
        mockExhaustion();
        mockSaturation();
        mockFoodLevel();
        mockBedSpawnLocation();
        mockInventory();
        mockEnderChest();
        mockHealth();
        mockMaximumNoDamageTicks();
        mockLastDamage();
        mockMaximumAir();
        mockPotionEffects();
        mockLocation();
        mockTeleport();
    }

    private void mockCompassTarget() {
        when(player.getCompassTarget()).thenAnswer(i -> data.compassTarget);
        doAnswer(invocation -> {
            data.compassTarget = invocation.getArgument(0);
            return null;
        }).when(player).setCompassTarget(any(Location.class));
    }

    private void mockExp() {
        when(player.getExp()).thenAnswer(i -> data.exp);
        doAnswer(invocation -> {
            data.exp = invocation.getArgument(0);
            return null;
        }).when(player).setExp(anyFloat());
    }

    private void mockLevel() {
        when(player.getLevel()).thenAnswer(i -> data.level);
        doAnswer(invocation -> {
            data.level = invocation.getArgument(0);
            return null;
        }).when(player).setLevel(anyInt());
    }

    private void mockTotalExperience() {
        when(player.getTotalExperience()).thenAnswer(i -> data.totalExperience);
        doAnswer(invocation -> {
            data.totalExperience = invocation.getArgument(0);
            return null;
        }).when(player).setTotalExperience(anyInt());
    }

    private void mockExhaustion() {
        when(player.getExhaustion()).thenAnswer(i -> data.exhaustion);
        doAnswer(invocation -> {
            data.exhaustion = invocation.getArgument(0);
            return null;
        }).when(player).setExhaustion(anyFloat());
    }

    private void mockSaturation() {
        when(player.getSaturation()).thenAnswer(i -> data.saturation);
        doAnswer(invocation -> {
            data.saturation = invocation.getArgument(0);
            return null;
        }).when(player).setSaturation(anyFloat());
    }

    private void mockFoodLevel() {
        when(player.getFoodLevel()).thenAnswer(i -> data.foodLevel);
        doAnswer(invocation -> {
            data.foodLevel = invocation.getArgument(0);
            return null;
        }).when(player).setFoodLevel(anyInt());
    }

    private void mockBedSpawnLocation() {
        when(player.getBedSpawnLocation()).thenAnswer(i -> data.bedSpawnLocation);
        doAnswer(invocation -> {
            data.bedSpawnLocation = invocation.getArgument(0);
            return null;
        }).when(player).setBedSpawnLocation(any(Location.class));
        doAnswer(invocation -> {
            data.bedSpawnLocation = invocation.getArgument(0);
            return null;
        }).when(player).setBedSpawnLocation(any(Location.class), anyBoolean());
    }

    private void mockInventory() {
        when(player.getInventory()).thenReturn(data.inventory);
    }

    private void mockEnderChest() {
        when(player.getEnderChest()).thenReturn(data.enderChest);
    }

    private void mockHealth() {
        when(player.getHealth()).thenAnswer(i -> data.health);
        doAnswer(invocation -> {
            data.health = invocation.getArgument(0);
            return null;
        }).when(player).setHealth(anyDouble());
    }

    private void mockMaximumNoDamageTicks() {
        when(player.getMaximumNoDamageTicks()).thenAnswer(i -> data.maximumNoDamageTicks);
        doAnswer(invocation -> {
            data.maximumNoDamageTicks = invocation.getArgument(0);
            return null;
        }).when(player).setMaximumNoDamageTicks(anyInt());
    }

    private void mockLastDamage() {
        when(player.getLastDamage()).thenAnswer(i -> data.lastDamage);
        doAnswer(invocation -> {
            data.lastDamage = invocation.getArgument(0);
            return null;
        }).when(player).setLastDamage(anyDouble());
    }

    private void mockMaximumAir() {
        when(player.getMaximumAir()).thenAnswer(i -> data.maximumAir);
        doAnswer(invocation -> {
            data.maximumAir = invocation.getArgument(0);
            return null;
        }).when(player).setMaximumAir(anyInt());
    }

    private void mockPotionEffects() {
        when(player.getActivePotionEffects()).thenAnswer(i -> new ArrayList<>(data.potionEffects.values()));

        doAnswer(invocation -> {
            PotionEffect effect = invocation.getArgument(0);
            data.potionEffects.put(effect.getType().getId(), effect);
            return true;
        }).when(player).addPotionEffect(any(PotionEffect.class));

        doAnswer(invocation -> {
            PotionEffectType type = invocation.getArgument(0);
            data.potionEffects.remove(type.getId());
            return null;
        }).when(player).removePotionEffect(any(PotionEffectType.class));
    }

    private void mockLocation() {
        when(player.getLocation()).thenAnswer(i -> data.location);
        when(player.getWorld()).thenAnswer(i -> data.location.getWorld());
    }

    private void mockTeleport() {
        doAnswer(invocation -> {
            data.location = invocation.getArgument(0);
            return true;
        }).when(player).teleport(any(Location.class));

        doAnswer(invocation -> {
            data.location = invocation.getArgument(0);
            return true;
        }).when(player).teleport(any(Location.class), any(PlayerTeleportEvent.TeleportCause.class));
    }

    private static class PlayerData {
        Location compassTarget = null;

        float exp = PlayerStats.EXPERIENCE;
        int level = PlayerStats.LEVEL;
        int totalExperience = PlayerStats.TOTAL_EXPERIENCE;
        float exhaustion = PlayerStats.EXHAUSTION;
        float saturation = PlayerStats.SATURATION;
        int foodLevel = PlayerStats.FOOD_LEVEL;

        Location bedSpawnLocation = null;

        PlayerInventory inventory = new MockPlayerInventory();
        PlayerInventory enderChest = new MockPlayerInventory();

        double health = PlayerStats.HEALTH;
        int maximumNoDamageTicks = 0;
        double lastDamage = 0D;

        int maximumAir = 20;

        Map<Integer, PotionEffect> potionEffects = new HashMap<>();

        Location location = new Location(MockWorldFactory.getWorld("world"), 0, 70, 0);
    }
}
