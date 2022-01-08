package com.refinedmods.refinedstorage2.platform.api.storage;

import com.refinedmods.refinedstorage2.api.storage.CapacityAccessor;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;

public class PlatformCappedStorage<T> extends PlatformStorage<T> implements CapacityAccessor {
    private final CappedStorage<T> capped;

    public PlatformCappedStorage(CappedStorage<T> parent, StorageType<T> type, Runnable listener) {
        super(parent, type, listener);
        this.capped = parent;
    }

    @Override
    public long getCapacity() {
        return capped.getCapacity();
    }
}