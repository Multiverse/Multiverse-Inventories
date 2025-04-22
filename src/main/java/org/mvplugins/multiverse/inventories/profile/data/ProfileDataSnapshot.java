package org.mvplugins.multiverse.inventories.profile.data;

import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.HashMap;
import java.util.Map;

public sealed class ProfileDataSnapshot implements Cloneable, ProfileData permits PlayerProfile {

    private final Map<Sharable, Object> data;

    public ProfileDataSnapshot() {
        this.data = new HashMap<>(Sharables.all().size(), 1);
    }

    @Override
    public <T> T get(Sharable<T> sharable) {
        return (T) this.data.get(sharable);
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, value);
    }

    @Override
    public Map<Sharable, Object> getData() {
        return data;
    }

    @Override
    public void update(ProfileData snapshot) {
        this.data.putAll(snapshot.getData());
    }

    @Override
    public void update(ProfileData snapshot, Shares shares) {
        shares.forEach(sharable -> {
            Object data = snapshot.getData().get(sharable);
            if (data != null) {
                this.data.put(sharable, data);
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public ProfileDataSnapshot clone() {
        try {
            return (ProfileDataSnapshot) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
