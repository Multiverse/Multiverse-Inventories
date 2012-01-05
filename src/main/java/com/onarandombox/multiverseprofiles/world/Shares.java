package com.onarandombox.multiverseprofiles.world;

public interface Shares {

    public void mergeShares(Shares newShares);

    public Sharing isSharingInventory();

    public void setSharingInventory(Sharing sharingInventory);

    public Sharing isSharingHealth();

    public void setSharingHealth(Sharing sharingHealth);

    public Sharing isSharingHunger();

    public void setSharingHunger(Sharing sharingHunger);

    public Sharing isSharingExp();

    public void setSharingExp(Sharing sharingExp);

    public Sharing isSharingEffects();

    public void setSharingEffects(Sharing sharingEffects);
}
