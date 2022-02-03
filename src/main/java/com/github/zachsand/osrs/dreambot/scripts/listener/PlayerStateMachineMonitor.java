package com.github.zachsand.osrs.dreambot.scripts.listener;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.dreambot.api.methods.MethodProvider;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.transition.Transition;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

import reactor.core.publisher.Mono;

public class PlayerStateMachineMonitor extends AbstractStateMachineMonitor<PlayerCharacterState, PlayerCharacterEvent> {
	@Override
	public void transition(final StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine,
			final Transition<PlayerCharacterState, PlayerCharacterEvent> transition, final long duration) {
		MethodProvider
				.log("Transition from " + transition.getSource().getId() + " to " + transition.getTarget().getId() + " took "
						+ TimeUnit.MILLISECONDS.toSeconds(duration) + "s");
	}

	@Override
	public void action(final StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine,
			final Function<StateContext<PlayerCharacterState, PlayerCharacterEvent>, Mono<Void>> action,
			final long duration) {
		MethodProvider.log("Action took " + TimeUnit.MILLISECONDS.toSeconds(duration) + "s");
	}
}
