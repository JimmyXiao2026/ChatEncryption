package com.jimmyxiao.chatencryption.client.mixin;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.client.gui.AdvancedTooltip;
import com.jimmyxiao.chatencryption.client.gui.EncryptionButton;
import com.jimmyxiao.chatencryption.client.gui.EncryptionWarningScreen;
import com.jimmyxiao.chatencryption.client.gui.TooltipHelper;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.encryption.Encryptor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds the encryption toggle button to the chat screen and encrypts outgoing
 * chat messages. Ported from No Chat Reports' MixinChatScreen (encryption parts
 * only).
 */
@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {
	protected MixinChatScreen(Component title) {
		super(title);
	}

	private static final int MESSAGE_MAX_LENGTH = 256;

	/**
	 * The button we add to the chat screen. Stored so {@code shiftButtonIfCovered}
	 * can move it away from other mods' buttons (e.g. No Chat Reports' own
	 * encryption button sits at the exact same spot).
	 */
	private EncryptionButton encryptionButton;

	@Inject(method = "normalizeChatMessage", at = @At("RETURN"), cancellable = true)
	public void onBeforeMessage(String original, CallbackInfoReturnable<String> info) {
		String message = info.getReturnValue();
		EncryptionConfig config = ChatEncryption.getEncryptionConfig();
		config.setLastMessage(message);
		if (!message.isEmpty() && !this.hasControlDown() && config.shouldEncrypt(message)) {
			config.getEncryptor().ifPresent(e -> {
				int index = config.getEncryptionStartIndex(message);
				String noencrypt = message.substring(0, index);
				String encrypt = message.substring(index);
				if (encrypt.length() > 0) {
					int maxEncryptedLength = MESSAGE_MAX_LENGTH - noencrypt.length();
					info.setReturnValue(noencrypt + this.getEncrypted(e, encrypt, maxEncryptedLength));
				}
			});
		}
	}

	private String getEncrypted(Encryptor<?> e, String encrypt, int maxLength) {
		while (encrypt.length() > 0) {
			String encrypted = e.encrypt("#%" + encrypt);
			if (encrypted.length() <= maxLength) {
				return encrypted;
			}
			encrypt = encrypt.substring(0, encrypt.length() - 1);
		}
		return "";
	}

	private boolean hasControlDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LCONTROL)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RCONTROL);
	}

	/**
	 * Vanilla stores the (now encrypted) message in the up/down-arrow recall
	 * history. Redirect it so the recall history keeps the plaintext message the
	 * user actually typed, not the encrypted text sent to the server.
	 */
	@Redirect(method = "handleChatInput",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/ChatComponent;addRecentChat(Ljava/lang/String;)V"))
	private void storePlaintextInHistory(ChatComponent chat, String encryptedMessage) {
		chat.addRecentChat(ChatEncryption.getEncryptionConfig().getLastMessage());
	}

	@Inject(method = "init", at = @At("HEAD"))
	private void onInit(CallbackInfo info) {
		if (!ChatEncryption.getEncryptionConfig().showEncryptionButton()) {
			return;
		}
		EncryptionConfig config = ChatEncryption.getEncryptionConfig();
		int buttonX = this.width - 23;
		int useSprites = !config.isValid() ? 2 : (config.isEnabled() ? 0 : 1);
		EncryptionButton button = new EncryptionButton(buttonX, this.height - 37, 20, 20, useSprites, btn -> {
			if (!(EncryptionWarningScreen.seenOnThisSession()
					|| config.isWarningDisabled()
					|| config.isEnabled())) {
				Minecraft.getInstance().setScreen(new EncryptionWarningScreen(this));
			}
			else if (config.isValid()) {
				config.toggleEncryption();
				((EncryptionButton) btn).useSprites(config.isEnabledAndValid() ? 0 : 1);
			}
			else {
				((EncryptionButton) btn).openEncryptionConfig();
			}
		}, Component.empty(), this);
		button.setTooltip(new AdvancedTooltip(() -> {
			if (config.isValid()) {
				return Component.translatable("gui.chatencryption.encryption_tooltip",
						Component.translatable("gui.chatencryption.encryption_state_"
								+ (config.isEnabledAndValid() ? "on" : "off")).withStyle(ChatFormatting.GOLD),
						TooltipHelper.getCtrl().withStyle(ChatFormatting.GREEN));
			}
			return Component.translatable("gui.chatencryption.encryption_tooltip_invalid",
					Component.translatable("gui.chatencryption.encryption_state_"
							+ (config.isEnabledAndValid() ? "on" : "off")).withStyle(ChatFormatting.GOLD));
		}).setMaxWidth(250));
		this.addRenderableWidget(button);
		this.encryptionButton = button;
	}

	/**
	 * After every mod has had its {@code init} HEAD injection run (and thus added
	 * its buttons), move our encryption button left until it is no longer covered
	 * by another widget. Handles the case where No Chat Reports puts its own
	 * button (or its safety-status button) at the exact same bottom-right spot.
	 */
	@Inject(method = "init", at = @At("RETURN"))
	private void shiftButtonIfCovered(CallbackInfo info) {
		if (this.encryptionButton == null) {
			return;
		}
		while (this.isCovered(this.encryptionButton)) {
			this.encryptionButton.setX(this.encryptionButton.getX() - 25);
			if (this.encryptionButton.getX() < 2) {
				break;
			}
		}
	}

	private boolean isCovered(AbstractWidget self) {
		for (GuiEventListener child : this.children()) {
			if (child == self || !(child instanceof AbstractWidget w)) {
				continue;
			}
			// Ignore the chat input box: it sits near the button but is not a
			// competing button, so it must not trigger a shift.
			if (w instanceof EditBox) {
				continue;
			}
			if (coversMostOf(w, self)) {
				return true;
			}
		}
		return false;
	}

	private static boolean coversMostOf(AbstractWidget a, AbstractWidget b) {
		int ix = Math.max(0, Math.min(a.getX() + a.getWidth(), b.getX() + b.getWidth())
				- Math.max(a.getX(), b.getX()));
		int iy = Math.max(0, Math.min(a.getY() + a.getHeight(), b.getY() + b.getHeight())
				- Math.max(a.getY(), b.getY()));
		return ix * iy > a.getWidth() * a.getHeight() / 2;
	}
}
