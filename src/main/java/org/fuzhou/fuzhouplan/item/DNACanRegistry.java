package org.fuzhou.fuzhouplan.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Fuzhouplan.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DNACanRegistry {

    private static final Map<ResourceLocation, UnresolvedDNACanItem> UNRESOLVED_DNA_CANS = new HashMap<>();
    private static final Map<ResourceLocation, DNACanItem> RESOLVED_DNA_CANS = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) return;

        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
            if (!isMobCategory(entityType)) continue;

            ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (entityKey == null) continue;

            String unresolvedName = "unresolved_dna_can_" + entityKey.getNamespace() + "_" + entityKey.getPath();
            ResourceLocation unresolvedRL = new ResourceLocation(Fuzhouplan.MODID, unresolvedName);

            UnresolvedDNACanItem unresolvedItem = new UnresolvedDNACanItem(entityType, new Item.Properties().stacksTo(64));
            event.register(Registries.ITEM, unresolvedRL, () -> unresolvedItem);
            UNRESOLVED_DNA_CANS.put(entityKey, unresolvedItem);

            String resolvedName = "dna_can_" + entityKey.getNamespace() + "_" + entityKey.getPath();
            ResourceLocation resolvedRL = new ResourceLocation(Fuzhouplan.MODID, resolvedName);

            DNACanItem resolvedItem = new DNACanItem(entityType, new Item.Properties().stacksTo(64));
            event.register(Registries.ITEM, resolvedRL, () -> resolvedItem);
            RESOLVED_DNA_CANS.put(entityKey, resolvedItem);
        }
    }

    private static boolean isMobCategory(EntityType<?> entityType) {
        MobCategory category = entityType.getCategory();
        return category == MobCategory.CREATURE
                || category == MobCategory.MONSTER
                || category == MobCategory.AMBIENT
                || category == MobCategory.WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE;
    }

    public static UnresolvedDNACanItem getUnresolvedDNACan(EntityType<?> entityType) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return key != null ? UNRESOLVED_DNA_CANS.get(key) : null;
    }

    public static DNACanItem getResolvedDNACan(EntityType<?> entityType) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return key != null ? RESOLVED_DNA_CANS.get(key) : null;
    }

    public static ItemStack createUnresolvedDNACan(EntityType<?> entityType) {
        UnresolvedDNACanItem item = getUnresolvedDNACan(entityType);
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public static ItemStack createResolvedDNACan(EntityType<?> entityType) {
        DNACanItem item = getResolvedDNACan(entityType);
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public static Map<ResourceLocation, UnresolvedDNACanItem> getAllUnresolvedCans() {
        return Collections.unmodifiableMap(UNRESOLVED_DNA_CANS);
    }

    public static Map<ResourceLocation, DNACanItem> getAllResolvedCans() {
        return Collections.unmodifiableMap(RESOLVED_DNA_CANS);
    }
}
