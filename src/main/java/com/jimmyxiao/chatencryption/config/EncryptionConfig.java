package com.jimmyxiao.chatencryption.config;

import com.jimmyxiao.chatencryption.encryption.Encryption;
import com.jimmyxiao.chatencryption.encryption.Encryptor;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Encryption configuration. Field names and the file location mirror No Chat
 * Reports' NCR-Encryption.json so that existing NCR encryption keys carry over.
 */
public class EncryptionConfig extends JSONConfig {
	public static final String FILE_NAME = "NoChatReports/NCR-Encryption.json";

	protected boolean skipWarning = false;
	protected boolean enableEncryption = false;
	protected boolean encryptPublic = true;
	protected boolean showEncryptionButton = true;
	protected boolean showEncryptionIndicators = true;
	protected boolean keepDecryptingWhenDisabled = true;
	protected boolean onlyDecryptActiveKey = false;
	protected String encryptionKey = Encryption.AES_CFB8.getDefaultKey();
	protected String encryptionPassphrase = "";
	protected String algorithmName = Encryption.AES_CFB8.getName();
	protected List<String> encryptableCommands = List.of(
			"msg:1", "w:1", "whisper:1", "tell:1", "r:0", "dm:1", "me:0", "m:1", "t:1", "pm:1",
			"emsg:1", "epm:1", "etell:1", "ewhisper:1", "message:1", "reply:0");

	/**
	 * The full list of saved keys. Each entry carries an optional note and the key
	 * itself. {@code encryptionKey} above remains the currently selected (active)
	 * key, kept for No Chat Reports single-key config compatibility.
	 */
	protected List<KeyEntry> keys = new ArrayList<>();

	/**
	 * A single saved key: an optional human-readable note, the key string, and
	 * the name of the encryption algorithm it is bound to. Serialized by Gson;
	 * public fields keep the JSON small. An empty {@code algorithm} means "use
	 * the config's default algorithm". {@code players} optionally lists (separated
	 * by ';') player names this key is restricted to: private messages to those
	 * players are encrypted with this key instead of the global one. {@code
	 * playerOnly} is the master switch for that restriction: when off, this key is
	 * a normal global key even if {@code players} is filled in.
	 */
	public static class KeyEntry {
		public String note = "";
		public String key = "";
		public String algorithm = "";
		public String players = "";
		public boolean playerOnly = false;
		public boolean enabled = true;

		public KeyEntry() {
		}

		public KeyEntry(String note, String key) {
			this(note, key, "", "", false);
		}

		public KeyEntry(String note, String key, String algorithm) {
			this(note, key, algorithm, "", false);
		}

		public KeyEntry(String note, String key, String algorithm, String players) {
			this(note, key, algorithm, players, false);
		}

		public KeyEntry(String note, String key, String algorithm, String players, boolean playerOnly) {
			this.note = note == null ? "" : note;
			this.key = key;
			this.algorithm = algorithm == null ? "" : algorithm;
			this.players = players == null ? "" : players;
			this.playerOnly = playerOnly;
		}

		/**
		 * Whether this key is restricted to the given player name (case-insensitive,
		 * entries separated by ';'). Only returns true when the player-only switch
		 * is on.
		 */
		public boolean matchesPlayer(String playerName) {
			if (!this.playerOnly || this.players == null || this.players.isEmpty()
					|| playerName == null || playerName.isEmpty()) {
				return false;
			}
			for (String p : this.players.split(";")) {
				String t = p.trim();
				if (!t.isEmpty() && t.equalsIgnoreCase(playerName.trim())) {
					return true;
				}
			}
			return false;
		}
	}

	private transient Encryption algorithm;
	private transient boolean isValid = false;
	private transient String lastMessage = "???";

	public EncryptionConfig() {
		super(FILE_NAME);
	}

	@Override
	public EncryptionConfig getDefault() {
		return new EncryptionConfig();
	}

	@Override
	protected void uponLoad() {
		this.algorithm = Encryption.getRegistered().stream()
				.filter(e -> e.getName().equals(this.algorithmName))
				.findFirst().orElse(Encryption.AES_CFB8);
		String defaultAlg = this.algorithm.getName();
		// Seed the key list from the legacy single-key field so an existing
		// No Chat Reports config carries over as the first saved key, bound to
		// the algorithm the old config had selected.
		if (this.keys.isEmpty() && !this.encryptionKey.isEmpty()
				&& !this.encryptionKey.equals(this.algorithm.getDefaultKey())) {
			this.keys.add(new KeyEntry("", this.encryptionKey, defaultAlg));
		}
		// Older configs (without per-key algorithm) bind to the default algorithm.
		for (KeyEntry e : this.keys) {
			if (e.algorithm == null || e.algorithm.isEmpty()) {
				e.algorithm = defaultAlg;
			}
			if (e.players == null) {
				e.players = "";
			}
		}
		this.validate();
	}

