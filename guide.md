# 浮舟计划 (FuZhou Plan) 开发指南

## 目录
- [项目介绍](#项目介绍)
- [学习路线](#学习路线)
- [常用方法详解](#常用方法详解)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

---

## 项目介绍

| 属性 | 值 |
|------|-----|
| **模组ID** | `fuzhouplan` |
| **Minecraft版本** | 1.20.1 |
| **Forge版本** | 47.4.10 |
| **Java版本** | 17 |
| **包名** | `org.fuzhou.fuzhouplan` |

---

## 学习路线

### 第一阶段：基础入门（1-3天）

1. **理解模组结构**
   ```
   src/main/java/org/fuzhou/fuzhouplan/
   ├── Fuzhouplan.java           # 主类，所有注册的入口
   ├── block/                    # 方块实现
   ├── item/                     # 物品实现
   ├── blockentity/              # 方块实体（机器核心）
   ├── menu/                     # GUI菜单
   ├── client/gui/               # 客户端GUI
   ├── network/                  # 网络包
   └── event/                    # 事件处理
   ```

2. **运行项目**
   ```bash
   ./gradlew build    # 构建
   ./gradlew runClient  # 运行客户端测试
   ```

3. **阅读主类 `Fuzhouplan.java`**
   - 理解 `DeferredRegister` 注册系统
   - 理解所有注册项的声明位置

### 第二阶段：简单物品/方块（3-7天）

1. **创建简单物品**
   ```java
   // 在 Fuzhouplan.java 中添加
   public static final RegistryObject<Item> MY_ITEM = ITEMS.register("my_item", 
       () -> new Item(new Item.Properties().stacksTo(64)));
   ```

2. **创建自定义物品类**（如 `BioAnestheticItem.java`）
   - 重写 `interactLivingEntity()` - 与生物交互
   - 重写 `appendHoverText()` - 添加提示信息

3. **创建简单方块**（如 `GreenBerryBushBlock.java`）
   - 继承 `BushBlock` 创建作物
   - 实现 `BonemealableBlock` 支持骨粉

### 第三阶段：机器方块（7-14天）

1. **理解机器四件套**
   ```
   Block (方块) → BlockEntity (方块实体) → Menu (菜单) → Screen (GUI屏幕)
   ```

2. **方块实体核心要素**
   - `ItemStackHandler` - 物品存储
   - `tick()` - 逻辑处理
   - `saveAdditional()/load()` - NBT持久化
   - `ContainerData` - GUI数据同步

3. **菜单核心要素**
   - `AbstractContainerMenu` - 菜单逻辑
   - `SimpleContainerData` - 同步数据

4. **GUI屏幕核心要素**
   - `Screen` - 渲染界面
   - `MenuScreens.register()` - 注册到游戏

### 第四阶段：高级功能（14天+）

- 网络包通信 (`NetworkHandler`)
- 事件系统 (`@SubscribeEvent`)
- Mixin 注入
- 自定义生物群系
- 自定义生物

---

## 常用方法详解

### 1. 注册物品

**简单物品（无自定义逻辑）**
```java
// 位置: Fuzhouplan.java

// 基本注册
public static final RegistryObject<Item> IRON_CAN = ITEMS.register("iron_can", 
    () -> new Item(new Item.Properties().stacksTo(64)));

// 带食物属性
public static final RegistryObject<Item> GREEN_BERRY = ITEMS.register("green_berry", 
    () -> new GreenBerryItem(new Item.Properties()
        .food(new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3f)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0f)
            .build())));
```

**自定义物品类**
```java
// 位置: item/MyItem.java
public class MyItem extends Item {
    public MyItem(Properties properties) {
        super(properties.stacksTo(16));  // 最大堆叠数
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // 右键使用逻辑
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, 
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fuzhouplan.my_item.tooltip")
            .withStyle(ChatFormatting.GRAY));
    }
}
```

### 2. 注册方块

**简单方块**
```java
// 基本方块
public static final RegistryObject<Block> MY_BLOCK = BLOCKS.register("my_block", 
    () -> new Block(BlockBehaviour.Properties.of()
        .mapColor(MapColor.STONE)
        .strength(2.5f)
        .sound(SoundType.STONE)));

// 作物方块（继承 BushBlock）
public static final RegistryObject<Block> MY_BUSH = BLOCKS.register("my_bush", 
    () -> new MyBushBlock(BlockBehaviour.Properties.of()
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(SoundType.SWEET_BERRY_BUSH)));
```

**带方块实体的机器方块**
```java
// 方块注册
public static final RegistryObject<Block> MY_MACHINE = BLOCKS.register("my_machine", 
    () -> new MyMachineBlock(BlockBehaviour.Properties.of()
        .mapColor(MapColor.METAL)
        .strength(3.0f)
        .sound(SoundType.METAL)));

// 方块物品注册（让方块可以在创造模式下放置）
public static final RegistryObject<Item> MY_MACHINE_ITEM = ITEMS.register("my_machine", 
    () -> new BlockItem(MY_MACHINE.get(), new Item.Properties()));

// 方块实体注册
public static final RegistryObject<BlockEntityType<MyMachineBlockEntity>> 
    MY_MACHINE_ENTITY = BLOCK_ENTITIES.register("my_machine",
        () -> BlockEntityType.Builder.of(
            MyMachineBlockEntity::new, 
            MY_MACHINE.get()
        ).build(null));
```

### 3. 方块实体 (BlockEntity)

```java
public class MyMachineBlockEntity extends BlockEntity implements MenuProvider {
    
    // 物品存储
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();  // 通知方块状态改变
        }
    };
    
    // GUI 数据同步
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) { return progress; }
        @Override
        public void set(int index, int value) { progress = value; }
        @Override
        public int getCount() { return 2; }
    };
    
    public MyMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Fuzhouplan.MY_MACHINE_ENTITY.get(), pos, state);
    }
    
    // Tick 逻辑
    public static void tick(Level level, BlockPos pos, BlockState state, 
                           MyMachineBlockEntity blockEntity) {
        if (level.isClientSide) return;
        // 逻辑处理
    }
    
    // NBT 保存
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
    }
    
    // NBT 加载
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}
```

### 4. 菜单 (Menu)

```java
public class MyMachineMenu extends AbstractContainerMenu {
    
    private final MyMachineBlockEntity blockEntity;
    private final Level level;
    
    public MyMachineMenu(int containerId, Inventory inventory, 
                         Component title, MyMachineBlockEntity blockEntity) {
        super(Fuzhouplan.MY_MACHINE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = inventory.player.level();
        
        // 添加物品槽
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 24));
        
        // 添加玩家背包槽
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        
        // 添加玩家快捷栏
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }
}
```

### 5. GUI 屏幕 (Screen)

```java
public class MyMachineScreen extends Screen {
    
    private final MyMachineMenu menu;
    private final ResourceLocation texture = 
        new ResourceLocation(Fuzhouplan.MODID, "textures/gui/my_machine.png");
    
    public MyMachineScreen(MyMachineMenu menu, Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, 
                           int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        this.blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
```

### 6. 客户端设置

```java
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // 注册渲染层（透明方块需要）
            ItemBlockRenderTypes.setRenderLayer(GREEN_BERRY_BUSH.get(), 
                RenderType.cutout());
            
            // 注册 GUI 屏幕
            MenuScreens.register(PRECISION_STIRRER_MENU.get(), 
                PrecisionStirrerScreen::new);
        });
    }
}
```

### 7. 网络包

```java
// NetworkHandler.java
public static void register() {
    INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Fuzhouplan.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    INSTANCE.messageBuilder(MyPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
        .encoder(MyPacket::encode)
        .decoder(MyPacket::new)
        .consumerMainThread(MyPacket::handle)
        .add();
}

// 数据包
public class MyPacket {
    private int data;
    
    public MyPacket(FriendlyByteBuf buf) {
        this.data = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(data);
    }
    
    public void handle(Supplier<NetworkTesting.PacketDistributor.PacketTarget> ctx) {
        // 逻辑处理
    }
}
```

### 8. 创造模式标签页

```java
public static final RegistryObject<CreativeModeTab> FUZHOU_TAB = 
    CREATIVE_MODE_TABS.register("fuzhou_tab", () -> 
        CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> BIO_ANESTHETIC.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.fuzhouplan"))
            .displayItems((parameters, output) -> {
                output.accept(SYRINGE.get());
                output.accept(BIO_ANESTHETIC.get());
                // 添加更多物品...
            }).build());
```

---

## 开发规范

### 1. 文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 方块 | `XxxBlock.java` | `FermentationBarrelBlock.java` |
| 方块实体 | `XxxBlockEntity.java` | `FermentationBarrelBlockEntity.java` |
| 物品 | `XxxItem.java` | `BioAnestheticItem.java` |
| 菜单 | `XxxMenu.java` | `FermentationBarrelMenu.java` |
| GUI | `XxxScreen.java` | `FermentationBarrelScreen.java` |

### 2. 注册名称

- 使用下划线分隔小写: `green_berry_bush`
- 与文件名对应
- 统一在 `Fuzhouplan.java` 中声明

### 3. 目录结构

```
org.fuzhou.fuzhouplan/
├── block/              # 方块实现
├── item/               # 物品实现
├── blockentity/        # 方块实体
├── menu/               # 容器菜单
├── client/gui/         # 客户端GUI
├── network/            # 网络通信
└── event/              # 事件处理
```

### 4. 代码规范

- 使用 `@NotNull` / `@Nullable` 注解
- 使用 `Component.translatable()` 处理文本
- 使用 `ChatFormatting` 处理颜色
- `BlockEntity` 必须实现 `MenuProvider`
- GUI 相关代码使用 `@Dist.CLIENT`

### 5. 资源文件位置

```
src/main/resources/
├── assets/fuzhouplan/
│   ├── lang/zh_cn.json    # 中文语言
│   ├── lang/en_us.json    # 英文语言
│   ├── blockstates/       # 方块状态
│   ├── models/block/      # 方块模型
│   ├── models/item/       # 物品模型
│   ├── textures/block/    # 方块纹理
│   ├── textures/item/     # 物品纹理
│   └── textures/gui/      # GUI纹理
└── data/fuzhouplan/
    └── recipes/           # 合成配方 JSON
```

### 6. 注册顺序

当添加新内容时，确保：

1. **先注册物品/方块**，再注册方块实体
2. **GUI 菜单**需要对应方块实体已存在
3. **客户端事件**使用 `@Dist.CLIENT` 注解
4. **网络包**在 `commonSetup` 中注册

### 7. 方块实体模式

对于带 GUI 的机器，必须注册四个部分：

```
方块 (Block) → 方块实体 (BlockEntity) → 菜单 (Menu) → 屏幕 (Screen)
```

缺一不可，且注册顺序为：方块实体 → 菜单 → 屏幕

---

## 常见问题

### Q: 为什么我的方块没有 GUI？
A: 检查是否：
1. 方块实体实现了 `MenuProvider`
2. 菜单已注册到 `MENUS`
3. 屏幕已注册到 `MenuScreens`
4. 使用 `NetworkHooks.openScreen()` 打开

### Q: 方块破坏后物品没掉落？
A: 重写 `onRemove()` 方法，调用方块实体的 `drops()` 方法

### Q: 机器不工作（tick 不执行）？
A: 检查：
1. 方块重写了 `getTicker()` 方法
2. `tick()` 方法中检查了 `level.isClientSide`

### Q: 物品/方块注册了但游戏里没有？
A: 检查：
1. 资源文件（模型、纹理、语言）是否完整
2. 物品是否添加到 `FUZHOU_TAB` 的 `displayItems` 中

### Q: 如何调试？
A: 使用 `LOGGER.info()` 输出日志，在 `commonSetup` 中打印调试信息

---

## 推荐学习资源

- [Minecraft Forge 官方文档](https://docs.minecraftforge.net/)
- [MCJavadoc](https:// oscuras.github.io/MCJavadoc/1.20.1/)
- Forge 源码学习现有代码

---

*最后更新: 2026-05-05*
