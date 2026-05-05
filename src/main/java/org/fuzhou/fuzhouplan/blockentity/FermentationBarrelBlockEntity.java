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
import org.fuzhou.fuzhouplan.block.FermentationBarrelBlock;
import org.fuzhou.fuzhouplan.menu.FermentationBarrelMenu;
import org.fuzhou.fuzhouplan.recipe.MachineRecipe;
import org.fuzhou.fuzhouplan.recipe.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FermentationBarrelBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int DEFAULT_FERMENT_TIME = 2400;
    public static final int MAX_ENERGY = 50000;
    public static final int ENERGY_PER_SPEED_LEVEL = 5000;
    public static final int ENERGY_COST_PER_LEVEL = 20;
    public static final int MAX_SPEED_MULTIPLIER = 10;

    public static final int SLOT = 0;
    public static final int SLOT_COUNT = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(MAX_ENERGY);

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    private int fermentProgress = 0;
    private int fermentTime = 0;
    private MachineRecipe currentRecipe = null;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentProgress;
                case 1 -> FermentationBarrelBlockEntity.this.fermentTime;
                case 2 -> FermentationBarrelBlockEntity.this.getEnergyStored();
                case 3 -> FermentationBarrelBlockEntity.this.MAX_ENERGY;
                case 4 -> FermentationBarrelBlockEntity.this.getSpeedMultiplier();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> FermentationBarrelBlockEntity.this.fermentProgress = value;
                case 1 -> FermentationBarrelBlockEntity.this.fermentTime = value;
                case 2 -> energyStorage.setEnergyStored(value);
                case 3 -> { }
                case 4 -> { }
            }
        }

        @Override
        public int getCount() {
            return 5;
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

    public static void tick(Level level, BlockPos pos, BlockState state, FermentationBarrelBlockEntity blockEntity) {
        if (level.isClientSide) return;

        ItemStack stack = blockEntity.itemHandler.getStackInSlot(SLOT);

        int hasOutput = blockEntity.isResultItem(stack) ? 1 : 0;
        if (state.getValue(FermentationBarrelBlock.HAS_OUTPUT) != hasOutput) {
            level.setBlock(pos, state.setValue(FermentationBarrelBlock.HAS_OUTPUT, hasOutput), 2);
        }

        if (!stack.isEmpty()) {
            SimpleContainer inputContainer = new SimpleContainer(stack);
            Optional<MachineRecipe> recipeOpt = level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.FERMENTING.get(), inputContainer, level);

            if (recipeOpt.isPresent()) {
                MachineRecipe recipe = recipeOpt.get();
                if (blockEntity.currentRecipe == null || blockEntity.currentRecipe != recipe) {
                    blockEntity.currentRecipe = recipe;
                    blockEntity.fermentProgress = 0;
                    blockEntity.fermentTime = recipe.getProcessingTime();
                }

                int speedMultiplier = blockEntity.getSpeedMultiplier();
                int energyToConsume = (speedMultiplier - 1) * ENERGY_COST_PER_LEVEL;
                if (energyToConsume > 0 && blockEntity.energyStorage.getEnergyStored() >= energyToConsume) {
                    blockEntity.energyStorage.consumeEnergy(energyToConsume);
                    blockEntity.fermentProgress += speedMultiplier;
                } else {
                    blockEntity.fermentProgress++;
                }
                blockEntity.setChanged();

                if (blockEntity.fermentProgress >= blockEntity.fermentTime) {
                    blockEntity.finishFermentation();
                }
            } else {
                if (blockEntity.fermentProgress > 0) {
                    blockEntity.fermentProgress = 0;
                    blockEntity.currentRecipe = null;
                    blockEntity.fermentTime = 0;
                    blockEntity.setChanged();
                }
            }
        } else {
            if (blockEntity.fermentProgress > 0) {
                blockEntity.fermentProgress = 0;
                blockEntity.currentRecipe = null;
                blockEntity.fermentTime = 0;
                blockEntity.setChanged();
            }
        }
    }

    public int getSpeedMultiplier() {
        int speedLevel = energyStorage.getEnergyStored() / ENERGY_PER_SPEED_LEVEL;
        return Math.min(MAX_SPEED_MULTIPLIER, 1 + speedLevel);
    }

    private boolean isResultItem(ItemStack stack) {
        if (stack.isEmpty() || level == null) return false;
        SimpleContainer inputContainer = new SimpleContainer(stack);
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.FERMENTING.get(), inputContainer, level).isEmpty();
    }

    private void finishFermentation() {
        if (currentRecipe == null) return;

        ItemStack stack = itemHandler.getStackInSlot(SLOT);
        ItemStack output = currentRecipe.getPrimaryOutput();
        int outputCount = Math.min(output.getCount() * stack.getCount(), output.getMaxStackSize());
        itemHandler.setStackInSlot(SLOT, new ItemStack(output.getItem(), outputCount));

        fermentProgress = 0;
        currentRecipe = null;
        fermentTime = 0;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("fermentProgress", fermentProgress);
        tag.putInt("fermentTime", fermentTime);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        fermentProgress = tag.getInt("fermentProgress");
        fermentTime = tag.getInt("fermentTime");
        if (tag.contains("Energy")) {
            energyStorage.receiveEnergy(tag.getInt("Energy"), false);
        }
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return !itemStack.isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return fermentProgress <= 0;
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageLazy.cast();
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
        energyStorageLazy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        energyStorageLazy = LazyOptional.of(() -> energyStorage);
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public float getProgressPercent() {
        if (fermentTime == 0) return 0.0f;
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
