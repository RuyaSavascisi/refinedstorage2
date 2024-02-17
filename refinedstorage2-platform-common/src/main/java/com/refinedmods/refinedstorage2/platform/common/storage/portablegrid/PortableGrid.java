package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.operations.NoopGridOperations;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridStorageChannelProvider;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManager;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.NoopStorage;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

class PortableGrid implements Grid, GridStorageChannelProvider {
    private final EnergyStorage energyStorage;
    private final DiskInventory diskInventory;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();
    private final StateTrackedStorage.Listener diskListener;
    @Nullable
    private PortableGridStorage<?> storage;

    PortableGrid(final EnergyStorage energyStorage,
                 final DiskInventory diskInventory,
                 final StateTrackedStorage.Listener diskListener) {
        this.energyStorage = energyStorage;
        this.diskInventory = diskInventory;
        this.diskListener = diskListener;
    }

    void updateStorage() {
        watchers.detachAll(this);
        this.storage = diskInventory.resolve(0)
            .map(diskStorage -> StateTrackedStorage.of(diskStorage, diskListener))
            .map(PortableGridStorage::new)
            .orElse(null);
        watchers.attachAll(this);
    }

    void activeChanged(final boolean active) {
        watchers.activeChanged(active);
    }

    StorageState getStorageState() {
        if (storage == null) {
            return StorageState.NONE;
        }
        if (!isGridActive()) {
            return StorageState.INACTIVE;
        }
        return storage.getState();
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        energyStorage.extract(Platform.INSTANCE.getConfig().getPortableGrid().getOpenEnergyUsage(), Action.EXECUTE);
        watchers.addWatcher(watcher, actorType, this);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        watchers.removeWatcher(watcher, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Storage<ItemResource> getItemStorage() {
        if (storage == null || storage.getStorageChannelType() != StorageChannelTypes.ITEM) {
            return new NoopStorage<>();
        }
        return (Storage<ItemResource>) storage.getStorageChannel();
    }

    @Override
    public boolean isGridActive() {
        return energyStorage.getStored() > 0 && storage != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<TrackedResourceAmount<T>> getResources(final StorageChannelType<T> type,
                                                           final Class<? extends Actor> actorType) {
        if (storage == null || storage.getStorageChannelType() != type) {
            return Collections.emptyList();
        }
        final StorageChannel<T> casted = (StorageChannel<T>) storage.getStorageChannel();
        return casted.getAll().stream().map(resource -> new TrackedResourceAmount<>(
            resource,
            casted.findTrackedResourceByActorType(resource.getResource(), actorType).orElse(null)
        )).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> GridOperations<T> createOperations(final PlatformStorageChannelType<T> storageChannelType,
                                                  final Actor actor) {
        if (storage == null || storage.getStorageChannelType() != storageChannelType) {
            return new NoopGridOperations<>();
        }
        final StorageChannel<T> casted = (StorageChannel<T>) storage.getStorageChannel();
        final GridOperations<T> operations = storageChannelType.createGridOperations(casted, actor);
        return new PortableGridOperations<>(operations, energyStorage);
    }

    @Override
    public Set<StorageChannelType<?>> getStorageChannelTypes() {
        return storage == null ? Collections.emptySet() : Set.of(storage.getStorageChannelType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> StorageChannel<T> getStorageChannel(final StorageChannelType<T> type) {
        if (storage == null || type != storage.getStorageChannelType()) {
            throw new IllegalArgumentException();
        }
        return (StorageChannel<T>) storage.getStorageChannel();
    }
}