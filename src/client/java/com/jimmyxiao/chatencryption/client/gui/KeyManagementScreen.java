package com.jimmyxiao.chatencryption.client.gui;

import com.jimmyxiao.chatencryption.ChatEncryption;
import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.encryption.Encryption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

/**
 * Sub-screen for managing multiple encryption keys. Each key carries an optional
 * note and is bound to an encryption algorithm. Keys can be added (with a note,
 * manual/random/passphrase key, and a chosen algorithm), edited (note, key,
 * passphrase, algorithm), selected as the active sending key, and deleted.
 * Falls back to manual input or a random key when no key has been added yet.
 */
public class KeyManagementScreen extends Screen {
	private static final Component HEADER = Component.translatable("gui.chatencryption.keys.header");
	private static final Component LIST_HEADER = Component.translatable("gui.chatencryption.keys.list_header");
	private static final Component NONE = Component.translatable("gui.chatencryption.keys.none");
	private static final Component ADD_HEADER = Component.translatable("gui.chatencryption.keys.add_header");
	private static final Component EDIT_HEADER = Component.translatable("gui.chatencryption.keys.edit_header");
	private static final Component NOTE_HINT = Component.translatable("gui.chatencryption.keys.note_field");
	private static final Component KEY_HINT = Component.translatable("gui.chatencryption.keys.key_field");
	private static final Component PASS_HINT = Component.translatable("gui.chatencryption.keys.pass_field");
	private static final Component PLAYERS_HINT = Component.translatable("gui.chatencryption.keys.players_field");
	private static final Component PLAYER_ONLY = Component.translatable("gui.chatencryption.keys.player_only");
	private static final Component SELECT = Component.translatable("gui.chatencryption.keys.select");
	private static final Component ACTIVE = Component.translatable("gui.chatencryption.keys.active");
	private static final Component ON = Component.translatable("gui.chatencryption.keys.on");
	private static final Component OFF = Component.translatable("gui.chatencryption.keys.off");
	private static final Component DELETE = Component.translatable("gui.chatencryption.keys.delete");
	private static final Component EDIT = Component.translatable("gui.chatencryption.keys.edit");
	private static final Component ADD = Component.translatable("gui.chatencryption.keys.add");
	private static final Component SAVE = Component.translatable("gui.chatencryption.keys.save");
	private static final Component MORE = Component.translatable("gui.chatencryption.keys.more");
	private static final Component VALIDATION_OK = Component.translatable("gui.chatencryption.encryption_config.validation_ok");
	private static final Component VALIDATION_FAILED = Component.translatable("gui.chatencryption.encryption_config.validation_failed");
	private static final Component DICE_TOOLTIP = Component.translatable("gui.chatencryption.encryption_config.dice_tooltip");
	private static final Component PASS_NOT_ALLOWED = Component.translatable("gui.chatencryption.encryption_config.pass_not_allowed");

	private final Screen previous;
	private EditBox noteField;
	private EditBox keyField;
	private EditBox passField;
	private EditBox playersField;
	private Checkbox playerOnlyCheck;
	private AdvancedImageButton validationIcon;
	private CycleButton<Encryption> algorithmButton;
	private Button addButton;
	private final List<Button> editButtons = new ArrayList<>();
	private final List<Button> selectButtons = new ArrayList<>();
	private final List<Button> deleteButtons = new ArrayList<>();
	private boolean settingPassKey = false;
	private int addHeaderY;
	private int scrollOffset = 0;
	private EncryptionConfig.KeyEntry editingEntry = null;

	public KeyManagementScreen(Screen previous) {
		super(CommonComponents.GUI_BACK);
		this.previous = previous;
	}

	private EncryptionConfig getConfig() {
		return ChatEncryption.getEncryptionConfig();
	}

	private int maxRows() {
		return this.hugeGUI() ? 4 : 5;
	}

	private int rowHeight() {
		return this.hugeGUI() ? 19 : 22;
	}

	private int fieldHeight() {
		return this.hugeGUI() ? 16 : 18;
	}

