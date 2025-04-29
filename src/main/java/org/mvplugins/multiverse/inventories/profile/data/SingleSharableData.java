package org.mvplugins.multiverse.inventories.profile.data;

import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.Map;

public final class SingleSharableData<T> implements ProfileData {

    private final Sharable<T> sharable;
    private T value;

    public SingleSharableData(Sharable<T> sharable, T value) {
        this.sharable = sharable;
        this.value = value;
    }

    @Override
    public <S> S get(Sharable<S> sharable) {
        return sharable.equals(this.sharable) ? (S) this.value : null;
    }

    @Override
    public <S> void set(Sharable<S> sharable, S value) {
        if (sharable.equals(this.sharable)) {
            this.value = (T) value;
        }
    }

    @Override
    public Map<Sharable, Object> getData() {
        return Map.of(sharable, value);
    }

    @Override
    public void update(ProfileData snapshot) {
        if (snapshot.get(sharable) != null) {
            this.value = snapshot.get(sharable);
        }
    }

    @Override
    public void update(ProfileData snapshot, Shares shares) {
        if (shares.contains(sharable)) {
            this.value = snapshot.get(sharable);
        }
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }
}
