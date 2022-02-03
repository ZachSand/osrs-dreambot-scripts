package com.github.zachsand.osrs.dreambot.scripts.actions;

import java.util.List;

import org.dreambot.api.Client;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;

public class WalkToTileAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int DESTINATION_DISTANCE_THRESHOLD = 10;
	private static final int RUN_ENERGY_THRESHOLD = 20;

	private final Tile destinationTile;
	private final List<String> tileInteractions;
	private final PlayerCharacterEvent completionEvent;

	private WalkToTileAction(final WalkToActionBuilder walkToActionBuilder) {
		this.destinationTile = walkToActionBuilder.destinationTile;
		this.tileInteractions = walkToActionBuilder.tileInteractions;
		this.completionEvent = walkToActionBuilder.completionEvent;
	}

	public static WalkToActionBuilder newBuilder() {
		return new WalkToActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		MethodProvider.log("Attempting to walk to destination: " + destinationTile);
		boolean arrivedAtTile = hasArrivedAtTile();

		while (!arrivedAtTile) {
			arrivedAtTile = walkToTile();
		}

		executeTileInteractions();
		sendCompletionEvent(context);
	}

	public static final class WalkToActionBuilder {
		private Tile destinationTile;
		private List<String> tileInteractions;
		private PlayerCharacterEvent completionEvent;

		public WalkToActionBuilder destinationTile(final Tile destinationTile) {
			this.destinationTile = destinationTile;
			return this;
		}

		public WalkToActionBuilder destinationTileInteractions(final List<String> tileInteractions) {
			this.tileInteractions = tileInteractions;
			return this;
		}

		public WalkToActionBuilder completionEvent(final PlayerCharacterEvent completionEvent) {
			this.completionEvent = completionEvent;
			return this;
		}

		private void validate() {
			if (destinationTile == null) {
				throw new ActionBuilderValidationException(" requires a non null destinationTile");
			}
		}

		public WalkToTileAction build() {
			validate();
			return new WalkToTileAction(this);
		}

	}

	private void sendCompletionEvent(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		if (completionEvent != null) {
			ActionUtil.sendEvent(context.getStateMachine(), completionEvent);
		}
	}

	private void executeTileInteractions() {
		if (!CollectionUtils.isEmpty(tileInteractions)) {
			GameObject objectOnTile = GameObjects.getTopObjectOnTile(destinationTile);
			if (objectOnTile != null) {
				tileInteractions.forEach(tileInteraction -> {
					MethodProvider
							.log(" attempting to interact with " + objectOnTile.getName() + " with action " + tileInteraction);
					objectOnTile.interact(tileInteraction);
					MethodProvider.sleep((int) ActionUtil.getShortSleepTimeout());
				});

			}
		}
	}

	private boolean hasArrivedAtTile() {
		return destinationTile.distance(Players.localPlayer()) <= DESTINATION_DISTANCE_THRESHOLD;
	}

	private boolean walkToTile() {
		enableRun();
		walkToDestination();
		return hasArrivedAtTile();
	}

	private void walkToDestination() {
		if (Walking.canWalk(destinationTile)) {
			Walking.walk(destinationTile);
			walkToDestinationWithTimeout();
		} else {
			if (Client.getDestination() != null) {
				Tile currentDestination = Client.getDestination();
				Tile randomTileNearDestination = currentDestination.getRandomizedTile();
				if (!Walking.canWalk(currentDestination)) {
					int randomDestinationCorrectionAttempts = 0;
					while (randomDestinationCorrectionAttempts < 5 && !Walking.canWalk(randomTileNearDestination)) {
						randomTileNearDestination = currentDestination.getRandomizedTile();
						randomDestinationCorrectionAttempts++;
					}
				}
				Walking.walk(randomTileNearDestination);
				walkToDestinationWithTimeout();
			} else {
				Walking.walk(Players.localPlayer().getTile().getRandomizedTile(1));
				walkToDestinationWithTimeout();
				Walking.walk(destinationTile);
			}
		}
	}

	private void enableRun() {
		if (!Walking.isRunEnabled() && Walking.getRunEnergy() > RUN_ENERGY_THRESHOLD) {
			Walking.toggleRun();
			Walking.setRunThreshold(RUN_ENERGY_THRESHOLD);
		}
	}

	private void walkToDestinationWithTimeout() {
		int walkingSleepScale = Walking.isRunEnabled() ? 1 : 2;
		MethodProvider.sleepUntil(() -> Players.localPlayer().isMoving(), walkingSleepScale * ActionUtil.getRegularSleepTimeout());
		Tile currentDestination = Client.getDestination();

		if (currentDestination != null) {
			MethodProvider.sleepUntil(() -> hasArrivedAtTile() || Players.localPlayer().isStandingStill(),
					ActionUtil.getRegularSleepTimeout());
		}
	}
}
