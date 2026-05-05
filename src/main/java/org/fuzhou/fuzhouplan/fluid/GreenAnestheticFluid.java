package org.fuzhou.fuzhouplan.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.function.Supplier;

public abstract class GreenAnestheticFluid extends ForgeFlowingFluid {

    public static Supplier<? extends Fluid> SOURCE = () -> Fuzhouplan.GREEN_ANESTHETIC_SOURCE.get();
    public static Supplier<? extends Fluid> FLOWING = () -> Fuzhouplan.GREEN_ANESTHETIC_FLOWING.get();
    public static Supplier<? extends LiquidBlock> BLOCK = () -> Fuzhouplan.GREEN_ANESTHETIC_BLOCK.get();
    public static Supplier<? extends Item> BUCKET = () -> Fuzhouplan.GREEN_ANESTHETIC_BUCKET.get();

    public GreenAnestheticFluid(Properties properties) {
        super(properties);
    }

    public static Properties makeProperties() {
        return new Properties(
                Fuzhouplan.GREEN_ANESTHETIC_FLUID_TYPE,
                Fuzhouplan.GREEN_ANESTHETIC_SOURCE,
                Fuzhouplan.GREEN_ANESTHETIC_FLOWING
        ).bucket(Fuzhouplan.GREEN_ANESTHETIC_BUCKET)
         .block(Fuzhouplan.GREEN_ANESTHETIC_BLOCK);
    }

    @Override
    public Fluid getFlowing() {
        return FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return SOURCE.get();
    }

    @Override
    public boolean canConvertToSource(FluidState state, Level level, BlockPos pos) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 20;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == SOURCE.get() || fluid == FLOWING.get();
    }

    public static class Source extends GreenAnestheticFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends GreenAnestheticFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }
}
