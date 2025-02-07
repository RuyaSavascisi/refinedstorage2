package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.platform.common.Platform;

class PortableGridOperations implements GridOperations {
    private final GridOperations delegate;
    private final EnergyStorage energyStorage;

    PortableGridOperations(final GridOperations delegate, final EnergyStorage energyStorage) {
        this.delegate = delegate;
        this.energyStorage = energyStorage;
    }

    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        if (delegate.extract(resource, extractMode, destination)) {
            energyStorage.extract(
                Platform.INSTANCE.getConfig().getPortableGrid().getExtractEnergyUsage(),
                Action.EXECUTE
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean insert(final ResourceKey resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        if (delegate.insert(resource, insertMode, source)) {
            energyStorage.extract(
                Platform.INSTANCE.getConfig().getPortableGrid().getInsertEnergyUsage(),
                Action.EXECUTE
            );
            return true;
        }
        return false;
    }
}
