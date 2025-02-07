package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface TransferDestination {
    @Nullable
    ItemStack transfer(ItemStack stack);
}
