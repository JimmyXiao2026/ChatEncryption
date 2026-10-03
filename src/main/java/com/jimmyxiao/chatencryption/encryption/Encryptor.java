package com.jimmyxiao.chatencryption.encryption;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract encryptor holding the shared Base64R character-shift encoding used
 * by No Chat Reports to make encrypted output transmissible through Minecraft
 * chat (avoiding special characters). Ported from No Chat Reports.
 */
public abstract class Encryptor<T extends Encryption> {
	protected static final SecureRandom RANDOM = new SecureRandom();
	protected static final Map<Character, Character> BASE64R_SHIFTS = Collections.unmodifiableMap(createBase64RShifts());
	protected static final Map<Character, Character> BASE64R_SHIFTS_REVERSE = createBase64RShiftsReverse();

	protected Encryptor() {
	}

	public abstract String encrypt(String message);

	public abstract String decrypt(String message);

	public abstract T getAlgorithm();

	public abstract String getKey();

	protected static String shiftBase64R(String string) {
		char[] chars = ensureUTF8(string).toCharArray();
		for (int i = 0; i < chars.length; i++) {
			chars[i] = BASE64R_SHIFTS.get(chars[i]);
		}
		return new String(chars);
	}

	protected static String unshiftBase64R(String string) {
		char[] chars = ensureUTF8(string).toCharArray();
		for (int i = 0; i < chars.length; i++) {
			chars[i] = BASE64R_SHIFTS_REVERSE.get(chars[i]);
		}
		return new String(chars);
	}

	protected static byte[] toBytes(String string) {
		return string.getBytes(StandardCharsets.UTF_8);
	}

	protected static String fromBytes(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	protected static String ensureUTF8(String string) {
		return fromBytes(toBytes(string));
	}

	protected static String encodeBase64R(String string) {
		return encodeBase64R(toBytes(string));
	}

	protected static String encodeBase64R(byte[] bytes) {
		return shiftBase64R(fromBytes(Encryption.BASE64_ENCODER.encode(bytes)));
	}

	protected static String decodeBase64R(String string) {
		return fromBytes(decodeBase64RBytes(string));
	}

	protected static byte[] decodeBase64RBytes(String string) {
		return Encryption.BASE64_DECODER.decode(toBytes(unshiftBase64R(string)));
	}

	protected static String encodeBinaryKey(byte[] key) {
		return fromBytes(Encryption.BASE64_ENCODER.encode(key));
	}

	protected static byte[] decodeBinaryKey(String key) throws InvalidKeyException {
		try {
			return Encryption.BASE64_DECODER.decode(toBytes(key));
		}
		catch (Exception ex) {
			throw new InvalidKeyException(ex);
		}
	}

	protected static byte[] mergeBytes(byte[] array1, byte[] array2) {
		byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	private static Map<Character, Character> createBase64RShiftsReverse() {
		Map<Character, Character> map = createBase64RShifts();
		Map<Character, Character> reverse = new HashMap<>(64);
		for (Map.Entry<Character, Character> entry : map.entrySet()) {
			reverse.put(entry.getValue(), entry.getKey());
		}
		return Collections.unmodifiableMap(reverse);
	}

	private static Map<Character, Character> createBase64RShifts() {
		Map<Character, Character> map = new HashMap<>(64);
		map.put('A', '!');
		map.put('B', '\"');
		map.put('C', '#');
		map.put('D', '$');
		map.put('E', '%');
		map.put('F', '\u00bc');
		map.put('G', '\'');
		map.put('H', '(');
		map.put('I', ')');
		map.put('J', ',');
		map.put('K', '-');
		map.put('L', '.');
		map.put('M', ':');
		map.put('N', ';');
		map.put('O', '<');
		map.put('P', '=');
		map.put('Q', '>');
		map.put('R', '?');
		map.put('S', '@');
		map.put('T', '[');
		map.put('U', '\\');
		map.put('V', ']');
		map.put('W', '^');
		map.put('X', '_');
		map.put('Y', '`');
		map.put('Z', '{');
		map.put('a', '|');
		map.put('b', '}');
		map.put('c', '~');
		map.put('d', '\u00a1');
		map.put('e', '\u00a2');
		map.put('f', '\u00a3');
		map.put('g', '\u00a4');
		map.put('h', '\u00a5');
		map.put('i', '\u00a6');
		map.put('j', '\u00a8');
		map.put('k', '\u00a9');
		map.put('l', '\u00aa');
		map.put('m', '\u00ab');
		map.put('n', '\u00ac');
		map.put('o', '\u00ae');
		map.put('p', '\u00af');
		map.put('q', '\u00b0');
		map.put('r', '\u00b1');
		map.put('s', '\u00b2');
		map.put('t', '\u00b3');
		map.put('u', '\u00b5');
		map.put('v', '\u00b6');
		map.put('w', '\u00b7');
		map.put('x', '\u00d7');
		map.put('y', '\u00b9');
		map.put('z', '\u00ba');
		map.put('0', '0');
		map.put('1', '1');
		map.put('2', '2');
		map.put('3', '3');
		map.put('4', '4');
		map.put('5', '5');
		map.put('6', '6');
		map.put('7', '7');
		map.put('8', '8');
		map.put('9', '9');
		map.put('+', '+');
		map.put('/', '\u00bb');
		map.put('=', '\u00bf');
		return map;
	}
}
