package com.jimmyxiao.chatencryption.client.gui;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.encryption.Encryption;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringUtil;

/**
 * The encryption configuration screen. Ported from No Chat Reports'
 * EncryptionConfigScreen. Lets the player pick an algorithm, set a key or
 * derive one from a passphrase, and toggle public-message encryption.
 */
public class EncryptionConfigScreen extends Screen {
	private static final Component HEADER = Component.translatable("gui.chatencryption.encryption_config.header");
	private static final Component KEY_DESC = Component.translatable("gui.chatencryption.encryption_config.key_desc");
	private static final Component PASS_DESC = Component.translatable("gui.chatencryption.encryption_config.passphrase_desc");
	private static final Component VALIDATION_OK = Component.translatable("gui.chatencryption.encryption_config.validation_ok");
	private static final Component VALIDATION_FAILED = Component.translatable("gui.chatencryption.encryption_config.validation_failed");
	private static final Component DICE_TOOLTIP = Component.translatable("gui.chatencryption.encryption_config.dice_tooltip");
	private static final Component PASS_NOT_ALLOWED = Component.translatable("gui.chatencryption.encryption_config.pass_not_allowed");
	private static final Component ENCRYPT_PUBLIC = Component.translatable("gui.chatencryption.encryption_config.encrypt_public");
	private static final Component KEEP_DECRYPT = Component.translatable("gui.chatencryption.encryption_config.keep_decrypting");
	private static final Component KEEP_DECRYPT_RECOMMENDED = Component.translatable(
			"gui.chatencryption.encryption_config.keep_decrypting_recommended").withStyle(ChatFormatting.GREEN);
	private static final Identifier CROSSMARK = Identifier.fromNamespaceAndPath("chatencryption", "encryption/crossmark_big");

	private final Screen previous;
	private EditBox keyField;
	private EditBox passField;
	private AdvancedImageButton validationIcon;
	private CycleButton<Encryption> algorithmButton;
	private Checkbox encryptPublicCheck;
	private Checkbox keepDecryptCheck;
	private boolean settingPassKey = false;

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

		int w = (int) (this.width * (this.hugeGUI() ? 0.9 : 0.7));
		int keyDescSpace = (this.wrap(KEY_DESC, w - 5).size() + 1) * this.getLineHeight();
		int passDescSpace = (this.wrap(PASS_DESC, w - 5).size() + 1) * this.getLineHeight();

		this.keyField = new EditBox(this.font, (this.width - (w -= 52)) / 2 - 2,
				(this.hugeGUI() ? 25 : 45) + keyDescSpace - 15, w, 18, Component.empty());
		this.keyField.setMaxLength(512);
		this.keyField.setResponder(this::onKeyUpdate);
		this.addRenderableWidget(this.keyField);

