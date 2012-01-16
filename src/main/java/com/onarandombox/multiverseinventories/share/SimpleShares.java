package com.onarandombox.multiverseinventories.share;

import java.util.LinkedList;
import java.util.List;

/**
 * Simple implementation of Shares.
 */
public class SimpleShares implements Shares {

    private boolean sharingInventory = false;
    private boolean sharingHealth = false;
    private boolean sharingHunger = false;
    private boolean sharingExp = false;
    private boolean sharingEffects = false;


    public SimpleShares() {
    }

    public SimpleShares(Shares shares) {
        this(shares.isSharingInventory(), shares.isSharingHealth(), shares.isSharingExp(),
                shares.isSharingHunger(), shares.isSharingEffects());
    }

    public SimpleShares(boolean sharingInventory, boolean sharingHealth, boolean sharingHunger,
                        boolean sharingExp, boolean sharingEffects) {
        this.sharingInventory = sharingInventory;
        this.sharingHealth = sharingHealth;
        this.sharingHunger = sharingHunger;
        this.sharingExp = sharingExp;
        this.sharingEffects = sharingEffects;
    }

    //@TODO add "all"
    public SimpleShares(List sharesList) {
        for (Object shareStringObj : sharesList) {
            if (shareStringObj.toString().equals("inv") || shareStringObj.toString().equals("inventory")) {
                this.setSharingInventory(true);
            } else if (shareStringObj.toString().equals("health") || shareStringObj.toString().equals("hp")) {
                this.setSharingHealth(true);
            } else if (shareStringObj.toString().equals("hunger") || shareStringObj.toString().equals("food")) {
                this.setSharingHunger(true);
            } else if (shareStringObj.toString().equals("exp") || shareStringObj.toString().equals("experience")) {
                this.setSharingExp(true);
            } else if (shareStringObj.toString().equals("effects") || shareStringObj.toString().equals("fx")) {
                this.setSharingEffects(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeShares(Shares newShares) {
        if (!this.isSharingInventory()) {
            this.setSharingInventory(newShares.isSharingInventory());
        }
        if (!this.isSharingHealth()) {
            this.setSharingHealth(newShares.isSharingHealth());
        }
        if (!this.isSharingHunger()) {
            this.setSharingHunger(newShares.isSharingHunger());
        }
        if (!this.isSharingExp()) {
            this.setSharingExp(newShares.isSharingExp());
        }
        if (!this.isSharingEffects()) {
            this.setSharingEffects(newShares.isSharingEffects());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharingInventory() {
        return sharingInventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingInventory(boolean sharingInventory) {
        this.sharingInventory = sharingInventory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharingHealth() {
        return sharingHealth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingHealth(boolean sharingHealth) {
        this.sharingHealth = sharingHealth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharingHunger() {
        return sharingHunger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingHunger(boolean sharingHunger) {
        this.sharingHunger = sharingHunger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharingExp() {
        return sharingExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingExp(boolean sharingExp) {
        this.sharingExp = sharingExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharingEffects() {
        return sharingEffects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharingEffects(boolean sharingEffects) {
        this.sharingEffects = sharingEffects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new LinkedList<String>();
        if (this.isSharingInventory()) {
            list.add("inventory");
        }
        if (this.isSharingHealth()) {
            list.add("health");
        }
        if (this.isSharingHunger()) {
            list.add("hunger");
        }
        if (this.isSharingExp()) {
            list.add("experience");
        }
        if (this.isSharingEffects()) {
            list.add("effects");
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Inventory: ");
        stringBuilder.append(this.isSharingInventory());
        stringBuilder.append(" Health: ");
        stringBuilder.append(this.isSharingHealth());
        stringBuilder.append(" Exp: ");
        stringBuilder.append(this.isSharingExp());
        stringBuilder.append(" Hunger: ");
        stringBuilder.append(this.isSharingHunger());
        stringBuilder.append(" Effects: ");
        stringBuilder.append(this.isSharingEffects());
        return stringBuilder.toString();
    }
}
