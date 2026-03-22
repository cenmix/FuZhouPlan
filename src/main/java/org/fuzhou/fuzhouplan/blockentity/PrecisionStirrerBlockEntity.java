package org.fuzhou.fuzhouplan.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.menu.PrecisionStirrerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * 精密搅拌器方块实体
 * 
 * PH值小游戏逻辑：
 * - 玩家PH值：通过按钮控制，范围0-100
 * - 目标PH值（targetPH）：自动移动，范围0-100
 * - 判定：两个值相差 > 15% 则合成失败，连续5次相差 <= 15% 则合成成功
 * 
 * 槽位布局（3槽）：
 *   [槽0] [槽1]  - Tris糊糊、EDTA糊糊
 *    [槽2]      - 无核酸酶水瓶输入 / TE缓冲液输出
 */
public class PrecisionStirrerBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int INPUT_SLOT_0 = 0;
    public static final int INPUT_SLOT_1 = 1;
    public static final int IO_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    // PH值范围
    public static final int PH_MIN = 0;
    public static final int PH_MAX = 100;
    public static final int PH_TOLERANCE = 15; // 允许的误差范围

    // 目标PH移动模式
    public enum MovementMode {
        CONSTANT,    // 匀速
        ACCELERATING, // 加速
        STOPPED       // 瞬间停下
    }

    // 物品存储
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT_0, INPUT_SLOT_1 -> true;
                case IO_SLOT -> stack.getItem() == Fuzhouplan.NUCLEASE_FREE_WATER.get();
                default -> false;
            };
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    // PH值状态
    private int playerPH = 50;      // 玩家控制的PH值
    private int targetPH = 50;      // 目标PH值（自动移动）
    private int successCount = 0;   // 连续成功次数
    private MovementMode currentMode = MovementMode.CONSTANT;
    private int movementDirection = 1; // 1: 向上, -1: 向下
    private int movementSpeed = 1;     // 当前移动速度
    private int ticksInMode = 0;       // 当前模式持续tick数
    private int nextModeChange = 60;   // 下次模式变化的tick

    // 游戏状态
    private boolean isProcessing = false;
    private boolean gameWon = false;
    private boolean gameLost = false;
    private int resultCooldown = 0;    // 结果显示冷却
    private int checkInterval = 0;     // 检查间隔计数器
    private static final int CHECK_INTERVAL_TICKS = 20; // 每20 ticks检查一次（1秒）

    private final Random random = new Random();

    // 用于GUI的数据访问
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> PrecisionStirrerBlockEntity.this.playerPH;
                case 1 -> PrecisionStirrerBlockEntity.this.targetPH;
                case 2 -> PrecisionStirrerBlockEntity.this.successCount;
                case 3 -> PrecisionStirrerBlockEntity.this.isProcessing ? 1 : 0;
                case 4 -> PrecisionStirrerBlockEntity.this.gameWon ? 1 : 0;
                case 5 -> PrecisionStirrerBlockEntity.this.gameLost ? 1 : 0;
                case 6 -> PrecisionStirrerBlockEntity.this.currentMode.ordinal();
                case 7 -> PrecisionStirrerBlockEntity.this.movementDirection;
                case 8 -> PrecisionStirrerBlockEntity.this.movementSpeed;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> PrecisionStirrerBlockEntity.this.playerPH = value;
                case 1 -> PrecisionStirrerBlockEntity.this.targetPH = value;
                case 2 -> PrecisionStirrerBlockEntity.this.successCount = value;
                case 3 -> PrecisionStirrerBlockEntity.this.isProcessing = value != 0;
                case 4 -> PrecisionStirrerBlockEntity.this.gameWon = value != 0;
                case 5 -> PrecisionStirrerBlockEntity.this.gameLost = value != 0;
                case 6 -> PrecisionStirrerBlockEntity.this.currentMode = MovementMode.values()[value];
                case 7 -> PrecisionStirrerBlockEntity.this.movementDirection = value;
                case 8 -> PrecisionStirrerBlockEntity.this.movementSpeed = value;
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    public PrecisionStirrerBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.PRECISION_STIRRER_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fuzhouplan.precision_stirrer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PrecisionStirrerMenu(containerId, inventory, this);
    }

    // 方块实体tick逻辑
    public static void tick(Level level, BlockPos pos, BlockState state, PrecisionStirrerBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        // 检查是否可以开始处理
        if (!blockEntity.isProcessing) {
            if (blockEntity.canStartProcessing()) {
                blockEntity.startProcessing();
            }
            return;
        }

        // 结果显示冷却
        if (blockEntity.resultCooldown > 0) {
            blockEntity.resultCooldown--;
            if (blockEntity.resultCooldown <= 0) {
                blockEntity.finishGame();
            }
            return;
        }

        // 更新目标PH值移动
        blockEntity.updateTargetPH();

        // 每隔一定时间检查PH值匹配
        blockEntity.checkInterval++;
        if (blockEntity.checkInterval >= CHECK_INTERVAL_TICKS) {
            blockEntity.checkInterval = 0;
            blockEntity.checkPHMatch();
        }

        blockEntity.setChanged();
    }

    private boolean canStartProcessing() {
        ItemStack ioSlot = itemHandler.getStackInSlot(IO_SLOT);
        if (ioSlot.isEmpty() || ioSlot.getItem() != Fuzhouplan.NUCLEASE_FREE_WATER.get()) {
            return false;
        }

        ItemStack slot0 = itemHandler.getStackInSlot(INPUT_SLOT_0);
        ItemStack slot1 = itemHandler.getStackInSlot(INPUT_SLOT_1);
        return !slot0.isEmpty() && !slot1.isEmpty();
    }

    private void startProcessing() {
        isProcessing = true;
        gameWon = false;
        gameLost = false;
        successCount = 0;
        playerPH = 50;
        targetPH = 50;
        checkInterval = 0;
        currentMode = MovementMode.CONSTANT;
        movementDirection = random.nextBoolean() ? 1 : -1;
        movementSpeed = 1;
        ticksInMode = 0;
        nextModeChange = 40 + random.nextInt(60);
        setChanged();
    }

    private void updateTargetPH() {
        ticksInMode++;

        // 随机切换移动模式
        if (ticksInMode >= nextModeChange) {
            changeMovementMode();
        }

        // 根据模式移动目标PH
        int moveAmount = 0;
        switch (currentMode) {
            case CONSTANT -> moveAmount = movementSpeed;
            case ACCELERATING -> {
                movementSpeed = Math.min(movementSpeed + 1, 5);
                moveAmount = movementSpeed;
            }
            case STOPPED -> moveAmount = 0;
        }

        // 应用移动
        targetPH += moveAmount * movementDirection;

        // 边界反弹
        if (targetPH >= PH_MAX) {
            targetPH = PH_MAX;
            movementDirection = -1;
        } else if (targetPH <= PH_MIN) {
            targetPH = PH_MIN;
            movementDirection = 1;
        }
    }

    private void changeMovementMode() {
        // 随机选择新模式
        int modeChance = random.nextInt(100);
        if (modeChance < 40) {
            currentMode = MovementMode.CONSTANT;
            movementSpeed = 1 + random.nextInt(2);
        } else if (modeChance < 70) {
            currentMode = MovementMode.ACCELERATING;
            movementSpeed = 1;
        } else {
            currentMode = MovementMode.STOPPED;
        }

        ticksInMode = 0;
        nextModeChange = 30 + random.nextInt(90);

        // 随机改变方向
        if (random.nextInt(100) < 30) {
            movementDirection *= -1;
        }
    }

    private void checkPHMatch() {
        int difference = Math.abs(playerPH - targetPH);

        if (difference > PH_TOLERANCE) {
            // PH值差异过大，合成失败
            gameLost = true;
            resultCooldown = 40; // 显示失败结果2秒
        } else {
            // PH值匹配
            successCount++;

            if (successCount >= 5) {
                // 连续5次成功，合成成功
                gameWon = true;
                resultCooldown = 40; // 显示成功结果2秒
            }
        }
    }

    private void finishGame() {
        if (gameWon) {
            // 合成成功，产出TE缓冲液
            produceOutput(true);
        } else if (gameLost) {
            // 合成失败，产出未知混合物
            produceOutput(false);
        }

        // 重置状态
        isProcessing = false;
        gameWon = false;
        gameLost = false;
        successCount = 0;
        resultCooldown = 0;
        setChanged();
    }

    private void produceOutput(boolean success) {
        itemHandler.extractItem(INPUT_SLOT_0, 1, false);
        itemHandler.extractItem(INPUT_SLOT_1, 1, false);

        Item resultItem = success ? Fuzhouplan.TE_BUFFER_CAN.get() : Fuzhouplan.UNKNOWN_MIXTURE.get();
        itemHandler.setStackInSlot(IO_SLOT, new ItemStack(resultItem, 1));
    }

    // 按钮操作方法（由网络包调用）
    public void adjustPlayerPH(int amount) {
        if (!isProcessing || resultCooldown > 0) {
            return;
        }
        playerPH = Math.max(PH_MIN, Math.min(PH_MAX, playerPH + amount));
        setChanged();
    }

    public void increasePH() {
        adjustPlayerPH(20);
    }

    public void increasePHSlightly() {
        adjustPlayerPH(5);
    }

    public void decreasePHSlightly() {
        adjustPlayerPH(-5);
    }

    public void decreasePH() {
        adjustPlayerPH(-20);
    }

    // NBT数据保存和加载
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("playerPH", playerPH);
        tag.putInt("targetPH", targetPH);
        tag.putInt("successCount", successCount);
        tag.putBoolean("isProcessing", isProcessing);
        tag.putBoolean("gameWon", gameWon);
        tag.putBoolean("gameLost", gameLost);
        tag.putInt("checkInterval", checkInterval);
        tag.putInt("currentMode", currentMode.ordinal());
        tag.putInt("movementDirection", movementDirection);
        tag.putInt("movementSpeed", movementSpeed);
        tag.putInt("ticksInMode", ticksInMode);
        tag.putInt("nextModeChange", nextModeChange);
        tag.putInt("resultCooldown", resultCooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        playerPH = tag.getInt("playerPH");
        targetPH = tag.getInt("targetPH");
        successCount = tag.getInt("successCount");
        isProcessing = tag.getBoolean("isProcessing");
        gameWon = tag.getBoolean("gameWon");
        gameLost = tag.getBoolean("gameLost");
        checkInterval = tag.getInt("checkInterval");
        currentMode = MovementMode.values()[tag.getInt("currentMode")];
        movementDirection = tag.getInt("movementDirection");
        movementSpeed = tag.getInt("movementSpeed");
        ticksInMode = tag.getInt("ticksInMode");
        nextModeChange = tag.getInt("nextModeChange");
        resultCooldown = tag.getInt("resultCooldown");
    }

    // 物品丢弃（方块破坏时）
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        net.minecraft.world.Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // WorldlyContainer 实现
    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{INPUT_SLOT_0, INPUT_SLOT_1, IO_SLOT};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return switch (index) {
            case INPUT_SLOT_0, INPUT_SLOT_1 -> true;
            case IO_SLOT -> itemStack.getItem() == Fuzhouplan.NUCLEASE_FREE_WATER.get();
            default -> false;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == IO_SLOT && stack.getItem() != Fuzhouplan.NUCLEASE_FREE_WATER.get();
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this &&
               player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // Capability支持
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    // Getter方法
    public int getPlayerPH() {
        return playerPH;
    }

    public int getTargetPH() {
        return targetPH;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public MovementMode getCurrentMode() {
        return currentMode;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }
}
