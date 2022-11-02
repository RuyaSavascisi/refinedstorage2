package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.storage.Storage;

public abstract class AbstractImmutableConfiguredProxyStorage<T, S extends Storage<T>>
    extends AbstractConfiguredProxyStorage<T, S> {
    private static final String ERROR_MESSAGE = "Cannot modify delegate, it's immutable";

    protected AbstractImmutableConfiguredProxyStorage(final StorageConfiguration config) {
        super(config);
    }

    @Override
    public void setDelegate(final S newDelegate) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public void clearDelegate() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }
}