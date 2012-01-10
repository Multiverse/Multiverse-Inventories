package com.onarandombox.multiverseinventories.share;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of Shares.
 */
public class SimpleShares implements Shares {

    private Sharing sharingInventory = Sharing.NOT_SET;
    private Sharing sharingHealth = Sharing.NOT_SET;
    private Sharing sharingHunger = Sharing.NOT_SET;
    private Sharing sharingExp = Sharing.NOT_SET;
    private Sharing sharingEffects = Sharing.NOT_SET;


    public SimpleShares() {
    }

    public SimpleShares(Shares shares) {
        this(shares.getSharingInventory(), shares.getSharingHealth(), shares.getSharingExp(),
                shares.getSharingHunger(), shares.getSharingEffects());
    }

    public SimpleShares(Sharing sharingInventory, Sharing sharingHealth, Sharing sharingHunger,
                        Sharing sharingExp, Sharing sharingEffects) {
        this.sharingInventory = sharingInventory;
        this.sharingHealth = sharingHealth;
        this.sharingHunger = sharingHunger;
        this.sharingExp = sharingExp;
        this.sharingEffects = sharingEffects;
    }

    public SimpleShares(List<String> sharesList) {
        for (String shareString : sharesList) {
            if (shareString.equals("inv") || shareString.equals("inventory")) {
                this.setSharingInventory(Sharing.TRUE);
            } else if (shareString.equals("health") || shareString.equals("hp")) {
                this.setSharingHealth(Sharing.TRUE);
            } else if (shareString.equals("hunger") || shareString.equals("food")) {
                this.setSharingHunger(Sharing.TRUE);
            } else if (shareString.equals("exp") || shareString.equals("experience")) {
                this.setSharingExp(Sharing.TRUE);
            } else if (shareString.equals("effects") || shareString.equals("fx")) {
                this.setSharingEffects(Sharing.TRUE);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeShares(Shares newShares) {
        if (this.getSharingInventory().isNotSet()) {
            this.setSharingInventory(newShares.getSharingInventory());
        }
        if (this.getSharingHealth().isNotSet()) {
            this.setSharingHealth(newShares.getSharingHealth());
        }
        if (this.getSharingHunger().isNotSet()) {
            this.setSharingHunger(newShares.getSharingHunger());
        }
        if (this.getSharingExp().isNotSet()) {
            this.setSharingExp(newShares.getSharingExp());
        }
        if (this.getSharingEffects().isNotSet()) {
            this.setSharingEffects(newShares.getSharingEffects());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharing getSharingInventory() {
        return sharingInventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingInventory(Sharing sharingInventory) {
        this.sharingInventory = sharingInventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharing getSharingHealth() {
        return sharingHealth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingHealth(Sharing sharingHealth) {
        this.sharingHealth = sharingHealth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharing getSharingHunger() {
        return sharingHunger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingHunger(Sharing sharingHunger) {
        this.sharingHunger = sharingHunger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharing getSharingExp() {
        return sharingExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingExp(Sharing sharingExp) {
        this.sharingExp = sharingExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sharing getSharingEffects() {
        return sharingEffects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingEffects(Sharing sharingEffects) {
        this.sharingEffects = sharingEffects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new LinkedList<String>();
        if (this.getSharingInventory() != Sharing.NOT_SET) {
            list.add("inventory");
        }
        if (this.getSharingHealth() != Sharing.NOT_SET) {
            list.add("health");
        }
        if (this.getSharingHunger() != Sharing.NOT_SET) {
            list.add("hunger");
        }
        if (this.getSharingExp() != Sharing.NOT_SET) {
            list.add("experience");
        }
        if (this.getSharingEffects() != Sharing.NOT_SET) {
            list.add("effects");
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Inventory: ");
        stringBuilder.append(this.getSharingInventory());
        stringBuilder.append(" Health: ");
        stringBuilder.append(this.getSharingHealth());
        stringBuilder.append(" Exp: ");
        stringBuilder.append(this.getSharingExp());
        stringBuilder.append(" Hunger: ");
        stringBuilder.append(this.getSharingHunger());
        stringBuilder.append(" Effects: ");
        stringBuilder.append(this.getSharingEffects());
        return stringBuilder.toString();
    }
}
