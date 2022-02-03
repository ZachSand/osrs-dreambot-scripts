package com.github.zachsand.osrs.dreambot.scripts.actions;

import static com.github.zachsand.osrs.dreambot.scripts.enums.CommonGroundItem.BONES;
import static org.dreambot.api.methods.skills.Skill.ATTACK;
import static org.dreambot.api.methods.skills.Skill.DEFENCE;
import static org.dreambot.api.methods.skills.Skill.STRENGTH;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.enums.CombatType;
import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionGameObjectInteractionException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionRequiredGameObjectMissingException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;

public class CombatAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int COMBAT_LEVEL_DIFFERENCE_THRESHOLD = 2;
	private static final int COMBAT_DISTANCE_THRESHOLD = 3;
	private static final int HEALTH_PERCENT_LOW_THRESHOLD = 40;

	private final CombatStyle combatStyle;
	private final CombatType combatType;
	private final Set<Item> foodToEat;
	private final Set<Item> itemsToPickup;
	private final PlayerCharacterEvent completionEvent;
	private final boolean balancedCombat;
	private final boolean buryBones;
	private final boolean stopWhenInventoryFull;

	private CombatAction(final CombatActionBuilder combatActionBuilder) {
		this.combatStyle = combatActionBuilder.combatStyle;
		this.combatType = combatActionBuilder.combatType;
		this.foodToEat = combatActionBuilder.foodToEat;
		this.itemsToPickup = combatActionBuilder.itemsToPickup;
		this.balancedCombat = combatActionBuilder.balancedCombat;
		this.buryBones = combatActionBuilder.buryBones;
		this.stopWhenInventoryFull = combatActionBuilder.stopWhenInventoryFull;
		this.completionEvent = combatActionBuilder.completionEvent;
	}

	public static CombatActionBuilder newBuilder() {
		return new CombatActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		if (!Combat.isAutoRetaliateOn()) {
			Combat.toggleAutoRetaliate(true);
		}

		NPC currentNpc = null;
		while (!isCombatFinished()) {
			if (currentNpc == null) {
				currentNpc = getNpcToAttack();
				walkToNpc(currentNpc);
				setCombatStyle();
			}

			if (!Players.localPlayer().isInCombat() && currentNpc.exists()) {
				attackNpc(currentNpc);
			}

			if (!Players.localPlayer().isInCombat() && !currentNpc.exists() && !Inventory.isFull()) {
				pickUpItems();
				buryBones();
			}
		}

		sendCompletionEvent(context);
	}

	private void sendCompletionEvent(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		if (completionEvent != null) {
			ActionUtil.sendEvent(context.getStateMachine(), completionEvent);
		}
	}

	private boolean isCombatFinished() {
		eatFood();
		return healthIsLow() || (stopWhenInventoryFull && Inventory.isFull());
	}

	private void buryBones() {
		if (buryBones) {
			List<Item> bonesToBury = Inventory.all(item -> item != null && item.getName().toLowerCase().contains(BONES.toString().toLowerCase()));
			if (!CollectionUtils.isEmpty(bonesToBury)) {
				bonesToBury.forEach(item -> {
					item.interact();
					MethodProvider.sleep(500);
				});
			}
		}
	}

	private void pickUpItems() {
		boolean moreToPickUp = true;
		while (moreToPickUp) {
			GroundItem groundItemToPickUp = GroundItems.closest(groundItem -> itemsToPickup.contains(groundItem.getItem()));
			if (groundItemToPickUp != null) {
				groundItemToPickUp.interact();
			}

			moreToPickUp = groundItemToPickUp != null && !Inventory.isFull();
		}
	}

	private void attackNpc(final NPC currentNpc) {
		if (Players.localPlayer().isInCombat()) {
			return;
		}

		currentNpc.interact();
		MethodProvider.sleepUntil(() -> !Players.localPlayer().isInCombat() || healthIsLow(), ActionUtil.getShortSleepTimeout());

	}

	private void walkToNpc(final NPC npcToAttack) {
		Walking.walk(npcToAttack.getTile().getRandomizedTile());
		MethodProvider.sleepUntil(() -> npcToAttack.getTile().distance(Players.localPlayer()) <= COMBAT_DISTANCE_THRESHOLD,
				ActionUtil.getRegularSleepTimeout());
	}

	private NPC getNpcToAttack() {
		NPC closestNpcToAttack = NPCs.closest(npc -> npc != null
				&& Math.abs(Combat.getCombatLevel() - npc.getLevel()) <= COMBAT_LEVEL_DIFFERENCE_THRESHOLD
				&& npc.canReach()
				&& npc.canAttack()
				&& !npc.isInCombat());

		if (closestNpcToAttack == null) {
			throw new ActionRequiredGameObjectMissingException(" can't find any NPCs to attack");
		}
		return closestNpcToAttack;
	}

	private void setCombatStyle() {
		if (balancedCombat) {
			setBalancedCombatStyle();
		} else if (!Combat.setCombatStyle(combatStyle)) {
			throw new ActionGameObjectInteractionException(" unable to enter combat style: " + combatStyle);
		}
	}

	private void eatFood() {
		boolean shouldEat = healthIsLow();
		while (shouldEat) {
			Item foodItem = Inventory.get(foodToEat::contains);
			if (foodItem != null) {
				foodItem.interact();
				MethodProvider.sleep(500);
			}
			shouldEat = foodItem == null || healthIsLow();
		}
	}

	private boolean healthIsLow() {
		return Combat.getHealthPercent() < HEALTH_PERCENT_LOW_THRESHOLD;
	}

	private void setBalancedCombatStyle() {
		switch (combatType) {
		case MELEE:
			setBalancedMeleeCombatStyle();
			break;
		case RANGED:
			setBalancedRangedCombatStyle();
			break;
		case MAGIC:
			setBalancedMagicCombatStyle();
			break;
		}
	}

	private void setBalancedMeleeCombatStyle() {
		CombatStyle lowestCombatStyleBySkill = CombatStyle.valueOf(Stream.of(ATTACK, STRENGTH, DEFENCE)
				.min(Comparator.comparingInt(Skills::getRealLevel))
				.orElse(ATTACK)
				.toString()
				.toUpperCase());
		if (Combat.getCombatStyle() != lowestCombatStyleBySkill) {
			Combat.setCombatStyle(lowestCombatStyleBySkill);
		}
	}

	private void setBalancedMagicCombatStyle() {
		//Switch between ranged styles
		Combat.setCombatStyle(CombatStyle.RANGED);
	}

	private void setBalancedRangedCombatStyle() {
		//Switch between magic and magic defence
		Combat.setCombatStyle(CombatStyle.MAGIC);
	}

	public static final class CombatActionBuilder {
		private CombatStyle combatStyle;
		private CombatType combatType;
		private Set<Item> foodToEat;
		private Set<Item> itemsToPickup;
		private PlayerCharacterEvent completionEvent;
		private boolean balancedCombat;
		private boolean buryBones;
		private boolean stopWhenInventoryFull;

		public CombatActionBuilder combatStyle(final CombatStyle combatStyle) {
			this.combatStyle = combatStyle;
			return this;
		}

		public CombatActionBuilder foodToEat(final Set<Item> foodToEat) {
			this.foodToEat = foodToEat;
			return this;
		}

		public CombatActionBuilder itemsToPickup(final Set<Item> itemsToPickup) {
			this.itemsToPickup = itemsToPickup;
			return this;
		}

		public CombatActionBuilder balancedCombat(final boolean balancedCombat, final CombatType combatType) {
			this.balancedCombat = balancedCombat;
			this.combatType = combatType;
			return this;
		}

		public CombatActionBuilder buryBones(final boolean buryBones) {
			this.buryBones = buryBones;
			return this;
		}

		public CombatActionBuilder stopWhenInventoryFull(final boolean stopWhenInventoryFull) {
			this.stopWhenInventoryFull = stopWhenInventoryFull;
			return this;
		}

		public CombatActionBuilder completionEvent(final PlayerCharacterEvent completionEvent) {
			this.completionEvent = completionEvent;
			return this;
		}

		private void validate() {
			if (!balancedCombat && combatStyle == null) {
				throw new ActionBuilderValidationException(
						" requires a either a non null " + CombatStyle.class.getSimpleName() + " or balancedCombat to be true");
			}

			if (balancedCombat && combatType == null) {
				throw new ActionBuilderValidationException(
						" requires a non null " + CombatStyle.class.getSimpleName() + " when balancedCombat is true");
			}

			if (balancedCombat && combatStyle != null) {
				MethodProvider.log(
						" using balancedCombat strategy over dedicated " + CombatStyle.class.getSimpleName() + ": " + combatStyle);
			}

			if (buryBones && itemsToPickup.stream().noneMatch(item -> item.getName().toLowerCase().contains(BONES.toString().toLowerCase()))) {
				throw new ActionBuilderValidationException(" requires bones item in itemsToPickup when buryBones is true");
			}
		}

		public CombatAction build() {
			validate();
			return new CombatAction(this);
		}
	}
}
