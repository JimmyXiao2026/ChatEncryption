package com.jimmyxiao.chatencryption.client.gui;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * A {@link Tooltip} that supports dynamic (supplier-backed) messages and a
 * custom wrapping width. Ported from No Chat Reports' AdvancedTooltip.
 */
public class AdvancedTooltip extends Tooltip {
	protected final Supplier<Component> supplier;
	protected final Component fallbackMessage;
	protected int maxWidth = 170;

	public AdvancedTooltip(Component message, Component narration) {
		super(message, narration);
		this.supplier = null;
		this.fallbackMessage = message;
	}

	public AdvancedTooltip(Component message) {
		this(message, message);
	}

	public AdvancedTooltip(Supplier<Component> message) {
		super(message.get(), message.get());
		this.supplier = message;
		this.fallbackMessage = null;
	}

	public AdvancedTooltip setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}

	public Component getMessage() {
		return this.supplier != null ? this.supplier.get() : this.fallbackMessage;
	}

	@Override
	public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
		return splitTooltip(minecraft, this.getMessage(), this.maxWidth);
	}

	public static List<FormattedCharSequence> splitTooltip(Minecraft minecraft, Component component, int maxWidth) {
		return minecraft.font.split(component, maxWidth);
	}
}
