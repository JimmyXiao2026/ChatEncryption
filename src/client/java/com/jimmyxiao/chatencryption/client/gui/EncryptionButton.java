package com.jimmyxiao.chatencryption.client.gui;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * The chat-screen encryption toggle button. Ported from No Chat Reports'
 * EncryptionButton. Right-clicking opens the configuration screen.
 */
public class EncryptionButton extends AdvancedImageButton {
	public EncryptionButton(int x, int y, int width, int height, int useSprites, OnPress onPress,
			Component name, Screen parent) {
		super(x, y, width, height, getSprites(useSprites), onPress, name, parent);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean b) {
		if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && this.isHoveredOrFocused()) {
			this.setFocused(true);
			this.openEncryptionConfig();
			return true;
		}
		return super.mouseClicked(event, b);
	}

	/**
	 * 1.21.11: the screen's click dispatcher calls {@code setFocused(widget)} on
	 * whatever widget was clicked when {@code shouldTakeFocusAfterInteraction()}
	 * returns true. Returning {@code false} here keeps the chat input box focused
	 * after clicking this toggle button, so the player can keep typing without
	 * having to re-click the input field.
	 */
	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}

	public void openEncryptionConfig() {
		if (!(EncryptionWarningScreen.seenOnThisSession()
				|| ChatEncryption.getEncryptionConfig().isWarningDisabled()
				|| ChatEncryption.getEncryptionConfig().isEnabledAndValid())) {
			Minecraft.getInstance().setScreen(new EncryptionWarningScreen(this.parent));
		}
		else {
			Minecraft.getInstance().setScreen(new EncryptionConfigScreen(this.parent));
		}
	}

	private static SwitchableSprites getSprites(int useIndex) {
		SwitchableSprites sprites = SwitchableSprites.of(
				GUIShenanigans.getSprites("encryption/active_button"),
				GUIShenanigans.getSprites("encryption/inactive_button"),
				GUIShenanigans.getSprites("encryption/error_button"));
		sprites.setIndex(useIndex);
		return sprites;
	}
}
