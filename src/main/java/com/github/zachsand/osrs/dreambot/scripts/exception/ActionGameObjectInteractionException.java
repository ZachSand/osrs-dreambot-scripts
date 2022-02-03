package com.github.zachsand.osrs.dreambot.scripts.exception;

public class ActionGameObjectInteractionException extends RuntimeException implements RecoverableActionException {
	public ActionGameObjectInteractionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionGameObjectInteractionException(final String message) {
		super(message);
	}
}
