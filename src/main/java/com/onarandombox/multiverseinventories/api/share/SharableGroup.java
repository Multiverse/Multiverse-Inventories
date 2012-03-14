package com.onarandombox.multiverseinventories.api.share;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a grouping of Sharable objects for the sole purpose of being able to use one keyword in
 * group setups to indicate multiple {@link Sharable}s.
 */
public final class SharableGroup implements Shares {

    private String[] names;
    private Shares shares;

    public SharableGroup(String name, Shares shares, String... alternateNames) {
        this.names = new String[alternateNames.length + 1];
        this.names[0] = name;
        System.arraycopy(alternateNames, 0, this.names, 1, alternateNames.length);
        this.shares = shares;
        for (String lookupName : this.names) {
            Sharables.LOOKUP_MAP.put(lookupName, this);
        }
    }

    /**
     * @return The names of this SharableGroup for setting as shared in the config.
     * All names in this array may be used to set a group as sharing this SharableGroup.
     */
    public String[] getNames() {
        return this.names;
    }

    @Override
    public void mergeShares(Shares newShares) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public boolean isSharing(Sharable sharable) {
        return shares.isSharing(sharable);
    }

    @Override
    public boolean isSharing(Shares shares) {
        return this.shares.isSharing(shares);
    }

    @Override
    public Shares compare(Shares shares) {
        return this.shares.compare(shares);
    }

    @Override
    public void setSharing(Sharable sharable, boolean sharing) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public void setSharing(Shares sharables, boolean sharing) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public List<String> toStringList() {
        return shares.toStringList();
    }

    @Override
    public Iterator<Sharable> iterator() {
        return shares.iterator();
    }

    @Override
    public int size() {
        return shares.size();
    }

    @Override
    public boolean isEmpty() {
        return shares.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return shares.contains(o);
    }

    @Override
    public Object[] toArray() {
        return Collections.unmodifiableCollection(this).toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return Collections.unmodifiableCollection(this).toArray(a);
    }

    @Override
    public boolean add(Sharable sharable) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public boolean remove(Object o) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return shares.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Sharable> c) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new IllegalStateException("May not alter SharableGroup!");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("May not alter SharableGroup!");
    }
}
