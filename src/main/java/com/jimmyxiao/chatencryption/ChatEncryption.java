package com.jimmyxiao.chatencryption;

import com.jimmyxiao.chatencryption.config.EncryptionConfig;
import com.jimmyxiao.chatencryption.config.JSONConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point. Loads the (client-oriented) encryption configuration so
 * that the chat mixins and GUI can access it lazily.
 */
public class ChatEncryption implements ModInitializer {
	public static final String MOD_ID = "chatencryption";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static EncryptionConfig encryption = null;

	@Override
	public void onInitialize() {
		encryption = JSONConfig.loadConfig(EncryptionConfig.class, EncryptionConfig::new,
				EncryptionConfig.FILE_NAME);
		encryption.saveFile();
	}

	public static EncryptionConfig getEncryptionConfig() {
		if (encryption == null) {
			encryption = JSONConfig.loadConfig(EncryptionConfig.class, EncryptionConfig::new,
					EncryptionConfig.FILE_NAME);
			encryption.saveFile();
		}
		return encryption;
	}
}
