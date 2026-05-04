package org.fuzhou.fuzhouplan.fluid;

import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.fluids.FluidType;

public class GlowingBlueDyeFluidType extends FluidType {

    public GlowingBlueDyeFluidType() {
        super(FluidType.Properties.create()
                .density(1500)
                .viscosity(2000)
                .lightLevel(10)
                .sound(SoundAction.get("bucket_fill"), SoundEvents.BUCKET_FILL)
                .sound(SoundAction.get("bucket_empty"), SoundEvents.BUCKET_EMPTY));
    }
}
