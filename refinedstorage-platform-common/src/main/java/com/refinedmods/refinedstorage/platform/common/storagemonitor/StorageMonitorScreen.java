package com.refinedmods.refinedstorage.platform.common.storagemonitor;

import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.platform.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class StorageMonitorScreen extends AbstractBaseScreen<StorageMonitorContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/storage_monitor.png");

    public StorageMonitorScreen(final StorageMonitorContainerMenu menu,
                                final Inventory playerInventory,
                                final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 43;
        this.imageWidth = 211;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.GENERIC
        ));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
