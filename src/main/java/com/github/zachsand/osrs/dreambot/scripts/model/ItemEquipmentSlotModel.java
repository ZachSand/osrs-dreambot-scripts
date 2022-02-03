package com.github.zachsand.osrs.dreambot.scripts.model;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;

import com.google.gson.annotations.SerializedName;

public enum ItemEquipmentSlotModel {
	@SerializedName("2h")
	TWO_HAND(EquipmentSlot.WEAPON),

	@SerializedName("ammo")
	AMMO(EquipmentSlot.ARROWS),

	@SerializedName("body")
	BODY(EquipmentSlot.CHEST),

	@SerializedName("cape")
	CAPE(EquipmentSlot.CAPE),

	@SerializedName("feet")
	FEET(EquipmentSlot.FEET),

	@SerializedName("hands")
	HANDS(EquipmentSlot.HANDS),

	@SerializedName("head")
	HEAD(EquipmentSlot.HAT),

	@SerializedName("legs")
	LEGS(EquipmentSlot.LEGS),

	@SerializedName("neck")
	NECK(EquipmentSlot.AMULET),

	@SerializedName("ring")
	RING(EquipmentSlot.RING),

	@SerializedName("shield")
	SHIELD(EquipmentSlot.SHIELD),

	@SerializedName("weapon")
	WEAPON(EquipmentSlot.WEAPON);

	private final EquipmentSlot equipmentSlot;

	ItemEquipmentSlotModel(final EquipmentSlot equipmentSlot) {
		this.equipmentSlot = equipmentSlot;
	}

	public EquipmentSlot getEquipmentSlot() {
		return equipmentSlot;
	}
}
