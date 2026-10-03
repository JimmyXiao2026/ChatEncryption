package com.jimmyxiao.chatencryption.encryption;

import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

public class AESECBEncryptor extends AESEncryptor<AESECBEncryption> {
	protected AESECBEncryptor(String key) throws InvalidKeyException {
		super(key, Encryption.AES_ECB);
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> generateIV() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Pair<AlgorithmParameterSpec, byte[]> splitIV(byte[] message) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
