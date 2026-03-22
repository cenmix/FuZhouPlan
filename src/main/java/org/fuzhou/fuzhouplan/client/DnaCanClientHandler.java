package org.fuzhou.fuzhouplan.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.fuzhou.fuzhouplan.Fuzhouplan;
import org.fuzhou.fuzhouplan.item.DnaCanManager;

@Mod.EventBusSubscriber(modid = Fuzhouplan.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DnaCanClientHandler {

    private static final ResourceLocation DNA_CAN_MODEL = ResourceLocation.tryBuild(Fuzhouplan.MODID, "item/dna_can");
    private static final ResourceLocation UNRESOLVED_DNA_CAN_MODEL = ResourceLocation.tryBuild(Fuzhouplan.MODID, "item/unresolved_dna_can");

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation dnaCanModelLoc = new ModelResourceLocation(DNA_CAN_MODEL, "inventory");
        ModelResourceLocation unresolvedModelLoc = new ModelResourceLocation(UNRESOLVED_DNA_CAN_MODEL, "inventory");
        
        var dnaCanModel = event.getModels().get(dnaCanModelLoc);
        var unresolvedModel = event.getModels().get(unresolvedModelLoc);
        
        if (dnaCanModel != null) {
            for (var entry : DnaCanManager.getAllDnaCans().entrySet()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getValue());
                if (itemId != null) {
                    ModelResourceLocation modelLoc = new ModelResourceLocation(itemId, "inventory");
                    event.getModels().put(modelLoc, dnaCanModel);
                }
            }
        }
        
        if (unresolvedModel != null) {
            for (var entry : DnaCanManager.getAllUnresolvedDnaCans().entrySet()) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getValue());
                if (itemId != null) {
                    ModelResourceLocation modelLoc = new ModelResourceLocation(itemId, "inventory");
                    event.getModels().put(modelLoc, unresolvedModel);
                }
            }
        }
    }
}
