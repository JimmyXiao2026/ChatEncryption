package com.jimmyxiao.chatencryption.client.gui;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Holds multiple {@link SpriteSet}s (one per visual state) and the index of the
 * currently used one.
 */
public class SwitchableSprites {
	private final List<SpriteSet> sprites;
	private int index = 0;

	private SwitchableSprites(SpriteSet def, SpriteSet... sprites) {
		ImmutableList.Builder<SpriteSet> builder = ImmutableList.builder();
		builder.add(def);
		builder.add(sprites);
		this.sprites = builder.build();
	}

	public SwitchableSprites setIndex(int index) {
		if (index >= this.sprites.size()) {
			index = this.sprites.size() - 1;
		}
		else if (index < 0) {
			index = 0;
		}
		this.index = index;
		return this;
	}

	public int getIndex() {
		return this.index;
	}

	public SpriteSet get(int index) {
		return this.sprites.get(index);
	}

	public SpriteSet getDefault() {
		return this.get(0);
	}

	public SpriteSet getCurrent() {
		return this.get(this.index);
	}

	public static SwitchableSprites of(SpriteSet def, SpriteSet... sprites) {
		return new SwitchableSprites(def, sprites);
	}
}
