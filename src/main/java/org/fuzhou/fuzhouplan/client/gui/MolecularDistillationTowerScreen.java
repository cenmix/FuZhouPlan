package org.fuzhou.fuzhouplan.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;

public class MolecularDistillationTowerScreen extends AbstractContainerScreen<MolecularDistillationTowerMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.tryParse("fuzhouplan:textures/gui/container/molecular_distillation_tower.png");

    private static final int ENERGY_BAR_X = 13;
    private static final int ENERGY_BAR_Y = 20;
    private static final int ENERGY_BAR_WIDTH = 14;
    private static final int ENERGY_BAR_HEIGHT = 42;

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

        int processingProgress = menu.getData().get(0);
        int totalProcessingTime = menu.getData().get(1);
        int energyStored = menu.getData().get(2);
        int maxEnergy = menu.getData().get(3);

        renderEnergyBar(guiGraphics, energyStored, maxEnergy);
        renderProgressBar(guiGraphics, processingProgress, totalProcessingTime);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int energyStored, int maxEnergy) {
        if (maxEnergy > 0) {
            int fillHeight = (int) ((energyStored / (double) maxEnergy) * ENERGY_BAR_HEIGHT);
            int actualX = this.leftPos + ENERGY_BAR_X;
            int actualY = this.topPos + ENERGY_BAR_Y;

            guiGraphics.fill(actualX, actualY, actualX + ENERGY_BAR_WIDTH, actualY + ENERGY_BAR_HEIGHT, 0xFF333333);

            if (fillHeight > 0) {
                guiGraphics.fill(actualX, actualY + ENERGY_BAR_HEIGHT - fillHeight,
                                actualX + ENERGY_BAR_WIDTH, actualY + ENERGY_BAR_HEIGHT, 0xFF00FF00);
            }

            guiGraphics.renderOutline(actualX - 1, actualY - 1, ENERGY_BAR_WIDTH + 2, ENERGY_BAR_HEIGHT + 2, 0xFF000000);
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int progress, int totalTime) {
        if (totalTime > 0 && progress > 0) {
            int progressWidth = (int) ((progress / (double) totalTime) * PROGRESS_ARROW_WIDTH);

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
        renderEnergyTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderEnergyTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int actualX = this.leftPos + ENERGY_BAR_X;
        int actualY = this.topPos + ENERGY_BAR_Y;

        if (mouseX >= actualX && mouseX < actualX + ENERGY_BAR_WIDTH &&
            mouseY >= actualY && mouseY < actualY + ENERGY_BAR_HEIGHT) {

            int energyStored = menu.getData().get(2);
            int maxEnergy = menu.getData().get(3);

            Component tooltip = Component.literal(energyStored + " / " + maxEnergy + " FE");
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
