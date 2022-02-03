package com.github.zachsand.osrs.dreambot.scripts.actions;

import org.dreambot.api.methods.MethodProvider;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionBuilderValidationException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionGameObjectInteractionException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionRequiredGameObjectMissingException;
import com.github.zachsand.osrs.dreambot.scripts.exception.ActionWalkingDistanceException;
import com.github.zachsand.osrs.dreambot.scripts.exception.RecoverableActionException;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

public class TransitionErrorAction implements Action<PlayerCharacterState, PlayerCharacterEvent> {

	private static final int RETRY_MAX_ATTEMPTS = 3;

	private int retryAttempt;

	public TransitionErrorAction() {
		this.retryAttempt = 0;
	}

	@Override
	public void execute(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {
		Exception exception = context.getException();
		if (exception == null) {
			MethodProvider.logError(" encountered error action for source " + context.getSource().getId() + " and target "
					+ context.getTarget().getId());
			return;
		}

		MethodProvider.logError("Encountered state machine error with exception: " + exception.getMessage());
		MethodProvider.log(exception);

		if (exception instanceof RecoverableActionException) {
			handleRecoverableException(exception);
		} else if (exception instanceof ActionBuilderValidationException) {
			MethodProvider.logError("Encountered exception due to a code configuration error that can't be recovered from");
		}
	}

	private void handleRecoverableException(final Exception exception) {
		if (exception instanceof ActionRequiredGameObjectMissingException) {
			MethodProvider.logError("Encountered exception happened due to a missing game object");
		}

		if (exception instanceof ActionWalkingDistanceException) {
			MethodProvider.logError("Encountered exception happened due to an issue with walking to a specific location");
		}

		if (exception instanceof ActionGameObjectInteractionException) {
			MethodProvider.logError("Encountered exception due to an failed interaction with a game object or item");
		}

		if (retryAttempt < RETRY_MAX_ATTEMPTS) {
			retryAttempt += 1;
			MethodProvider.logError(
					"Encountered exception is recoverable. Will attempt to recover, retry attempts: (" + retryAttempt + "/" + RETRY_MAX_ATTEMPTS + ")");

			//Start machine back to initial state or similar
			//context.getStateMachine().sendEvent()
		}
	}
}
