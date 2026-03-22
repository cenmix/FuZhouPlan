package org.fuzhou.fuzhouplan.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraft.core.Registry;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Fuzhouplan.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DnaCanManager {

    private static final Map<EntityType<?>, Supplier<Item>> DNA_CAN_MAP = new HashMap<>();
    private static final Map<EntityType<?>, Supplier<Item>> UNRESOLVED_DNA_CAN_MAP = new HashMap<>();
    private static final Map<ResourceLocation, Item> DNA_CAN_BY_ID = new HashMap<>();
    private static final Map<ResourceLocation, Item> UNRESOLVED_DNA_CAN_BY_ID = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
            return;
        }

        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (entityId == null) continue;

            String path = entityId.getNamespace() + "_" + entityId.getPath();

            Item.Properties properties = new Item.Properties().stacksTo(64);

            Item dnaCan = new DnaCanItem(properties, entityType);
            Item unresolvedDnaCan = new UnresolvedDnaCanItem(properties, entityType);

            ResourceLocation dnaCanId = new ResourceLocation(Fuzhouplan.MODID, "dna_can_" + path);
            ResourceLocation unresolvedDnaCanId = new ResourceLocation(Fuzhouplan.MODID, "unresolved_dna_can_" + path);

            event.register(Registry.ITEM_REGISTRY, dnaCanId, () -> dnaCan);
            event.register(Registry.ITEM_REGISTRY, unresolvedDnaCanId, () -> unresolvedDnaCan);

            DNA_CAN_BY_ID.put(entityId, dnaCan);
            UNRESOLVED_DNA_CAN_BY_ID.put(entityId, unresolvedDnaCan);
        }
    }

    public static Item getDnaCan(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return entityId != null ? DNA_CAN_BY_ID.get(entityId) : null;
    }

    public static Item getUnresolvedDnaCan(EntityType<?> entityType) {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return entityId != null ? UNRESOLVED_DNA_CAN_BY_ID.get(entityId) : null;
    }

    public static Item getDnaCan(ResourceLocation entityId) {
        return DNA_CAN_BY_ID.get(entityId);
    }

    public static Item getUnresolvedDnaCan(ResourceLocation entityId) {
        return UNRESOLVED_DNA_CAN_BY_ID.get(entityId);
    }

    public static EntityType<?> getEntityTypeFromDnaCan(Item item) {
        for (Map.Entry<ResourceLocation, Item> entry : DNA_CAN_BY_ID.entrySet()) {
            if (entry.getValue() == item) {
                return ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
            }
        }
        return null;
    }

    public static EntityType<?> getEntityTypeFromUnresolvedDnaCan(Item item) {
        for (Map.Entry<ResourceLocation, Item> entry : UNRESOLVED_DNA_CAN_BY_ID.entrySet()) {
            if (entry.getValue() == item) {
                return ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
            }
        }
        return null;
    }

    public static Map<ResourceLocation, Item> getAllDnaCans() {
        return DNA_CAN_BY_ID;
    }

    public static Map<ResourceLocation, Item> getAllUnresolvedDnaCans() {
        return UNRESOLVED_DNA_CAN_BY_ID;
    }
}
