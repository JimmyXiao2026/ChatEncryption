package com.jimmyxiao.chatencryption.encryption;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;
import javax.crypto.spec.IvParameterSpec;

public class AESCFB8Encryptor extends AESEncryptor<AESCFB8Encryption> {
	protected AESCFB8Encryptor(String key) throws InvalidKeyException {
		super(key, Encryption.AES_CFB8);
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> generateIV() throws UnsupportedOperationException {
		long nonce = RANDOM.nextLong();
		byte[] iv = new byte[16];
		new Random(nonce).nextBytes(iv);
		return new Pair<>(new IvParameterSpec(iv), ByteBuffer.allocate(8).putLong(nonce).array());
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> splitIV(byte[] message) throws UnsupportedOperationException {
		ByteBuffer buffer = ByteBuffer.wrap(message);
		int size = buffer.capacity();
		long nonce = buffer.getLong();
		byte[] encrypted = new byte[size - 8];
		buffer.get(encrypted);
		byte[] iv = new byte[16];
		new Random(nonce).nextBytes(iv);
		return new Pair<>(new IvParameterSpec(iv), encrypted);
	}
}
