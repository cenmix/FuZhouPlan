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
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;
import org.fuzhou.fuzhouplan.recipe.MachineRecipe;
import org.fuzhou.fuzhouplan.recipe.ModRecipeTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class MolecularDistillationTowerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int DEFAULT_ENERGY_PER_OPERATION = 1000;
    public static final int DEFAULT_PROCESSING_TIME = 200;
    public static final int MAX_ENERGY = 100000;

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return true;
            }
            return slot == OUTPUT_SLOT;
        }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(MAX_ENERGY);

    private LazyOptional<IItemHandler> itemHandlerLazy = LazyOptional.of(() -> createSidedHandler());
    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    private int processingProgress = 0;
    private boolean isProcessing = false;
    private MachineRecipe currentRecipe = null;

    public MolecularDistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.MOLECULAR_DISTILLATION_TOWER_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (!isProcessing) {
            if (canStartProcessing(level)) {
                isProcessing = true;
                processingProgress = 0;
            }
        }

        if (isProcessing) {
            int energyCost = currentRecipe != null ? currentRecipe.getEnergyCost() : DEFAULT_ENERGY_PER_OPERATION;
            int processingTime = currentRecipe != null ? currentRecipe.getProcessingTime() : DEFAULT_PROCESSING_TIME;
            int energyPerTick = energyCost / processingTime;
            if (energyPerTick <= 0) energyPerTick = 1;

            if (energyStorage.getEnergyStored() >= energyPerTick) {
                energyStorage.consumeEnergy(energyPerTick);
                processingProgress++;
                if (processingProgress >= processingTime) {
                    finishProcessing();
                    isProcessing = false;
                    processingProgress = 0;
                    currentRecipe = null;
                }
            } else {
                isProcessing = false;
                processingProgress = 0;
            }
        }

        setChanged();
    }

    private boolean canStartProcessing(Level level) {
        ItemStack inputStack = itemHandler.getStackInSlot(INPUT_SLOT);
        if (inputStack.isEmpty()) return false;

        SimpleContainer inputContainer = new SimpleContainer(inputStack);
        Optional<MachineRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.DISTILLING.get(), inputContainer, level);

        if (recipeOpt.isEmpty()) return false;

        MachineRecipe recipe = recipeOpt.get();
        ItemStack output = recipe.getPrimaryOutput();

        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (!outputStack.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(outputStack, output)) return false;
            if (outputStack.getCount() + output.getCount() > outputStack.getMaxStackSize()) return false;
        }

        int energyCost = recipe.getEnergyCost();
        if (energyCost > 0 && energyStorage.getEnergyStored() < energyCost) return false;

        currentRecipe = recipe;
        return true;
    }

    private void finishProcessing() {
        if (currentRecipe == null) return;

        itemHandler.extractItem(INPUT_SLOT, 1, false);

        ItemStack output = currentRecipe.getPrimaryOutput();
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, output.copy());
        } else {
            outputStack.grow(output.getCount());
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

    private int syncedProcessingTime = DEFAULT_PROCESSING_TIME;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MolecularDistillationTowerBlockEntity.this.processingProgress;
                case 1 -> currentRecipe != null ? currentRecipe.getProcessingTime() : syncedProcessingTime;
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
                case 1 -> syncedProcessingTime = value;
                case 2 -> energyStorage.setEnergyStored(value);
                case 3 -> { }
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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
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
                if (slot != INPUT_SLOT) return stack;
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
