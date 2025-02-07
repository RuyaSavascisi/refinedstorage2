package com.refinedmods.refinedstorage.platform.common.grid.view;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.view.AbstractPlatformGridResource;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResourceRendering;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.MouseClientTooltipComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class FluidGridResource extends AbstractPlatformGridResource {
    private final FluidResource fluidResource;
    private final int id;

    public FluidGridResource(final ResourceAmount resourceAmount,
                             final String name,
                             final String modId,
                             final String modName,
                             final Set<String> tags,
                             final String tooltip) {
        super(resourceAmount, name, Map.of(
            GridResourceAttributeKeys.MOD_ID, Set.of(modId),
            GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            GridResourceAttributeKeys.TAGS, tags,
            GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.fluidResource = (FluidResource) resourceAmount.getResource();
        this.id = BuiltInRegistries.FLUID.getId(fluidResource.fluid());
    }

    @Override
    public int getRegistryId() {
        return id;
    }

    @Override
    public List<ClientTooltipComponent> getExtractionHints() {
        return Platform.INSTANCE.getFilledBucket(fluidResource).map(bucket -> MouseClientTooltipComponent.item(
            MouseClientTooltipComponent.Type.LEFT,
            bucket,
            null
        )).stream().toList();
    }

    @Nullable
    @Override
    public PlatformResourceKey getUnderlyingResource() {
        return fluidResource;
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(fluidResource, extractMode, cursor);
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        // no-op
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        Platform.INSTANCE.getFluidRenderer().render(graphics.pose(), x, y, fluidResource);
    }

    @Override
    public String getDisplayedAmount() {
        return FluidResourceRendering.formatWithUnits(getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return FluidResourceRendering.format(getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(fluidResource);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage() {
        return Optional.empty();
    }
}
