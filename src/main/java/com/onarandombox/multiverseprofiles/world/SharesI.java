package com.onarandombox.multiverseprofiles.world;

public interface SharesI {

    public void mergeShares(SharesI newShares);

    public boolean isSharingInventory();

    public void setSharingInventory(boolean sharingInventory);

    public boolean isSharingHealth();

    public void setSharingHealth(boolean sharingHealth);

    public boolean isSharingHunger();

    public void setSharingHunger(boolean sharingHunger);

    public boolean isSharingExp();

    public void setSharingExp(boolean sharingExp);

    public boolean isSharingEffects();

    public void setSharingEffects(boolean sharingEffects);
}
