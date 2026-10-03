package com.jimmyxiao.chatencryption.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TooltipHelper {
	private TooltipHelper() {
		throw new IllegalStateException("Can't touch this");
	}

	public static MutableComponent getCtrl() {
		if (isMac()) {
			return Component.translatable("key.chatencryption.cmd");
		}
		return Component.translatable("key.chatencryption.ctrl");
	}

	private static boolean isMac() {
		String os = System.getProperty("os.name", "").toLowerCase();
		return os.contains("mac") || os.contains("osx");
	}
}
