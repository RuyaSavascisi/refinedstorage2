package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeState;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class FluidBreakDestructorStrategyFactory implements DestructorStrategyFactory {
    @Override
    public Optional<DestructorStrategy> create(final ServerLevel level,
                                               final BlockPos pos,
                                               final Direction direction,
                                               final UpgradeState upgradeState,
                                               final boolean pickupItems) {
        return Optional.of(new FluidBreakDestructorStrategy(level, pos));
    }
}