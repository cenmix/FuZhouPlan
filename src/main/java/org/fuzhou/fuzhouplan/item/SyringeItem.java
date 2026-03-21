package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SyringeItem extends Item {

    public SyringeItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.syringe.tooltip")
                .withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
