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
 * DNA储存罐（已解析）
 * 包含特定生物DNA的储存容器
 */
public class DNACanItem extends Item {
    
    private static final String NBT_ENTITY_TYPE = "EntityType";
    private static final String NBT_ENTITY_NAME = "EntityName";
    private static final String NBT_RESOLVED = "Resolved";

    public DNACanItem(Properties properties) {
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
                Component.literal("生物: ").append(displayName).withStyle(ChatFormatting.GREEN)
            );
            tooltipComponents.add(
                Component.literal("类型: " + entityType).withStyle(ChatFormatting.GRAY)
            );
            tooltipComponents.add(
                Component.literal("状态: 已解析").withStyle(ChatFormatting.BLUE)
            );
        } else {
            tooltipComponents.add(
                Component.translatable("item.fuzhouplan.dna_can.empty").withStyle(ChatFormatting.GRAY)
            );
        }
        
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    /**
     * 创建带有生物信息的DNA储存罐
     */
    public static ItemStack createResolvedDNACan(String entityType, String entityName) {
        ItemStack stack = new ItemStack(org.fuzhou.fuzhouplan.Fuzhouplan.DNA_CAN.get());
        
        CompoundTag tag = new CompoundTag();
        tag.putString(NBT_ENTITY_TYPE, entityType);
        tag.putString(NBT_ENTITY_NAME, entityName);
        tag.putBoolean(NBT_RESOLVED, true);
        
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
            Component.translatable("item.fuzhouplan.dna_can.resolved", displayName)
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
}