	private void validate() {
		this.isValid = this.getActiveAlgorithm().validateKey(this.encryptionKey);
	}

	public void toggleEncryption() {
		this.enableEncryption = !this.enableEncryption;
		this.saveFile();
	}

	public void setAlgorithm(Encryption encryption) {
		this.algorithm = encryption;
		this.algorithmName = encryption.getName();
		this.validate();
		this.saveFile();
	}

	public void setEncryptionKey(String key) {
		this.encryptionKey = key;
		this.validate();
		this.saveFile();
	}

	public void setEncryptionPassphrase(String pass) {
		this.encryptionPassphrase = pass;
		this.saveFile();
	}

	public void setEncryptPublic(boolean encryptPublic) {
		this.encryptPublic = encryptPublic;
		this.saveFile();
	}

	public void disableWarning() {
		this.skipWarning = true;
	}

	public boolean isWarningDisabled() {
		return this.skipWarning;
	}

	public boolean isEnabled() {
		return this.enableEncryption;
	}

	public String getEncryptionKey() {
		return this.encryptionKey;
	}

	public String getEncryptionPassphrase() {
		return this.encryptionPassphrase;
	}

	public boolean isValid() {
		return this.isValid;
	}

	public boolean shouldEncryptPublic() {
		return this.encryptPublic;
	}

	public boolean showEncryptionIndicators() {
		return this.showEncryptionIndicators;
	}

	public boolean isEnabledAndValid() {
		return this.isEnabled() && this.isValid();
	}

	public boolean keepDecryptingWhenDisabled() {
		return this.keepDecryptingWhenDisabled;
	}

	public void setKeepDecryptingWhenDisabled(boolean value) {
		this.keepDecryptingWhenDisabled = value;
		this.saveFile();
	}

	public boolean onlyDecryptActiveKey() {
		return this.onlyDecryptActiveKey;
	}

	public void setOnlyDecryptActiveKey(boolean value) {
		this.onlyDecryptActiveKey = value;
		this.saveFile();
	}

	/**
	 * Whether we should still decrypt incoming encrypted messages: whenever any
	 * saved key is valid, and encryption is enabled OR "keep decrypting when
	 * disabled" is on. When encryption is off and this is off, incoming
	 * encrypted chat is left as-is.
	 */
	public boolean shouldDecryptIncoming() {
		return this.hasAnyValidKey() && (this.isEnabled() || this.keepDecryptingWhenDisabled);
	}

	public Encryption getAlgorithm() {
		return this.algorithm;
	}

	public boolean shouldEncrypt(String message) {
		return this.isEnabledAndValid() && this.getEncryptionStartIndex(message) != -1;
	}

	public boolean showEncryptionButton() {
		return this.showEncryptionButton;
	}

