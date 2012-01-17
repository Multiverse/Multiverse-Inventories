package com.onarandombox.multiverseinventories.share;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of Shares.
 */
public class SimpleShares implements Shares {

    private EnumSet<Sharable> sharing = EnumSet.noneOf(Sharable.class);

    public SimpleShares(Sharable... sharables) {
        for (Sharable sharable : sharables) {
            this.sharing.add(sharable);
        }
    }

    public SimpleShares(EnumSet<Sharable> sharables) {
        this.sharing = sharables;
    }

    public SimpleShares(Shares shares) {
        this.sharing = shares.getSharables();
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
            String shareString = shareStringObj.toString();
            Sharable sharable = Sharable.lookup(shareString);
            if (sharable != null) {
                this.sharing.add(sharable);
            } else {
                if (shareString.equals("*") || shareString.equalsIgnoreCase("all")
                        || shareString.equalsIgnoreCase("everything")) {
                    this.sharing = Sharable.all();
                    break;
                }
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
    public boolean isSharing(EnumSet<Sharable> sharables) {
        boolean isSharing = this.getSharables().equals(sharables);
        if (!isSharing) {
            for (Sharable sharable : sharables) {
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
    public EnumSet<Sharable> isSharingAnyOf(EnumSet<Sharable> sharables) {
        EnumSet<Sharable> bothSharing = EnumSet.noneOf(Sharable.class);
        for (Sharable sharable : sharables) {
            if (this.isSharing(sharable)) {
                bothSharing.add(sharable);
            }
        }
        return bothSharing;
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
        if (this.isSharing(Sharable.all())) {
            list.add("*");
        } else {
            for (Sharable sharable : this.getSharables()) {
                list.add(sharable.toString());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Sharable sharable : this.getSharables()) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(sharable);
        }
        return stringBuilder.toString();
    }
}
