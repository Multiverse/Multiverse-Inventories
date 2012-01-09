package com.onarandombox.multiverseinventories.share;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dumptruckman
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
        this(shares.isSharingInventory(), shares.isSharingHealth(), shares.isSharingExp(),
                shares.isSharingHunger(), shares.isSharingEffects());
    }

    public SimpleShares(Sharing sharingInventory, Sharing sharingHealth, Sharing sharingHunger,
                        Sharing sharingExp, Sharing sharingEffects) {
        this.sharingInventory = sharingInventory;
        this.sharingHealth = sharingHealth;
        this.sharingHunger = sharingHunger;
        this.sharingExp = sharingExp;
        this.sharingEffects = sharingEffects;
    }

    public void mergeShares(Shares newShares) {
        if (this.isSharingInventory() == Sharing.NOT_SET) {
            this.setSharingInventory(newShares.isSharingInventory());
        }
        if (this.isSharingHealth() == Sharing.NOT_SET) {
            this.setSharingHealth(newShares.isSharingHealth());
        }
        if (this.isSharingHunger() == Sharing.NOT_SET) {
            this.setSharingHunger(newShares.isSharingHunger());
        }
        if (this.isSharingExp() == Sharing.NOT_SET) {
            this.setSharingExp(newShares.isSharingExp());
        }
        if (this.isSharingEffects() == Sharing.NOT_SET) {
            this.setSharingEffects(newShares.isSharingEffects());
        }
    }

    public Sharing isSharingInventory() {
        return sharingInventory;
    }

    public void setSharingInventory(Sharing sharingInventory) {
        this.sharingInventory = sharingInventory;
    }

    public Sharing isSharingHealth() {
        return sharingHealth;
    }

    public void setSharingHealth(Sharing sharingHealth) {
        this.sharingHealth = sharingHealth;
    }

    public Sharing isSharingHunger() {
        return sharingHunger;
    }

    public void setSharingHunger(Sharing sharingHunger) {
        this.sharingHunger = sharingHunger;
    }

    public Sharing isSharingExp() {
        return sharingExp;
    }

    public void setSharingExp(Sharing sharingExp) {
        this.sharingExp = sharingExp;
    }

    public Sharing isSharingEffects() {
        return sharingEffects;
    }

    public void setSharingEffects(Sharing sharingEffects) {
        this.sharingEffects = sharingEffects;
    }

    public List<String> toStringList() {
        List<String> list = new ArrayList<String>(5);
        if (this.isSharingInventory() != Sharing.NOT_SET) {
            list.add("inventory");
        }
        if (this.isSharingHealth() != Sharing.NOT_SET) {
            list.add("health");
        }
        if (this.isSharingHunger() != Sharing.NOT_SET) {
            list.add("hunger");
        }
        if (this.isSharingExp() != Sharing.NOT_SET) {
            list.add("experience");
        }
        if (this.isSharingEffects() != Sharing.NOT_SET) {
            list.add("effects");
        }
        return list;
    }

    public static Shares parseShares(List<String> sharesList) {
        Shares shares = new SimpleShares();
        for (String shareString : sharesList) {
            if (shareString.equals("inv") || shareString.equals("inventory")) {
                shares.setSharingInventory(Sharing.TRUE);
            } else if (shareString.equals("health") || shareString.equals("hp")) {
                shares.setSharingHealth(Sharing.TRUE);
            } else if (shareString.equals("hunger") || shareString.equals("food")) {
                shares.setSharingHunger(Sharing.TRUE);
            } else if (shareString.equals("exp") || shareString.equals("experience")) {
                shares.setSharingExp(Sharing.TRUE);
            } else if (shareString.equals("effects") || shareString.equals("fx")) {
                shares.setSharingEffects(Sharing.TRUE);
            }
        }
        return shares;
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
