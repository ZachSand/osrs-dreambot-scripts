package com.github.zachsand.osrs.dreambot.scripts.actions;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.combat.Combat;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;
import com.github.zachsand.osrs.dreambot.scripts.utils.ActionUtil;

public class PassiveHealthRegenerationAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private final int healthPercentToWaitFor;

	private PassiveHealthRegenerationAction(final PassiveHealthRegenerationActionBuilder passiveHealthRegenerationActionBuilder) {
		this.healthPercentToWaitFor = passiveHealthRegenerationActionBuilder.healthPercentToWaitFor;
	}

	public static PassiveHealthRegenerationActionBuilder newBuilder() {
		return new PassiveHealthRegenerationActionBuilder();
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		while (!isDoneHealing()) {
			MethodProvider.sleepUntil(this::isDoneHealing, ActionUtil.getShortSleepTimeout());
		}
	}

	private boolean isDoneHealing() {
		return Combat.getHealthPercent() < healthPercentToWaitFor;
	}

	public static final class PassiveHealthRegenerationActionBuilder {
		private int healthPercentToWaitFor;

		public PassiveHealthRegenerationActionBuilder healthPercentToWaitFor(final int healthPercentToWaitFor) {
			this.healthPercentToWaitFor = healthPercentToWaitFor;
			return this;
		}

		private void validate() {
			if (healthPercentToWaitFor <= 0 || healthPercentToWaitFor >= 100) {
				throw new ActionBuilderValidationException(
						" requires healthPercentToWaitFor greater than 0 and less than or equal to 100");
			}
		}

		public PassiveHealthRegenerationAction build() {
			validate();
			return new PassiveHealthRegenerationAction(this);
		}
	}
}
