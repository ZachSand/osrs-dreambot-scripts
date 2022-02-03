package com.github.zachsand.osrs.dreambot.scripts.model;

public class EquipmentStatsModel {
	private int attackStab;
	private int attackSlash;
	private int attackCrush;
	private int attackMagic;
	private int attackRanged;
	private int defenceStab;
	private int defenceSlash;
	private int defenceCrush;
	private int defenceMagic;
	private int defenceRanged;
	private int meleeStrength;
	private int rangedStrength;
	private int magicDamage;
	private int prayer;
	private ItemEquipmentSlotModel slot;
	private ItemRequirementsModel requirements;

	public int getAttackStab() {
		return attackStab;
	}

	public void setAttackStab(final int attackStab) {
		this.attackStab = attackStab;
	}

	public int getAttackSlash() {
		return attackSlash;
	}

	public void setAttackSlash(final int attackSlash) {
		this.attackSlash = attackSlash;
	}

	public int getAttackCrush() {
		return attackCrush;
	}

	public void setAttackCrush(final int attackCrush) {
		this.attackCrush = attackCrush;
	}

	public int getAttackMagic() {
		return attackMagic;
	}

	public void setAttackMagic(final int attackMagic) {
		this.attackMagic = attackMagic;
	}

	public int getAttackRanged() {
		return attackRanged;
	}

	public void setAttackRanged(final int attackRanged) {
		this.attackRanged = attackRanged;
	}

	public int getDefenceStab() {
		return defenceStab;
	}

	public void setDefenceStab(final int defenceStab) {
		this.defenceStab = defenceStab;
	}

	public int getDefenceSlash() {
		return defenceSlash;
	}

	public void setDefenceSlash(final int defenceSlash) {
		this.defenceSlash = defenceSlash;
	}

	public int getDefenceCrush() {
		return defenceCrush;
	}

	public void setDefenceCrush(final int defenceCrush) {
		this.defenceCrush = defenceCrush;
	}

	public int getDefenceMagic() {
		return defenceMagic;
	}

	public void setDefenceMagic(final int defenceMagic) {
		this.defenceMagic = defenceMagic;
	}

	public int getDefenceRanged() {
		return defenceRanged;
	}

	public void setDefenceRanged(final int defenceRanged) {
		this.defenceRanged = defenceRanged;
	}

	public int getMeleeStrength() {
		return meleeStrength;
	}

	public void setMeleeStrength(final int meleeStrength) {
		this.meleeStrength = meleeStrength;
	}

	public int getRangedStrength() {
		return rangedStrength;
	}

	public void setRangedStrength(final int rangedStrength) {
		this.rangedStrength = rangedStrength;
	}

	public int getMagicDamage() {
		return magicDamage;
	}

	public void setMagicDamage(final int magicDamage) {
		this.magicDamage = magicDamage;
	}

	public int getPrayer() {
		return prayer;
	}

	public void setPrayer(final int prayer) {
		this.prayer = prayer;
	}

	public ItemEquipmentSlotModel getSlot() {
		return slot;
	}

	public void setSlot(final ItemEquipmentSlotModel slot) {
		this.slot = slot;
	}

	public ItemRequirementsModel getRequirements() {
		return requirements;
	}

	public void setRequirements(final ItemRequirementsModel requirements) {
		this.requirements = requirements;
	}
}
