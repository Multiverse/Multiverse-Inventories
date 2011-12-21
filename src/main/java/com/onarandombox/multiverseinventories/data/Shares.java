package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.MVIManager;

/**
 * @author dumptruckman
 */
public class Shares {

    private Boolean sharingInventory = null;
    private Boolean sharingArmor = null;
    private Boolean sharingHealth = null;
    private Boolean sharingExp = null;
    private Boolean sharingHunger = null;
    private Boolean sharingEffects = null;

    public void mergeShares(Shares newShares) {
        if (newShares.isSharingInventory() != null) {
            this.setSharingInventory(newShares.isSharingInventory());
        }
        if (newShares.isSharingArmor() != null) {
            this.setSharingArmor(newShares.isSharingArmor());
        }
        if (newShares.isSharingHealth() != null) {
            this.setSharingHealth(newShares.isSharingHealth());
        }
        if (newShares.isSharingExp() != null) {
            this.setSharingExp(newShares.isSharingExp());
        }
        if (newShares.isSharingHunger() != null) {
            this.setSharingHunger(newShares.isSharingHunger());
        }
        if (newShares.isSharingEffects() != null) {
            this.setSharingEffects(newShares.isSharingEffects());
        }
    }

    public void fillNullsWithDefaults() {
        if (this.isSharingInventory() == null) {
            this.setSharingInventory(MVIManager.getDefaultShares().isSharingInventory());
        }
        if (this.isSharingArmor() == null) {
            this.setSharingArmor(MVIManager.getDefaultShares().isSharingArmor());
        }
        if (this.isSharingHealth() == null) {
            this.setSharingHealth(MVIManager.getDefaultShares().isSharingHealth());
        }
        if (this.isSharingExp() == null) {
            this.setSharingExp(MVIManager.getDefaultShares().isSharingExp());
        }
        if (this.isSharingHunger() == null) {
            this.setSharingHunger(MVIManager.getDefaultShares().isSharingHunger());
        }
        if (this.isSharingEffects() == null) {
            this.setSharingEffects(MVIManager.getDefaultShares().isSharingEffects());
        }
    }

    public Boolean isSharingInventory() {
        return sharingInventory;
    }

    public void setSharingInventory(Boolean sharingInventory) {
        this.sharingInventory = sharingInventory;
    }

    public Boolean isSharingArmor() {
        return sharingArmor;
    }

    public void setSharingArmor(Boolean sharingArmor) {
        this.sharingArmor = sharingArmor;
    }

    public Boolean isSharingHealth() {
        return sharingHealth;
    }

    public void setSharingHealth(Boolean sharingHealth) {
        this.sharingHealth = sharingHealth;
    }

    public Boolean isSharingExp() {
        return sharingExp;
    }

    public void setSharingExp(Boolean sharingExp) {
        this.sharingExp = sharingExp;
    }

    public Boolean isSharingHunger() {
        return sharingHunger;
    }

    public void setSharingHunger(Boolean sharingHunger) {
        this.sharingHunger = sharingHunger;
    }

    public Boolean isSharingEffects() {
        return sharingEffects;
    }

    public void setSharingEffects(Boolean sharingEffects) {
        this.sharingEffects = sharingEffects;
    }
}
