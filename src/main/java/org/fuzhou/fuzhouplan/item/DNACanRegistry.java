package org.fuzhou.fuzhouplan.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DNACanRegistry {

    private static final Map<EntityType<?>, UnresolvedDNACanItem> UNRESOLVED_CANS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, DNACanItem> RESOLVED_CANS = new LinkedHashMap<>();

    public static ItemStack createUnresolvedDNACan(EntityType<?> entityType) {
        UnresolvedDNACanItem item = UNRESOLVED_CANS.get(entityType);
        if (item == null) {
            item = new UnresolvedDNACanItem(entityType, new Item.Properties().stacksTo(1));
            UNRESOLVED_CANS.put(entityType, item);
        }
        return new ItemStack(item);
    }

    public static ItemStack createResolvedDNACan(EntityType<?> entityType) {
        DNACanItem item = RESOLVED_CANS.get(entityType);
        if (item == null) {
            item = new DNACanItem(entityType, new Item.Properties().stacksTo(1));
            RESOLVED_CANS.put(entityType, item);
        }
        return new ItemStack(item);
    }

    public static Map<EntityType<?>, UnresolvedDNACanItem> getAllUnresolvedCans() {
        return Collections.unmodifiableMap(UNRESOLVED_CANS);
    }

    public static Map<EntityType<?>, DNACanItem> getAllResolvedCans() {
        return Collections.unmodifiableMap(RESOLVED_CANS);
    }
}
