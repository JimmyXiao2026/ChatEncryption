package com.jimmyxiao.chatencryption.encryption;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.GCMParameterSpec;

public class AESGCMEncryptor extends AESEncryptor<AESGCMEncryption> {
	protected AESGCMEncryptor(String key) throws InvalidKeyException {
		super(key, Encryption.AES_GCM);
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> generateIV() throws UnsupportedOperationException {
		byte[] iv = new byte[12];
		RANDOM.nextBytes(iv);
		return new Pair<>(new GCMParameterSpec(96, iv), iv);
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> splitIV(byte[] message) throws UnsupportedOperationException {
		byte[] iv = new byte[12];
		byte[] msg = new byte[message.length - 12];
		ByteBuffer.wrap(message).get(iv).get(msg);
		return new Pair<>(new GCMParameterSpec(96, iv), msg);
	}
}
