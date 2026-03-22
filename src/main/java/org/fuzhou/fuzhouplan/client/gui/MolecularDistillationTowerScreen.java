package org.fuzhou.fuzhouplan.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;

/**
 * 分子蒸馏塔GUI界面
 * 
 * 显示内容：
 * - 能量条（左侧）
 * - 处理进度条（中央）
 * - 输入槽（蒸馏水）
 * - 输出槽（无核酸酶水瓶）
 */
public class MolecularDistillationTowerScreen extends AbstractContainerScreen<MolecularDistillationTowerMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.tryParse("textures/gui/container/furnace.png");

    // 能量条参数
    private static final int ENERGY_BAR_X = 13;
    private static final int ENERGY_BAR_Y = 20;
    private static final int ENERGY_BAR_WIDTH = 14;
    private static final int ENERGY_BAR_HEIGHT = 42;

    // 进度条参数（箭头位置）
    private static final int PROGRESS_ARROW_X = 79;
    private static final int PROGRESS_ARROW_Y = 35;
    private static final int PROGRESS_ARROW_WIDTH = 24;
    private static final int PROGRESS_ARROW_HEIGHT = 16;

    public MolecularDistillationTowerScreen(MolecularDistillationTowerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(CONTAINER_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // 获取数据
        int energyStored = menu.getData().get(0);
        int maxEnergy = menu.getData().get(1);
        int processingProgress = menu.getData().get(2);
        int totalProcessingTime = menu.getData().get(3);

        // 渲染能量条
        renderEnergyBar(guiGraphics, energyStored, maxEnergy);

        // 渲染进度条（箭头）
        renderProgressBar(guiGraphics, processingProgress, totalProcessingTime);
    }

    /**
     * 渲染能量条
     */
    private void renderEnergyBar(GuiGraphics guiGraphics, int energyStored, int maxEnergy) {
        if (maxEnergy > 0) {
            int fillHeight = (int) ((energyStored / (double) maxEnergy) * ENERGY_BAR_HEIGHT);
            int actualX = this.leftPos + ENERGY_BAR_X;
            int actualY = this.topPos + ENERGY_BAR_Y;

            // 背景
            guiGraphics.fill(actualX, actualY, actualX + ENERGY_BAR_WIDTH, actualY + ENERGY_BAR_HEIGHT, 0xFF333333);
            
            // 填充（从下往上）
            if (fillHeight > 0) {
                int fillColor = 0xFF00FF00; // 绿色表示能量
                guiGraphics.fill(actualX, actualY + ENERGY_BAR_HEIGHT - fillHeight, 
                                actualX + ENERGY_BAR_WIDTH, actualY + ENERGY_BAR_HEIGHT, fillColor);
            }

            // 边框
            guiGraphics.renderOutline(actualX - 1, actualY - 1, ENERGY_BAR_WIDTH + 2, ENERGY_BAR_HEIGHT + 2, 0xFF000000);
        }
    }

    /**
     * 渲染进度条（使用箭头样式）
     */
    private void renderProgressBar(GuiGraphics guiGraphics, int progress, int totalTime) {
        if (totalTime > 0 && progress > 0) {
            int progressWidth = (int) ((progress / (double) totalTime) * PROGRESS_ARROW_WIDTH);
            
            // 使用熔炉的箭头纹理
            // 箭头进度从左到右填充
            guiGraphics.blit(CONTAINER_BACKGROUND, 
                    this.leftPos + PROGRESS_ARROW_X, this.topPos + PROGRESS_ARROW_Y,
                    176, 14, progressWidth, PROGRESS_ARROW_HEIGHT);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // 渲染能量条的悬停提示
        renderEnergyTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 渲染能量条的悬停提示
     */
    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int actualX = this.leftPos + ENERGY_BAR_X;
        int actualY = this.topPos + ENERGY_BAR_Y;

        if (mouseX >= actualX && mouseX < actualX + ENERGY_BAR_WIDTH &&
            mouseY >= actualY && mouseY < actualY + ENERGY_BAR_HEIGHT) {
            
            int energyStored = menu.getData().get(0);
            int maxEnergy = menu.getData().get(1);
            
            Component tooltip = Component.literal(energyStored + " / " + maxEnergy + " FE");
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
