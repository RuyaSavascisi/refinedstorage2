package com.refinedmods.refinedstorage.api.storage.external;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.resource.list.ResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.composite.CompositeAwareChild;
import com.refinedmods.refinedstorage.api.storage.composite.ParentComposite;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class ExternalStorage implements CompositeAwareChild {
    private final ExternalStorageProvider provider;
    private final Set<ParentComposite> parents = new HashSet<>();
    private final ResourceList cache = new ResourceListImpl();
    private final ExternalStorageListener listener;

    public ExternalStorage(final ExternalStorageProvider provider, final ExternalStorageListener listener) {
        this.provider = provider;
        this.listener = listener;
    }

    public ExternalStorageProvider getProvider() {
        return provider;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long extracted = provider.extract(resource, amount, action, actor);
        if (action == Action.EXECUTE && extracted > 0) {
            listener.beforeDetectChanges(resource, actor);
            detectChanges();
        }
        return extracted;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final long inserted = provider.insert(resource, amount, action, actor);
        if (action == Action.EXECUTE && inserted > 0) {
            listener.beforeDetectChanges(resource, actor);
            detectChanges();
        }
        return inserted;
    }

    public boolean detectChanges() {
        final ResourceList updatedCache = buildCache();
        boolean hasChanges = detectCompleteRemovals(updatedCache);
        hasChanges |= detectAdditionsAndPartialRemovals(updatedCache);
        return hasChanges;
    }

    private boolean detectCompleteRemovals(final ResourceList updatedCache) {
        final Set<ResourceAmount> removedInUpdatedCache = new HashSet<>();
        for (final ResourceAmount inOldCache : cache.getAll()) {
            final Optional<ResourceAmount> inUpdatedCache = updatedCache.get(inOldCache.getResource());
            if (inUpdatedCache.isEmpty()) {
                removedInUpdatedCache.add(inOldCache);
            }
        }
        removedInUpdatedCache.forEach(removed -> removeFromCache(removed.getResource(), removed.getAmount()));
        return !removedInUpdatedCache.isEmpty();
    }

    private boolean detectAdditionsAndPartialRemovals(final ResourceList updatedCache) {
        boolean hasChanges = false;
        for (final ResourceAmount inUpdatedCache : updatedCache.getAll()) {
            final Optional<ResourceAmount> inOldCache = cache.get(inUpdatedCache.getResource());
            final boolean doesNotExistInOldCache = inOldCache.isEmpty();
            if (doesNotExistInOldCache) {
                addToCache(inUpdatedCache.getResource(), inUpdatedCache.getAmount());
                hasChanges = true;
            } else {
                hasChanges |= detectPotentialDifference(inUpdatedCache, inOldCache.get());
            }
        }
        return hasChanges;
    }

    private boolean detectPotentialDifference(final ResourceAmount inUpdatedCache,
                                              final ResourceAmount inOldCache) {
        final ResourceKey resource = inUpdatedCache.getResource();
        final long diff = inUpdatedCache.getAmount() - inOldCache.getAmount();
        if (diff > 0) {
            addToCache(resource, diff);
            return true;
        } else if (diff < 0) {
            removeFromCache(resource, Math.abs(diff));
            return true;
        }
        return false;
    }

    private void addToCache(final ResourceKey resource, final long amount) {
        cache.add(resource, amount);
        parents.forEach(parent -> parent.addToCache(resource, amount));
    }

    private void removeFromCache(final ResourceKey resource, final long amount) {
        cache.remove(resource, amount);
        parents.forEach(parent -> parent.removeFromCache(resource, amount));
    }

    private ResourceList buildCache() {
        final ResourceList list = new ResourceListImpl();
        provider.iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        return cache.getAll();
    }

    @Override
    public long getStored() {
        return getAll().stream().mapToLong(ResourceAmount::getAmount).sum();
    }

    @Override
    public void onAddedIntoComposite(final ParentComposite parentComposite) {
        parents.add(parentComposite);
    }

    @Override
    public void onRemovedFromComposite(final ParentComposite parentComposite) {
        parents.remove(parentComposite);
    }

    @Override
    public Amount compositeInsert(final ResourceKey resource,
                                  final long amount,
                                  final Action action,
                                  final Actor actor) {
        final long inserted = insert(resource, amount, action, actor);
        return new Amount(inserted, 0);
    }

    @Override
    public Amount compositeExtract(final ResourceKey resource,
                                   final long amount,
                                   final Action action,
                                   final Actor actor) {
        final long extracted = extract(resource, amount, action, actor);
        return new Amount(extracted, 0);
    }
}
