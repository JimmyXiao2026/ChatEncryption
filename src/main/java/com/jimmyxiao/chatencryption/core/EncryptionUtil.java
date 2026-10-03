package com.jimmyxiao.chatencryption.core;

import com.jimmyxiao.chatencryption.encryption.Encryptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Decryption helpers used on the receiving side. Ported and rewritten against
 * the 1.21.11 API from No Chat Reports' EncryptionUtil. Instead of mutating
 * component fields (which required an access widener there), it builds a fresh,
 * decrypted component tree whenever anything was decrypted.
 */
public final class EncryptionUtil {
	private EncryptionUtil() {
		throw new IllegalStateException("Can't touch this");
	}

	/**
	 * Attempts to decrypt the given component. Returns the decrypted component if
	 * any encrypted part was found and decrypted, otherwise {@link Optional#empty()}.
	 */
	public static Optional<Component> tryDecrypt(Component component, Encryptor<?> encryptor) {
		Component result = decryptTree(component, encryptor);
		return result == component ? Optional.empty() : Optional.of(result);
	}

	private static Component decryptTree(Component component, Encryptor<?> encryptor) {
		Style style = component.getStyle();
		List<Component> siblings = component.getSiblings();

		List<Component> newSiblings = null;
		for (int i = 0; i < siblings.size(); i++) {
			Component s = siblings.get(i);
			Component d = decryptTree(s, encryptor);
			if (d != s) {
				if (newSiblings == null) {
					newSiblings = new ArrayList<>(siblings);
				}
				newSiblings.set(i, d);
			}
		}

		ComponentContents contents = component.getContents();
		Component replacement = null;

		if (contents instanceof PlainTextContents literal) {
			Optional<String> d = tryDecrypt(literal.text(), encryptor);
			if (d.isPresent()) {
				replacement = Component.literal(d.get()).withStyle(style);
			}
		}
		else if (contents instanceof TranslatableContents translatable) {
			Object[] args = translatable.getArgs();
			Object[] newArgs = args.clone();
			boolean argChanged = false;
			for (int i = 0; i < newArgs.length; i++) {
				Object a = args[i];
				if (a instanceof Component c) {
					Component d = decryptTree(c, encryptor);
					if (d != c) {
						newArgs[i] = d;
						argChanged = true;
					}
				}
				else if (a instanceof String str) {
					Optional<String> d = tryDecrypt(str, encryptor);
					if (d.isPresent()) {
						newArgs[i] = d.get();
						argChanged = true;
					}
				}
			}
			if (argChanged) {
				replacement = Component.translatable(translatable.getKey(), newArgs).withStyle(style);
			}
		}

		if (replacement != null) {
			MutableComponent mc = (MutableComponent) replacement;
			if (newSiblings != null) {
				for (Component s : newSiblings) {
					mc.append(s);
				}
			}
			else {
				for (Component s : siblings) {
					mc.append(s);
				}
			}
			return mc;
		}

		if (newSiblings != null) {
			MutableComponent mc = MutableComponent.create(contents).withStyle(style);
			for (Component s : newSiblings) {
				mc.append(s);
			}
			return mc;
		}

		return component;
	}

	public static Optional<String> tryDecrypt(String message, Encryptor<?> encryptor) {
		try {
			String messageCopy = message.replace('\uff1a', ' ');
			String decryptable;
			if (messageCopy.contains(" ")) {
				String[] splat = messageCopy.split(" ");
				decryptable = splat[splat.length - 1];
			}
			else {
				decryptable = messageCopy;
			}
			String decrypted = encryptor.decrypt(decryptable);
			if (decrypted.startsWith("#%")) {
				return Optional.of(message.substring(0, message.length() - decryptable.length()) + decrypted.substring(2));
			}
			return Optional.empty();
		}
		catch (Exception ex) {
			return Optional.empty();
		}
	}
}
