package com.jimmyxiao.chatencryption.client.mixin;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.core.EncryptionUtil;
import com.jimmyxiao.chatencryption.encryption.Encryptor;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Decrypts received chat messages and tags them with the encryption indicator.
 * Ported from No Chat Reports' MixinChatComponent.
 */
@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {
	@Shadow
	@Final
	private Minecraft minecraft;

	private static final GuiMessageTag.Icon ENCRYPTED_ICON = GuiMessageTag.Icon.valueOf("CHAT_NCR_ENCRYPTED");

	@ModifyVariable(index = -1,
			method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
			at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.BEFORE,
					target = "Lnet/minecraft/client/gui/components/ChatComponent;logChatMessage(Lnet/minecraft/client/GuiMessage;)V"))
	private GuiMessage modifyGUIMessage(GuiMessage msg) {
		EncryptionConfig config = ChatEncryption.getEncryptionConfig();
		if (!config.shouldDecryptIncoming()) {
			return msg;
		}
		if (this.minecraft.level == null) {
			ChatEncryption.LOGGER.warn("Chat message cannot be decrypted since level didn't load in yet!");
			return msg;
		}
		Optional<Encryptor<?>> encryptor = config.getEncryptor();
		if (encryptor.isEmpty()) {
			return msg;
		}
		Optional<Component> decrypted = EncryptionUtil.tryDecrypt(msg.content(), encryptor.get());
		if (decrypted.isPresent()) {
			Component decryptedComponent = decrypted.get();
			Component original = msg.content();
			GuiMessageTag newTag = msg.tag();
			if (config.showEncryptionIndicators()) {
				MutableComponent tooltip = Component.translatable("tag.chatencryption.encrypted",
						Component.translatable(config.getAlgorithm().getNameLocalizationKey())
								.withStyle(ChatFormatting.GOLD))
						.append(CommonComponents.NEW_LINE)
						.append(Component.translatable("tag.chatencryption.encrypted_original", original));
				newTag = new GuiMessageTag(9125575, ENCRYPTED_ICON, tooltip, "Encrypted");
			}
			return new GuiMessage(msg.addedTime(), decryptedComponent, msg.signature(), newTag);
		}
		return msg;
	}
}