	private int fieldGap() {
		return this.hugeGUI() ? 18 : 20;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		super.init();
		this.editButtons.clear();
		this.selectButtons.clear();
		this.deleteButtons.clear();

		EncryptionConfig config = this.getConfig();
		List<EncryptionConfig.KeyEntry> keys = config.getKeys();
		int maxRows = this.maxRows();
		int maxOffset = Math.max(0, keys.size() - maxRows);
		if (this.scrollOffset > maxOffset) {
			this.scrollOffset = maxOffset;
		}
		int visible = Math.min(keys.size() - this.scrollOffset, maxRows);

		int rowsStart = this.hugeGUI() ? 27 : 34;
		int listBottom = rowsStart + visible * this.rowHeight() + (visible > 0 ? (this.hugeGUI() ? 4 : 6) : 8);

		// Existing key rows: edit + select + delete buttons.
		for (int i = 0; i < visible; i++) {
			EncryptionConfig.KeyEntry entry = keys.get(this.scrollOffset + i);
			int y = rowsStart + i * this.rowHeight();
			boolean active = entry.key != null && entry.key.equals(config.getEncryptionKey());

			Button ed = Button.builder(EDIT, btn -> {
				this.editingEntry = entry;
				this.refresh();
			}).pos(this.width / 2 + 34, y).size(40, this.rowHeight() - 2).build();
			this.addRenderableWidget(ed);
			this.editButtons.add(ed);

			if (entry.playerOnly) {
				// Player-only key: show current state (on/off), click to toggle.
				Button tog = Button.builder(entry.enabled ? ON : OFF, btn -> {
					this.getConfig().toggleKeyEnabled(entry);
					this.refresh();
				}).pos(this.width / 2 + 80, y).size(46, this.rowHeight() - 2).build();
				this.addRenderableWidget(tog);
				this.selectButtons.add(tog);
			}
			else {
				Button sel = Button.builder(active ? ACTIVE : SELECT, btn -> {
					this.getConfig().selectKey(entry);
					this.refresh();
				}).pos(this.width / 2 + 80, y).size(46, this.rowHeight() - 2).build();
				sel.active = !active;
				this.addRenderableWidget(sel);
				this.selectButtons.add(sel);
			}

			Button del = Button.builder(DELETE, btn -> {
				this.getConfig().removeKey(entry);
				this.refresh();
			}).pos(this.width / 2 + 132, y).size(40, this.rowHeight() - 2).build();
			this.addRenderableWidget(del);
			this.deleteButtons.add(del);
		}

		int addHeadY = listBottom;
		this.addHeaderY = addHeadY;
		int fieldTop = addHeadY + (this.hugeGUI() ? 12 : 14);

		// Note field (above the key field), key field (+ validation + random),
		// optional passphrase field, and the algorithm selector.
		this.noteField = new EditBox(this.font, this.width / 2 - 120, fieldTop, 240, this.fieldHeight(), Component.empty());
		this.noteField.setMaxLength(64);
		this.addRenderableWidget(this.noteField);

		this.keyField = new EditBox(this.font, this.width / 2 - 120, fieldTop + this.fieldGap(), 240,
				this.fieldHeight(), Component.empty());
		this.keyField.setMaxLength(512);
		this.keyField.setResponder(this::onKeyUpdate);
		this.addRenderableWidget(this.keyField);

		AdvancedImageButton icon = new AdvancedImageButton(
				this.keyField.getX() + this.keyField.getWidth() - 15, this.keyField.getY() + 1, 12, 12,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/checkmark", false, false),
						GUIShenanigans.getSprites("encryption/crossmark", false, false)),
				btn -> {}, Component.empty(), this);
		icon.setTooltip(new AdvancedTooltip(() -> this.validationIcon != null
				&& this.validationIcon.getSpritesIndex() == 0 ? VALIDATION_OK : VALIDATION_FAILED).setMaxWidth(250));
		icon.active = false;
		this.validationIcon = icon;
		this.addRenderableWidget(this.validationIcon);

		AdvancedImageButton random = new AdvancedImageButton(
				this.keyField.getX() - 23, this.keyField.getY() - 1, 23, 20,
				SwitchableSprites.of(GUIShenanigans.getSprites("encryption/random_button")),
				btn -> {
					this.unfocusFields();
					this.keyField.setValue(this.algorithmButton.getValue().getRandomKey());
				}, Component.empty(), this);
		random.setTooltip(new AdvancedTooltip(DICE_TOOLTIP).setMaxWidth(250));
		this.addRenderableWidget(random);

