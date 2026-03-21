package org.fuzhou.fuzhouplan;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.fuzhou.fuzhouplan.item.BioAnestheticItem;
import org.fuzhou.fuzhouplan.item.SyringeItem;
import org.fuzhou.fuzhouplan.item.GreenBerryItem;
import org.fuzhou.fuzhouplan.block.GreenBerryBushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Mod(Fuzhouplan.MODID)
public class Fuzhouplan {

    public static final String MODID = "fuzhouplan";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> BIO_ANESTHETIC = ITEMS.register("bio_anesthetic", () -> new BioAnestheticItem(new Item.Properties()));
    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(new Item.Properties()));
    public static final RegistryObject<Item> GREEN_BERRY = ITEMS.register("green_berry", () -> new GreenBerryItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0f).build())));
    public static final RegistryObject<Block> GREEN_BERRY_BUSH = BLOCKS.register("green_berry_bush", () -> new GreenBerryBushBlock(BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(net.minecraft.world.level.block.SoundType.SWEET_BERRY_BUSH)));

    public static final RegistryObject<CreativeModeTab> FUZHOU_TAB = CREATIVE_MODE_TABS.register("fuzhou_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> BIO_ANESTHETIC.get().getDefaultInstance()).title(Component.translatable("itemGroup.fuzhouplan")).displayItems((parameters, output) -> {
        output.accept(SYRINGE.get());
        output.accept(BIO_ANESTHETIC.get());
        output.accept(GREEN_BERRY.get());
    }).build());

    public Fuzhouplan() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
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
            });
        }
    }
}
