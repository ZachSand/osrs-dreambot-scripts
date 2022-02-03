package com.github.zachsand.osrs.dreambot.scripts.model;

public class ItemModel {
	private int id;
	private String name;
	private EquipmentStatsModel equipment;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public EquipmentStatsModel getEquipment() {
		return equipment;
	}

	public void setEquipment(final EquipmentStatsModel equipment) {
		this.equipment = equipment;
	}

}