		AdvancedImageButton button = new AdvancedImageButton(
				this.keyField.getX() + this.keyField.getWidth() - 15, this.keyField.getY() + 3, 12, 12,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/checkmark", false, false),
						GUIShenanigans.getSprites("encryption/crossmark", false, false)),
				btn -> {}, Component.empty(), this);
		button.setTooltip(new AdvancedTooltip(() -> this.validationIcon != null
				&& this.validationIcon.getSpritesIndex() == 0 ? VALIDATION_OK : VALIDATION_FAILED).setMaxWidth(250));
		button.active = false;
		this.validationIcon = button;
		this.addRenderableWidget(this.validationIcon);

		button = new AdvancedImageButton(this.keyField.getX() - 22, this.keyField.getY(), 18, 18,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/key_button", false, false)),
				btn -> {}, Component.empty(), this);
		button.active = false;
		this.addRenderableWidget(button);

		button = new AdvancedImageButton(this.keyField.getX() + this.keyField.getWidth() + 4, this.keyField.getY() - 1,
				23, 20,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/random_button")),
				btn -> {
					this.unfocusFields();
					this.keyField.setValue(this.algorithmButton.getValue().getRandomKey());
				}, Component.empty(), this);
		button.setTooltip(new AdvancedTooltip(DICE_TOOLTIP).setMaxWidth(250));
		this.addRenderableWidget(button);

		this.passField = new EditBox(this.font, (this.width - (w += 25)) / 2 + 11,
				this.keyField.getY() + this.keyField.getHeight() + passDescSpace + (this.hugeGUI() ? -3 : 13),
				w, 18, Component.empty());
		this.passField.setMaxLength(512);
		this.passField.setResponder(this::onPassphraseUpdate);
		this.addRenderableWidget(this.passField);

		button = new AdvancedImageButton(this.passField.getX() - 22, this.passField.getY(), 18, 18,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/lock_button", false, false)),
				btn -> {}, Component.empty(), this);
		button.active = false;
		this.addRenderableWidget(button);

		int y = this.passField.getY() + 24;
		int gap = 8;
		this.keepDecryptCheck = Checkbox.builder(KEEP_DECRYPT, this.font)
				.pos(this.width / 2 - 100, y)
				.selected(this.getConfig().keepDecryptingWhenDisabled())
				.build();
		this.keepDecryptCheck.setTooltip(new AdvancedTooltip(() -> Component.translatable(
				"gui.chatencryption.encryption_config.keep_decrypting_tooltip", KEEP_DECRYPT_RECOMMENDED))
				.setMaxWidth(250));
		this.addRenderableWidget(this.keepDecryptCheck);
		this.encryptPublicCheck = Checkbox.builder(ENCRYPT_PUBLIC, this.font)
				.pos(this.keepDecryptCheck.getX() + this.keepDecryptCheck.getWidth() + gap, y)
				.selected(this.getConfig().shouldEncryptPublic())
				.build();
		this.addRenderableWidget(this.encryptPublicCheck);
		// Recenter the two checkboxes as a pair.
		int totalWidth = this.encryptPublicCheck.getX() + this.encryptPublicCheck.getWidth()
				- this.keepDecryptCheck.getX();
		int startX = this.width / 2 - totalWidth / 2;
		this.keepDecryptCheck.setX(startX);
		this.encryptPublicCheck.setX(startX + this.keepDecryptCheck.getWidth() + gap);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
			this.onDone();
			this.minecraft.setScreen(this.previous);
		}).pos(this.width / 2 + 4, this.passField.getY() + 48).size(219, 20).build());

		CycleButton<Encryption> cycle = CycleButton.builder(
					value -> Component.translatable("gui.chatencryption.encryption_config.algorithm",
							Component.translatable("algorithm.chatencryption." + value.getID() + ".name")),
					this.getConfig().getAlgorithm())
				.withValues(Encryption.getRegistered())
				.displayOnlyValue()
				.withTooltip(value -> new AdvancedTooltip(
						Component.translatable("algorithm.chatencryption." + value.getID())).setMaxWidth(250))
				.create(this.width / 2 - 4 - 218, this.passField.getY() + 48, 218, 20, Component.empty(),
						(cycleButton, value) -> {
							this.unfocusFields();
							this.onAlgorithmUpdate(value);
						});
		this.algorithmButton = cycle;
		this.addRenderableWidget(this.algorithmButton);

		this.onAlgorithmUpdate(this.algorithmButton.getValue());

		if (!StringUtil.isNullOrEmpty(this.getConfig().getEncryptionPassphrase())) {
			this.passField.setValue(this.getConfig().getEncryptionPassphrase());
		}
		else if (!StringUtil.isNullOrEmpty(this.getConfig().getEncryptionKey())) {
			if (!this.getConfig().getEncryptionKey().equals(this.algorithmButton.getValue().getDefaultKey())) {
				this.keyField.setValue(this.getConfig().getEncryptionKey());
			}
			else {
				this.keyField.setValue("");
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (!this.passField.isActive()) {
			if (this.passField.isFocused()) {
				this.passField.setFocused(false);
			}
			this.passField.setEditable(false);
		}
		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.drawCenteredString(this.font, HEADER, this.width / 2, this.hugeGUI() ? 8 : 16, 0xFFFFFFFF);
		this.renderWrapped(graphics, KEY_DESC, this.keyField.getX() - 20, this.hugeGUI() ? 25 : 45);
		this.renderWrapped(graphics, PASS_DESC, this.passField.getX() - 20,
				this.keyField.getY() + this.keyField.getHeight() + (this.hugeGUI() ? 12 : 28));

		if (StringUtil.isNullOrEmpty(this.keyField.getValue()) && !this.keyField.isFocused()) {
			graphics.drawString(this.font, Component.translatable("gui.chatencryption.encryption_config.default_key",
					this.algorithmButton.getValue().getDefaultKey()),
					this.keyField.getX() + 4, this.keyField.getY() + 5, 0xFF999999);
		}
		if (!this.passField.active) {
			graphics.drawString(this.font, PASS_NOT_ALLOWED, this.passField.getX() + 4, this.passField.getY() + 5, 0xFF999999);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CROSSMARK, this.passField.getX() - 20, this.passField.getY() + 3, 14, 13);
		}
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

	private void onKeyUpdate(String key) {
		if (!this.settingPassKey) {
			this.passField.setValue("");
		}
		if (!StringUtil.isNullOrEmpty(key)) {
			this.validationIcon.useSprites(this.algorithmButton.getValue().validateKey(key) ? 0 : 1);
		}
		else {
			this.validationIcon.useSprites(0);
		}
	}

	private void onPassphraseUpdate(String pass) {
		Encryption encryption = this.algorithmButton.getValue();
		this.settingPassKey = true;
		if (!StringUtil.isNullOrEmpty(pass)) {
			if (encryption.supportsPassphrases()) {
				this.keyField.setValue(encryption.getPassphraseKey(pass));
			}
		}
		else {
			this.onKeyUpdate(this.keyField.getValue());
		}
		this.settingPassKey = false;
	}

	private void onAlgorithmUpdate(Encryption encryption) {
		if (!encryption.supportsPassphrases()) {
			this.passField.setFocused(false);
			this.passField.active = false;
			this.passField.setEditable(false);
			this.onKeyUpdate(this.keyField.getValue());
		}
		else {
			this.passField.active = true;
			this.passField.setEditable(true);
			this.onPassphraseUpdate(this.passField.getValue());
		}
	}

	private void unfocusFields() {
		this.keyField.setFocused(false);
		this.passField.setFocused(false);
	}

	private void onDone() {
		EncryptionConfig config = this.getConfig();
		Encryption encryption = this.algorithmButton.getValue();
		config.setAlgorithm(encryption);
		config.setEncryptionKey(!StringUtil.isNullOrEmpty(this.keyField.getValue())
				? this.keyField.getValue() : encryption.getDefaultKey());
		config.setEncryptPublic(this.encryptPublicCheck.selected());
		config.setKeepDecryptingWhenDisabled(this.keepDecryptCheck.selected());
	}

	private boolean hugeGUI() {
		return this.height <= 270;
	}
}
