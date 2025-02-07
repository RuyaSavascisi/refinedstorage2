package com.refinedmods.refinedstorage.platform.common.grid.view;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.view.AbstractPlatformGridResource;
import com.refinedmods.refinedstorage.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.MouseClientTooltipComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGridResource extends AbstractPlatformGridResource {
    private final int id;
    private final ItemStack itemStack;
    private final ItemResource itemResource;

    public ItemGridResource(final ResourceAmount resourceAmount,
                            final ItemStack itemStack,
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
        this.itemResource = (ItemResource) resourceAmount.getResource();
        this.id = Item.getId(itemResource.item());
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    @Override
    public PlatformResourceKey getUnderlyingResource() {
        return itemResource;
    }

    @Override
    public int getRegistryId() {
        return id;
    }

    @Override
    public List<ClientTooltipComponent> getExtractionHints() {
        final long extractableAmount = Math.min(getAmount(), itemStack.getMaxStackSize());
        final long halfExtractionAmount = extractableAmount == 1 ? 1 : extractableAmount / 2;
        return List.of(
            MouseClientTooltipComponent.itemWithDecorations(
                MouseClientTooltipComponent.Type.LEFT,
                itemStack,
                extractableAmount == 1 ? null : AmountFormatting.format(extractableAmount)
            ),
            MouseClientTooltipComponent.itemWithDecorations(
                MouseClientTooltipComponent.Type.RIGHT,
                itemStack,
                halfExtractionAmount == 1 ? null : AmountFormatting.format(halfExtractionAmount)
            )
        );
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(itemResource, extractMode, cursor);
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        scrollingStrategy.onScroll(itemResource, scrollMode, -1);
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        final Font font = Minecraft.getInstance().font;
        graphics.renderItem(itemStack, x, y);
        graphics.renderItemDecorations(font, itemStack, x, y, null);
    }

    @Override
    public String getDisplayedAmount() {
        return AmountFormatting.formatWithUnits(getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return AmountFormatting.format(getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        final Minecraft minecraft = Minecraft.getInstance();
        return Screen.getTooltipFromItem(minecraft, itemStack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage() {
        return itemStack.getTooltipImage();
    }
}
