package org.fuzhou.fuzhouplan.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.blockentity.MolecularDistillationTowerBlockEntity;

import javax.annotation.Nullable;

public class MolecularDistillationTowerBlock extends BaseEntityBlock {

    public MolecularDistillationTowerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MolecularDistillationTowerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MolecularDistillationTowerBlockEntity towerEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, towerEntity, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MolecularDistillationTowerBlockEntity towerEntity) {
                towerEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, Fuzhouplan.MOLECULAR_DISTILLATION_TOWER_ENTITY.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1));
    }
}
