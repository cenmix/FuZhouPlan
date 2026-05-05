package org.fuzhou.fuzhouplan;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import org.fuzhou.fuzhouplan.item.BioAnestheticItem;
import org.fuzhou.fuzhouplan.item.MolecularArmorItem;
import org.fuzhou.fuzhouplan.item.BioGeneExtractorItem;
import org.fuzhou.fuzhouplan.item.SyringeItem;
import org.fuzhou.fuzhouplan.item.GreenBerryItem;
import org.fuzhou.fuzhouplan.item.BlueBerryItem;
import org.fuzhou.fuzhouplan.item.GlowingBlueDyeBucketItem;
import org.fuzhou.fuzhouplan.item.GlowingBlueDyeItem;
import org.fuzhou.fuzhouplan.fluid.GlowingBlueDyeFluid;
import org.fuzhou.fuzhouplan.fluid.GlowingBlueDyeFluidType;
import org.fuzhou.fuzhouplan.fluid.GreenAnestheticFluid;
import org.fuzhou.fuzhouplan.fluid.GreenAnestheticFluidType;
import org.fuzhou.fuzhouplan.item.DNACanRegistry;
import org.fuzhou.fuzhouplan.item.DNACanItem;
import org.fuzhou.fuzhouplan.item.UnresolvedDNACanItem;
import org.fuzhou.fuzhouplan.block.GreenBerryBushBlock;
import org.fuzhou.fuzhouplan.block.BlueBerryBushBlock;
import org.fuzhou.fuzhouplan.block.FermentationBarrelBlock;
import org.fuzhou.fuzhouplan.block.MolecularDistillationTowerBlock;
import org.fuzhou.fuzhouplan.block.PrecisionStirrerBlock;
import org.fuzhou.fuzhouplan.block.DryerBlock;
import org.fuzhou.fuzhouplan.block.InfiniteGeneratorBlock;
import org.fuzhou.fuzhouplan.block.ResolverBlock;
import org.fuzhou.fuzhouplan.blockentity.FermentationBarrelBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.InfiniteGeneratorBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.MolecularDistillationTowerBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.PrecisionStirrerBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.DryerBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.ResolverBlockEntity;
import org.fuzhou.fuzhouplan.menu.PrecisionStirrerMenu;
import org.fuzhou.fuzhouplan.menu.FermentationBarrelMenu;
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;
import org.fuzhou.fuzhouplan.menu.DryerMenu;
import org.fuzhou.fuzhouplan.menu.ResolverMenu;
import org.fuzhou.fuzhouplan.network.NetworkHandler;
import org.fuzhou.fuzhouplan.recipe.ModRecipeTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import org.fuzhou.fuzhouplan.client.gui.PrecisionStirrerScreen;
import org.fuzhou.fuzhouplan.client.gui.FermentationBarrelScreen;
import org.fuzhou.fuzhouplan.client.gui.MolecularDistillationTowerScreen;
import org.fuzhou.fuzhouplan.client.gui.DryerScreen;
import org.fuzhou.fuzhouplan.client.gui.ResolverScreen;
import org.slf4j.Logger;

@Mod(Fuzhouplan.MODID)
public class Fuzhouplan {

    public static final String MODID = "fuzhouplan";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

