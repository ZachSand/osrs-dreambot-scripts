package com.github.zachsand.osrs.dreambot.scripts.enums;

import org.dreambot.api.wrappers.items.Item;

public enum AxeItem {
	IRON_AXE(1349),
	BRONZE_AXE(1351),
	STEEL_AXE(1354),
	MITHRIL_AXE(1355),
	ADAMANT_AXE(1357),
	RUNE_AXE(1359),
	BLACK_AXE(1361),
	DRAGON_AXE(6739),
	INFERNAL_AXE(13241),
	CRYSTAL_AXE(23673),
	THIRD_AGE_AXE(20011),
	TRAILBLAZER_AXE(25110),
	GILDED_AXE(23279);

	private final int id;

	AxeItem(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public Item getItem() {
		return new Item(this.id, 1);
	}
}
