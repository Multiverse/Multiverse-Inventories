package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Shares;

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

    @Override
    public Shares getShares() {
        return shares;
    }

    public boolean equals(Object o) {
        return o instanceof ProfileType && ((ProfileType) o).getName().equals(this.getName());
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public String toString() {
        return "ProfileType:" + getName();
    }
}
