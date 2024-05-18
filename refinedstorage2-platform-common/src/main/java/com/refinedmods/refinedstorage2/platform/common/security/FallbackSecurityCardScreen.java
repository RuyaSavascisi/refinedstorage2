package com.refinedmods.refinedstorage2.platform.common.security;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FallbackSecurityCardScreen extends AbstractSecurityCardScreen<FallbackSecurityCardContainerMenu> {
    public FallbackSecurityCardScreen(final FallbackSecurityCardContainerMenu menu,
                                      final Inventory playerInventory,
                                      final Component text) {
        super(menu, playerInventory, text);
    }
}