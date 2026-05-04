package org.fuzhou.fuzhouplan.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BucketItem;
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
import org.fuzhou.fuzhouplan.menu.DryerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DryerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ENERGY_PER_OPERATION = 400;
    public static final int PROCESSING_TIME = 300;
    public static final int MAX_ENERGY = 50000;

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT_1 = 1;
    public static final int OUTPUT_SLOT_2 = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return stack.getItem() == Fuzhouplan.GLOWING_BLUE_DYE_BUCKET.get();
            }
            return slot == OUTPUT_SLOT_1 || slot == OUTPUT_SLOT_2;
        }
    };

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
            return 0;
        }
    };

    private LazyOptional<ItemStackHandler> itemHandlerLazy = LazyOptional.of(() -> itemHandler);
    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    private int processingProgress = 0;
    private boolean isProcessing = false;

    public DryerBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.DRYER_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (!isProcessing) {
            if (canStartProcessing()) {
                isProcessing = true;
                processingProgress = 0;
            }
        }

        if (isProcessing) {
            int energyPerTick = ENERGY_PER_OPERATION / PROCESSING_TIME;
            if (energyStorage.getEnergyStored() >= energyPerTick) {
                energyStorage.extractEnergy(energyPerTick, false);
                processingProgress++;
                if (processingProgress >= PROCESSING_TIME) {
                    finishProcessing();
                    isProcessing = false;
                    processingProgress = 0;
                }
            } else {
                isProcessing = false;
                processingProgress = 0;
            }
        }

        setChanged();
    }

    private boolean canStartProcessing() {
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        if (inputStack.isEmpty() || inputStack.getItem() != Fuzhouplan.GLOWING_BLUE_DYE_BUCKET.get()) {
            return false;
        }

        ItemStack output1 = itemHandler.getStackInSlot(OUTPUT_SLOT_1);
        if (!output1.isEmpty() && (output1.getItem() != Items.BUCKET || output1.getCount() >= output1.getMaxStackSize())) {
            return false;
        }

        ItemStack output2 = itemHandler.getStackInSlot(OUTPUT_SLOT_2);
        if (!output2.isEmpty() && (output2.getItem() != Fuzhouplan.GLOWING_BLUE_DYE.get() || output2.getCount() >= output2.getMaxStackSize())) {
            return false;
        }

        return energyStorage.getEnergyStored() >= ENERGY_PER_OPERATION;
    }

    private void finishProcessing() {
        itemHandler.extractItem(INPUT_SLOT, 1, false);

        ItemStack output1 = itemHandler.getStackInSlot(OUTPUT_SLOT_1);
        if (output1.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT_1, new ItemStack(Items.BUCKET));
        } else {
            output1.grow(1);
        }

        ItemStack output2 = itemHandler.getStackInSlot(OUTPUT_SLOT_2);
        if (output2.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT_2, new ItemStack(Fuzhouplan.GLOWING_BLUE_DYE.get()));
        } else {
            output2.grow(1);
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

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> DryerBlockEntity.this.processingProgress;
                case 1 -> DryerBlockEntity.this.PROCESSING_TIME;
                case 2 -> DryerBlockEntity.this.getEnergyStored();
                case 3 -> DryerBlockEntity.this.MAX_ENERGY;
                case 4 -> DryerBlockEntity.this.isProcessing ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> DryerBlockEntity.this.processingProgress = value;
                case 4 -> DryerBlockEntity.this.isProcessing = value != 0;
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
        return Component.translatable("block.fuzhouplan.dryer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DryerMenu(containerId, inventory, this);
    }
}
