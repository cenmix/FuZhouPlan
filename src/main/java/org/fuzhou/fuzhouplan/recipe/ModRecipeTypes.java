package org.fuzhou.fuzhouplan.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzhou.fuzhouplan.Fuzhouplan;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Fuzhouplan.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Fuzhouplan.MODID);

    public static final RegistryObject<RecipeType<MachineRecipe>> DRYING =
            RECIPE_TYPES.register("drying", () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return Fuzhouplan.MODID + ":drying";
                }
            });

    public static final RegistryObject<RecipeType<MachineRecipe>> DISTILLING =
            RECIPE_TYPES.register("distilling", () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return Fuzhouplan.MODID + ":distilling";
                }
            });

    public static final RegistryObject<RecipeType<MachineRecipe>> FERMENTING =
            RECIPE_TYPES.register("fermenting", () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return Fuzhouplan.MODID + ":fermenting";
                }
            });

    public static final RegistryObject<RecipeSerializer<MachineRecipe>> DRYING_SERIALIZER =
            SERIALIZERS.register("drying", () -> new MachineRecipe.Serializer(DRYING.get()));
    public static final RegistryObject<RecipeSerializer<MachineRecipe>> DISTILLING_SERIALIZER =
            SERIALIZERS.register("distilling", () -> new MachineRecipe.Serializer(DISTILLING.get()));
    public static final RegistryObject<RecipeSerializer<MachineRecipe>> FERMENTING_SERIALIZER =
            SERIALIZERS.register("fermenting", () -> new MachineRecipe.Serializer(FERMENTING.get()));

    public static RecipeSerializer<?> getSerializerForType(RecipeType<?> type) {
        if (type == DRYING.get()) return DRYING_SERIALIZER.get();
        if (type == DISTILLING.get()) return DISTILLING_SERIALIZER.get();
        if (type == FERMENTING.get()) return FERMENTING_SERIALIZER.get();
        return null;
    }
}
