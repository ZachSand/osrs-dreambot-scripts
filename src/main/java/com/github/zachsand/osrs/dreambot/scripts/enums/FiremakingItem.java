package com.github.zachsand.osrs.dreambot.scripts.enums;

import org.dreambot.api.wrappers.items.Item;

public enum FiremakingItem {
	TINDERBOX(590);

	private final int itemId;

	FiremakingItem(final int itemId) {
		this.itemId = itemId;
	}

	public int getItemId() {
		return this.itemId;
	}

	public Item getItem() {
		return new Item(itemId, 1);
	}

}