		this.passField = new EditBox(this.font, this.width / 2 - 120, this.keyField.getY() + this.fieldGap(), 240,
				this.fieldHeight(), Component.empty());
		this.passField.setMaxLength(512);
		this.passField.setResponder(this::onPassphraseUpdate);
		this.addRenderableWidget(this.passField);

		// Algorithm selector for the add/edit form.
		Encryption initial = this.editingEntry != null
				? config.getAlgorithmForKey(this.editingEntry) : config.getAlgorithm();
		CycleButton<Encryption> cycle = CycleButton.builder(
					value -> Component.translatable("gui.chatencryption.encryption_config.algorithm",
							Component.translatable("algorithm.chatencryption." + value.getID() + ".name")),
					initial)
				.withValues(Encryption.getRegistered())
				.displayOnlyValue()
				.withTooltip(value -> new AdvancedTooltip(
						Component.translatable("algorithm.chatencryption." + value.getID())).setMaxWidth(250))
				.create(this.width / 2 - 120, this.passField.getY() + this.fieldGap(), 240, 20, Component.empty(),
						(cycleButton, value) -> {
							this.unfocusFields();
							this.onAlgorithmChange();
						});
		this.algorithmButton = cycle;
		this.addRenderableWidget(this.algorithmButton);

		// Player-only switch + player list (same row). The switch must be on for
		// this key to act as a player-restricted key; when off it is a normal
		// global key and the player list is ignored.
		int playerRowY = this.algorithmButton.getY() + this.algorithmButton.getHeight() + 2;
		this.playerOnlyCheck = Checkbox.builder(PLAYER_ONLY, this.font)
				.pos(this.width / 2 - 120, playerRowY)
				.selected(this.editingEntry != null && this.editingEntry.playerOnly)
				.onValueChange((box, value) -> this.updatePlayersFieldState())
				.build();
		this.addRenderableWidget(this.playerOnlyCheck);
		int playersX = this.playerOnlyCheck.getX() + this.playerOnlyCheck.getWidth() + 6;
		this.playersField = new EditBox(this.font, playersX, playerRowY,
				this.width / 2 + 120 - playersX, this.fieldHeight(), Component.empty());
		this.playersField.setMaxLength(512);
		this.addRenderableWidget(this.playersField);

