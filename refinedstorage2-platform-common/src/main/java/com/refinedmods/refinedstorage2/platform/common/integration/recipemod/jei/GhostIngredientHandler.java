package com.refinedmods.refinedstorage2.platform.common.integration.recipemod.jei;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.AbstractBaseScreen;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;

@SuppressWarnings("rawtypes")
public class GhostIngredientHandler implements IGhostIngredientHandler<AbstractBaseScreen> {
    private final IngredientConverter ingredientConverter;

    public GhostIngredientHandler(final IngredientConverter ingredientConverter) {
        this.ingredientConverter = ingredientConverter;
    }

    @Override
    public <I> List<Target<I>> getTargetsTyped(final AbstractBaseScreen screen,
                                               final ITypedIngredient<I> ingredient,
                                               final boolean doStart) {
        if (screen.getMenu() instanceof AbstractResourceContainerMenu menu) {
            return getTargets(screen, ingredient.getIngredient(), menu);
        }
        return Collections.emptyList();
    }

    private <I> List<Target<I>> getTargets(final AbstractBaseScreen screen,
                                           final I ingredient,
                                           final AbstractResourceContainerMenu menu) {
        return menu.getResourceSlots().stream()
            .flatMap(slot -> ingredientConverter.convertToResource(ingredient).map(resource -> {
                final Rect2i bounds = getBounds(screen, slot);
                return new TargetImpl<I>(bounds, slot.index);
            }).stream()).collect(Collectors.toList());
    }

    private Rect2i getBounds(final AbstractBaseScreen screen, final ResourceSlot slot) {
        return new Rect2i(screen.getLeftPos() + slot.x, screen.getTopPos() + slot.y, 17, 17);
    }

    @Override
    public void onComplete() {
        // no op
    }

    private class TargetImpl<I> implements Target<I> {
        private final Rect2i area;
        private final int slotIndex;

        TargetImpl(final Rect2i area, final int slotIndex) {
            this.area = area;
            this.slotIndex = slotIndex;
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(final I ingredient) {
            ingredientConverter.convertToResource(ingredient).ifPresent(this::accept);
        }

        private <T> void accept(final ResourceTemplate<T> resource) {
            Platform.INSTANCE.getClientToServerCommunications().sendResourceFilterSlotChange(
                (PlatformStorageChannelType<T>) resource.storageChannelType(),
                resource.resource(),
                slotIndex
            );
        }
    }
}

