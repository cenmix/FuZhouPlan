package org.fuzhou.fuzhouplan.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MolecularDistillationTowerBlockEntity extends BlockEntity implements MenuProvider {

    // 配置参数
    public static final int ENERGY_PER_OPERATION = 1000; // 每次操作消耗的FE
    public static final int PROCESSING_TIME = 200; // 处理时间（ticks）
    public static final int MAX_ENERGY = 100000; // 最大能量存储

    // 物品槽位
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    // 物品存储
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                // 输入槽只接受蒸馏水
                return stack.getItem() == Fuzhouplan.DISTILLED_WATER.get();
            }
            return slot == OUTPUT_SLOT;
        }
    };

    // 能量存储
    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0; // 不允许从该设备提取能量
        }
    };

    // LazyOptional 缓存
    private LazyOptional<ItemStackHandler> itemHandlerLazy = LazyOptional.of(() -> itemHandler);
    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    // 处理进度
    private int processingProgress = 0;
    private boolean isProcessing = false;

    public MolecularDistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.MOLECULAR_DISTILLATION_TOWER_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        // 检查是否可以开始处理
        if (!isProcessing) {
            if (canStartProcessing()) {
                isProcessing = true;
                processingProgress = 0;
            }
        }

        // 处理中
        if (isProcessing) {
            // 检查能量是否足够
            if (energyStorage.getEnergyStored() >= ENERGY_PER_OPERATION / PROCESSING_TIME) {
                // 每tick消耗一部分能量
                int energyPerTick = ENERGY_PER_OPERATION / PROCESSING_TIME;
                energyStorage.extractEnergy(energyPerTick, false);
                processingProgress++;

                // 处理完成
                if (processingProgress >= PROCESSING_TIME) {
                    finishProcessing();
                    isProcessing = false;
                    processingProgress = 0;
                }
            } else {
                // 能量不足，暂停处理
                isProcessing = false;
                processingProgress = 0;
            }
        }

        setChanged();
    }

    private boolean canStartProcessing() {
        // 检查输入槽是否有蒸馏水
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        if (inputStack.isEmpty() || inputStack.getItem() != Fuzhouplan.DISTILLED_WATER.get()) {
            return false;
        }

        // 检查输出槽是否有空间
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (!outputStack.isEmpty() && outputStack.getItem() != Fuzhouplan.NUCLEASE_FREE_WATER.get()) {
            return false;
        }

        // 检查输出槽是否已满
        if (!outputStack.isEmpty() && outputStack.getCount() >= outputStack.getMaxStackSize()) {
            return false;
        }

        // 检查能量是否足够
        return energyStorage.getEnergyStored() >= ENERGY_PER_OPERATION;
    }

    private void finishProcessing() {
        // 消耗输入物品
        itemHandler.extractItem(INPUT_SLOT, 1, false);

        // 产出输出物品
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(Fuzhouplan.NUCLEASE_FREE_WATER.get(), 1));
        } else {
            outputStack.grow(1);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("Energy")) {
            energyStorage.receiveEnergy(tag.getInt("Energy"), false);
        }
        processingProgress = tag.getInt("ProcessingProgress");
        isProcessing = tag.getBoolean("IsProcessing");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Energy", energyStorage.getEnergyStored());
        tag.putInt("ProcessingProgress", processingProgress);
        tag.putBoolean("IsProcessing", isProcessing);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazy.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageLazy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerLazy.invalidate();
        energyStorageLazy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlerLazy = LazyOptional.of(() -> itemHandler);
        energyStorageLazy = LazyOptional.of(() -> energyStorage);
    }

    // 获取当前能量
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    // 获取最大能量
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    // 获取处理进度
    public int getProcessingProgress() {
        return processingProgress;
    }

    // 获取总处理时间
    public int getTotalProcessingTime() {
        return PROCESSING_TIME;
    }

    // 是否正在处理
    public boolean isProcessing() {
        return isProcessing;
    }

    // 插入物品（用于玩家交互）
    public boolean insertItem(ItemStack stack) {
        if (stack.getItem() == Fuzhouplan.DISTILLED_WATER.get()) {
            ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
            if (inputStack.isEmpty()) {
                itemHandler.setStackInSlot(INPUT_SLOT, stack.split(1));
                return true;
            } else if (ItemStack.isSameItemSameTags(inputStack, stack) && inputStack.getCount() < inputStack.getMaxStackSize()) {
                inputStack.grow(1);
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    // 提取输出物品（用于玩家交互）
    public ItemStack extractOutputItem() {
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (!outputStack.isEmpty()) {
            ItemStack result = outputStack.copy();
            itemHandler.setStackInSlot(OUTPUT_SLOT, ItemStack.EMPTY);
            return result;
        }
        return ItemStack.EMPTY;
    }

    // 物品丢弃（方块破坏时）
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // 用于GUI的数据访问
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MolecularDistillationTowerBlockEntity.this.processingProgress;
                case 1 -> MolecularDistillationTowerBlockEntity.this.PROCESSING_TIME;
                case 2 -> MolecularDistillationTowerBlockEntity.this.getEnergyStored();
                case 3 -> MolecularDistillationTowerBlockEntity.this.MAX_ENERGY;
                case 4 -> MolecularDistillationTowerBlockEntity.this.isProcessing ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> MolecularDistillationTowerBlockEntity.this.processingProgress = value;
                case 4 -> MolecularDistillationTowerBlockEntity.this.isProcessing = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this &&
               player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fuzhouplan.molecular_distillation_tower");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MolecularDistillationTowerMenu(containerId, inventory, this);
    }
}
