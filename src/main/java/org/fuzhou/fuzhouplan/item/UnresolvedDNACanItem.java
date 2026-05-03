package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnresolvedDNACanItem extends Item {

    private final EntityType<?> entityType;

    public UnresolvedDNACanItem(EntityType<?> entityType, Properties properties) {
        super(properties);
        this.entityType = entityType;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public String getDescriptionId() {
        return "item.fuzhouplan.unresolved_dna_can";
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.fuzhouplan.unresolved_dna_can.named", entityType.getDescription());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entityType);

        tooltipComponents.add(
                Component.literal("生物: ").append(entityType.getDescription()).withStyle(ChatFormatting.YELLOW)
        );
        tooltipComponents.add(
                Component.literal("类型: " + (entityKey != null ? entityKey.toString() : "未知")).withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(
                Component.literal("状态: 未解析").withStyle(ChatFormatting.RED)
        );
        tooltipComponents.add(
                Component.translatable("item.fuzhouplan.unresolved_dna_can.tooltip").withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    public static ItemStack resolve(ItemStack unresolvedStack) {
        if (unresolvedStack.getItem() instanceof UnresolvedDNACanItem dnaCan) {
            return DNACanRegistry.createResolvedDNACan(dnaCan.getEntityType());
        }
        return ItemStack.EMPTY;
    }
}
