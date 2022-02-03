package com.github.zachsand.osrs.dreambot.scripts.listener;

import org.dreambot.api.methods.MethodProvider;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

public class PlayerStateMachineListener extends StateMachineListenerAdapter<PlayerCharacterState, PlayerCharacterEvent> {

	@Override
	public void stateMachineStarted(final StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine) {
		MethodProvider.log("State machine has started");
	}

	@Override
	public void stateChanged(final State<PlayerCharacterState, PlayerCharacterEvent> from, final State<PlayerCharacterState, PlayerCharacterEvent> to) {
		MethodProvider.log("State changed from " + from.getId() + " to " + to.getId());
	}

	@Override
	public void stateEntered(final State<PlayerCharacterState, PlayerCharacterEvent> state) {
		MethodProvider.log("Entering state: " + state.getId());
	}

	@Override
	public void stateExited(final State<PlayerCharacterState, PlayerCharacterEvent> state) {
		MethodProvider.log("Exiting state: " + state.getId());
	}

	@Override
	public void transitionStarted(final Transition<PlayerCharacterState, PlayerCharacterEvent> transition) {
		MethodProvider.log("Transition started for source " + transition.getSource().getId() + " to target " + transition.getTarget().getId());
	}

	@Override
	public void transitionEnded(final Transition<PlayerCharacterState, PlayerCharacterEvent> transition) {
		MethodProvider.log("Transition ended for source " + transition.getSource().getId() + " to target " + transition.getTarget().getId());
	}

	@Override
	public void eventNotAccepted(final Message<PlayerCharacterEvent> event) {
		MethodProvider.logError("Event not accepted " + event.getPayload());
	}

	@Override
	public void stateMachineError(final StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine, final Exception exception) {
		if (exception != null) {
			MethodProvider.logError("Encountered state machine error with exception: " + exception.getMessage());
			MethodProvider.log(exception);
		}
	}
}
