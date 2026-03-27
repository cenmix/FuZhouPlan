package org.fuzhou.fuzhouplan.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 未解析的DNA储存罐
 * 包含未解析生物DNA的储存容器，需要进一步处理
 */
public class UnresolvedDNACanItem extends Item {
    
    private static final String NBT_ENTITY_TYPE = "EntityType";
    private static final String NBT_ENTITY_NAME = "EntityName";
    private static final String NBT_RESOLVED = "Resolved";

    public UnresolvedDNACanItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();
        
        if (tag != null && tag.contains(NBT_ENTITY_TYPE)) {
            String entityType = tag.getString(NBT_ENTITY_TYPE);
            String entityName = tag.getString(NBT_ENTITY_NAME);
            
            // 将JSON格式的生物名称转换为可显示的文本
            Component displayName;
            try {
                displayName = Component.Serializer.fromJson(entityName);
            } catch (Exception e) {
                // 如果解析失败，使用默认名称
                displayName = Component.literal("未知生物");
            }
            
            tooltipComponents.add(
                Component.literal("生物: ").append(displayName).withStyle(ChatFormatting.YELLOW)
            );
            tooltipComponents.add(
                Component.literal("类型: " + entityType).withStyle(ChatFormatting.GRAY)
            );
            tooltipComponents.add(
                Component.literal("状态: 未解析").withStyle(ChatFormatting.RED)
            );
            tooltipComponents.add(
                Component.translatable("item.fuzhouplan.unresolved_dna_can.tooltip").withStyle(ChatFormatting.DARK_GRAY)
            );
        } else {
            tooltipComponents.add(
                Component.translatable("item.fuzhouplan.unresolved_dna_can.empty").withStyle(ChatFormatting.GRAY)
            );
        }
        
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    /**
     * 创建未解析的DNA储存罐
     */
    public static ItemStack createUnresolvedDNACan(String entityType, String entityName) {
        ItemStack stack = new ItemStack(org.fuzhou.fuzhouplan.Fuzhouplan.UNRESOLVED_DNA_CAN.get());
        
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_ENTITY_TYPE, entityType);
        tag.putString(NBT_ENTITY_NAME, entityName);
        tag.putBoolean(NBT_RESOLVED, false);
        
        stack.setTag(tag);
        
        // 将JSON格式的生物名称转换为可显示的文本
        Component displayName;
        try {
            displayName = Component.Serializer.fromJson(entityName);
        } catch (Exception e) {
            // 如果解析失败，使用默认名称
            displayName = Component.literal("未知生物");
        }
        
        stack.setHoverName(
            Component.translatable("item.fuzhouplan.unresolved_dna_can", displayName)
        );
        
        return stack;
    }

    /**
     * 检查DNA储存罐是否已解析
     */
    public static boolean isResolved(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(NBT_RESOLVED);
    }

    /**
     * 获取DNA储存罐中的生物类型
     */
    public static String getEntityType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString(NBT_ENTITY_TYPE) : "";
    }

    /**
     * 将未解析的DNA储存罐转换为已解析的DNA储存罐
     */
    public static ItemStack resolve(ItemStack unresolvedStack) {
        if (!(unresolvedStack.getItem() instanceof UnresolvedDNACanItem)) {
            return ItemStack.EMPTY;
        }
        
        CompoundTag tag = unresolvedStack.getTag();
        if (tag == null || !tag.contains(NBT_ENTITY_TYPE)) {
            return ItemStack.EMPTY;
        }
        
        String entityType = tag.getString(NBT_ENTITY_TYPE);
        String entityName = tag.getString(NBT_ENTITY_NAME);
        
        return DNACanItem.createResolvedDNACan(entityType, entityName);
    }
}