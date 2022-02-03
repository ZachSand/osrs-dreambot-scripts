package com.github.zachsand.osrs.dreambot.scripts.exception;

public class ActionRequiredGameObjectMissingException extends RuntimeException implements RecoverableActionException {
	public ActionRequiredGameObjectMissingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionRequiredGameObjectMissingException(final String message) {
		super(message);
	}
}
