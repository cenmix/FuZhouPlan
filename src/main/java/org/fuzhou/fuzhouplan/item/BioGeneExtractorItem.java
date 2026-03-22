package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.List;

/**
 * 生物基因提取器
 * 用于从麻醉状态的生物中提取基因样本
 */
public class BioGeneExtractorItem extends Item {

    public BioGeneExtractorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget, @NotNull InteractionHand usedHand) {
        Level level = player.level();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // 检查是否为Mob类型
        if (!(interactionTarget instanceof Mob mob)) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_gene_extractor.not_mob")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
            return InteractionResult.FAIL;
        }

        // 检查生物是否处于麻醉状态
        if (!mob.getPersistentData().getBoolean("Anesthetized")) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_gene_extractor.not_anesthetized")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // 检查玩家背包中是否有TE缓冲液储存罐
        ItemStack teBufferStack = findTEBufferInInventory(player);
        if (teBufferStack.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_gene_extractor.no_te_buffer")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // 成功提取：消耗1个TE缓冲液储存罐
        teBufferStack.shrink(1);

        // 创建带有生物信息的TE缓冲液储存罐
        ItemStack resultStack = createGeneSample(mob);

        // 给予玩家产物
        if (!player.getInventory().add(resultStack)) {
            player.drop(resultStack, false);
        }

        // 播放音效
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
            SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);

        // 显示成功消息
        player.displayClientMessage(
            Component.translatable("item.fuzhouplan.bio_gene_extractor.success", mob.getDisplayName())
                .withStyle(ChatFormatting.GREEN),
            true
        );

        return InteractionResult.CONSUME;
    }

    /**
     * 在玩家背包中查找TE缓冲液储存罐
     */
    private ItemStack findTEBufferInInventory(Player player) {
        // 检查主背包
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Fuzhouplan.TE_BUFFER_CAN.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 创建对应生物的未解析DNA储存罐
     */
    private ItemStack createGeneSample(Mob mob) {
        Item unresolvedDnaCanItem = DnaCanManager.getUnresolvedDnaCan(mob.getType());
        if (unresolvedDnaCanItem == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(unresolvedDnaCanItem);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_gene_extractor.tooltip.line1")
                .withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_gene_extractor.tooltip.line2")
                .withStyle(ChatFormatting.AQUA)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_gene_extractor.tooltip.line3")
                .withStyle(ChatFormatting.YELLOW)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
