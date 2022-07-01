package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractSideButtonWidget extends Button implements Button.OnTooltip {
    public static final ResourceLocation DEFAULT_TEXTURE = createIdentifier("textures/icons.png");

    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;

    protected AbstractSideButtonWidget(final OnPress pressAction) {
        super(-1, -1, WIDTH, HEIGHT, Component.empty(), pressAction);
    }

    protected abstract int getXTexture();

    protected abstract int getYTexture();

    protected ResourceLocation getTextureIdentifier() {
        return DEFAULT_TEXTURE;
    }

    @Override
    public void renderButton(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getTextureIdentifier());
        RenderSystem.enableDepthTest();

        this.isHovered = mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;

        // Ensure that the tooltip is drawn over the side buttons (tooltips have a Z offset of 400).
        final int originalZOffset = getBlitOffset();
        setBlitOffset(300);
        blit(poseStack, x, y, 238, isHovered ? 35 : 16, WIDTH, HEIGHT);
        blit(poseStack, x + 1, y + 1, getXTexture(), getYTexture(), WIDTH, HEIGHT);

        if (isHovered) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
            blit(poseStack, x, y, 238, 54, WIDTH, HEIGHT);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        setBlitOffset(originalZOffset);

        if (isHovered) {
            onTooltip(this, poseStack, mouseX, mouseY);
        }

        RenderSystem.disableDepthTest();
    }
}