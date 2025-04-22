package org.mvplugins.multiverse.inventories.profile.data;

import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.Map;

final class EmptyProfileData implements ProfileData {
    private static final EmptyProfileData INSTANCE = new EmptyProfileData();

    static EmptyProfileData getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> T get(Sharable<T> sharable) {
        return null;
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Sharable, Object> getData() {
        return Map.of();
    }

    @Override
    public void update(ProfileData snapshot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(ProfileData snapshot, Shares shares) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
