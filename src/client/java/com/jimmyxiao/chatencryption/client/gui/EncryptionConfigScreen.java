package com.jimmyxiao.chatencryption.client.gui;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.encryption.Encryption;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The encryption configuration screen. The key field from No Chat Reports has
 * been replaced with a button that opens the {@link KeyManagementScreen}, where
 * multiple keys with optional notes can be added, selected and deleted.
 */
public class EncryptionConfigScreen extends Screen {
	private static final Component HEADER = Component.translatable("gui.chatencryption.encryption_config.header");
	private static final Component KEY_DESC = Component.translatable("gui.chatencryption.encryption_config.key_desc");
	private static final Component MANAGE_KEYS = Component.translatable("gui.chatencryption.encryption_config.manage_keys");
	private static final Component ENCRYPT_PUBLIC = Component.translatable("gui.chatencryption.encryption_config.encrypt_public");
	private static final Component KEEP_DECRYPT = Component.translatable("gui.chatencryption.encryption_config.keep_decrypting");
	private static final Component ONLY_DECRYPT_ACTIVE = Component.translatable("gui.chatencryption.encryption_config.only_decrypt_active");
	private static final Component KEEP_DECRYPT_RECOMMENDED = Component.translatable(
			"gui.chatencryption.encryption_config.keep_decrypting_recommended").withStyle(net.minecraft.ChatFormatting.GREEN);
	private static final Component NONE = Component.translatable("gui.chatencryption.keys.none");

	private final Screen previous;
	private Button keyButton;
	private Checkbox encryptPublicCheck;
	private Checkbox keepDecryptCheck;
	private Checkbox onlyDecryptActiveCheck;

	public EncryptionConfigScreen(Screen previous) {
		super(CommonComponents.GUI_BACK);
		this.previous = previous;
	}

	private EncryptionConfig getConfig() {
		return ChatEncryption.getEncryptionConfig();
	}

	@Override
	protected void init() {
		this.clearWidgets();
		super.init();

		int keyDescSpace = (this.wrap(KEY_DESC, this.width - 57).size() + 1) * this.getLineHeight();

		int y = (this.hugeGUI() ? 25 : 45) + keyDescSpace - 15;

		this.keyButton = Button.builder(this.keyButtonLabel(), btn -> {
			this.minecraft.setScreen(new KeyManagementScreen(this));
		}).pos(this.width / 2 - 109, y).size(218, 20).build();
		this.keyButton.setTooltip(new AdvancedTooltip(KEY_DESC).setMaxWidth(250));
		this.addRenderableWidget(this.keyButton);

		int gap = 8;
		this.keepDecryptCheck = Checkbox.builder(KEEP_DECRYPT, this.font)
				.pos(this.width / 2 - 160, y + 26)
				.selected(this.getConfig().keepDecryptingWhenDisabled())
				.onValueChange((box, value) -> this.getConfig().setKeepDecryptingWhenDisabled(value))
				.build();
		this.keepDecryptCheck.setTooltip(new AdvancedTooltip(() -> Component.translatable(
				"gui.chatencryption.encryption_config.keep_decrypting_tooltip", KEEP_DECRYPT_RECOMMENDED))
				.setMaxWidth(250));
		this.addRenderableWidget(this.keepDecryptCheck);
		this.onlyDecryptActiveCheck = Checkbox.builder(ONLY_DECRYPT_ACTIVE, this.font)
				.pos(this.keepDecryptCheck.getX() + this.keepDecryptCheck.getWidth() + gap, y + 26)
				.selected(this.getConfig().onlyDecryptActiveKey())
				.onValueChange((box, value) -> this.getConfig().setOnlyDecryptActiveKey(value))
				.build();
		this.addRenderableWidget(this.onlyDecryptActiveCheck);
		this.encryptPublicCheck = Checkbox.builder(ENCRYPT_PUBLIC, this.font)
				.pos(this.onlyDecryptActiveCheck.getX() + this.onlyDecryptActiveCheck.getWidth() + gap, y + 26)
				.selected(this.getConfig().shouldEncryptPublic())
				.onValueChange((box, value) -> this.getConfig().setEncryptPublic(value))
				.build();
		this.addRenderableWidget(this.encryptPublicCheck);
		// Recenter the three checkboxes as a group.
		int totalWidth = this.encryptPublicCheck.getX() + this.encryptPublicCheck.getWidth()
				- this.keepDecryptCheck.getX();
		int startX = this.width / 2 - totalWidth / 2;
		this.keepDecryptCheck.setX(startX);
		this.onlyDecryptActiveCheck.setX(startX + this.keepDecryptCheck.getWidth() + gap);
		this.encryptPublicCheck.setX(startX + this.keepDecryptCheck.getWidth() + gap
				+ this.onlyDecryptActiveCheck.getWidth() + gap);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
			this.onDone();
			this.minecraft.setScreen(this.previous);
		}).pos(this.width / 2 - 109, y + 46).size(218, 20).build());
	}

	private Component keyButtonLabel() {
		EncryptionConfig config = this.getConfig();
		String note = config.getActiveKeyNote();
		if (!note.isEmpty()) {
			return Component.translatable("gui.chatencryption.encryption_config.manage_keys", Component.literal(note));
		}
		String key = config.getEncryptionKey();
		if (!key.isEmpty() && !key.equals(config.getAlgorithm().getDefaultKey())) {
			return Component.translatable("gui.chatencryption.encryption_config.manage_keys", Component.literal(key));
		}
		return Component.translatable("gui.chatencryption.encryption_config.manage_keys", NONE);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, HEADER, this.width / 2, this.hugeGUI() ? 8 : 16, 0xFFFFFFFF);
		this.renderWrapped(graphics, KEY_DESC, this.width / 2 - (this.width - 57) / 2, this.hugeGUI() ? 25 : 45);
	}

	private List<FormattedCharSequence> wrap(Component component, int width) {
		return this.font.split(component, width);
	}

	private void renderWrapped(GuiGraphics graphics, Component component, int x, int y) {
		List<FormattedCharSequence> lines = this.wrap(component, this.width - 57);
		int i = 0;
		for (FormattedCharSequence line : lines) {
			graphics.drawString(this.font, line, x, y + i * this.getLineHeight(), 0xFFFFFFFF);
			i++;
		}
	}

	private int getLineHeight() {
		if (this.hugeGUI()) {
			return (int) (9.0 * 1.5) + 1;
		}
		return 9 * 2;
	}

	private void onDone() {
		EncryptionConfig config = this.getConfig();
		config.setEncryptPublic(this.encryptPublicCheck.selected());
		config.setKeepDecryptingWhenDisabled(this.keepDecryptCheck.selected());
		config.setOnlyDecryptActiveKey(this.onlyDecryptActiveCheck.selected());
	}

	private boolean hugeGUI() {
		return this.height <= 270;
	}
}
