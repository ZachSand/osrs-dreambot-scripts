package com.github.zachsand.osrs.dreambot.scripts.enums;

import org.dreambot.api.wrappers.items.Item;

public enum FoodItem {

	RAW_RAT_MEAT(2134),
	COOKED_MEAT(2142);

	private final int id;

	FoodItem(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public Item getItem() {
		return new Item(this.id, 1);
	}
}
