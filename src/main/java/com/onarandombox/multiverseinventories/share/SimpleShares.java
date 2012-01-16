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

    public SimpleShares(boolean sharingInventory, boolean sharingHealth, boolean sharingHunger,
                        boolean sharingExp, boolean sharingEffects) {
        this.sharingInventory = sharingInventory;
        this.sharingHealth = sharingHealth;
        this.sharingHunger = sharingHunger;
        this.sharingExp = sharingExp;
        this.sharingEffects = sharingEffects;
    }

    public SimpleShares(List sharesList) {
        for (Object shareStringObj : sharesList) {
            if (shareStringObj.toString().equals("inv") || shareStringObj.toString().equals("inventory")) {
                this.setSharing(Sharable.INVENTORY, true);
            } else if (shareStringObj.toString().equals("health") || shareStringObj.toString().equals("hp")) {
                this.setSharing(Sharable.HEALTH, true);
            } else if (shareStringObj.toString().equals("hunger") || shareStringObj.toString().equals("food")) {
                this.setSharing(Sharable.HUNGER, true);
            } else if (shareStringObj.toString().equals("exp") || shareStringObj.toString().equals("experience")) {
                this.setSharing(Sharable.EXPERIENCE, true);
            } else if (shareStringObj.toString().equals("effects") || shareStringObj.toString().equals("fx")) {
                this.setSharing(Sharable.EFFECTS, true);
            } else if (shareStringObj.toString().equals("everything")
                    || shareStringObj.toString().equals("all")
                    || shareStringObj.toString().equals("*")) {
                this.setSharing(Sharable.INVENTORY, true);
                this.setSharing(Sharable.HEALTH, true);
                this.setSharing(Sharable.HUNGER, true);
                this.setSharing(Sharable.EXPERIENCE, true);
                this.setSharing(Sharable.EFFECTS, true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeShares(Shares newShares) {
        if (!this.isSharing(Sharable.INVENTORY)) {
            this.setSharing(Sharable.INVENTORY, newShares.isSharing(Sharable.INVENTORY));
        }
        if (!this.isSharing(Sharable.HEALTH)) {
            this.setSharing(Sharable.HEALTH, newShares.isSharing(Sharable.HEALTH));
        }
        if (!this.isSharing(Sharable.HUNGER)) {
            this.setSharing(Sharable.HUNGER, newShares.isSharing(Sharable.HUNGER));
        }
        if (!this.isSharing(Sharable.EXPERIENCE)) {
            this.setSharing(Sharable.EXPERIENCE, newShares.isSharing(Sharable.EXPERIENCE));
        }
        if (!this.isSharing(Sharable.EFFECTS)) {
            this.setSharing(Sharable.EFFECTS, newShares.isSharing(Sharable.EFFECTS));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSharing(Sharable sharable) {
        switch (sharable) {
            case INVENTORY:
                return this.sharingInventory;
            case HEALTH:
                return this.sharingHealth;
            case EXPERIENCE:
                return this.sharingExp;
            case HUNGER:
                return this.sharingHunger;
            case EFFECTS:
                return this.sharingEffects;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharing(Sharable sharable, boolean sharing) {
        switch (sharable) {
            case INVENTORY:
                this.sharingInventory = sharing;
                break;
            case HEALTH:
                this.sharingHealth = sharing;
            case EXPERIENCE:
                this.sharingExp = sharing;
            case HUNGER:
                this.sharingHunger = sharing;
            case EFFECTS:
                this.sharingEffects = sharing;
            default:
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toStringList() {
        List<String> list = new LinkedList<String>();
        if (this.isSharing(Sharable.INVENTORY) && this.isSharing(Sharable.HEALTH)
                && this.isSharing(Sharable.HUNGER) && this.isSharing(Sharable.EXPERIENCE)
                /* && this.isSharing(Sharable.EFFECTS)*/) {
            list.add("*");
        } else {
            if (this.isSharing(Sharable.INVENTORY)) {
                list.add("inventory");
            }
            if (this.isSharing(Sharable.HEALTH)) {
                list.add("health");
            }
            if (this.isSharing(Sharable.HUNGER)) {
                list.add("hunger");
            }
            if (this.isSharing(Sharable.EXPERIENCE)) {
                list.add("experience");
            }
            /*
            if (this.isSharing(Sharable.EFFECTS)) {
                list.add("effects");
            }
            */
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
