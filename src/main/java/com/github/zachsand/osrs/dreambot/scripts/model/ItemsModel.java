package com.github.zachsand.osrs.dreambot.scripts.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ItemsModel {

	@SerializedName(value = "_items")
	private List<ItemModel> items;

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(final List<ItemModel> items) {
		this.items = items;
	}

}
