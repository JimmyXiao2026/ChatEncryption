package com.jimmyxiao.chatencryption.encryption;

/**
 * A minimal generic pair. No Chat Reports relied on a Minecraft utility pair
 * which no longer exists in 1.21.11, so this replaces it. The generic type is
 * {@code Pair<AlgorithmParameterSpec, byte[]>} throughout the AES encryptors:
 * first is the parameter spec, second the raw IV bytes.
 */
public final class Pair<F, S> {
	private final F first;
	private final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}
}
