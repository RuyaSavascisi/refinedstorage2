package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.AbstractPlatformGridResource;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

public class FluidGridResource extends AbstractPlatformGridResource {
    private final FluidResource fluidResource;
    private final int id;

    @SuppressWarnings("deprecation") // forge deprecates Registry access
    public FluidGridResource(final ResourceAmount<FluidResource> resourceAmount,
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
        this.id = BuiltInRegistries.FLUID.getId(resourceAmount.getResource().fluid());
        this.fluidResource = resourceAmount.getResource();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void render(final PoseStack poseStack, final int slotX, final int slotY) {
        Platform.INSTANCE.getFluidRenderer().render(
            poseStack,
            slotX,
            slotY,
            0,
            fluidResource
        );
    }

    @Override
    public String getAmount() {
        return Platform.INSTANCE.getBucketQuantityFormatter().formatWithUnits(getResourceAmount().getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return Platform.INSTANCE.getBucketQuantityFormatter().format(getResourceAmount().getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        return Platform.INSTANCE.getFluidRenderer().getTooltip(fluidResource);
    }
}
