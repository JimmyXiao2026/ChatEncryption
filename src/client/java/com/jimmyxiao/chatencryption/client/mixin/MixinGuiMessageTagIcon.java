package com.jimmyxiao.chatencryption.client.mixin;

import java.util.Arrays;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Appends the {@code CHAT_NCR_ENCRYPTED} icon to {@link GuiMessageTag.Icon},
 * rendered from the encrypted-tag sprite. Ported from No Chat Reports'
 * MixinGuiMessageTagIcon.
 */
@Mixin(GuiMessageTag.Icon.class)
public abstract class MixinGuiMessageTagIcon {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chatencryption", "encryption/encrypted_tag");

	@Shadow
	@Final
	@Mutable
	private static GuiMessageTag.Icon[] $VALUES;

	@Invoker("<init>")
	private static GuiMessageTag.Icon create(String name, int ordinal, Identifier texture, int width, int height) {
		throw new IllegalStateException("Invoker transformation failed");
	}

	static {
		int ordinal = $VALUES.length;
		GuiMessageTag.Icon[] values = Arrays.copyOf($VALUES, ordinal + 1);
		values[ordinal] = create("CHAT_NCR_ENCRYPTED", ordinal, TEXTURE, 9, 9);
		$VALUES = values;
	}
}
