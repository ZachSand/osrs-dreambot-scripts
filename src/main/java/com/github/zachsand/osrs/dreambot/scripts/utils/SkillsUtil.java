package com.github.zachsand.osrs.dreambot.scripts.utils;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import com.github.zachsand.osrs.dreambot.scripts.model.ItemRequirementsModel;

public class SkillsUtil {

	private SkillsUtil() {}

	public static boolean canUse(final ItemRequirementsModel itemRequirementsModel) {
		return canUseByCombatSkill(itemRequirementsModel)
				&& canUseByFreeToPlaySkill(itemRequirementsModel)
				&& canUseByMemberSkill(itemRequirementsModel);
	}

	public static boolean canUseByCombatSkill(final ItemRequirementsModel itemRequirementsModel) {
		return itemRequirementsModel.getAttack() <= Skills.getRealLevel(Skill.ATTACK)
				&& itemRequirementsModel.getDefence() <= Skills.getRealLevel(Skill.DEFENCE)
				&& itemRequirementsModel.getHitpoints() <= Skills.getRealLevel(Skill.HITPOINTS)
				&& itemRequirementsModel.getMagic() <= Skills.getRealLevel(Skill.MAGIC)
				&& itemRequirementsModel.getPrayer() <= Skills.getRealLevel(Skill.PRAYER)
				&& itemRequirementsModel.getStrength() <= Skills.getRealLevel(Skill.STRENGTH)
				&& itemRequirementsModel.getRanged() <= Skills.getRealLevel(Skill.RANGED);
	}

	public static boolean canUseByFreeToPlaySkill(final ItemRequirementsModel itemRequirementsModel) {
		return itemRequirementsModel.getRunecraft() <= Skills.getRealLevel(Skill.RUNECRAFTING)
				&& itemRequirementsModel.getCrafting() <= Skills.getRealLevel(Skill.CRAFTING)
				&& itemRequirementsModel.getMining() <= Skills.getRealLevel(Skill.MINING)
				&& itemRequirementsModel.getSmithing() <= Skills.getRealLevel(Skill.SMITHING)
				&& itemRequirementsModel.getFishing() <= Skills.getRealLevel(Skill.FISHING)
				&& itemRequirementsModel.getCooking() <= Skills.getRealLevel(Skill.COOKING)
				&& itemRequirementsModel.getFiremaking() <= Skills.getRealLevel(Skill.FIREMAKING)
				&& itemRequirementsModel.getWoodcutting() <= Skills.getRealLevel(Skill.WOODCUTTING);
	}

	public static boolean canUseByMemberSkill(final ItemRequirementsModel itemRequirementsModel) {
		return itemRequirementsModel.getAgility() <= Skills.getRealLevel(Skill.AGILITY)
				&& itemRequirementsModel.getHerblore() <= Skills.getRealLevel(Skill.HERBLORE)
				&& itemRequirementsModel.getThieving() <= Skills.getRealLevel(Skill.THIEVING)
				&& itemRequirementsModel.getFletching() <= Skills.getRealLevel(Skill.FLETCHING)
				&& itemRequirementsModel.getSlayer() <= Skills.getRealLevel(Skill.SLAYER)
				&& itemRequirementsModel.getFarming() <= Skills.getRealLevel(Skill.FARMING)
				&& itemRequirementsModel.getConstruction() <= Skills.getRealLevel(Skill.CONSTRUCTION)
				&& itemRequirementsModel.getHunter() <= Skills.getRealLevel(Skill.HUNTER);
	}
}
