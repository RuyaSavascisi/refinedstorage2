package com.refinedmods.refinedstorage.platform.fabric.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;

import java.util.Optional;
import java.util.function.Function;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FabricStoragePlatformExternalStorageProviderFactory<T>
    implements PlatformExternalStorageProviderFactory {
    private final BlockApiLookup<Storage<T>, Direction> lookup;
    private final Function<T, ResourceKey> fromPlatformMapper;
    private final Function<ResourceKey, T> toPlatformMapper;

    public FabricStoragePlatformExternalStorageProviderFactory(final BlockApiLookup<Storage<T>, Direction> lookup,
                                                               final Function<T, ResourceKey> fromPlatformMapper,
                                                               final Function<ResourceKey, T> toPlatformMapper) {
        this.lookup = lookup;
        this.fromPlatformMapper = fromPlatformMapper;
        this.toPlatformMapper = toPlatformMapper;
    }

    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction) {
        return Optional.of(new FabricStorageExternalStorageProvider<>(
            lookup,
            fromPlatformMapper,
            toPlatformMapper,
            level,
            pos,
            direction
        ));
    }
}
