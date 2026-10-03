package com.jimmyxiao.chatencryption.config;

import com.jimmyxiao.chatencryption.encryption.Encryption;
import com.jimmyxiao.chatencryption.encryption.Encryptor;
import java.security.InvalidKeyException;
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
	protected String encryptionKey = Encryption.AES_CFB8.getDefaultKey();
	protected String encryptionPassphrase = "";
	protected String algorithmName = Encryption.AES_CFB8.getName();
	protected List<String> encryptableCommands = List.of(
			"msg:1", "w:1", "whisper:1", "tell:1", "r:0", "dm:1", "me:0", "m:1", "t:1", "pm:1",
			"emsg:1", "epm:1", "etell:1", "ewhisper:1", "message:1", "reply:0");

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
		this.validate();
	}

	private void validate() {
		this.isValid = this.algorithm.validateKey(this.encryptionKey);
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

	/**
	 * Whether we should still decrypt incoming encrypted messages: whenever we
	 * have a valid key, and encryption is enabled OR "keep decrypting when
	 * disabled" is on. When encryption is off and this is off, incoming
	 * encrypted chat is left as-is.
	 */
	public boolean shouldDecryptIncoming() {
		return this.isValid() && (this.isEnabled() || this.keepDecryptingWhenDisabled);
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
		// Available for decrypting incoming chat even when encryption is off, as
		// long as "keep decrypting when disabled" is on. Outgoing encryption is
		// still gated separately by shouldEncrypt() (requires enabled + valid).
		if (!this.isValid() || (!this.isEnabled() && !this.keepDecryptingWhenDisabled)) {
			return Optional.empty();
		}
		try {
			return Optional.of(this.algorithm.getProcessor(this.encryptionKey));
		}
		catch (InvalidKeyException ex) {
			throw new RuntimeException(ex);
		}
	}
}
