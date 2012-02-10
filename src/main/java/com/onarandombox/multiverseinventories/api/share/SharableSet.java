package com.onarandombox.multiverseinventories.api.share;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SharableSet implements Shares {

    private static Shares allSharables =
            new SharableSet(new LinkedHashSet<ISharable>(EnumSet.allOf(DefaultSharable.class)));

    public static boolean register(ISharable sharable) {
        return allSharables.add(sharable);
    }

    public static Shares all() {
        return allSharables;
    }

    public static Shares allOf() {
        return new SharableSet(new LinkedHashSet<ISharable>(allSharables));
    }
    
    public static Shares noneOf() {
        return new SharableSet(new LinkedHashSet<ISharable>(allSharables.size()));
    }
    
    public static Shares complementOf(Shares shares) {
        Set<ISharable> compliment = SharableSet.allOf();
        compliment.removeAll(shares);
        return new SharableSet(compliment);
    }
    
    public static Shares fromShares(Shares shares) {
        return new SharableSet(shares);
    }

    private Set<ISharable> sharables;

    private SharableSet(Set<ISharable> sharableSet) {
        this.sharables = sharableSet;
    }
    
    private SharableSet(Shares shares) {
        this.sharables = new LinkedHashSet<ISharable>(allSharables.size());
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
    public Iterator<ISharable> iterator() {
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
    public boolean add(ISharable sharable) {
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
    public boolean addAll(Collection<? extends ISharable> c) {
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
        return o instanceof Shares && ((Shares)o).isSharing(this);
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
    public void setSharing(ISharable sharable, boolean sharing) {
        if (sharing) {
            this.add(sharable);
        } else {
            this.remove(sharable);
        }
    }

    @Override
    public Shares compare(Shares shares) {
        Shares bothSharing = SharableSet.noneOf();
        for (ISharable sharable : shares) {
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
    public boolean isSharing(ISharable sharable) {
        return this.contains(sharable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharing(Shares shares) {
        boolean isSharing = this.equals(shares);
        if (!isSharing) {
            for (ISharable sharable : shares) {
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
        if (this.isSharing(SharableSet.allOf())) {
            list.add("*");
        } else {
            for (ISharable sharable : this) {
                list.add(sharable.toString());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ISharable sharable : this) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(sharable);
        }
        return stringBuilder.toString();
    }


}
