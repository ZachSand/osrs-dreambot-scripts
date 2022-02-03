package com.github.zachsand.osrs.dreambot.scripts.utils;

import java.util.concurrent.TimeUnit;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import com.github.zachsand.osrs.dreambot.scripts.event.PlayerCharacterEvent;
import com.github.zachsand.osrs.dreambot.scripts.state.PlayerCharacterState;

import reactor.core.publisher.Mono;

public class ActionUtil {

	private ActionUtil() {}

	public static long getShortSleepTimeout() {
		return Calculations.random(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(2));
	}

	public static long getRegularSleepTimeout() {
		return Calculations.random(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(5));
	}

	public static long getLongSleepTimeout() {
		return Calculations.random(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(8));
	}

	public static void sendEvent(final StateMachine<PlayerCharacterState, PlayerCharacterEvent> stateMachine, final PlayerCharacterEvent event) {
		MethodProvider.log("Sending event: " + event);

		stateMachine
				.sendEvent(Mono.just(MessageBuilder
						.withPayload(event)
						.build()))
				.subscribe(eventResult -> MethodProvider.log("Finished sending event " + event + " with result " + eventResult.getResultType()));
	}
}
