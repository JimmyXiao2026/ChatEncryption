package com.jimmyxiao.chatencryption.client.gui;

import com.jimmyxiao.chatencryption.ChatEncryption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The first-time warning shown before configuring encryption. Ported from No
 * Chat Reports' EncryptionWarningScreen.
 */
public class EncryptionWarningScreen extends TriageWarningScreen {
	private static final Component TITLE = Component.translatable("gui.chatencryption.encryption_warning.header")
			.withStyle(ChatFormatting.GOLD);
	private static final Component CONTENT = Component.translatable("gui.chatencryption.encryption_warning.contents");
	private static final Component CHECK = Component.translatable("gui.chatencryption.encryption_warning.check");
	private static final String WIKI_LINK = "https://github.com/Aizistral-Studios/No-Chat-Reports/wiki/To-Encrypt-or-Not-to-Encrypt";
	private static boolean sessionSeen = false;

	public EncryptionWarningScreen(Screen previous) {
		super(TITLE, CONTENT, CHECK, WIKI_LINK, previous);
	}

	@Override
	protected void onProceed(Button button) {
		this.minecraft.setScreen(new EncryptionConfigScreen(this.previous));
		if (this.stopShowing != null && this.stopShowing.selected()) {
			ChatEncryption.getEncryptionConfig().disableWarning();
		}
		sessionSeen = true;
	}

	@Override
	protected void onBack(Button button) {
		this.minecraft.setScreen(this.previous);
	}

	public static boolean seenOnThisSession() {
		return sessionSeen;
	}
}
