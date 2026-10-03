package com.jimmyxiao.chatencryption.client.gui;

import net.minecraft.resources.Identifier;

/**
 * Builds {@link SpriteSet}s for textures placed under
 * {@code assets/chatencryption/textures/gui/sprites/}. Faithfully replicates
 * No Chat Reports' quirk where the disabled state reuses the hovered texture.
 */
public final class GUIShenanigans {
	private GUIShenanigans() {
		throw new IllegalStateException("Can't touch this");
	}

	public static SpriteSet getSprites(String path) {
		return getSprites(path, true, true);
	}

	public static SpriteSet getSprites(String path, boolean hasHovered) {
		return getSprites(path, hasHovered, true);
	}

	public static SpriteSet getSprites(String path, boolean hasHovered, boolean hasDisabled) {
		Identifier normal = Identifier.fromNamespaceAndPath("chatencryption", path);
		Identifier hovered = hasHovered ? Identifier.fromNamespaceAndPath("chatencryption", path + "_hovered") : normal;
		Identifier disabled = hasDisabled ? Identifier.fromNamespaceAndPath("chatencryption", path + "_hovered") : hovered;
		return new SpriteSet(normal, hovered, disabled);
	}
}
