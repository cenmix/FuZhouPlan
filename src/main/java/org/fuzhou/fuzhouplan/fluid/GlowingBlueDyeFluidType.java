package org.fuzhou.fuzhouplan.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.function.Consumer;

public class GlowingBlueDyeFluidType extends FluidType {

    public GlowingBlueDyeFluidType() {
        super(FluidType.Properties.create()
                .density(1500)
                .viscosity(2000)
                .lightLevel(10));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("minecraft", "block/water_still");
            private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation("minecraft", "block/water_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0xFF4488FF;
            }
        });
    }
}
