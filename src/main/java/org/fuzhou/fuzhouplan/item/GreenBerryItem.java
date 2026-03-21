package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreenBerryItem extends Item {

    public GreenBerryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK) || level.getBlockState(pos).is(Blocks.DIRT)) {
            BlockPos placePos = pos.above();
            if (level.getBlockState(placePos).isAir()) {
                level.setBlock(placePos, Fuzhouplan.GREEN_BERRY_BUSH.get().defaultBlockState(), 2);
                ItemStack stack = context.getItemInHand();
                stack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.green_berry.tooltip")
                .withStyle(ChatFormatting.GREEN)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
