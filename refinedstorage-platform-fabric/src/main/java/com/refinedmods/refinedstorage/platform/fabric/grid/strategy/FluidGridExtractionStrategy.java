package com.refinedmods.refinedstorage.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.storage.EmptyActor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.platform.fabric.util.SimpleSingleStackStorage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;

import static com.refinedmods.refinedstorage.platform.fabric.support.resource.VariantUtil.toFluidVariant;

public class FluidGridExtractionStrategy implements GridExtractionStrategy {
    private static final ItemVariant BUCKET_ITEM_VARIANT = ItemVariant.of(Items.BUCKET);
    private static final ItemResource BUCKET_ITEM_RESOURCE = new ItemResource(Items.BUCKET);

    private final GridOperations gridOperations;
    private final PlayerInventoryStorage playerInventoryStorage;
    private final net.fabricmc.fabric.api.transfer.v1.storage.Storage<ItemVariant> playerCursorStorage;
    private final Storage itemStorage;

    public FluidGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                       final ServerPlayer player,
                                       final Grid grid) {
        this.gridOperations = grid.createOperations(ResourceTypes.FLUID, player);
        this.playerInventoryStorage = PlayerInventoryStorage.of(player.getInventory());
        this.playerCursorStorage = PlayerInventoryStorage.getCursorStorage(containerMenu);
        this.itemStorage = grid.getItemStorage();
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (resource instanceof FluidResource fluidResource) {
            final boolean bucketInInventory = hasBucketInInventory();
            final boolean bucketInStorageChannel = hasBucketInStorage();
            if (bucketInInventory) {
                extractWithBucketInInventory(fluidResource, extractMode, cursor);
            } else if (bucketInStorageChannel) {
                extractWithBucketInStorage(fluidResource, extractMode, cursor);
            }
            return true;
        }
        return false;
    }

    private void extractWithBucketInStorage(final FluidResource fluidResource,
                                            final GridExtractMode mode,
                                            final boolean cursor) {
        final SimpleSingleStackStorage interceptingStorage = SimpleSingleStackStorage.forEmptyBucket();
        final net.fabricmc.fabric.api.transfer.v1.storage.Storage<FluidVariant> destination = FluidStorage.ITEM.find(
            interceptingStorage.getStack(),
            ContainerItemContext.ofSingleSlot(interceptingStorage)
        );
        if (destination == null) {
            return;
        }
        gridOperations.extract(fluidResource, mode, (resource, amount, action, source) -> {
            if (!(resource instanceof FluidResource fluidResource2)) {
                return 0;
            }
            try (Transaction tx = Transaction.openOuter()) {
                final long inserted = destination.insert(toFluidVariant(fluidResource2), amount, tx);
                final boolean couldInsertBucket = insertResultingBucketIntoInventory(interceptingStorage, cursor, tx);
                if (!couldInsertBucket) {
                    return 0;
                }
                if (action == Action.EXECUTE) {
                    itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.EXECUTE, source);
                    tx.commit();
                }
                return inserted;
            }
        });
    }

    private void extractWithBucketInInventory(final FluidResource fluidResource,
                                              final GridExtractMode mode,
                                              final boolean cursor) {
        try (Transaction tx = Transaction.openOuter()) {
            playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx);
            final SimpleSingleStackStorage interceptingStorage = SimpleSingleStackStorage.forEmptyBucket();
            final net.fabricmc.fabric.api.transfer.v1.storage.Storage<FluidVariant> dest = FluidStorage.ITEM.find(
                interceptingStorage.getStack(),
                ContainerItemContext.ofSingleSlot(interceptingStorage)
            );
            if (dest == null) {
                return;
            }
            gridOperations.extract(fluidResource, mode, (resource, amount, action, source) -> {
                if (!(resource instanceof FluidResource fluidResource2)) {
                    return 0;
                }
                try (Transaction innerTx = tx.openNested()) {
                    final long inserted = dest.insert(toFluidVariant(fluidResource2), amount, innerTx);
                    final boolean couldInsertBucket = insertResultingBucketIntoInventory(
                        interceptingStorage,
                        cursor,
                        innerTx
                    );
                    if (!couldInsertBucket) {
                        return 0;
                    }
                    if (action == Action.EXECUTE) {
                        innerTx.commit();
                        tx.commit();
                    }
                    return inserted;
                }
            });
        }
    }

    private boolean insertResultingBucketIntoInventory(final SimpleSingleStackStorage interceptingStorage,
                                                       final boolean cursor,
                                                       final Transaction innerTx) {
        final net.fabricmc.fabric.api.transfer.v1.storage.Storage<ItemVariant> relevantStorage = cursor
            ? playerCursorStorage
            : playerInventoryStorage;
        final ItemVariant itemVariant = ItemVariant.of(interceptingStorage.getStack());
        return relevantStorage.insert(itemVariant, 1, innerTx) != 0;
    }

    private boolean hasBucketInInventory() {
        try (Transaction tx = Transaction.openOuter()) {
            return playerInventoryStorage.extract(BUCKET_ITEM_VARIANT, 1, tx) == 1;
        }
    }

    private boolean hasBucketInStorage() {
        return itemStorage.extract(BUCKET_ITEM_RESOURCE, 1, Action.SIMULATE, EmptyActor.INSTANCE) == 1;
    }
}
