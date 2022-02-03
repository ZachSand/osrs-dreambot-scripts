package com.github.zachsand.osrs.dreambot.scripts.actions;

import static com.github.zachsand.osrs.dreambot.scripts.enums.FiremakingItem.TINDERBOX;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionGameObjectInteractionException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionRequiredGameObjectMissingException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;

public class LogFireCookingAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int FIRE_TILE_DEVIATION = 2;
	private static final int MAX_FIRE_ATTEMPTS = 5;

	private final Set<Item> itemsToCook;
	private final Tile fireTile;

	private LogFireCookingAction(final LogFireCookingActionBuilder logFireCookingActionBuilder) {
		this.itemsToCook = logFireCookingActionBuilder.itemsToCook;
		this.fireTile = logFireCookingActionBuilder.fireTile;
	}

	public static LogFireCookingActionBuilder newBuilder() {
		return new LogFireCookingActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		if (!isMoreFoodToCook()) {
			return;
		}
		validateInventoryForFire();
		Item tinderbox = Inventory.get(TINDERBOX.getItemId());
		Item logs = Inventory.get(logsFilter());

		int fireAttempt = 0;
		Tile randomFireTile = fireTile.getRandomizedTile(FIRE_TILE_DEVIATION);
		while (fireAttempt < MAX_FIRE_ATTEMPTS && !isFireCreated(randomFireTile, tinderbox, logs)) {
			randomFireTile = fireTile.getRandomizedTile(FIRE_TILE_DEVIATION);
			fireAttempt++;
		}

		GameObject fireObject = getClosestFireByTile(randomFireTile);
		if (fireObject == null) {
			throw new ActionGameObjectInteractionException(" wasn't able to create a fire for cooking");
		}

		while (isMoreFoodToCook()) {
			Item itemToCook = Inventory.get(itemsToCook::contains);
			itemToCook.useOn(fireObject);
			MethodProvider.sleepUntil(() -> !Players.localPlayer().isAnimating(), ActionUtil.getShortSleepTimeout());
		}
	}

	private boolean isFireCreated(final Tile fireTile, final Item tinderbox, final Item logs) {
		Walking.walk(fireTile);
		MethodProvider.sleepUntil(() -> Players.localPlayer().isStandingStill(), ActionUtil.getShortSleepTimeout());

		Tile currentDestination = Client.getDestination();
		if (currentDestination != null) {
			MethodProvider.sleepUntil(() -> fireTile.distance(Players.localPlayer()) <= FIRE_TILE_DEVIATION || Players.localPlayer().isStandingStill(),
					ActionUtil.getShortSleepTimeout());
		}

		tinderbox.useOn(logs);
		MethodProvider.sleep((int) Calculations.random(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(5)));

		return getClosestFireByTile(fireTile) != null;
	}

	private GameObject getClosestFireByTile(final Tile fireTile) {
		return GameObjects.closest(gameObject -> gameObject.getName().toLowerCase().contains("fire") && gameObject.distance(fireTile) <= FIRE_TILE_DEVIATION);
	}

	private boolean isMoreFoodToCook() {
		return Inventory.contains(itemsToCook::contains);
	}

	private void validateInventoryForFire() {
		if (!Inventory.contains(TINDERBOX) || !Inventory.contains(logsFilter())) {
			throw new ActionRequiredGameObjectMissingException(
					" requires a tinderbox and logs to make a fire but none are in the player's inventory");
		}
	}

	private Filter<Item> logsFilter() {
		return item -> item.getName().toLowerCase().contains("logs");
	}

	public static final class LogFireCookingActionBuilder {
		private Set<Item> itemsToCook;
		private Tile fireTile;

		public LogFireCookingActionBuilder itemsToCook(final Set<Item> itemsToCook) {
			this.itemsToCook = itemsToCook;
			return this;
		}

		public LogFireCookingActionBuilder fireTile(final Tile fireTile) {
			this.fireTile = fireTile;
			return this;
		}

		private void validate() {
			if (CollectionUtils.isEmpty(itemsToCook)) {
				throw new ActionBuilderValidationException(" requires itemsToCook");
			}

			if (fireTile == null) {
				throw new ActionBuilderValidationException(" requires fireTile to start fire by");
			}
		}

		public LogFireCookingAction build() {
			validate();
			return new LogFireCookingAction(this);
		}
	}
}
