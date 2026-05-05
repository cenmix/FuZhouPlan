package org.fuzhou.fuzhouplan.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InfiniteGeneratorBlockEntity extends BlockEntity {

    public static final int MAX_ENERGY = Integer.MAX_VALUE;
    public static final int ENERGY_OUTPUT_PER_TICK = 100000;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (simulate) return Math.min(maxExtract, ENERGY_OUTPUT_PER_TICK);
            return Math.min(maxExtract, ENERGY_OUTPUT_PER_TICK);
        }

        @Override
        public int getEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    private LazyOptional<IEnergyStorage> energyStorageLazy = LazyOptional.of(() -> energyStorage);

    public InfiniteGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.INFINITE_GENERATOR_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
            if (neighborEntity == null) continue;

            neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(neighborStorage -> {
                if (neighborStorage.canReceive()) {
                    neighborStorage.receiveEnergy(ENERGY_OUTPUT_PER_TICK, false);
                }
            });
        }

        setChanged();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageLazy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyStorageLazy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyStorageLazy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }

    public int getEnergyStored() {
        return MAX_ENERGY;
    }

    public int getMaxEnergyStored() {
        return MAX_ENERGY;
    }
}