    public static final RegistryObject<Item> BIO_ANESTHETIC = ITEMS.register("bio_anesthetic", () -> new BioAnestheticItem(new Item.Properties()));
    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> BIO_GENE_EXTRACTOR = ITEMS.register("bio_gene_extractor", () -> new BioGeneExtractorItem(new Item.Properties()));
    public static final RegistryObject<Item> GREEN_BERRY = ITEMS.register("green_berry", () -> new GreenBerryItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0f).build())));
    public static final RegistryObject<Block> GREEN_BERRY_BUSH = BLOCKS.register("green_berry_bush", () -> new GreenBerryBushBlock(BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)));

    public static final RegistryObject<Item> BLUE_BERRY = ITEMS.register("blue_berry", () -> new BlueBerryItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0), 1.0f).build())));
    public static final RegistryObject<Block> BLUE_BERRY_BUSH = BLOCKS.register("blue_berry_bush", () -> new BlueBerryBushBlock(BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)));

    public static final RegistryObject<Item> GLOWING_BLUE_DYE_BUCKET = ITEMS.register("glowing_blue_dye_bucket", () -> new BucketItem(GlowingBlueDyeFluid.SOURCE, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GLOWING_BLUE_DYE = ITEMS.register("glowing_blue_dye", () -> new GlowingBlueDyeItem(new Item.Properties().stacksTo(64)));

    // 发光蓝色染料流体
    public static final RegistryObject<FluidType> GLOWING_BLUE_DYE_FLUID_TYPE = FLUID_TYPES.register("glowing_blue_dye", GlowingBlueDyeFluidType::new);

    public static final RegistryObject<GlowingBlueDyeFluid.Source> GLOWING_BLUE_DYE_SOURCE = FLUIDS.register("glowing_blue_dye", () -> new GlowingBlueDyeFluid.Source(GlowingBlueDyeFluid.makeProperties()));
    public static final RegistryObject<GlowingBlueDyeFluid.Flowing> GLOWING_BLUE_DYE_FLOWING = FLUIDS.register("flowing_glowing_blue_dye", () -> new GlowingBlueDyeFluid.Flowing(GlowingBlueDyeFluid.makeProperties()));

    public static final RegistryObject<LiquidBlock> GLOWING_BLUE_DYE_BLOCK = BLOCKS.register("glowing_blue_dye", () -> new LiquidBlock(GLOWING_BLUE_DYE_SOURCE, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WATER).noCollission().strength(100.0F).noLootTable()));

    // 绿色麻醉液流体
    public static final RegistryObject<Item> GREEN_ANESTHETIC_BUCKET = ITEMS.register("green_anesthetic_bucket", () -> new BucketItem(GreenAnestheticFluid.SOURCE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<FluidType> GREEN_ANESTHETIC_FLUID_TYPE = FLUID_TYPES.register("green_anesthetic", GreenAnestheticFluidType::new);

    public static final RegistryObject<GreenAnestheticFluid.Source> GREEN_ANESTHETIC_SOURCE = FLUIDS.register("green_anesthetic", () -> new GreenAnestheticFluid.Source(GreenAnestheticFluid.makeProperties()));
    public static final RegistryObject<GreenAnestheticFluid.Flowing> GREEN_ANESTHETIC_FLOWING = FLUIDS.register("flowing_green_anesthetic", () -> new GreenAnestheticFluid.Flowing(GreenAnestheticFluid.makeProperties()));

    public static final RegistryObject<LiquidBlock> GREEN_ANESTHETIC_BLOCK = BLOCKS.register("green_anesthetic", () -> new LiquidBlock(GREEN_ANESTHETIC_SOURCE, net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).noCollission().strength(100.0F).noLootTable()));

    // 分子材料注册
    public static final RegistryObject<Item> MOLECULAR_MATERIAL = ITEMS.register("molecular_material", () -> new Item(new Item.Properties().stacksTo(64)));

    // 分子盔甲注册
    public static final RegistryObject<Item> MOLECULAR_HELMET = ITEMS.register("molecular_helmet",
            () -> new MolecularArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> MOLECULAR_CHESTPLATE = ITEMS.register("molecular_chestplate",
            () -> new MolecularArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> MOLECULAR_LEGGINGS = ITEMS.register("molecular_leggings",
            () -> new MolecularArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> MOLECULAR_BOOTS = ITEMS.register("molecular_boots",
            () -> new MolecularArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));



    // Task 1: 基础物品注册
    public static final RegistryObject<Item> IRON_CAN = ITEMS.register("iron_can", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> LIME_POWDER = ITEMS.register("lime_powder", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> DISTILLED_WATER = ITEMS.register("distilled_water", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> NUCLEASE_FREE_WATER = ITEMS.register("nuclease_free_water", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> AMMONIA_BOTTLE = ITEMS.register("ammonia_bottle", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> VINEGAR_BOTTLE = ITEMS.register("vinegar_bottle", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> ETHYLENEDIAMINE = ITEMS.register("ethylenediamine", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TRIS_PASTE = ITEMS.register("tris_paste", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TRIS_POWDER = ITEMS.register("tris_powder", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> EDTA_PASTE = ITEMS.register("edta_paste", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> EDTA_POWDER = ITEMS.register("edta_powder", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TE_BUFFER = ITEMS.register("te_buffer", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> TE_BUFFER_CAN = ITEMS.register("te_buffer_can", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> UNKNOWN_MIXTURE = ITEMS.register("unknown_mixture", () -> new Item(new Item.Properties().stacksTo(64)));
    
    // DNA储存罐物品通过DNACanRegistry动态注册

    // Task 5: 发酵桶
    public static final RegistryObject<Block> FERMENTATION_BARREL = BLOCKS.register("fermentation_barrel", 
            () -> new FermentationBarrelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(net.minecraft.world.level.block.SoundType.WOOD)));
    public static final RegistryObject<Item> FERMENTATION_BARREL_ITEM = ITEMS.register("fermentation_barrel", 
            () -> new BlockItem(FERMENTATION_BARREL.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FermentationBarrelBlockEntity>> FERMENTATION_BARREL_ENTITY = BLOCK_ENTITIES.register("fermentation_barrel",
            () -> BlockEntityType.Builder.of(FermentationBarrelBlockEntity::new, FERMENTATION_BARREL.get()).build(null));

    // Task 6: 分子蒸馏塔
    public static final RegistryObject<Block> MOLECULAR_DISTILLATION_TOWER = BLOCKS.register("molecular_distillation_tower",
            () -> new MolecularDistillationTowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).sound(net.minecraft.world.level.block.SoundType.METAL)));
    public static final RegistryObject<Item> MOLECULAR_DISTILLATION_TOWER_ITEM = ITEMS.register("molecular_distillation_tower",
            () -> new BlockItem(MOLECULAR_DISTILLATION_TOWER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<MolecularDistillationTowerBlockEntity>> MOLECULAR_DISTILLATION_TOWER_ENTITY = BLOCK_ENTITIES.register("molecular_distillation_tower",
            () -> BlockEntityType.Builder.of(MolecularDistillationTowerBlockEntity::new, MOLECULAR_DISTILLATION_TOWER.get()).build(null));

    // Task 7: 精密搅拌器
    public static final RegistryObject<Block> PRECISION_STIRRER = BLOCKS.register("precision_stirrer",
            () -> new PrecisionStirrerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(net.minecraft.world.level.block.SoundType.METAL)));
    public static final RegistryObject<Item> PRECISION_STIRRER_ITEM = ITEMS.register("precision_stirrer",
            () -> new BlockItem(PRECISION_STIRRER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<PrecisionStirrerBlockEntity>> PRECISION_STIRRER_ENTITY = BLOCK_ENTITIES.register("precision_stirrer",
            () -> BlockEntityType.Builder.of(PrecisionStirrerBlockEntity::new, PRECISION_STIRRER.get()).build(null));
    public static final RegistryObject<MenuType<PrecisionStirrerMenu>> PRECISION_STIRRER_MENU = MENUS.register("precision_stirrer",
            () -> IForgeMenuType.create(PrecisionStirrerMenu::new));

    public static final RegistryObject<MenuType<FermentationBarrelMenu>> FERMENTATION_BARREL_MENU = MENUS.register("fermentation_barrel",
            () -> IForgeMenuType.create(FermentationBarrelMenu::new));

    public static final RegistryObject<MenuType<MolecularDistillationTowerMenu>> MOLECULAR_DISTILLATION_TOWER_MENU = MENUS.register("molecular_distillation_tower",
            () -> IForgeMenuType.create(MolecularDistillationTowerMenu::new));

    // 烘干机
    public static final RegistryObject<Block> DRYER = BLOCKS.register("dryer",
            () -> new DryerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(net.minecraft.world.level.block.SoundType.METAL)));
    public static final RegistryObject<Item> DRYER_ITEM = ITEMS.register("dryer",
            () -> new BlockItem(DRYER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<DryerBlockEntity>> DRYER_ENTITY = BLOCK_ENTITIES.register("dryer",
            () -> BlockEntityType.Builder.of(DryerBlockEntity::new, DRYER.get()).build(null));
    public static final RegistryObject<MenuType<DryerMenu>> DRYER_MENU = MENUS.register("dryer",
            () -> IForgeMenuType.create(DryerMenu::new));

    // 解析器
    public static final RegistryObject<Block> RESOLVER = BLOCKS.register("resolver",
            () -> new ResolverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).sound(net.minecraft.world.level.block.SoundType.METAL)));
    public static final RegistryObject<Item> RESOLVER_ITEM = ITEMS.register("resolver",
            () -> new BlockItem(RESOLVER.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ResolverBlockEntity>> RESOLVER_ENTITY = BLOCK_ENTITIES.register("resolver",
            () -> BlockEntityType.Builder.of(ResolverBlockEntity::new, RESOLVER.get()).build(null));
    public static final RegistryObject<MenuType<ResolverMenu>> RESOLVER_MENU = MENUS.register("resolver",
            () -> IForgeMenuType.create(ResolverMenu::new));

    // 无限发电机
    public static final RegistryObject<Block> INFINITE_GENERATOR = BLOCKS.register("infinite_generator",
            () -> new InfiniteGeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).sound(net.minecraft.world.level.block.SoundType.METAL).lightLevel(state -> 15)));
    public static final RegistryObject<Item> INFINITE_GENERATOR_ITEM = ITEMS.register("infinite_generator",
            () -> new BlockItem(INFINITE_GENERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<InfiniteGeneratorBlockEntity>> INFINITE_GENERATOR_ENTITY = BLOCK_ENTITIES.register("infinite_generator",
            () -> BlockEntityType.Builder.of(InfiniteGeneratorBlockEntity::new, INFINITE_GENERATOR.get()).build(null));

    //创造模式物品栏标签页 #1
    public static final RegistryObject<CreativeModeTab> FUZHOU_TAB = CREATIVE_MODE_TABS.register("fuzhou_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> BIO_ANESTHETIC.get().getDefaultInstance()).title(Component.translatable("itemGroup.fuzhouplan")).displayItems((parameters, output) -> {
        output.accept(SYRINGE.get());
        output.accept(BIO_ANESTHETIC.get());
        output.accept(GREEN_BERRY.get());
        // Task 1: 新物品
        output.accept(IRON_CAN.get());
        output.accept(LIME_POWDER.get());
        output.accept(DISTILLED_WATER.get());
        output.accept(NUCLEASE_FREE_WATER.get());
        output.accept(AMMONIA_BOTTLE.get());
        output.accept(VINEGAR_BOTTLE.get());
        output.accept(ETHYLENEDIAMINE.get());
        output.accept(TRIS_PASTE.get());
        output.accept(TRIS_POWDER.get());
        output.accept(EDTA_PASTE.get());
        output.accept(EDTA_POWDER.get());
        output.accept(TE_BUFFER.get());
        output.accept(TE_BUFFER_CAN.get());
        output.accept(UNKNOWN_MIXTURE.get());
        output.accept(BIO_GENE_EXTRACTOR.get());
        output.accept(BLUE_BERRY.get());
        output.accept(GLOWING_BLUE_DYE_BUCKET.get());
        output.accept(GLOWING_BLUE_DYE.get());
        output.accept(GREEN_ANESTHETIC_BUCKET.get());
        for (UnresolvedDNACanItem item : DNACanRegistry.getAllUnresolvedCans().values()) {
            output.accept(item);
        }
        for (DNACanItem item : DNACanRegistry.getAllResolvedCans().values()) {
            output.accept(item);
        }
        // Task 5: 发酵桶
        output.accept(FERMENTATION_BARREL_ITEM.get());
        // Task 6: 分子蒸馏塔
        output.accept(MOLECULAR_DISTILLATION_TOWER_ITEM.get());
        // Task 7: 精密搅拌器
        output.accept(PRECISION_STIRRER_ITEM.get());
        // 烘干机
        output.accept(DRYER_ITEM.get());
        // 解析器
        output.accept(RESOLVER_ITEM.get());
        // 无限发电机
        output.accept(INFINITE_GENERATOR_ITEM.get());
    }).build());

    //创造模式物品栏 #2 分子物品
    public static final RegistryObject<CreativeModeTab> FUZHOU_TAB_MOLECULAR_ITEMS = CREATIVE_MODE_TABS.register("fuzhou_tab_molecular_items", () -> CreativeModeTab.builder().withTabsAfter(FUZHOU_TAB.getId()).icon(() -> MOLECULAR_MATERIAL.get().getDefaultInstance()).title(Component.translatable("itemGroup.fuzhouplan_molecular_items")).displayItems((parameters, output) -> {
        output.accept(MOLECULAR_MATERIAL.get());
        // 分子盔甲
        output.accept(MOLECULAR_HELMET.get());
        output.accept(MOLECULAR_CHESTPLATE.get());
        output.accept(MOLECULAR_LEGGINGS.get());
        output.accept(MOLECULAR_BOOTS.get());

    }).build());

    public Fuzhouplan() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeTypes.SERIALIZERS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
        
        // 注册网络包
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(GREEN_BERRY_BUSH.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(BLUE_BERRY_BUSH.get(), RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(GLOWING_BLUE_DYE_SOURCE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(GLOWING_BLUE_DYE_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(GREEN_ANESTHETIC_SOURCE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(GREEN_ANESTHETIC_FLOWING.get(), RenderType.translucent());
                MenuScreens.register(PRECISION_STIRRER_MENU.get(), PrecisionStirrerScreen::new);
                MenuScreens.register(FERMENTATION_BARREL_MENU.get(), FermentationBarrelScreen::new);
                MenuScreens.register(MOLECULAR_DISTILLATION_TOWER_MENU.get(), MolecularDistillationTowerScreen::new);
                MenuScreens.register(DRYER_MENU.get(), DryerScreen::new);
                MenuScreens.register(RESOLVER_MENU.get(), ResolverScreen::new);
            });
        }
    }
}
