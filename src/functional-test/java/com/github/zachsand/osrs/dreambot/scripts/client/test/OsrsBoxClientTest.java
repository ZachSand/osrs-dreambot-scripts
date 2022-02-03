package com.github.zachsand.osrs.dreambot.scripts.client.test;

import static com.github.zachsand.osrs.dreambot.scripts.enums.AxeItem.STEEL_AXE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.junit.jupiter.api.Test;

import com.github.zachsand.osrs.dreambot.scripts.client.OsrsBoxClient;
import com.github.zachsand.osrs.dreambot.scripts.model.EquipmentStatsModel;
import com.github.zachsand.osrs.dreambot.scripts.model.ItemModel;

public class OsrsBoxClientTest {

	@Test
	public void testGetItemModel() {
		Optional<ItemModel> itemModelResponse = OsrsBoxClient.getItemModel(STEEL_AXE.getId());
		assertNotNull(itemModelResponse);

		// assert from JUnit doesn't seem to quiet the isPresent warning
		assert itemModelResponse.isPresent();
		ItemModel itemModel = itemModelResponse.get();
		assertEquals(itemModel.getId(), STEEL_AXE.getId());

		assertNotNull(itemModel.getEquipment());
		EquipmentStatsModel equipmentStats = itemModel.getEquipment();

		// see https://oldschool.runescape.wiki/w/Steel_axe 
		assertEquals(-2, equipmentStats.getAttackStab());
		assertEquals(8, equipmentStats.getAttackSlash());
		assertEquals(6, equipmentStats.getAttackCrush());
		assertEquals(1, equipmentStats.getDefenceSlash());
		assertEquals(9, equipmentStats.getMeleeStrength());

		assertNotNull(equipmentStats.getSlot());
		assertEquals(EquipmentSlot.WEAPON, equipmentStats.getSlot().getEquipmentSlot());
	}
}
