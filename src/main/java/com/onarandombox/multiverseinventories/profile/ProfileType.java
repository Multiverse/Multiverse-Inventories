package com.onarandombox.multiverseinventories.profile;

/**
 * Used to differentiate between profiles in the same world or world group, primarily for game modes.
 */
public final class ProfileType {

    static ProfileType createProfileType(String name) {
        return new ProfileType(name);
    }

    private String name;

    private ProfileType(String name) {
        this.name = name;
    }

    /**
     * @return The name of the profile.  The default profile type will return a blank string.
     */
    public String getName() {
        return name;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof ProfileType && ((ProfileType) o).getName().equals(this.getName());
    }

    @Override
    public final int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "ProfileType:" + getName();
    }
}
