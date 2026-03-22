package org.fuzhou.fuzhouplan.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.fuzhou.fuzhouplan.blockentity.FermentationBarrelBlockEntity;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.jetbrains.annotations.Nullable;

/**
 * 发酵桶方块
 * 一个用于发酵物品的方块，支持：
 * - 腐肉 → 氨水瓶
 * - 小麦 → 醋瓶
 * 
 * 侧面和底面使用固定贴图
 * 顶面根据输出槽状态切换：有产出时显示 stage1 贴图
 */
public class FermentationBarrelBlock extends BaseEntityBlock {

    public static final IntegerProperty HAS_OUTPUT = IntegerProperty.create("has_output", 0, 1);

    public FermentationBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_OUTPUT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_OUTPUT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentationBarrelBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FermentationBarrelBlockEntity barrelEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, barrelEntity, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FermentationBarrelBlockEntity barrelEntity) {
                barrelEntity.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, Fuzhouplan.FERMENTATION_BARREL_ENTITY.get(),
                (level1, pos, state1, blockEntity) -> FermentationBarrelBlockEntity.tick(level1, pos, state1, blockEntity));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FermentationBarrelBlockEntity barrelEntity) {
            float progress = barrelEntity.getProgressPercent();
            return (int) (progress * 15);
        }
        return 0;
    }
}
