package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GlowingBlueDyeBucketItem extends Item {

    public GlowingBlueDyeBucketItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.glowing_blue_dye_bucket.tooltip")
                .withStyle(ChatFormatting.AQUA)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.glowing_blue_dye_bucket.tooltip.usage")
                .withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
