package com.refinedmods.refinedstorage.platform.common.grid.screen.hint;

import com.refinedmods.refinedstorage.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.platform.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResourceRendering;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.MouseClientTooltipComponent;

import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class FluidGridInsertionHint implements GridInsertionHint {
    @Override
    public Optional<ClientTooltipComponent> getHint(final ItemStack carried) {
        return Platform.INSTANCE.drainContainer(carried).map(this::createComponent);
    }

    private ClientTooltipComponent createComponent(final FluidOperationResult result) {
        return MouseClientTooltipComponent.fluid(
            MouseClientTooltipComponent.Type.RIGHT,
            (FluidResource) result.fluid(),
            result.amount() == Platform.INSTANCE.getBucketAmount()
                ? null
                : FluidResourceRendering.format(result.amount())
        );
    }
}
