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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.blockentity.PrecisionStirrerBlockEntity;

/**
 * 精密搅拌器菜单
 * 
 * 槽位布局（3槽）：
 * - 槽0: Tris糊糊输入
 * - 槽1: EDTA糊糊输入
 * - 槽2: 无核酸酶水瓶输入 / TE缓冲液输出
 * - 槽3-38: 玩家背包
 * - 槽39-47: 玩家快捷栏
 */
public class PrecisionStirrerMenu extends AbstractContainerMenu {

    private final PrecisionStirrerBlockEntity blockEntity;
    private final ContainerData data;

    private static final int TE_SLOTS = 3;
    private static final int TE_SLOT_START = 0;
    private static final int PLAYER_INV_START = TE_SLOTS;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    public PrecisionStirrerMenu(int containerId, Inventory inventory, PrecisionStirrerBlockEntity blockEntity) {
        super(Fuzhouplan.PRECISION_STIRRER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = blockEntity.getDataAccess();

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, PrecisionStirrerBlockEntity.INPUT_SLOT_0, 44, 17));
            addSlot(new SlotItemHandler(handler, PrecisionStirrerBlockEntity.INPUT_SLOT_1, 116, 17));
            addSlot(new SlotItemHandler(handler, PrecisionStirrerBlockEntity.IO_SLOT, 80, 53));
        });

        // 添加玩家背包槽位
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 添加玩家快捷栏槽位
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        // 添加数据访问
        addDataSlots(data);
    }

    /**
     * 从网络包创建菜单（客户端使用）
     */
    public PrecisionStirrerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer.readBlockPos()));
    }

    private static PrecisionStirrerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PrecisionStirrerBlockEntity) {
            return (PrecisionStirrerBlockEntity) blockEntity;
        }
        throw new IllegalStateException("Block entity is not a PrecisionStirrerBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index < TE_SLOTS) {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, TE_SLOT_START, TE_SLOTS, false)) {
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

    // 获取数据访问
    public ContainerData getData() {
        return data;
    }

    // 获取方块实体
    public PrecisionStirrerBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
