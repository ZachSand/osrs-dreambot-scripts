package com.github.zachsand.osrs.dreambot.scripts.config;

import static com.github.zachsand.osrs.dreambot.scripts.enums.CombatType.MELEE;
import static com.github.zachsand.osrs.dreambot.scripts.enums.CommonGroundItem.BONES;
import static com.github.zachsand.osrs.dreambot.scripts.enums.FiremakingItem.TINDERBOX;
import static com.github.zachsand.osrs.dreambot.scripts.enums.FoodItem.COOKED_MEAT;
import static com.github.zachsand.osrs.dreambot.scripts.enums.FoodItem.RAW_RAT_MEAT;
import static com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent.ARRIVED_AT_BANK;
import static com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent.ARRIVED_AT_SEWER;
import static com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent.BANKING_ENDED;
import static com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent.COMBAT_ENDED;
import static com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent.ENTERED_SEWER;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.AT_SEWER_MANHOLE;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.BANKING;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.COLLECTING_LOG;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.COOKING;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.ENTERING_COMBAT;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.ENTERING_SEWER;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.WAITING_FOR_HEALTH;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.WALKING_TO_SEWER;
import static com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState.WALKING_TO_VARROCK_EAST_BANK;
import static org.dreambot.api.methods.container.impl.bank.BankLocation.VARROCK_EAST;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.items.Item;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.github.zachsand.osrs.dreambot.scripts.actions.BankExchangeAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.CombatAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.LogFireCookingAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.PassiveHealthRegenerationAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.TransitionErrorAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.WalkToTileAction;
import com.github.zachsand.osrs.dreambot.scripts.actions.WoodcuttingAction;
import com.github.zachsand.osrs.dreambot.scripts.enums.AxeItem;
import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.listener.PlayerStateMachineListener;
import com.github.zachsand.osrs.dreambot.scripts.listener.PlayerStateMachineMonitor;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

public class StateMachineConfig {

	private StateMachineConfig() {}

	private static final String STATE_MACHINE_ID = UUID.randomUUID().toString();
	private static final String TREE_NAME = "Tree";
	private static final String CLIMB_DOWN_ACTION = "Climb Down";
	private static final int SAFE_HEALTH_PERCENTAGE = 85;

	private static final Tile MANHOLE = new Tile(3237, 3458);
	private static final Tile SEWER_TO_SURFACE_LADDER = new Tile(3237, 9858);
	private static final Tile FIRE_TILE = new Tile(3240, 3461);
	private static final List<String> MANHOLE_INTERACTIONS = Arrays.asList("Open", CLIMB_DOWN_ACTION);

	private static final TransitionErrorAction ACTION_ERROR_HANDLER = new TransitionErrorAction();

	public static void configureStateMachineTransitions(final StateMachineBuilder.Builder<PlayerCharacterState, PlayerCharacterEvent> builder)
			throws Exception {
		StateMachineTransitionConfigurer<PlayerCharacterState, PlayerCharacterEvent> config = builder.configureTransitions();

		/* Start walking to Varrock East Bank and begin banking */
		configureTransition(config, WALKING_TO_VARROCK_EAST_BANK, BANKING, ARRIVED_AT_BANK, getWalkToVarrockEastBankAction());

		/* Start banking once the player has arrived at the bank, walk to the sewer once banking has ended */
		configureTransition(config, BANKING, WALKING_TO_SEWER, BANKING_ENDED, getBankExchangeAction());

		/* Walk to the sewer, stopping once arrived */
		configureTransition(config, WALKING_TO_SEWER, AT_SEWER_MANHOLE, ARRIVED_AT_SEWER, getWalkToSewerAction());

		/* Choice state: Collect logs -> cook food -> wait for safe health -> enter sewer */
		config.withChoice()
				.source(AT_SEWER_MANHOLE)
				.first(COLLECTING_LOG, context -> true, Actions.errorCallingAction(getWoodcuttingAction(), ACTION_ERROR_HANDLER))
				.then(COOKING, context -> true, Actions.errorCallingAction(getLogFireCookingAction(), ACTION_ERROR_HANDLER))
				.then(WAITING_FOR_HEALTH, context -> true, Actions.errorCallingAction(getPassiveHealthRegenerationAction(), ACTION_ERROR_HANDLER))
				.last(ENTERING_SEWER, Actions.errorCallingAction(getEnterSewerAction(), ACTION_ERROR_HANDLER));

		/* Enter the sewer and enter combat */
		configureTransition(config, ENTERING_SEWER, ENTERING_COMBAT, ENTERED_SEWER, getSewerCombatAction());

		/* Leave sewer once combat is over */
		configureTransition(config, ENTERING_COMBAT, AT_SEWER_MANHOLE, COMBAT_ENDED, getLeaveSewerAction());
	}

