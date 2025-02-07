package com.refinedmods.refinedstorage.api.grid.operations;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.api.storage.TransferHelper;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;

import java.util.function.ToLongFunction;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class GridOperationsImpl implements GridOperations {
    private final StorageChannel storageChannel;
    private final Actor actor;
    private final ToLongFunction<ResourceKey> maxAmountProvider;
    private final long singleAmount;

    /**
     * @param storageChannel    the storage channel to act on
     * @param actor             the actor performing the grid operations
     * @param maxAmountProvider provider for the maximum amount of a given resource
     * @param singleAmount      amount that needs to be extracted when using
     *                          {@link GridInsertMode#SINGLE_RESOURCE} or {@link GridExtractMode#SINGLE_RESOURCE}
     */
    public GridOperationsImpl(final StorageChannel storageChannel,
                              final Actor actor,
                              final ToLongFunction<ResourceKey> maxAmountProvider,
                              final long singleAmount) {
        this.storageChannel = storageChannel;
        this.actor = actor;
        this.maxAmountProvider = maxAmountProvider;
        this.singleAmount = singleAmount;
    }

    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        final long amount = getExtractableAmount(resource, extractMode);
        if (amount == 0) {
            return false;
        }
        return TransferHelper.transfer(resource, amount, actor, storageChannel, destination, storageChannel) > 0;
    }

    private long getExtractableAmount(final ResourceKey resource, final GridExtractMode extractMode) {
        final long extractableAmount = getExtractableAmount(resource);
        return adjustExtractableAmountAccordingToExtractMode(extractMode, extractableAmount);
    }

    private long getExtractableAmount(final ResourceKey resource) {
        final long totalSize = storageChannel.get(resource).map(ResourceAmount::getAmount).orElse(0L);
        final long maxAmount = maxAmountProvider.applyAsLong(resource);
        return Math.min(totalSize, maxAmount);
    }

    private long adjustExtractableAmountAccordingToExtractMode(final GridExtractMode extractMode,
                                                               final long extractableAmount) {
        return switch (extractMode) {
            case ENTIRE_RESOURCE -> extractableAmount;
            case HALF_RESOURCE -> extractableAmount == 1 ? 1 : extractableAmount / 2;
            case SINGLE_RESOURCE -> Math.min(singleAmount, extractableAmount);
        };
    }

    @Override
    public boolean insert(final ResourceKey resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        final long amount = switch (insertMode) {
            case ENTIRE_RESOURCE -> maxAmountProvider.applyAsLong(resource);
            case SINGLE_RESOURCE -> singleAmount;
        };
        return TransferHelper.transfer(resource, amount, actor, source, storageChannel, null) > 0;
    }
}
