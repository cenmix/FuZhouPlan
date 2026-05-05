package org.fuzhou.fuzhouplan.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.fuzhou.fuzhouplan.Fuzhouplan;

public class MolecularArmorMaterial implements ArmorMaterial {
    public static final MolecularArmorMaterial INSTANCE = new MolecularArmorMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 150;
            case CHESTPLATE -> 200;
            case LEGGINGS -> 175;
            case BOOTS -> 150;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 2;
            case CHESTPLATE -> 5;
            case LEGGINGS -> 4;
            case BOOTS -> 2;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Fuzhouplan.DISTILLED_WATER.get());
    }

    @Override
    public String getName() {
        return "molecular_armor";
    }

    @Override
    public float getToughness() {
        return 1.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.1f;
    }
}