	public static void configureStateMachineStates(final StateMachineBuilder.Builder<PlayerCharacterState, PlayerCharacterEvent> builder) throws Exception {
		builder.configureStates()
				.withStates()
				.initial(WALKING_TO_VARROCK_EAST_BANK)
				.choice(AT_SEWER_MANHOLE)
				.states(EnumSet.allOf(PlayerCharacterState.class));
	}

	public static void configureStateMachineConfiguration(final StateMachineBuilder.Builder<PlayerCharacterState, PlayerCharacterEvent> builder)
			throws Exception {
		builder
				.configureConfiguration()
				.withConfiguration()
				.machineId(STATE_MACHINE_ID)
				.listener(new PlayerStateMachineListener())
				.autoStartup(false);

		builder.configureConfiguration()
				.withMonitoring()
				.monitor(new PlayerStateMachineMonitor());
	}

	private static PassiveHealthRegenerationAction getPassiveHealthRegenerationAction() {
		return PassiveHealthRegenerationAction.newBuilder()
				.healthPercentToWaitFor(SAFE_HEALTH_PERCENTAGE)
				.build();
	}

	private static WalkToTileAction getWalkToVarrockEastBankAction() {
		return WalkToTileAction.newBuilder()
				.destinationTile(VARROCK_EAST.getTile().getRandomizedTile())
				.completionEvent(ARRIVED_AT_BANK)
				.build();
	}

	private static WalkToTileAction getWalkToSewerAction() {
		return WalkToTileAction.newBuilder()
				.destinationTile(MANHOLE)
				.completionEvent(ARRIVED_AT_SEWER)
				.build();
	}

	private static WalkToTileAction getLeaveSewerAction() {
		return WalkToTileAction.newBuilder()
				.destinationTile(SEWER_TO_SURFACE_LADDER)
				.destinationTileInteractions(Collections.singletonList(CLIMB_DOWN_ACTION))
				.completionEvent(ARRIVED_AT_SEWER)
				.build();
	}

	private static CombatAction getSewerCombatAction() {
		return CombatAction.newBuilder()
				.balancedCombat(true, MELEE)
				.foodToEat(new HashSet<>(Collections.singletonList(COOKED_MEAT.getItem())))
				.itemsToPickup(new HashSet<>(Arrays.asList(RAW_RAT_MEAT.getItem(), BONES.getItem())))
				.buryBones(true)
				.stopWhenInventoryFull(true)
				.completionEvent(COMBAT_ENDED)
				.build();
	}

	private static WoodcuttingAction getWoodcuttingAction() {
		return WoodcuttingAction.newBuilder()
				.logsToCollect(1)
				.treeName(TREE_NAME)
				.build();
	}

	private static BankExchangeAction getBankExchangeAction() {
		PriorityQueue<Item> axes = new PriorityQueue<>(Comparator.comparing(Item::getValue));
		axes.addAll(Stream.of(AxeItem.values()).map(axe -> new Item(axe.getId(), 1)).collect(Collectors.toList()));
		return BankExchangeAction.newBuilder()
				.depositInventory(true)
				.itemsToWithdraw(Collections.singleton(TINDERBOX.getItem()))
				.itemsToWithdrawByPriority(axes)
				.equipBestEquipmentForCombatStyle(true, MELEE)
				.completionEvent(BANKING_ENDED)
				.build();
	}

	private static LogFireCookingAction getLogFireCookingAction() {
		return LogFireCookingAction.newBuilder()
				.fireTile(FIRE_TILE)
				.itemsToCook(new HashSet<>(Collections.singletonList(RAW_RAT_MEAT.getItem())))
				.build();
	}

	private static WalkToTileAction getEnterSewerAction() {
		return WalkToTileAction.newBuilder()
				.destinationTile(MANHOLE)
				.destinationTileInteractions(MANHOLE_INTERACTIONS)
				.completionEvent(ENTERED_SEWER)
				.build();
	}

	private static void configureTransition(final StateMachineTransitionConfigurer<PlayerCharacterState, PlayerCharacterEvent> transitionConfigurer,
			final PlayerCharacterState source, final PlayerCharacterState target, final PlayerCharacterEvent event,
			final Action<PlayerCharacterState, PlayerCharacterEvent> action) throws Exception {
		transitionConfigurer
				.withInternal()
				.source(source)
				.action(Actions.errorCallingAction(action, ACTION_ERROR_HANDLER))
				.timerOnce(TimeUnit.SECONDS.toMillis(1));

		transitionConfigurer
				.withExternal()
				.source(source)
				.target(target)
				.event(event);
	}
}
