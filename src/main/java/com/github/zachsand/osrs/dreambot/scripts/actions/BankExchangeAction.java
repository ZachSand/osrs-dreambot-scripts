package com.github.zachsand.osrs.dreambot.scripts.actions;

import static org.dreambot.api.methods.container.impl.equipment.EquipmentSlot.WEAPON;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.bank.BankQuantitySelection;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.items.Item;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.client.OsrsBoxClient;
import com.github.zachsand.osrs.dreambot.scripts.enums.CombatType;
import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionWalkingDistanceException;
import com.github.zachsand.osrs.dreambot.scripts.model.EquipmentStatsModel;
import com.github.zachsand.osrs.dreambot.scripts.model.ItemModel;
import com.github.zachsand.osrs.dreambot.scripts.model.ItemRequirementsModel;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;
import com.github.zachsand.osrs.dreambot.scripts.utils.SkillsUtil;

public class BankExchangeAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int BANK_DISTANCE_HIGH_THRESHOLD = 50;
	private static final int BANK_DISTANCE_LOW_THRESHOLD = 5;

	private final Set<Item> itemsToWithdraw;
	private final PriorityQueue<Item> itemsToWithdrawByPriority;
	private final CombatType combatType;
	private final PlayerCharacterEvent completionEvent;
	private final boolean depositInventory;
	private final boolean equipBestEquipment;

	private BankExchangeAction(final BankExchangeActionBuilder bankExchangeActionBuilder) {
		this.itemsToWithdraw = bankExchangeActionBuilder.itemsToWithdraw;
		this.depositInventory = bankExchangeActionBuilder.depositInventory;
		this.itemsToWithdrawByPriority = bankExchangeActionBuilder.itemsToWithdrawByPriority;
		this.completionEvent = bankExchangeActionBuilder.completionEvent;
		this.equipBestEquipment = bankExchangeActionBuilder.equipBestEquipment;
		this.combatType = bankExchangeActionBuilder.combatType;
	}

	public static BankExchangeActionBuilder newBuilder() {
		return new BankExchangeActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		BankLocation closestBank = Bank.getClosestBankLocation();
		validatePlayerDistanceToBank(closestBank);

		Walking.walk(closestBank.getTile().getRandomizedTile());
		MethodProvider.sleepUntil(
				() -> closestBank.getTile().distance(Players.localPlayer()) <= BANK_DISTANCE_LOW_THRESHOLD || Players.localPlayer().isStandingStill(),
				ActionUtil.getRegularSleepTimeout());

		Bank.openClosest();
		MethodProvider.sleepUntil(() -> Bank.isOpen() || Bank.openClosest(), ActionUtil.getRegularSleepTimeout());
		Bank.setWithdrawMode(BankMode.ITEM);
		Bank.setDefaultQuantity(BankQuantitySelection.ONE);

		MethodProvider.sleep((int) ActionUtil.getRegularSleepTimeout());
		depositInventory();

		MethodProvider.sleep((int) ActionUtil.getShortSleepTimeout());
		withdrawInventoryItems();

		MethodProvider.sleep((int) ActionUtil.getShortSleepTimeout());
		withdrawItemByPriority();

		MethodProvider.sleep((int) ActionUtil.getShortSleepTimeout());

		List<ItemModel> equipment = getBetterEquipment();
		equipment.forEach(itemModel -> {
			EquipmentSlot equipmentSlot = itemModel.getEquipment().getSlot().getEquipmentSlot();
			Item currentItemInSlot = Equipment.getItemInSlot(equipmentSlot);

			if (currentItemInSlot != null) {
				MethodProvider.log("Depositing item " + currentItemInSlot.getName());
				Equipment.unequip(equipmentSlot);
				Bank.deposit(currentItemInSlot);
			}

			MethodProvider.log("Withdrawing item " + itemModel.getName());
			Bank.withdraw(itemModel.getId());
		});

		Bank.close();

		MethodProvider.sleep((int) ActionUtil.getRegularSleepTimeout());
		equipment.forEach(itemModel -> {
			MethodProvider.log("Equipping item " + itemModel.getName() + " for slot " + itemModel.getEquipment().getSlot());
			Equipment.equip(itemModel.getEquipment().getSlot().getEquipmentSlot(), itemModel.getId());
		});

		sendCompletionEvent(context);
	}

	private void sendCompletionEvent(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		if (completionEvent != null) {
			ActionUtil.sendEvent(context.getStateMachine(), completionEvent);
		}
	}

	private List<ItemModel> getBetterEquipment() {
		if (equipBestEquipment) {
			MethodProvider.log("Attempting to withdraw and equip equipment for " + combatType);
			return Arrays.stream(EquipmentSlot.values())
					.map(this::getBetterEquipmentForSlot)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private void withdrawItemByPriority() {
		if (!CollectionUtils.isEmpty(itemsToWithdrawByPriority)) {
			MethodProvider
					.log("Attempting to withdraw items by priority: " + itemsToWithdrawByPriority
							.stream()
							.sorted(itemsToWithdrawByPriority.comparator())
							.map(Item::getName)
							.collect(Collectors.joining(", ")));

			Iterator<Item> itemPriorityIterator = itemsToWithdrawByPriority.iterator();
			boolean withdrewItem = false;
			while (itemPriorityIterator.hasNext() && !withdrewItem) {
				withdrewItem = Bank.withdraw(itemPriorityIterator.next().getID());
			}
		}
	}

	private void withdrawInventoryItems() {
		if (!CollectionUtils.isEmpty(itemsToWithdraw)) {
			MethodProvider.log("Attempting to withdraw items: " + itemsToWithdraw.stream()
					.map(Item::getName)
					.collect(Collectors.joining(",")));
			Bank.withdrawAll(itemsToWithdraw::contains);
		}
	}

	private void depositInventory() {
		if (depositInventory) {
			MethodProvider.log("Depositing all inventory");
			Bank.depositAllExcept(itemsToWithdraw::contains);
		}
	}

	public static final class BankExchangeActionBuilder {
		private Set<Item> itemsToWithdraw;
		private PriorityQueue<Item> itemsToWithdrawByPriority;
		private CombatType combatType;
		private PlayerCharacterEvent completionEvent;
		private boolean equipBestEquipment;
		private boolean depositInventory;

		public BankExchangeActionBuilder itemsToWithdraw(final Set<Item> itemsToWithdraw) {
			this.itemsToWithdraw = itemsToWithdraw;
			return this;
		}

		public BankExchangeActionBuilder depositInventory(final boolean depositInventory) {
			this.depositInventory = depositInventory;
			return this;
		}

		public BankExchangeActionBuilder itemsToWithdrawByPriority(final PriorityQueue<Item> itemsToWithdrawByPriority) {
			this.itemsToWithdrawByPriority = itemsToWithdrawByPriority;
			return this;
		}

		public BankExchangeActionBuilder equipBestEquipmentForCombatStyle(final boolean equipBestEquipment, final CombatType combatType) {
			this.equipBestEquipment = equipBestEquipment;
			this.combatType = combatType;
			return this;
		}

		public BankExchangeActionBuilder completionEvent(final PlayerCharacterEvent completionEvent) {
			this.completionEvent = completionEvent;
			return this;
		}

		private void validate() {
			if (CollectionUtils.isEmpty(itemsToWithdraw) && CollectionUtils.isEmpty(itemsToWithdrawByPriority)) {
				throw new ActionBuilderValidationException("Requires at least one item to withdraw");
			}

			if (equipBestEquipment && combatType == null) {
				throw new ActionBuilderValidationException(
						"Requires " + CombatType.class.getSimpleName() + " to be set when equipBestEquipment is true");
			}
		}

		public BankExchangeAction build() {
			validate();
			return new BankExchangeAction(this);
		}
	}

	private void validatePlayerDistanceToBank(final BankLocation closestBank) {
		double playerDistanceFromClosestBank = closestBank.distance(Players.localPlayer().getTile());
		if (playerDistanceFromClosestBank > BANK_DISTANCE_HIGH_THRESHOLD) {
			throw new ActionWalkingDistanceException(
					" requires bank to be within " + BANK_DISTANCE_HIGH_THRESHOLD
							+ " from player's current location, but closest bank: "
							+ closestBank + " was " + playerDistanceFromClosestBank + " distance from player");
		}
	}

	private ItemModel getBetterEquipmentForSlot(final EquipmentSlot equipmentSlot) {
		List<ItemModel> betterEquipment = Bank.all()
				.stream()
				.filter(Objects::nonNull)
				.map(bankItem -> getBetterEquipment(bankItem, Equipment.getItemInSlot(equipmentSlot.getSlot()), equipmentSlot))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (CollectionUtils.isEmpty(betterEquipment)) {
			return null;
		}

		return betterEquipment.get(0);
	}

	private boolean canEquipItem(final EquipmentStatsModel equipmentStatsResponse, final EquipmentSlot equipmentSlot) {
		return equipmentStatsResponse.getSlot() != null
				&& equipmentStatsResponse.getSlot().getEquipmentSlot() == equipmentSlot
				&& equipmentStatsResponse.getRequirements() != null
				&& SkillsUtil.canUse(equipmentStatsResponse.getRequirements())
				&& (equipmentStatsResponse.getSlot().getEquipmentSlot() != WEAPON || isValidWeaponForCombat(equipmentStatsResponse.getRequirements()));
	}

	// Not really a great way to do this
	private boolean isValidWeaponForCombat(final ItemRequirementsModel requirements) {
		switch (combatType) {
		case MELEE:
			return requirements.getAttack() > 0 || requirements.getStrength() > 0;
		case RANGED:
			return requirements.getRanged() > 0;
		case MAGIC:
			return requirements.getMagic() > 0;
		default:
			return false;
		}
	}

	private ItemModel getBetterEquipment(final Item item, final Item currentItem, final EquipmentSlot equipmentSlot) {
		Optional<ItemModel> bankItemModelResponse = OsrsBoxClient.getItemModel(item.getID());
		if (!bankItemModelResponse.isPresent()) {
			return null;
		}

		ItemModel bankItem = bankItemModelResponse.get();

		// If the current item is null and the bank item is equipable for the slot, it's better than nothing! 
		if (currentItem == null) {
			return canEquipItem(bankItem.getEquipment(), equipmentSlot) ? bankItem : null;
		}

		Optional<ItemModel> currentItemEquipmentStatsResponse = OsrsBoxClient.getItemModel(currentItem.getID());
		return currentItemEquipmentStatsResponse.isPresent()
				&& canEquipItem(bankItem.getEquipment(), equipmentSlot)
				&& isBetterItem(currentItemEquipmentStatsResponse.get().getEquipment(), bankItem.getEquipment()) ? currentItemEquipmentStatsResponse.get()
						: null;
	}

	//Make this more intelligent -- it's pretty bad right now
	private boolean isBetterItem(final EquipmentStatsModel currentItemEquipmentStats, final EquipmentStatsModel bankItemEquipmentStats) {
		switch (combatType) {
		case MELEE:
			return hasAnyBetterMeleeStats(currentItemEquipmentStats, bankItemEquipmentStats);
		case RANGED:
			return hasAnyBetterRangedStats(currentItemEquipmentStats, bankItemEquipmentStats);
		case MAGIC:
			return hasAnyBetterMagicStats(currentItemEquipmentStats, bankItemEquipmentStats);
		default:
			return false;
		}
	}

	private boolean hasAnyBetterMeleeStats(final EquipmentStatsModel currentItemEquipmentStats, final EquipmentStatsModel bankItemEquipmentStats) {
		return bankItemEquipmentStats.getAttackStab() > currentItemEquipmentStats.getAttackStab()
				|| bankItemEquipmentStats.getAttackCrush() > currentItemEquipmentStats.getAttackCrush()
				|| bankItemEquipmentStats.getAttackSlash() > currentItemEquipmentStats.getAttackStab()
				|| bankItemEquipmentStats.getMeleeStrength() > currentItemEquipmentStats.getMeleeStrength();
	}

	private boolean hasAnyBetterRangedStats(final EquipmentStatsModel currentItemEquipmentStats, final EquipmentStatsModel bankItemEquipmentStats) {
		return bankItemEquipmentStats.getAttackRanged() > currentItemEquipmentStats.getAttackRanged()
				|| bankItemEquipmentStats.getRangedStrength() > currentItemEquipmentStats.getRangedStrength();
	}

	private boolean hasAnyBetterMagicStats(final EquipmentStatsModel currentItemEquipmentStats, final EquipmentStatsModel bankItemEquipmentStats) {
		return bankItemEquipmentStats.getAttackMagic() > currentItemEquipmentStats.getAttackMagic()
				|| bankItemEquipmentStats.getMagicDamage() > currentItemEquipmentStats.getMagicDamage();
	}
}
