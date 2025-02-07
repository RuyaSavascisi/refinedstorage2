package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.platform.common.support.TextureIds;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractGridSynchronizer implements GridSynchronizer {
    @Override
    public ResourceLocation getTextureIdentifier() {
        return TextureIds.ICONS;
    }

    @Override
    public int getYTexture() {
        return 96;
    }
}
