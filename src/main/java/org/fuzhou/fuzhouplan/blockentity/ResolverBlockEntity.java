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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.item.DNACanRegistry;
import org.fuzhou.fuzhouplan.item.GlowingBlueDyeItem;
import org.fuzhou.fuzhouplan.item.UnresolvedDNACanItem;
import org.fuzhou.fuzhouplan.menu.ResolverMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResolverBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ENERGY_PER_OPERATION = 50000;
    public static final int PROCESSING_TIME = 600;
    public static final int MAX_ENERGY = 500000;

    public static final int INPUT_SLOT_DYE = 0;
    public static final int INPUT_SLOT_DNA = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == INPUT_SLOT_DYE) {
                return stack.getItem() instanceof GlowingBlueDyeItem;
            }
            if (slot == INPUT_SLOT_DNA) {
                return stack.getItem() instanceof UnresolvedDNACanItem;
            }
            return slot == OUTPUT_SLOT;
        }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(MAX_ENERGY);

    private LazyOptional<IItemHandler> itemHandlerLazy = LazyOptional.of(() -> createSidedHandler());
    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    private int processingProgress = 0;
    private boolean isProcessing = false;

    public ResolverBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.RESOLVER_ENTITY.get(), pos, state);
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
                energyStorage.consumeEnergy(energyPerTick);
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
        ItemStack dyeStack = itemHandler.getStackInSlot(INPUT_SLOT_DYE);
        if (dyeStack.isEmpty() || !(dyeStack.getItem() instanceof GlowingBlueDyeItem)) {
            return false;
        }

        ItemStack dnaStack = itemHandler.getStackInSlot(INPUT_SLOT_DNA);
        if (dnaStack.isEmpty() || !(dnaStack.getItem() instanceof UnresolvedDNACanItem dnaCan)) {
            return false;
        }

        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        ItemStack expectedResult = DNACanRegistry.createResolvedDNACan(dnaCan.getEntityType());
        if (expectedResult.isEmpty()) {
            return false;
        }

        if (!outputStack.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(outputStack, expectedResult)) {
                return false;
            }
            if (outputStack.getCount() >= outputStack.getMaxStackSize()) {
                return false;
            }
        }

        return energyStorage.getEnergyStored() >= ENERGY_PER_OPERATION;
    }

    private void finishProcessing() {
        ItemStack dnaStack = itemHandler.getStackInSlot(INPUT_SLOT_DNA);
        if (dnaStack.isEmpty() || !(dnaStack.getItem() instanceof UnresolvedDNACanItem canItem)) {
            return;
        }

        ItemStack result = DNACanRegistry.createResolvedDNACan(canItem.getEntityType());
        if (result.isEmpty()) return;

        itemHandler.extractItem(INPUT_SLOT_DYE, 1, false);
        itemHandler.extractItem(INPUT_SLOT_DNA, 1, false);

        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItemSameTags(outputStack, result)) {
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
        itemHandlerLazy = LazyOptional.of(this::createSidedHandler);
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
                case 0 -> ResolverBlockEntity.this.processingProgress;
                case 1 -> ResolverBlockEntity.this.PROCESSING_TIME;
                case 2 -> ResolverBlockEntity.this.getEnergyStored();
                case 3 -> ResolverBlockEntity.this.MAX_ENERGY;
                case 4 -> ResolverBlockEntity.this.isProcessing ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ResolverBlockEntity.this.processingProgress = value;
                case 1 -> { }
                case 2 -> energyStorage.setEnergyStored(value);
                case 3 -> { }
                case 4 -> ResolverBlockEntity.this.isProcessing = value != 0;
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this &&
               player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fuzhouplan.resolver");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResolverMenu(containerId, inventory, this);
    }

    private IItemHandler createSidedHandler() {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return itemHandler.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return itemHandler.getStackInSlot(slot);
            }

            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (slot != INPUT_SLOT_DYE && slot != INPUT_SLOT_DNA) return stack;
                return itemHandler.insertItem(slot, stack, simulate);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot != OUTPUT_SLOT) return ItemStack.EMPTY;
                return itemHandler.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return itemHandler.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return itemHandler.isItemValid(slot, stack);
            }
        };
    }

    private class MachineEnergyStorage extends EnergyStorage {
        public MachineEnergyStorage(int capacity) {
            super(capacity);
        }

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

        @Override
        public boolean canExtract() {
            return false;
        }

        public void consumeEnergy(int amount) {
            this.energy = Math.max(0, this.energy - amount);
            setChanged();
        }

        public void setEnergyStored(int energy) {
            this.energy = Math.max(0, Math.min(energy, capacity));
        }
    }
}
