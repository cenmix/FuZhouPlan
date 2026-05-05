package org.fuzhou.fuzhouplan.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MolecularArmorItem Tests")
class MolecularArmorItemTest {

    private MolecularArmorItem helmet;
    private MolecularArmorItem chestplate;
    private MolecularArmorItem leggings;
    private MolecularArmorItem boots;

    @BeforeEach
    void setUp() {
        helmet = new MolecularArmorItem(ArmorItem.Type.HELMET, new net.minecraft.world.item.Item.Properties().durability(150));
        chestplate = new MolecularArmorItem(ArmorItem.Type.CHESTPLATE, new net.minecraft.world.item.Item.Properties().durability(200));
        leggings = new MolecularArmorItem(ArmorItem.Type.LEGGINGS, new net.minecraft.world.item.Item.Properties().durability(175));
        boots = new MolecularArmorItem(ArmorItem.Type.BOOTS, new net.minecraft.world.item.Item.Properties().durability(150));
    }

    @Test
    @DisplayName("Helmet should not be null")
    void testHelmetNotNull() {
        assertNotNull(helmet, "Helmet should not be null");
    }

    @Test
    @DisplayName("Chestplate should not be null")
    void testChestplateNotNull() {
        assertNotNull(chestplate, "Chestplate should not be null");
    }

    @Test
    @DisplayName("Leggings should not be null")
    void testLeggingsNotNull() {
        assertNotNull(leggings, "Leggings should not be null");
    }

    @Test
    @DisplayName("Boots should not be null")
    void testBootsNotNull() {
        assertNotNull(boots, "Boots should not be null");
    }

    @Test
    @DisplayName("Helmet should use HELMET type")
    void testHelmetType() {
        assertEquals(ArmorItem.Type.HELMET, helmet.getType(), "Helmet type should be HELMET");
    }

    @Test
    @DisplayName("Chestplate should use CHESTPLATE type")
    void testChestplateType() {
        assertEquals(ArmorItem.Type.CHESTPLATE, chestplate.getType(), "Chestplate type should be CHESTPLATE");
    }

    @Test
    @DisplayName("Leggings should use LEGGINGS type")
    void testLeggingsType() {
        assertEquals(ArmorItem.Type.LEGGINGS, leggings.getType(), "Leggings type should be LEGGINGS");
    }

    @Test
    @DisplayName("Boots should use BOOTS type")
    void testBootsType() {
        assertEquals(ArmorItem.Type.BOOTS, boots.getType(), "Boots type should be BOOTS");
    }

    @Test
    @DisplayName("Helmet should have correct max damage")
    void testHelmetMaxDamage() {
        assertEquals(150, helmet.getMaxDamage(new ItemStack(helmet)), "Helmet max damage should be 150");
    }

    @Test
    @DisplayName("Chestplate should have correct max damage")
    void testChestplateMaxDamage() {
        assertEquals(200, chestplate.getMaxDamage(new ItemStack(chestplate)), "Chestplate max damage should be 200");
    }

    @Test
    @DisplayName("Leggings should have correct max damage")
    void testLeggingsMaxDamage() {
        assertEquals(175, leggings.getMaxDamage(new ItemStack(leggings)), "Leggings max damage should be 175");
    }

    @Test
    @DisplayName("Boots should have correct max damage")
    void testBootsMaxDamage() {
        assertEquals(150, boots.getMaxDamage(new ItemStack(boots)), "Boots max damage should be 150");
    }

    @ParameterizedTest
    @EnumSource(ArmorItem.Type.class)
    @DisplayName("All armor types should have correct material")
    void testArmorMaterial(ArmorItem.Type type) {
        MolecularArmorItem armor = new MolecularArmorItem(type, new net.minecraft.world.item.Item.Properties());
        assertEquals(MolecularArmorMaterial.INSTANCE, armor.getMaterial(), "Armor material should match");
    }

    @Test
    @DisplayName("Armor should use correct armor material")
    void testArmorUsesCorrectMaterial() {
        assertEquals(MolecularArmorMaterial.INSTANCE, helmet.getMaterial());
        assertEquals(MolecularArmorMaterial.INSTANCE, chestplate.getMaterial());
        assertEquals(MolecularArmorMaterial.INSTANCE, leggings.getMaterial());
        assertEquals(MolecularArmorMaterial.INSTANCE, boots.getMaterial());
    }
}
