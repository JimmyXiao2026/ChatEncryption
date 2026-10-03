package com.jimmyxiao.chatencryption.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A {@link Button} whose texture is taken from a {@link SwitchableSprites}
 * state, rendered directly from the gui atlas. Ported from No Chat Reports'
 * AdvancedImageButton (simplified to direct sprite blitting for 1.21.11).
 */
public class AdvancedImageButton extends Button {
	protected final Screen parent;
	protected final SwitchableSprites switchable;

	public AdvancedImageButton(int x, int y, int width, int height, SwitchableSprites sprites,
			OnPress onPress, Component name, Screen parent) {
		super(x, y, width, height, name, onPress, DEFAULT_NARRATION);
		this.parent = parent;
		this.switchable = sprites;
	}

	public void useSprites(int index) {
		this.switchable.setIndex(index);
	}

	public int getSpritesIndex() {
		return this.switchable.getIndex();
	}

	public Identifier getCurrentTexture() {
		SpriteSet set = this.switchable.getCurrent();
		return !this.active ? set.disabled() : (this.isHoveredOrFocused() ? set.hovered() : set.normal());
	}

	@Override
	protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getCurrentTexture(),
				this.getX(), this.getY(), this.width, this.height);
	}
}
