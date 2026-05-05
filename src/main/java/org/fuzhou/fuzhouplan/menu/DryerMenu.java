package org.fuzhou.fuzhouplan.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.blockentity.DryerBlockEntity;

public class DryerMenu extends AbstractContainerMenu {

    private final DryerBlockEntity blockEntity;
    private final ContainerData data;

    public DryerMenu(int containerId, Inventory inventory, DryerBlockEntity blockEntity) {
        super(Fuzhouplan.DRYER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), DryerBlockEntity.INPUT_SLOT, 56, 35));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), DryerBlockEntity.OUTPUT_SLOT_1, 110, 35));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), DryerBlockEntity.OUTPUT_SLOT_2, 130, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public DryerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer.readBlockPos()));
    }

    private static DryerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DryerBlockEntity) {
            return (DryerBlockEntity) blockEntity;
        }
        throw new IllegalStateException("Block entity is not a DryerBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index < 3) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && blockEntity.stillValid(player);
    }

    public ContainerData getData() {
        return data;
    }

    public DryerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
