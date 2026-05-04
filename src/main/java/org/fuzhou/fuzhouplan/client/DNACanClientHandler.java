package org.fuzhou.fuzhouplan.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.item.DNACanItem;
import org.fuzhou.fuzhouplan.item.DNACanRegistry;
import org.fuzhou.fuzhouplan.item.UnresolvedDNACanItem;

@Mod.EventBusSubscriber(modid = Fuzhouplan.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class DNACanClientHandler {

    private static final ResourceLocation UNRESOLVED_BASE_MODEL = new ResourceLocation(Fuzhouplan.MODID, "item/unresolved_dna_can");
    private static final ResourceLocation RESOLVED_BASE_MODEL = new ResourceLocation(Fuzhouplan.MODID, "item/dna_can");

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(UNRESOLVED_BASE_MODEL);
        event.register(RESOLVED_BASE_MODEL);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        BakedModel unresolvedBase = event.getModels().get(UNRESOLVED_BASE_MODEL);
        BakedModel resolvedBase = event.getModels().get(RESOLVED_BASE_MODEL);

        if (unresolvedBase != null) {
            for (UnresolvedDNACanItem item : DNACanRegistry.getAllUnresolvedCans().values()) {
                ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(item);
                if (itemRL != null) {
                    event.getModels().put(new ModelResourceLocation(itemRL, "inventory"), unresolvedBase);
                }
            }
        }

        if (resolvedBase != null) {
            for (DNACanItem item : DNACanRegistry.getAllResolvedCans().values()) {
                ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(item);
                if (itemRL != null) {
                    event.getModels().put(new ModelResourceLocation(itemRL, "inventory"), resolvedBase);
                }
            }
        }
    }
}
