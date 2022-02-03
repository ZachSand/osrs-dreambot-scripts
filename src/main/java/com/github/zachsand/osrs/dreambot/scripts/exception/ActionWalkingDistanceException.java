package com.github.zachsand.osrs.dreambot.scripts.exception;

public class ActionWalkingDistanceException extends RuntimeException implements RecoverableActionException {
	public ActionWalkingDistanceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionWalkingDistanceException(final String message) {
		super(message);
	}
}
