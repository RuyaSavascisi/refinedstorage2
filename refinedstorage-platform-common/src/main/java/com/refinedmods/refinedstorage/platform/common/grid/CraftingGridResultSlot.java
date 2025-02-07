package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

class CraftingGridResultSlot extends ResultSlot {
    private final CraftingGridSource source;

    CraftingGridResultSlot(final Player player,
                           final CraftingGridSource source,
                           final int x,
                           final int y) {
        super(player, source.getCraftingMatrix(), source.getCraftingResult(), 0, x, y);
        this.source = source;
    }

    @SuppressWarnings("resource")
    public ItemStack onQuickCraft(final Player player) {
        if (!hasItem() || player.level().isClientSide()) {
            return ItemStack.EMPTY;
        }
        final ItemStack singleResultStack = getItem().copy();
        final int maxCrafted = singleResultStack.getMaxStackSize();
        int crafted = 0;
        try (CraftingGridRefillContext refillContext = source.openSnapshotRefillContext(player)) {
            while (ItemStack.isSameItemSameComponents(singleResultStack, getItem()) && crafted < maxCrafted) {
                doTake(player, refillContext);
                crafted += singleResultStack.getCount();
            }
        }
        return singleResultStack.copyWithCount(crafted);
    }

    @Override
    @SuppressWarnings("resource")
    public void onTake(final Player player, final ItemStack stack) {
        if (player.level().isClientSide()) {
            return;
        }
        try (CraftingGridRefillContext refillContext = source.openRefillContext()) {
            doTake(player, refillContext);
        }
    }

    private void doTake(final Player player, final CraftingGridRefillContext refillContext) {
        fireCraftingEvents(player, getItem().copy());
        final CraftingInput.Positioned positioned = source.getCraftingMatrix().asPositionedCraftInput();
        final CraftingInput input = positioned.input();
        final int left = positioned.left();
        final int top = positioned.top();
        final NonNullList<ItemStack> remainingItems = source.getRemainingItems(player, input);
        for (int y = 0; y < input.height(); ++y) {
            for (int x = 0; x < input.width(); ++x) {
                final int index = x + left + (y + top) * source.getCraftingMatrix().getWidth();
                final ItemStack matrixStack = source.getCraftingMatrix().getItem(index);
                final ItemStack remainingItem = remainingItems.get(x + y * input.width());
                if (!remainingItem.isEmpty()) {
                    useIngredientWithRemainingItem(player, index, remainingItem);
                } else if (!matrixStack.isEmpty()) {
                    useIngredient(player, refillContext, index, matrixStack);
                }
            }
        }
        source.getCraftingMatrix().changed();
    }

    private void useIngredientWithRemainingItem(final Player player,
                                                final int index,
                                                final ItemStack remainingItem) {
        final ItemStack matrixStack = decrementMatrixSlot(index);
        if (matrixStack.isEmpty()) {
            source.getCraftingMatrix().setItem(index, remainingItem);
        } else if (ItemStack.isSameItemSameComponents(matrixStack, remainingItem)) {
            remainingItem.grow(matrixStack.getCount());
            source.getCraftingMatrix().setItem(index, remainingItem);
        } else if (!player.getInventory().add(remainingItem)) {
            player.drop(remainingItem, false);
        }
    }

    private void useIngredient(final Player player,
                               final CraftingGridRefillContext refillContext,
                               final int index,
                               final ItemStack matrixStack) {
        if (matrixStack.getCount() > 1 || !refillContext.extract(ItemResource.ofItemStack(matrixStack), player)) {
            decrementMatrixSlot(index);
        }
    }

    private ItemStack decrementMatrixSlot(final int index) {
        final CraftingMatrix matrix = source.getCraftingMatrix();
        matrix.removeItem(index, 1);
        return matrix.getItem(index);
    }

    private void fireCraftingEvents(final Player player, final ItemStack crafted) {
        // reimplementation of checkTakeAchievements
        crafted.onCraftedBy(player.level(), player, crafted.getCount());
        Platform.INSTANCE.onItemCrafted(player, crafted, source.getCraftingMatrix());
        if (container instanceof RecipeCraftingHolder recipeHolder) {
            recipeHolder.awardUsedRecipes(player, List.of(crafted));
        }
    }
}
