package com.jimmyxiao.chatencryption.client.gui;

import net.minecraft.resources.Identifier;

/**
 * A set of gui-atlas sprite paths for the normal, hovered and disabled states
 * of a button.
 */
public record SpriteSet(Identifier normal, Identifier hovered, Identifier disabled) {
}
