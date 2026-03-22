package org.fuzhou.fuzhouplan.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.fuzhou.fuzhouplan.blockentity.PrecisionStirrerBlockEntity;
import org.fuzhou.fuzhouplan.menu.PrecisionStirrerMenu;
import org.fuzhou.fuzhouplan.network.NetworkHandler;
import org.fuzhou.fuzhouplan.network.PrecisionStirrerPHAdjustPacket;

/**
 * 精密搅拌器GUI界面
 */
public class PrecisionStirrerScreen extends AbstractContainerScreen<PrecisionStirrerMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.tryParse("fuzhouplan:textures/gui/container/precision_stirrer.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int PROGRESS_BAR_X = 25;
    private static final int PROGRESS_BAR_Y = 20;
    private static final int PROGRESS_BAR_WIDTH = 12;
    private static final int PROGRESS_BAR_HEIGHT = 50;
    private static final int TARGET_BAR_X = 139;

    private static final int BUTTON_Y = 55;
    private static final int BUTTON_WIDTH = 24;
    private static final int BUTTON_HEIGHT = 12;

    private Button increaseButton;
    private Button increaseSlightlyButton;
    private Button decreaseSlightlyButton;
    private Button decreaseButton;

    public PrecisionStirrerScreen(PrecisionStirrerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int leftPos = this.leftPos;
        int topPos = this.topPos;

        increaseButton = this.addRenderableWidget(Button.builder(
                Component.literal("+20"),
                button -> sendPHAdjustPacket(20))
                .bounds(leftPos + 53, topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        increaseSlightlyButton = this.addRenderableWidget(Button.builder(
                Component.literal("+5"),
                button -> sendPHAdjustPacket(5))
                .bounds(leftPos + 53, topPos + BUTTON_Y + 14, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        decreaseSlightlyButton = this.addRenderableWidget(Button.builder(
                Component.literal("-5"),
                button -> sendPHAdjustPacket(-5))
                .bounds(leftPos + 99, topPos + BUTTON_Y + 14, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        decreaseButton = this.addRenderableWidget(Button.builder(
                Component.literal("-20"),
                button -> sendPHAdjustPacket(-20))
                .bounds(leftPos + 99, topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void sendPHAdjustPacket(int adjustment) {
        PrecisionStirrerBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            NetworkHandler.sendToServer(new PrecisionStirrerPHAdjustPacket(
                    blockEntity.getBlockPos(), adjustment));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(CONTAINER_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int playerPH = menu.getData().get(0);
        int targetPH = menu.getData().get(1);
        int successCount = menu.getData().get(2);
        boolean isProcessing = menu.getData().get(3) != 0;
        boolean gameWon = menu.getData().get(4) != 0;
        boolean gameLost = menu.getData().get(5) != 0;

        renderProgressBar(guiGraphics, PROGRESS_BAR_X, PROGRESS_BAR_Y, playerPH, 0xFF4CAF50);
        renderProgressBar(guiGraphics, TARGET_BAR_X, PROGRESS_BAR_Y, targetPH, 0xFF2196F3);

        guiGraphics.drawCenteredString(this.font, Component.literal(successCount + "/5"),
                this.leftPos + 80, this.topPos + 35, 0xFFFFFF);

        if (gameWon) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.fuzhouplan.precision_stirrer.success"),
                    this.leftPos + 80, this.topPos + 50, 0x55FF55);
        } else if (gameLost) {
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.fuzhouplan.precision_stirrer.failed"),
                    this.leftPos + 80, this.topPos + 50, 0xFF5555);
        }

        updateButtonStates(isProcessing && !gameWon && !gameLost);
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int value, int color) {
        int fillHeight = (int) ((value / 100.0) * PROGRESS_BAR_HEIGHT);

        int actualX = this.leftPos + x;
        int actualY = this.topPos + y;

        guiGraphics.fill(actualX, actualY, actualX + PROGRESS_BAR_WIDTH, actualY + PROGRESS_BAR_HEIGHT, 0xFF333333);
        guiGraphics.fill(actualX, actualY + PROGRESS_BAR_HEIGHT - fillHeight, actualX + PROGRESS_BAR_WIDTH, actualY + PROGRESS_BAR_HEIGHT, color);
        
        guiGraphics.renderOutline(actualX - 1, actualY - 1, PROGRESS_BAR_WIDTH + 2, PROGRESS_BAR_HEIGHT + 2, 0xFF000000);
    }

    private void updateButtonStates(boolean enabled) {
        increaseButton.active = enabled;
        increaseSlightlyButton.active = enabled;
        decreaseSlightlyButton.active = enabled;
        decreaseButton.active = enabled;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("PH"), 27, 72, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("Tgt"), 130, 72, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
