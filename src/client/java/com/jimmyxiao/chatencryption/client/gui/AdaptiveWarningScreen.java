package com.jimmyxiao.chatencryption.client.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

/**
 * A warning screen with a wrapped message, an optional "don't show again"
 * checkbox and abstractly defined buttons. Ported from No Chat Reports'
 * AdaptiveWarningScreen.
 */
public abstract class AdaptiveWarningScreen extends Screen {
	private final Component title;
	private final Component content;
	private final Component narration;
	protected final Screen previous;
	private final Component check;
	protected Checkbox stopShowing = null;
	protected List<FormattedCharSequence> messageLines = List.of();
	protected int messageWidth = 0;

	protected AdaptiveWarningScreen(Component title, Component content, Component check,
			Screen previous) {
		super(title);
		this.title = title;
		this.content = content;
		this.check = check;
		this.narration = title.copy().append("\n").append(content);
		this.previous = previous;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		super.init();
		this.buildMessage();
		int i = (this.messageLines.size() + 1) * this.getLineHeight();
		if (this.check != null) {
			int checkY = this.hugeGUI() ? 27 : 76;
			int j = this.font.width(this.check);
			this.stopShowing = Checkbox.builder(this.check, this.font)
					.pos(this.width / 2 - j / 2 - 8, checkY + i)
					.build();
			this.addRenderableWidget(this.stopShowing);
		}
		this.initButtons(i + (this.hugeGUI() ? 55 : 100));
	}

	private void buildMessage() {
		int wrapWidth = this.width - (this.hugeGUI() ? 65 : 100);
		this.messageLines = this.font.split(this.content, wrapWidth);
		int w = 0;
		for (FormattedCharSequence line : this.messageLines) {
			int lw = this.font.width(line);
			if (lw > w) {
				w = lw;
			}
		}
		this.messageWidth = w;
	}

	protected abstract void initButtons(int y);

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		this.renderTitle(graphics);
		int k = this.width / 2 - this.messageWidth / 2;
		int y = this.hugeGUI() ? 35 : 70;
		int i = 0;
		for (FormattedCharSequence line : this.messageLines) {
			graphics.drawString(this.font, line, k, y + i * this.getLineHeight(), 0xFFFFFFFF);
			i++;
		}
	}

	private void renderTitle(GuiGraphics graphics) {
		graphics.drawString(this.font, this.title, 25, this.hugeGUI() ? 15 : 30, 0xFFFFFFFF);
	}

	private boolean hugeGUI() {
		return this.height <= 270;
	}

	protected int getLineHeight() {
		if (this.hugeGUI()) {
			return (int) (9.0 * 1.5) + 1;
		}
		return 9 * 2;
	}
}
