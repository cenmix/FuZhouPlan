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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.fuzhou.fuzhouplan.item.BioAnestheticItem;
import org.fuzhou.fuzhouplan.item.BioGeneExtractorItem;
import org.fuzhou.fuzhouplan.item.SyringeItem;
import org.fuzhou.fuzhouplan.item.GreenBerryItem;
import org.fuzhou.fuzhouplan.item.DNACanItem;
import org.fuzhou.fuzhouplan.item.UnresolvedDNACanItem;
import org.fuzhou.fuzhouplan.block.GreenBerryBushBlock;
import org.fuzhou.fuzhouplan.block.FermentationBarrelBlock;
import org.fuzhou.fuzhouplan.block.MolecularDistillationTowerBlock;
import org.fuzhou.fuzhouplan.block.PrecisionStirrerBlock;
import org.fuzhou.fuzhouplan.blockentity.FermentationBarrelBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.MolecularDistillationTowerBlockEntity;
import org.fuzhou.fuzhouplan.blockentity.PrecisionStirrerBlockEntity;
import org.fuzhou.fuzhouplan.menu.PrecisionStirrerMenu;
import org.fuzhou.fuzhouplan.menu.FermentationBarrelMenu;
import org.fuzhou.fuzhouplan.menu.MolecularDistillationTowerMenu;
import org.fuzhou.fuzhouplan.network.NetworkHandler;
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

    public static final RegistryObject<Item> BIO_ANESTHETIC = ITEMS.register("bio_anesthetic", () -> new BioAnestheticItem(new Item.Properties()));
    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> BIO_GENE_EXTRACTOR = ITEMS.register("bio_gene_extractor", () -> new BioGeneExtractorItem(new Item.Properties()));
    public static final RegistryObject<Item> GREEN_BERRY = ITEMS.register("green_berry", () -> new GreenBerryItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0f).build())));
    public static final RegistryObject<Block> GREEN_BERRY_BUSH = BLOCKS.register("green_berry_bush", () -> new GreenBerryBushBlock(BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)));

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
    
    // DNA储存罐物品
    public static final RegistryObject<Item> DNA_CAN = ITEMS.register("dna_can", () -> new DNACanItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> UNRESOLVED_DNA_CAN = ITEMS.register("unresolved_dna_can", () -> new UnresolvedDNACanItem(new Item.Properties().stacksTo(64)));

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
        output.accept(DNA_CAN.get());
        output.accept(UNRESOLVED_DNA_CAN.get());
        // Task 5: 发酵桶
        output.accept(FERMENTATION_BARREL_ITEM.get());
        // Task 6: 分子蒸馏塔
        output.accept(MOLECULAR_DISTILLATION_TOWER_ITEM.get());
        // Task 7: 精密搅拌器
        output.accept(PRECISION_STIRRER_ITEM.get());
    }).build());

    public Fuzhouplan() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
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
                MenuScreens.register(PRECISION_STIRRER_MENU.get(), PrecisionStirrerScreen::new);
                MenuScreens.register(FERMENTATION_BARREL_MENU.get(), FermentationBarrelScreen::new);
                MenuScreens.register(MOLECULAR_DISTILLATION_TOWER_MENU.get(), MolecularDistillationTowerScreen::new);
            });
        }
    }
}
