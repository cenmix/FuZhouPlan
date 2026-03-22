package org.fuzhou.fuzhouplan.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.block.FermentationBarrelBlock;
import org.fuzhou.fuzhouplan.menu.FermentationBarrelMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * 发酵桶方块实体
 * 实现发酵逻辑：
 * - 腐肉 → 氨水瓶（48000 ticks = 2个游戏日）
 * - 小麦 → 醋瓶（24000 ticks = 1个游戏日）
 * 
 * 单槽设计：发酵完成后直接替换物品
 */
public class FermentationBarrelBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    // 发酵时间常量（ticks）
    public static final int ROTTON_FLESH_FERMENT_TIME = 2400;
    public static final int WHEAT_FERMENT_TIME = 1200;

    // 槽位定义
    public static final int SLOT = 0;
    public static final int SLOT_COUNT = 1;

    // 物品存储
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == Items.ROTTEN_FLESH || stack.getItem() == Items.WHEAT;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // 发酵状态
    private int fermentProgress = 0;
    private int fermentTime = 0; // 总发酵时间
    private Item currentInputItem = null;

    // 用于GUI的数据访问
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentProgress;
                case 1 -> FermentationBarrelBlockEntity.this.fermentTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentProgress = value;
                case 1 -> FermentationBarrelBlockEntity.this.fermentTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FermentationBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.FERMENTATION_BARREL_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fuzhouplan.fermentation_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FermentationBarrelMenu(containerId, inventory, this);
    }

    // 方块实体tick逻辑
    public static void tick(Level level, BlockPos pos, BlockState state, FermentationBarrelBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        ItemStack stack = blockEntity.itemHandler.getStackInSlot(SLOT);

        // 更新方块状态：有物品时显示有产出贴图
        int hasOutput = blockEntity.isResultItem(stack) ? 1 : 0;
        if (state.getValue(FermentationBarrelBlock.HAS_OUTPUT) != hasOutput) {
            level.setBlock(pos, state.setValue(FermentationBarrelBlock.HAS_OUTPUT, hasOutput), 2);
        }

        // 检查是否有有效的输入物品（腐肉或小麦）
        if (blockEntity.isValidInput(stack)) {
            // 初始化发酵
            if (blockEntity.currentInputItem == null || blockEntity.currentInputItem != stack.getItem()) {
                blockEntity.currentInputItem = stack.getItem();
                blockEntity.fermentProgress = 0;
                blockEntity.fermentTime = blockEntity.getFermentTimeForItem(stack.getItem());
            }

            // 进行发酵
            blockEntity.fermentProgress++;
            blockEntity.setChanged();

            // 发酵完成
            if (blockEntity.fermentProgress >= blockEntity.fermentTime) {
                blockEntity.finishFermentation();
            }
        } else {
            // 无有效输入，重置进度
            if (blockEntity.fermentProgress > 0) {
                blockEntity.fermentProgress = 0;
                blockEntity.currentInputItem = null;
                blockEntity.fermentTime = 0;
                blockEntity.setChanged();
            }
        }
    }

    private boolean isValidInput(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.ROTTEN_FLESH || stack.getItem() == Items.WHEAT);
    }

    private boolean isResultItem(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Fuzhouplan.AMMONIA_BOTTLE.get() || stack.getItem() == Fuzhouplan.VINEGAR_BOTTLE.get());
    }

    private int getFermentTimeForItem(Item item) {
        if (item == Items.ROTTEN_FLESH) {
            return ROTTON_FLESH_FERMENT_TIME;
        } else if (item == Items.WHEAT) {
            return WHEAT_FERMENT_TIME;
        }
        return 0;
    }

    @Nullable
    private Item getResultItem(Item inputItem) {
        if (inputItem == Items.ROTTEN_FLESH) {
            return Fuzhouplan.AMMONIA_BOTTLE.get();
        } else if (inputItem == Items.WHEAT) {
            return Fuzhouplan.VINEGAR_BOTTLE.get();
        }
        return null;
    }

    private void finishFermentation() {
        ItemStack stack = itemHandler.getStackInSlot(SLOT);
        Item resultItem = getResultItem(currentInputItem);

        if (resultItem != null) {
            // 直接替换为输出物品
            itemHandler.setStackInSlot(SLOT, new ItemStack(resultItem, stack.getCount()));

            // 重置发酵状态
            fermentProgress = 0;
            currentInputItem = null;
            fermentTime = 0;
            setChanged();
        }
    }

    // NBT数据保存和加载
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("fermentProgress", fermentProgress);
        tag.putInt("fermentTime", fermentTime);
        if (currentInputItem != null) {
            tag.putString("currentInputItem", currentInputItem.builtInRegistryHolder().key().location().toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        fermentProgress = tag.getInt("fermentProgress");
        fermentTime = tag.getInt("fermentTime");
        String itemKey = tag.getString("currentInputItem");
        if (!itemKey.isEmpty()) {
            currentInputItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(itemKey));
        }
    }

    // 物品丢弃（方块破坏时）
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // WorldlyContainer 实现
    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return isValidInput(itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isResultItem(stack);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this &&
               player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // Capability支持
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    // 获取进度百分比（用于显示）
    public float getProgressPercent() {
        if (fermentTime == 0) {
            return 0.0f;
        }
        return (float) fermentProgress / fermentTime;
    }

    public int getFermentProgress() {
        return fermentProgress;
    }

    public int getFermentTime() {
        return fermentTime;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }
}
