package com.github.zachsand.osrs.dreambot.scripts.guards;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

public class LogFireCookingGuard implements Guard<PlayerCharacterState, PlayerCharacterEvent> {

	@Override
	public boolean evaluate(final StateContext<PlayerCharacterState, PlayerCharacterEvent> context) {

		return false;
	}
}
