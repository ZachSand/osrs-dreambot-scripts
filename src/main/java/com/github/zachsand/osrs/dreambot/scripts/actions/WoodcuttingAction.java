package com.github.zachsand.osrs.dreambot.scripts.actions;

import java.util.Set;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionRequiredGameObjectMissingException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionWalkingDistanceException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;

public class WoodcuttingAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int TREE_DISTANCE_HIGH_THRESHOLD = 10;
	private static final int TREE_DISTANCE_LOW_THRESHOLD = 5;

	private static final String LOGS_ITEM = "logs";
	private static final String CHOP_TREE_ACTION = "Chop down";

	private final String treeName;
	private final Set<Integer> treeIds;
	private final int logsToCollect;

	private WoodcuttingAction(final WoodcuttingActionBuilder woodcuttingActionBuilder) {
		this.treeName = woodcuttingActionBuilder.treeName;
		this.treeIds = woodcuttingActionBuilder.treeIds;
		this.logsToCollect = woodcuttingActionBuilder.logsToCollect;
	}

	public static WoodcuttingActionBuilder newBuilder() {
		return new WoodcuttingActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		MethodProvider.log("Attempting to collect " + logsToCollect + " logs");
		boolean finishedCollecting = isFinishedCollecting();
		while (!finishedCollecting) {
			finishedCollecting = collectLogs();
		}
	}

	private boolean isFinishedCollecting() {
		return currentLogCount() == logsToCollect || Inventory.isFull();
	}

	private boolean collectLogs() {
		GameObject closestTree = CollectionUtils.isEmpty(treeIds) ? getClosestTreeByName() : getClosestTreeById();

		double playerDistanceFromTree = Players.localPlayer().getTile().distance(closestTree.getTile());
		if (playerDistanceFromTree > TREE_DISTANCE_HIGH_THRESHOLD) {
			throw new ActionWalkingDistanceException(
					" requires player must be " + TREE_DISTANCE_HIGH_THRESHOLD + " from the tree: " + closestTree.getName());
		}

		if (playerDistanceFromTree > TREE_DISTANCE_LOW_THRESHOLD) {
			Walking.walk(closestTree.getTile());
			MethodProvider.sleepUntil(() -> closestTree.distance(Players.localPlayer().getTile()) < TREE_DISTANCE_LOW_THRESHOLD,
					ActionUtil.getShortSleepTimeout());
		}

		int currentLogCount = currentLogCount();
		closestTree.interact(CHOP_TREE_ACTION);
		MethodProvider.sleepUntil(() -> currentLogCount() > currentLogCount, ActionUtil.getLongSleepTimeout());
		return isFinishedCollecting();
	}

	private int currentLogCount() {
		//This should be more specific to the type of logs actually being chopped
		return Inventory.count(item -> item.getName().toLowerCase().contains(LOGS_ITEM));
	}

	public static final class WoodcuttingActionBuilder {
		private String treeName;
		private Set<Integer> treeIds;
		private int logsToCollect;

		public WoodcuttingActionBuilder treeName(final String treeName) {
			this.treeName = treeName;
			return this;
		}

		public WoodcuttingActionBuilder treeIds(final Set<Integer> treeIds) {
			this.treeIds = treeIds;
			return this;
		}

		public WoodcuttingActionBuilder logsToCollect(final int logsToCollect) {
			this.logsToCollect = logsToCollect;
			return this;
		}

		private void validate() {
			if (treeName == null && CollectionUtils.isEmpty(treeIds)) {
				throw new ActionBuilderValidationException(" requires treeName or treeId");
			}

			if (logsToCollect <= 0) {
				throw new ActionBuilderValidationException(" requires logsToCollect > 0");
			}
		}

		public WoodcuttingAction build() {
			validate();
			return new WoodcuttingAction(this);
		}
	}

	private GameObject getClosestTreeByName() {
		GameObject closestTree = GameObjects.closest(treeName);
		if (closestTree == null) {
			throw new ActionRequiredGameObjectMissingException("Couldn't find a tree with name: " + treeName);
		}
		return closestTree;
	}

	private GameObject getClosestTreeById() {
		GameObject closestTree = GameObjects.closest(gameObject -> treeIds.contains(gameObject.getID()));
		if (closestTree == null) {
			throw new ActionRequiredGameObjectMissingException("Couldn't find a tree with ID: " + treeIds);
		}
		return closestTree;
	}
}
