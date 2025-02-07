package com.refinedmods.refinedstorage.platform.api.grid.view;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface PlatformGridResource extends GridResource {
    void onExtract(GridExtractMode extractMode,
                   boolean cursor,
                   GridExtractionStrategy extractionStrategy);

    void onScroll(GridScrollMode scrollMode,
                  GridScrollingStrategy scrollingStrategy);

    void render(GuiGraphics graphics, int x, int y);

    String getDisplayedAmount();

    String getAmountInTooltip();

    List<Component> getTooltip();

    Optional<TooltipComponent> getTooltipImage();

    int getRegistryId();

    List<ClientTooltipComponent> getExtractionHints();

    @Nullable
    @API(status = API.Status.INTERNAL)
    PlatformResourceKey getUnderlyingResource();
}
