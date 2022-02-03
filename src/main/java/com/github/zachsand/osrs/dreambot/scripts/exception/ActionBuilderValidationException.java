package com.github.zachsand.osrs.dreambot.scripts.exception;

public class ActionBuilderValidationException extends RuntimeException {

	public ActionBuilderValidationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ActionBuilderValidationException(final String message) {
		super(message);
	}
}
