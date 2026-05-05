package org.fuzhou.fuzhouplan.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.fuzhou.fuzhouplan.Fuzhouplan;

import java.util.List;

public class MolecularArmorItem extends ArmorItem {
    public MolecularArmorItem(Type type, Properties properties) {
        super(MolecularArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.fuzhouplan.molecular_armor.tooltip"));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
