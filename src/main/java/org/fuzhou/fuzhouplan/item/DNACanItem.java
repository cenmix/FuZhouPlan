package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DnaCanItem extends Item {

    private final EntityType<?> entityType;

    public DnaCanItem(Properties properties, EntityType<?> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component entityName = entityType.getDescription();
        return Component.translatable("item.fuzhouplan.dna_can", entityName);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.dna_can.tooltip")
                .withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.dna_can.entity", entityType.getDescription())
                .withStyle(ChatFormatting.AQUA)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
