package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.ConstructorStrategy;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class PlaceFluidConstructorStrategy implements ConstructorStrategy {
    protected final ServerLevel level;
    protected final BlockPos pos;
    protected final Direction direction;

    public PlaceFluidConstructorStrategy(final ServerLevel level, final BlockPos pos, final Direction direction) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
    }

    @Override
    public boolean apply(
        final ResourceKey resource,
        final Actor actor,
        final Player actingPlayer,
        final Network network
    ) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        if (!(resource instanceof FluidResource fluidResource)) {
            return false;
        }
        final StorageChannel storageChannel = network.getComponent(StorageNetworkComponent.class);
        final long bucketAmount = Platform.INSTANCE.getBucketAmount();
        final long extractedAmount = storageChannel.extract(
            fluidResource,
            bucketAmount,
            Action.SIMULATE,
            actor
        );
        if (bucketAmount != extractedAmount) {
            return false;
        }
        final boolean success = Platform.INSTANCE.placeFluid(level, pos, direction, actingPlayer, fluidResource);
        if (success) {
            storageChannel.extract(fluidResource, bucketAmount, Action.EXECUTE, actor);
        }
        return success;
    }
}
