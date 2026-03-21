package org.fuzhou.fuzhouplan.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import net.minecraft.util.RandomSource;

public class GreenBerryBushBlock extends BushBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SMALL_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape LARGE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public GreenBerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Fuzhouplan.GREEN_BERRY.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) < 2 ? SMALL_SHAPE : LARGE_SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 3 && level.getRawBrightness(pos.above(), 0) >= 9 && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        if (age > 1) {
            int count = 1 + level.random.nextInt(2);
            popResource(level, pos, new ItemStack(Fuzhouplan.GREEN_BERRY.get(), count));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(AGE, 1), 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return super.use(state, level, pos, player, hand, hit);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = Math.min(3, state.getValue(AGE) + 1);
        level.setBlock(pos, state.setValue(AGE, age), 2);
    }
}