		// Add / save + done buttons.
		int formBottomY = playerRowY + this.fieldGap();
		this.addButton = Button.builder(this.editingEntry != null ? SAVE : ADD, btn -> this.onAdd())
				.pos(this.width / 2 - 4 - 218, formBottomY).size(218, 20).build();
		this.addRenderableWidget(this.addButton);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn ->
				this.minecraft.setScreen(this.previous)).pos(this.width / 2 + 4,
				formBottomY).size(219, 20).build());

		// Prefill when editing an existing key.
		if (this.editingEntry != null) {
			this.noteField.setValue(this.editingEntry.note == null ? "" : this.editingEntry.note);
			this.keyField.setValue(this.editingEntry.key == null ? "" : this.editingEntry.key);
			this.playersField.setValue(this.editingEntry.players == null ? "" : this.editingEntry.players);
		}
		this.updatePlayersFieldState();

		this.onAlgorithmChange();
	}

	private void updatePlayersFieldState() {
		boolean on = this.playerOnlyCheck != null && this.playerOnlyCheck.selected();
		this.playersField.active = on;
		this.playersField.setEditable(on);
		if (!on && this.playersField.isFocused()) {
			this.playersField.setFocused(false);
		}
	}

	private void refresh() {
		this.init();
	}

	private void onAdd() {
		EncryptionConfig config = this.getConfig();
		Encryption encryption = this.algorithmButton.getValue();
		String note = this.noteField.getValue();
		String key = this.keyField.getValue();
		String players = this.playersField.getValue();
		if (StringUtil.isNullOrEmpty(key) && !StringUtil.isNullOrEmpty(this.passField.getValue())
				&& encryption.supportsPassphrases()) {
			key = encryption.getPassphraseKey(this.passField.getValue());
		}
		if (StringUtil.isNullOrEmpty(key)) {
			return;
		}
		if (this.editingEntry == null) {
			config.addKey(note, key, encryption.getName(), players, this.playerOnlyCheck.selected());
		}
		else {
			config.editKey(this.editingEntry, note, key, encryption.getName(), players, this.playerOnlyCheck.selected());
		}
		this.editingEntry = null;
		this.noteField.setValue("");
		this.keyField.setValue("");
		this.passField.setValue("");
		this.playersField.setValue("");
		this.refresh();
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

		EncryptionConfig config = this.getConfig();
		List<EncryptionConfig.KeyEntry> keys = config.getKeys();
		int rowsStart = this.hugeGUI() ? 27 : 34;
		int rowHeight = this.rowHeight();
		int maxRows = this.maxRows();
		int maxOffset = Math.max(0, keys.size() - maxRows);
		if (this.scrollOffset > maxOffset) {
			this.scrollOffset = maxOffset;
		}
		int visible = Math.min(keys.size() - this.scrollOffset, maxRows);

		// List header (with count). When there are more keys than fit, show the
		// visible range and a scroll hint on the right, so the user knows the
		// list can be scrolled with the mouse wheel.
		int headerY = this.hugeGUI() ? 25 : 28;
		graphics.drawCenteredString(this.font, Component.translatable("gui.chatencryption.keys.list_header", keys.size()),
				this.width / 2, headerY, 0xFFAAAAAA);
		if (keys.size() > maxRows) {
			int first = this.scrollOffset + 1;
			int last = this.scrollOffset + visible;
			String range = first + "–" + last + " / " + keys.size();
			graphics.drawString(this.font, range, this.width / 2 + 120 - this.font.width(range), headerY, 0xFF999999);
		}

		// Add/edit header.
		graphics.drawString(this.font, this.editingEntry != null ? EDIT_HEADER : ADD_HEADER,
				this.width / 2 - 120, this.addHeaderY, 0xFFAAAAAA);

		// Row texts.
		for (int i = 0; i < visible; i++) {
			EncryptionConfig.KeyEntry entry = keys.get(this.scrollOffset + i);
			boolean active = entry.key != null && entry.key.equals(config.getEncryptionKey());
			String label = StringUtil.isNullOrEmpty(entry.note) ? entry.key : entry.note;
			label = this.font.plainSubstrByWidth(label, (this.width / 2 + 32) - (this.width / 2 - 120) - 6);
			graphics.drawString(this.font, label, this.width / 2 - 120, rowsStart + i * rowHeight + 3,
					active ? 0xFFFFAA00 : 0xFFFFFFFF);
		}
		if (keys.isEmpty()) {
			graphics.drawString(this.font, NONE, this.width / 2 - 120, rowsStart + 2, 0xFF999999);
		}

		// Add/edit-form hints (gray) for empty fields.
		this.drawHint(graphics, NOTE_HINT, this.noteField);
		this.drawHint(graphics, KEY_HINT, this.keyField);
		if (!this.passField.isActive()) {
			graphics.drawString(this.font, PASS_NOT_ALLOWED, this.passField.getX() + 4, this.passField.getY() + 3, 0xFF999999);
		}
		else {
			this.drawHint(graphics, PASS_HINT, this.passField);
		}
		this.drawHint(graphics, PLAYERS_HINT, this.playersField);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int total = this.getConfig().getKeys().size();
		int maxOffset = Math.max(0, total - this.maxRows());
		if (maxOffset == 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int delta = verticalAmount > 0 ? -1 : (verticalAmount < 0 ? 1 : 0);
		if (delta == 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int newOffset = Math.max(0, Math.min(maxOffset, this.scrollOffset + delta));
		if (newOffset != this.scrollOffset) {
			this.scrollOffset = newOffset;
			this.refresh();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void drawHint(GuiGraphics graphics, Component hint, EditBox field) {
		if (StringUtil.isNullOrEmpty(field.getValue()) && !field.isFocused()) {
			graphics.drawString(this.font, hint, field.getX() + 4, field.getY() + 3, 0xFF999999);
		}
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

	private void onAlgorithmChange() {
		Encryption encryption = this.algorithmButton.getValue();
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

	private boolean hugeGUI() {
		return this.height <= 270;
	}
}
