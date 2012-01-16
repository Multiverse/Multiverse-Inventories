package com.onarandombox.multiverseinventories.share;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of Shares.
 */
public class SimpleShares implements Shares {

    private EnumSet<Sharable> sharing = EnumSet.noneOf(Sharable.class);

    public SimpleShares() {
    }

    public SimpleShares(boolean sharingInventory, boolean sharingHealth, boolean sharingHunger,
                        boolean sharingExp, boolean sharingEffects) {
        if (sharingInventory) {
            this.sharing.add(Sharable.INVENTORY);
        }
        if (sharingHealth) {
            this.sharing.add(Sharable.HEALTH);
        }
        if (sharingHunger) {
            this.sharing.add(Sharable.HUNGER);
        }
        if (sharingExp) {
            this.sharing.add(Sharable.EXPERIENCE);
        }
        if (sharingEffects) {
            this.sharing.add(Sharable.EFFECTS);
        }
    }

    public SimpleShares(List sharesList) {
        for (Object shareStringObj : sharesList) {
            Sharable sharable = Sharable.lookup(shareStringObj.toString());
            if (sharable != null) {
                this.sharing.add(sharable);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeShares(Shares newShares) {
        this.sharing.addAll(newShares.getSharables());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<Sharable> getSharables() {
        return this.sharing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharing(Sharable sharable) {
        return this.getSharables().contains(sharable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharing(Sharable sharable, boolean sharing) {
        if (sharing) {
            this.getSharables().add(sharable);
        } else {
            this.getSharables().remove(sharable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new LinkedList<String>();
        for (Sharable sharable : this.getSharables()) {
            list.add(sharable.toString());
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Inventory: ");
        stringBuilder.append(this.isSharing(Sharable.INVENTORY));
        stringBuilder.append(" Health: ");
        stringBuilder.append(this.isSharing(Sharable.HEALTH));
        stringBuilder.append(" Exp: ");
        stringBuilder.append(this.isSharing(Sharable.EXPERIENCE));
        stringBuilder.append(" Hunger: ");
        stringBuilder.append(this.isSharing(Sharable.HUNGER));
        /*
        stringBuilder.append(" Effects: ");
        stringBuilder.append(this.isSharing(Sharable.EFFECTS));
        */
        return stringBuilder.toString();
    }
}
