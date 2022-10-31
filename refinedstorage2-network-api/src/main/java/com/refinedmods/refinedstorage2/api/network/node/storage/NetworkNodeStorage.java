package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.network.node.AbstractConfiguredProxyStorage;
import com.refinedmods.refinedstorage2.api.network.node.StorageConfiguration;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage2.api.storage.composite.ParentComposite;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class NetworkNodeStorage<T> extends AbstractConfiguredProxyStorage<T, Storage<T>>
    implements TrackedStorage<T>, CompositeAwareChild<T> {
    private final Set<ParentComposite<T>> parentComposites = new HashSet<>();

    NetworkNodeStorage(final StorageConfiguration config) {
        super(config);
    }

    @Override
    public Optional<TrackedResource> findTrackedResourceByActorType(final T resource,
                                                                    final Class<? extends Actor> actorType) {
        return delegate instanceof TrackedStorage<T> trackedStorage
            ? trackedStorage.findTrackedResourceByActorType(resource, actorType)
            : Optional.empty();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite<T> parentComposite) {
        parentComposites.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite<T> parentComposite) {
        parentComposites.remove(parentComposite);
    }

    public long getCapacity() {
        return delegate instanceof LimitedStorage<?> limitedStorage ? limitedStorage.getCapacity() : 0L;
    }

    public void setSource(final Storage<T> source) {
        CoreValidations.validateNotNull(source, "Source cannot be null");
        this.delegate = source;
        parentComposites.forEach(parentComposite -> parentComposite.onSourceAddedToChild(delegate));
    }

    public void removeSource() {
        CoreValidations.validateNotNull(this.delegate, "Cannot remove source when no source was present");
        parentComposites.forEach(parentComposite -> parentComposite.onSourceRemovedFromChild(this.delegate));
        this.delegate = null;
    }
}
