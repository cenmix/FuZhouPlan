package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

import java.util.List;

public class BioAnestheticItem extends Item {

    private static final float MAX_HEALTH_LIMIT = 30.0f;
    private static final int ANESTHETIC_DURATION = 20 * 5; // 5秒 (20 ticks/秒)

    public BioAnestheticItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity interactionTarget, @NotNull InteractionHand usedHand) {
        Level level = player.level();
        
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        float maxHealth = interactionTarget.getMaxHealth();
        
        if (maxHealth > MAX_HEALTH_LIMIT) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_anesthetic.too_strong")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        if (!(interactionTarget instanceof Mob mob)) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_anesthetic.not_mob")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
            return InteractionResult.FAIL;
        }

        if (mob.getPersistentData().getBoolean("Anesthetized")) {
            player.displayClientMessage(
                Component.translatable("item.fuzhouplan.bio_anesthetic.already_anesthetized")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
            return InteractionResult.FAIL;
        }

        applyAnesthetic(mob);
        
        stack.shrink(1);
        
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), 
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        player.displayClientMessage(
            Component.translatable("item.fuzhouplan.bio_anesthetic.success")
                .withStyle(ChatFormatting.GREEN),
            true
        );

        return InteractionResult.CONSUME;
    }

    private void applyAnesthetic(Mob mob) {
        mob.getPersistentData().putBoolean("Anesthetized", true);
        mob.getPersistentData().putInt("AnestheticTimer", ANESTHETIC_DURATION);
        
        mob.setNoAi(true);
        mob.setDeltaMovement(0, 0, 0);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_anesthetic.tooltip.line1")
                .withStyle(ChatFormatting.GRAY)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_anesthetic.tooltip.line2", MAX_HEALTH_LIMIT)
                .withStyle(ChatFormatting.AQUA)
        );
        tooltipComponents.add(
            Component.translatable("item.fuzhouplan.bio_anesthetic.tooltip.line3")
                .withStyle(ChatFormatting.YELLOW)
        );
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
