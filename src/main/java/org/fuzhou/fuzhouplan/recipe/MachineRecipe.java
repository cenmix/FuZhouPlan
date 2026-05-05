package org.fuzhou.fuzhouplan.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class MachineRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final RecipeType<MachineRecipe> type;
    private final String group;
    private final Ingredient input;
    private final ItemStack primaryOutput;
    private final ItemStack secondaryOutput;
    private final int processingTime;
    private final int energyCost;

    public MachineRecipe(ResourceLocation id, RecipeType<MachineRecipe> type, String group,
                         Ingredient input, ItemStack primaryOutput, ItemStack secondaryOutput,
                         int processingTime, int energyCost) {
        this.id = id;
        this.type = type;
        this.group = group;
        this.input = input;
        this.primaryOutput = primaryOutput;
        this.secondaryOutput = secondaryOutput;
        this.processingTime = processingTime;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return primaryOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return primaryOutput.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.getSerializerForType(type);
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getPrimaryOutput() {
        return primaryOutput;
    }

    public ItemStack getSecondaryOutput() {
        return secondaryOutput;
    }

    public boolean hasSecondaryOutput() {
        return !secondaryOutput.isEmpty();
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    public static class Serializer implements RecipeSerializer<MachineRecipe> {

        private final RecipeType<MachineRecipe> recipeType;

        public Serializer(RecipeType<MachineRecipe> recipeType) {
            this.recipeType = recipeType;
        }

        @Override
        public MachineRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack primaryOutput = itemFromJson(GsonHelper.getAsJsonObject(json, "primary_output"));
            ItemStack secondaryOutput = ItemStack.EMPTY;
            if (json.has("secondary_output")) {
                secondaryOutput = itemFromJson(GsonHelper.getAsJsonObject(json, "secondary_output"));
            }
            int processingTime = GsonHelper.getAsInt(json, "processing_time", 200);
            int energyCost = GsonHelper.getAsInt(json, "energy_cost", 0);
            return new MachineRecipe(id, recipeType, group, input, primaryOutput, secondaryOutput, processingTime, energyCost);
        }

        private static ItemStack itemFromJson(JsonObject json) {
            String itemStr = GsonHelper.getAsString(json, "item");
            int count = GsonHelper.getAsInt(json, "count", 1);
            ResourceLocation itemRL = ResourceLocation.tryParse(itemStr);
            if (itemRL == null) return ItemStack.EMPTY;
            Item item = ForgeRegistries.ITEMS.getValue(itemRL);
            return new ItemStack(item, count);
        }

        @Nullable
        @Override
        public MachineRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            String group = buf.readUtf();
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack primaryOutput = buf.readItem();
            ItemStack secondaryOutput = buf.readItem();
            int processingTime = buf.readVarInt();
            int energyCost = buf.readVarInt();
            return new MachineRecipe(id, recipeType, group, input, primaryOutput, secondaryOutput, processingTime, energyCost);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buf, @Nonnull MachineRecipe recipe) {
            buf.writeUtf(recipe.group);
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.primaryOutput);
            buf.writeItem(recipe.secondaryOutput);
            buf.writeVarInt(recipe.processingTime);
            buf.writeVarInt(recipe.energyCost);
        }
    }
}
