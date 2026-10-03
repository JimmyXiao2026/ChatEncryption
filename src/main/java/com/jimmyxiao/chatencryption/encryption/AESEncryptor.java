package com.jimmyxiao.chatencryption.encryption;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Base AES encryptor. Ported from No Chat Reports (AESEncryptor). The
 * {@link Pair} holds {@code (AlgorithmParameterSpec, byte[])}: first is the
 * parameter spec used to initialise the cipher, second is the raw IV bytes to
 * be prepended to the ciphertext.
 */
public abstract class AESEncryptor<T extends AESEncryption> extends Encryptor<T> {
	private final T encryption;
	private final SecretKey key;
	private final Cipher encryptor;
	private final Cipher decryptor;
	private final boolean useIV;

	protected AESEncryptor(String key, T encryption) throws InvalidKeyException {
		this(new SecretKeySpec(decodeBinaryKey(key), "AES"), encryption);
	}

	protected AESEncryptor(SecretKey key, T encryption) throws InvalidKeyException {
		this.encryption = encryption;
		this.useIV = encryption.requiresIV();
		String mode = encryption.getMode();
		String padding = encryption.getPadding();
		try {
			this.key = key;
			Cipher encryptor = Cipher.getInstance(this.key.getAlgorithm() + "/" + mode + "/" + padding);
			if (this.useIV) {
				encryptor.init(Cipher.ENCRYPT_MODE, this.key, this.generateIV().getFirst());
			} else {
				encryptor.init(Cipher.ENCRYPT_MODE, this.key);
			}
			this.encryptor = encryptor;
			Cipher decryptor = Cipher.getInstance(this.key.getAlgorithm() + "/" + mode + "/" + padding);
			if (this.useIV) {
				decryptor.init(Cipher.DECRYPT_MODE, this.key, this.generateIV().getFirst());
			} else {
				decryptor.init(Cipher.DECRYPT_MODE, this.key);
			}
			this.decryptor = decryptor;
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new RuntimeException(ex);
		}
		catch (InvalidAlgorithmParameterException ex) {
			throw new InvalidKeyException(ex);
		}
	}

	@Override
	public String encrypt(String message) {
		try {
			if (this.useIV) {
				Pair<AlgorithmParameterSpec, byte[]> tuple = this.generateIV();
				this.encryptor.init(Cipher.ENCRYPT_MODE, this.key, tuple.getFirst());
				byte[] encrypted = this.encryptor.doFinal(toBytes(message));
				return encodeBase64R(ByteBuffer.allocate(encrypted.length + tuple.getSecond().length)
						.put(tuple.getSecond()).put(encrypted).array());
			}
			return encodeBase64R(this.encryptor.doFinal(toBytes(message)));
		}
		catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String decrypt(String message) {
		try {
			if (this.useIV) {
				Pair<AlgorithmParameterSpec, byte[]> tuple = this.splitIV(decodeBase64RBytes(message));
				this.decryptor.init(Cipher.DECRYPT_MODE, this.key, tuple.getFirst());
				return fromBytes(this.decryptor.doFinal(tuple.getSecond()));
			}
			return fromBytes(this.decryptor.doFinal(decodeBase64RBytes(message)));
		}
		catch (AEADBadTagException ex) {
			return "???";
		}
		catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getKey() {
		return encodeBinaryKey(this.key.getEncoded());
	}

	@Override
	public T getAlgorithm() {
		return this.encryption;
	}

	protected abstract Pair<AlgorithmParameterSpec, byte[]> generateIV() throws UnsupportedOperationException;

	protected abstract Pair<AlgorithmParameterSpec, byte[]> splitIV(byte[] message) throws UnsupportedOperationException;
}
