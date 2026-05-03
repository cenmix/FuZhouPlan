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

        if (!(interactionTarget instanceof Mob mob)) {
            player.displayClientMessage(
                    Component.translatable("item.fuzhouplan.bio_gene_extractor.not_mob")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            return InteractionResult.FAIL;
        }

        if (!mob.getPersistentData().getBoolean("Anesthetized")) {
            player.displayClientMessage(
                    Component.translatable("item.fuzhouplan.bio_gene_extractor.not_anesthetized")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }

        ItemStack teBufferStack = findTEBufferInInventory(player);
        if (teBufferStack.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("item.fuzhouplan.bio_gene_extractor.no_te_buffer")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }

        teBufferStack.shrink(1);

        ItemStack resultStack = DNACanRegistry.createUnresolvedDNACan(mob.getType());
        if (resultStack.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("item.fuzhouplan.bio_gene_extractor.unsupported_entity")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }

        if (!player.getInventory().add(resultStack)) {
            player.drop(resultStack, false);
        }

        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_gene_extractor.success", mob.getDisplayName())
                        .withStyle(ChatFormatting.GREEN),
                true
        );

        return InteractionResult.CONSUME;
    }

    private ItemStack findTEBufferInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Fuzhouplan.TE_BUFFER_CAN.get()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
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