	public int getEncryptionStartIndex(String message) {
		if (!message.startsWith("/")) {
			return this.encryptPublic ? 0 : -1;
		}
		for (String rule : this.encryptableCommands) {
			String[] splat = rule.split(":");
			if (splat.length != 2) {
				throw new IllegalArgumentException("Invalid encryptable command definition: " + rule
						+ ", in file: " + FILE_NAME);
			}
			String cmd = splat[0];
			String args = splat[1];
			int argnum;
			try {
				argnum = Integer.parseInt(args);
			}
			catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid encryptable command definition: " + rule
						+ ", in file: " + FILE_NAME);
			}
			String prefix = "/(" + cmd + "|.*:" + cmd + ") .*";
			if (!message.matches(prefix)) {
				continue;
			}
			splat = message.split(" ", 2);
			char[] array = splat[1].toCharArray();
			for (int i = 0; i < array.length; i++) {
				char ch = array[i];
				if (argnum > 0) {
					if (ch != ' ') {
						continue;
					}
					argnum--;
					continue;
				}
				int index = i + splat[0].length() + 1;
				return index < message.length() ? index : -1;
			}
		}
		return -1;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public String getLastMessage() {
		return this.lastMessage;
	}

	public Optional<Encryptor<?>> getEncryptor() {
		// Uses the active key and the algorithm it is bound to. Outgoing
		// encryption is still gated separately by shouldEncrypt() (enabled+valid).
		if (!this.isValid() || (!this.isEnabled() && !this.keepDecryptingWhenDisabled)) {
			return Optional.empty();
		}
		try {
			return Optional.of(this.getActiveAlgorithm().getProcessor(this.encryptionKey));
		}
		catch (InvalidKeyException ex) {
			throw new RuntimeException(ex);
		}
	}

	// ------------------------------------------------------------------
	// Multi-key support
	// ------------------------------------------------------------------

	/**
	 * Unmodifiable view of all saved keys (note + key + bound algorithm).
	 */
	public List<KeyEntry> getKeys() {
		return Collections.unmodifiableList(this.keys);
	}

	/**
	 * The entry whose key matches the active {@code encryptionKey}, or the first
	 * entry, or {@code null} when the list is empty.
	 */
	public KeyEntry getActiveKeyEntry() {
		for (KeyEntry e : this.keys) {
			if (e.key != null && e.key.equals(this.encryptionKey)) {
				return e;
			}
		}
		return this.keys.isEmpty() ? null : this.keys.get(0);
	}

	/**
	 * The note attached to the active key, or an empty string.
	 */
	public String getActiveKeyNote() {
		KeyEntry e = this.getActiveKeyEntry();
		return e == null ? "" : e.note;
	}

	/**
	 * Resolves the encryption algorithm a key entry is bound to, falling back to
	 * the config's default algorithm when the entry has none.
	 */
	public Encryption getAlgorithmForKey(KeyEntry entry) {
		if (entry != null && entry.algorithm != null && !entry.algorithm.isEmpty()) {
			Encryption alg = Encryption.getRegistered().stream()
					.filter(e -> e.getName().equals(entry.algorithm)).findFirst().orElse(null);
			if (alg != null) {
				return alg;
			}
		}
		return this.algorithm;
	}

	/**
	 * The algorithm bound to the currently active key.
	 */
	public Encryption getActiveAlgorithm() {
		return this.getAlgorithmForKey(this.getActiveKeyEntry());
	}

	/**
	 * Whether any saved key is valid (against its own bound algorithm). Used to
	 * decide whether incoming messages may be decrypted at all.
	 */
	public boolean hasAnyValidKey() {
		if (this.keys.isEmpty()) {
			return this.isValid();
		}
		for (KeyEntry e : this.keys) {
			if (e.key != null && this.getAlgorithmForKey(e).validateKey(e.key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a new key (with an optional note, bound algorithm, player list and
	 * player-only switch) and makes it the active key. If an identical key with
	 * the same algorithm already exists, it is merely selected instead.
	 */
	public void addKey(String note, String key, String algorithmName, String players, boolean playerOnly) {
		for (KeyEntry e : this.keys) {
			// Duplicate only when key, algorithm and player list all match. A key
			// with the same value but a different player list is allowed (e.g. the
			// same secret used for different players).
			if (e.key != null && e.key.equals(key)
					&& (algorithmName == null || algorithmName.isEmpty() || algorithmName.equals(e.algorithm))
					&& (players == null || players.isEmpty() || players.equals(e.players))) {
				this.selectKey(e);
				return;
			}
		}
		this.keys.add(new KeyEntry(note, key, algorithmName, players, playerOnly));
		this.encryptionKey = key;
		this.validate();
		this.saveFile();
	}

	/**
	 * Removes a key entry. If it was the active key, the first remaining key
	 * becomes active (or the default key when the list becomes empty).
	 */
	public void removeKey(KeyEntry entry) {
		this.keys.remove(entry);
		if (this.getActiveKeyEntry() == null) {
			this.encryptionKey = this.keys.isEmpty()
					? this.algorithm.getDefaultKey() : this.keys.get(0).key;
		}
		this.validate();
		this.saveFile();
	}

	/**
	 * Marks the given key entry as the active (sending) key.
	 */
	public void selectKey(KeyEntry entry) {
		this.encryptionKey = entry == null ? this.algorithm.getDefaultKey() : entry.key;
		this.validate();
		this.saveFile();
	}

	/**
	 * Edits an existing key entry's note, key, bound algorithm, player list and
	 * player-only switch in place. If it was the active key, the active key string
	 * follows the edited key.
	 */
	public void editKey(KeyEntry entry, String note, String key, String algorithmName, String players, boolean playerOnly) {
		boolean wasActive = this.encryptionKey != null && this.encryptionKey.equals(entry.key);
		entry.note = note == null ? "" : note;
		entry.key = key;
		entry.algorithm = algorithmName == null ? "" : algorithmName;
		entry.players = players == null ? "" : players;
		entry.playerOnly = playerOnly;
		if (wasActive) {
			this.encryptionKey = key;
		}
		this.validate();
		this.saveFile();
	}

	/**
	 * Extracts the recipient player name from a private-message command, or
	 * {@code null} when the message is not an encryptable command that takes a
	 * player name argument. Commands with {@code argnum 0} (such as {@code me},
	 * {@code r} or {@code reply}) have no player name and return {@code null}.
	 */
	public String getRecipientPlayerName(String message) {
		if (message == null || !message.startsWith("/")) {
			return null;
		}
		for (String rule : this.encryptableCommands) {
			String[] splat = rule.split(":");
			if (splat.length != 2) {
				continue;
			}
			int argnum;
			try {
				argnum = Integer.parseInt(splat[1]);
			}
			catch (NumberFormatException ex) {
				continue;
			}
			if (argnum <= 0) {
				// No player-name argument for these commands.
				continue;
			}
			String cmd = splat[0];
			String prefix = "/(" + cmd + "|.*:" + cmd + ") .*";
			if (!message.matches(prefix)) {
				continue;
			}
			String[] parts = message.split(" ", 2);
			if (parts.length < 2) {
				return null;
			}
			String rest = parts[1];
			int space = rest.indexOf(' ');
			String first = space == -1 ? rest : rest.substring(0, space);
			return first.isEmpty() ? null : first;
		}
		return null;
	}

	/**
	 * The first key entry restricted to the given player name, or empty. Only
	 * enabled player-only keys are considered.
	 */
	public Optional<KeyEntry> getKeyEntryForPlayer(String playerName) {
		if (playerName == null || playerName.isEmpty()) {
			return Optional.empty();
		}
		for (KeyEntry e : this.keys) {
			if (e.enabled && e.matchesPlayer(playerName)) {
				return Optional.of(e);
			}
		}
		return Optional.empty();
	}

	/**
	 * Toggles the enabled state of a player-only key and saves the config.
	 */
	public void toggleKeyEnabled(KeyEntry entry) {
		entry.enabled = !entry.enabled;
		this.saveFile();
	}

	/**
	 * An encryptor built from the first key restricted to the given player name,
	 * using that key's own bound algorithm. Empty when no key targets the player
	 * or the key is invalid.
	 */
	public Optional<Encryptor<?>> getEncryptorForPlayer(String playerName) {
		return this.getKeyEntryForPlayer(playerName).flatMap(e -> {
			Encryption alg = this.getAlgorithmForKey(e);
			if (alg.validateKey(e.key)) {
				try {
					return Optional.of(alg.getProcessor(e.key));
				}
				catch (InvalidKeyException ignored) {
					// not a valid key for this algorithm
				}
			}
			return Optional.empty();
		});
	}

	/**
	 * One encryptor per valid saved key (each using its own bound algorithm), for
	 * trying to decrypt an incoming message with any of the keys we know. Empty
	 * when there are no valid keys.
	 */
	public List<Encryptor<?>> getDecryptors() {
		List<Encryptor<?>> result = new ArrayList<>();
		if (this.onlyDecryptActiveKey) {
			// "Only decrypt the active key": try just the selected key.
			Encryption alg = this.getActiveAlgorithm();
			if (alg.validateKey(this.encryptionKey)) {
				try {
					result.add(alg.getProcessor(this.encryptionKey));
				}
				catch (InvalidKeyException ignored) {
					// not a valid key
				}
			}
			return result;
		}
		if (this.keys.isEmpty()) {
			Encryption alg = this.getActiveAlgorithm();
			if (alg.validateKey(this.encryptionKey)) {
				try {
					result.add(alg.getProcessor(this.encryptionKey));
				}
				catch (InvalidKeyException ignored) {
					// not a valid key
				}
			}
			return result;
		}
		for (KeyEntry e : this.keys) {
			if (e.key == null) {
				continue;
			}
			Encryption alg = this.getAlgorithmForKey(e);
			if (alg.validateKey(e.key)) {
				try {
					result.add(alg.getProcessor(e.key));
				}
				catch (InvalidKeyException ignored) {
					// not a valid key for this algorithm
				}
			}
		}
		return result;
	}
}
