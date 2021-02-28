package com.onarandombox.multiverseinventories.util;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldsSet implements Cloneable, Set<String> {

    final private Set<String> worlds;

    public WorldsSet() {
        worlds = new HashSet<>();
    }

    public WorldsSet(@NotNull Set<String> worlds) {
        this.worlds = worlds;
    }

    @Override
    public int size() {
        return this.worlds.size();
    }

    @Override
    public boolean isEmpty() {
        return this.worlds.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        return this.worlds.contains(o.toString().toLowerCase());
    }

    public boolean contains(World world) {
        return this.contains(world.getName());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.worlds.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return this.worlds.toArray(a);
    }

    @Override
    public boolean add(String s) {
        if (s == null) {
            return false;
        }
        return this.worlds.add(s.toLowerCase());
    }

    public boolean add(World world) {
        return this.add(world.getName());
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        return this.worlds.remove(o.toString().toLowerCase());
    }

    public boolean remove(World world) {
        return this.remove(world.getName());
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.worlds.containsAll(collectionToLowerCase(c));
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends String> c) {
        return this.worlds.addAll(collectionToLowerCase(c));
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.worlds.retainAll(collectionToLowerCase(c));
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.worlds.removeAll(collectionToLowerCase(c));
    }

    private Collection<String> worldCollectionToLowerCase(@NotNull Collection<?> c) {
        return c.stream()
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }

    private Collection<String> collectionToLowerCase(@NotNull Collection<?> c) {
        return c.stream()
                .map(o -> {
                    String s = String.valueOf(o);
                    return (s == null) ? "" : s.toLowerCase();
                })
                .collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        this.worlds.clear();
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return this.worlds.iterator();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String worldName : this) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(worldName);
        }
        return stringBuilder.toString();
    }
}
