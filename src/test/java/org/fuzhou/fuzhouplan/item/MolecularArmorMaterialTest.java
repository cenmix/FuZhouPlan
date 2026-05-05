package org.fuzhou.fuzhouplan.item;

import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MolecularArmorMaterial Tests")
class MolecularArmorMaterialTest {

    private MolecularArmorMaterial material;

    @BeforeEach
    void setUp() {
        material = MolecularArmorMaterial.INSTANCE;
    }

    @Test
    @DisplayName("Material should not be null")
    void testMaterialNotNull() {
        assertNotNull(material, "Material instance should not be null");
    }

    @ParameterizedTest
    @EnumSource(ArmorItem.Type.class)
    @DisplayName("Durability should be positive for all armor types")
    void testDurabilityIsPositive(ArmorItem.Type type) {
        int durability = material.getDurabilityForType(type);
        assertTrue(durability > 0, "Durability for " + type + " should be positive");
    }

    @ParameterizedTest
    @EnumSource(ArmorItem.Type.class)
    @DisplayName("Defense should be non-negative for all armor types")
    void testDefenseIsNonNegative(ArmorItem.Type type) {
        int defense = material.getDefenseForType(type);
        assertTrue(defense >= 0, "Defense for " + type + " should be non-negative");
    }

    @Test
    @DisplayName("Enchantment value should be positive")
    void testEnchantmentValuePositive() {
        int enchantmentValue = material.getEnchantmentValue();
        assertTrue(enchantmentValue > 0, "Enchantment value should be positive");
    }

    @Test
    @DisplayName("Equip sound should not be null")
    void testEquipSoundNotNull() {
        assertNotNull(material.getEquipSound(), "Equip sound should not be null");
    }

    @Test
    @DisplayName("Repair ingredient should not be null")
    void testRepairIngredientNotNull() {
        assertNotNull(material.getRepairIngredient(), "Repair ingredient should not be null");
    }

    @Test
    @DisplayName("Material name should be 'molecular_armor'")
    void testMaterialName() {
        assertEquals("molecular_armor", material.getName(), "Material name should match");
    }

    @Test
    @DisplayName("Toughness should be positive")
    void testToughnessPositive() {
        float toughness = material.getToughness();
        assertTrue(toughness >= 0, "Toughness should be non-negative");
    }

    @Test
    @DisplayName("Knockback resistance should be non-negative")
    void testKnockbackResistance() {
        float knockbackResistance = material.getKnockbackResistance();
        assertTrue(knockbackResistance >= 0, "Knockback resistance should be non-negative");
    }

    @Test
    @DisplayName("Helmet should have 150 durability")
    void testHelmetDurability() {
        assertEquals(150, material.getDurabilityForType(ArmorItem.Type.HELMET));
    }

    @Test
    @DisplayName("Chestplate should have 200 durability")
    void testChestplateDurability() {
        assertEquals(200, material.getDurabilityForType(ArmorItem.Type.CHESTPLATE));
    }

    @Test
    @DisplayName("Leggings should have 175 durability")
    void testLeggingsDurability() {
        assertEquals(175, material.getDurabilityForType(ArmorItem.Type.LEGGINGS));
    }

    @Test
    @DisplayName("Boots should have 150 durability")
    void testBootsDurability() {
        assertEquals(150, material.getDurabilityForType(ArmorItem.Type.BOOTS));
    }
}
