package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.Shares;

/**
 * Default implementation of a profile type.
 */
class DefaultProfileType implements ProfileType {

    private String name;
    private Shares shares;

    DefaultProfileType(String name, Shares shares) {
        this.name = name;
        this.shares = shares;
    }

    @Override
    public String getName() {
        return name;
    }

    /*
    @Override
    public Shares getShares() {
        return shares;
    }
    */

    @Override
    public boolean equals(Object o) {
        return o instanceof ProfileType && ((ProfileType) o).getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "ProfileType:" + getName();
    }
}
