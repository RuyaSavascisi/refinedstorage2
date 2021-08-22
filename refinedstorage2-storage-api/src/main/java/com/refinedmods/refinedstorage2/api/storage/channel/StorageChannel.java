package com.refinedmods.refinedstorage2.api.storage.channel;

import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Optional;

public interface StorageChannel<T> extends Storage<T> {
    void addListener(StackListListener<T> listener);

    void removeListener(StackListListener<T> listener);

    Optional<T> extract(T template, long amount, Source source);

    Optional<T> insert(T template, long amount, Source source);

    StorageTracker<T, ?> getTracker();

    Optional<T> get(T template);

    void sortSources();

    void addSource(Storage<?> source);

    void removeSource(Storage<?> source);

    void invalidate();
}