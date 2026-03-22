package org.fuzhou.fuzhouplan.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.fuzhou.fuzhouplan.menu.FermentationBarrelMenu;

/**
 * 发酵桶GUI界面
 * 单槽设计：发酵完成后直接替换物品
 */
public class FermentationBarrelScreen extends AbstractContainerScreen<FermentationBarrelMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.tryParse("fuzhouplan:textures/gui/container/fermentation_barrel.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int PROGRESS_BAR_X = 80;
    private static final int PROGRESS_BAR_Y = 56;
    private static final int PROGRESS_BAR_WIDTH = 16;
    private static final int PROGRESS_BAR_HEIGHT = 3;

    public FermentationBarrelScreen(FermentationBarrelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(CONTAINER_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int fermentProgress = menu.getData().get(0);
        int fermentTime = menu.getData().get(1);

        if (fermentTime > 0) {
            int progressWidth = (int) (((float) fermentProgress / fermentTime) * PROGRESS_BAR_WIDTH);
            if (progressWidth > 0) {
                guiGraphics.fill(
                    this.leftPos + PROGRESS_BAR_X,
                    this.topPos + PROGRESS_BAR_Y,
                    this.leftPos + PROGRESS_BAR_X + progressWidth,
                    this.topPos + PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT,
                    0xFF4CAF50
                );
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        
        int fermentProgress = menu.getData().get(0);
        int fermentTime = menu.getData().get(1);
        
        if (fermentTime > 0) {
            int percent = (int) (((float) fermentProgress / fermentTime) * 100);
            guiGraphics.drawCenteredString(this.font, Component.literal(percent + "%"), 88, 52, 0xFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
