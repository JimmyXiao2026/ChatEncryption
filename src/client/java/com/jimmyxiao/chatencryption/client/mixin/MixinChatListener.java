package com.jimmyxiao.chatencryption.client.mixin;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.core.EncryptionUtil;
import com.jimmyxiao.chatencryption.encryption.Encryptor;
import java.util.Optional;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Decrypts messages before they are read aloud by the narration feature. Ported
 * from No Chat Reports' MixinChatListener.
 */
@Mixin(ChatListener.class)
public abstract class MixinChatListener {

	@ModifyVariable(method = "narrateChatMessage(Lnet/minecraft/network/chat/ChatType$Bound;Lnet/minecraft/network/chat/Component;)V",
			at = @At("HEAD"), argsOnly = true)
	private Component decryptNarratedMessage(Component msg) {
		EncryptionConfig config = ChatEncryption.getEncryptionConfig();
		Optional<Encryptor<?>> encryptor = config.getEncryptor();
		if (encryptor.isEmpty()) {
			return msg;
		}
		return EncryptionUtil.tryDecrypt(msg, encryptor.get()).orElse(msg);
	}
}
