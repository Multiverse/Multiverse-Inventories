package com.onarandombox.multiverseinventories.share;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sharables implements Shares {

    public static final Sharable INVENTORY = DefaultSharable.INVENTORY;
    public static final Sharable EXPERIENCE = DefaultSharable.EXPERIENCE;
    public static final Sharable HEALTH = DefaultSharable.HEALTH;
    public static final Sharable HUNGER = DefaultSharable.HUNGER;
    public static final Sharable BED_SPAWN = DefaultSharable.BED_SPAWN;

    private static Shares allSharables = new Sharables(new LinkedHashSet<Sharable>());
    private static Map<String, Sharable> lookupMap = new HashMap<String, Sharable>();

    static {
        for (Sharable sharable : EnumSet.allOf(DefaultSharable.class)) {
            allSharables.add(sharable);
            for (String name : sharable.getNames()) {
                lookupMap.put(name.toLowerCase(), sharable);
            }
        }
    }

    public static boolean register(Sharable sharable) {
        if (allSharables.add(sharable)) {
            for (String name : sharable.getNames()) {
                lookupMap.put(name.toLowerCase(), sharable);
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
    public static Sharable lookup(String name) {
        return lookupMap.get(name.toLowerCase());
    }

    public static Shares all() {
        return (Shares) Collections.unmodifiableSet(allSharables);
    }

    public static Shares allOf() {
        Shares shares = new Sharables(new LinkedHashSet<Sharable>(allSharables));
        System.out.println("allOf(): " + shares);
        return shares;
    }

    public static Shares noneOf() {
        return new Sharables(new LinkedHashSet<Sharable>(allSharables.size()));
    }

    public static Shares complementOf(Shares shares) {
        Set<Sharable> compliment = Sharables.allOf();
        compliment.removeAll(shares);
        return new Sharables(compliment);
    }

    public static Shares fromShares(Shares shares) {
        return new Sharables(shares);
    }

    public static Shares fromList(List sharesList) {
        Shares shares = noneOf();
        for (Object shareStringObj : sharesList) {
            String shareString = shareStringObj.toString();
            Sharable sharable = Sharables.lookup(shareString);
            if (sharable != null) {
                shares.add(sharable);
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

    private Set<Sharable> sharables;

    private Sharables(Set<Sharable> sharableSet) {
        this.sharables = sharableSet;
    }

    private Sharables(Shares shares) {
        this.sharables = new LinkedHashSet<Sharable>(allSharables.size());
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
    public void setSharing(Sharable sharable, boolean sharing) {
        if (sharing) {
            this.add(sharable);
        } else {
            this.remove(sharable);
        }
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
            list.add("*");
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
