package com.github.zachsand.osrs.dreambot.scripts.enums;

import org.dreambot.api.wrappers.items.Item;

public enum CommonGroundItem {
	BONES(526);

	private final int id;

	CommonGroundItem(final int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public Item getItem() {
		return new Item(this.id, 1);
	}
}
