package com.github.zachsand.osrs.dreambot.scripts.script;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import com.github.zachsand.osrs.dreambot.scripts.config.StateMachineConfig;
import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

@ScriptManifest(name = "Test", description = "Test Script!", author = "testAuthor", version = 0.1, category = Category.COMBAT, image = "")
public class Script extends AbstractScript {

	private StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine;

	@Override
	public void onStart() {
		StateMachineBuilder.Builder<PlayerCharacterState, PlayerCharacterEvent> builder = StateMachineBuilder.builder();
		try {
			StateMachineConfig.configureStateMachineConfiguration(builder);
			StateMachineConfig.configureStateMachineStates(builder);
			StateMachineConfig.configureStateMachineTransitions(builder);
			stateMachine = builder.build();
		} catch (final Exception e) {
			MethodProvider.logError("Unable to start state machine: " + e.getMessage());
		}
		if (stateMachine != null) {
			stateMachine.startReactively().block();
		}
	}

	@Override
	public int onLoop() {
		if (stateMachine != null && !stateMachine.hasStateMachineError() && !stateMachine.isComplete()) {
			return Integer.MAX_VALUE;
		}
		return -1;
	}

	@Override
	public void onExit() {
		if (stateMachine != null) {
			stateMachine.stopReactively().block();
		}
	}

}
