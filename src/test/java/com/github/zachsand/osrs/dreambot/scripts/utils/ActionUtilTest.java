package com.github.zachsand.osrs.dreambot.scripts.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ActionUtilTest {

	@Test
	void test() {
		assertTrue(ActionUtil.getShortSleepTimeout() > 0);
	}
}
