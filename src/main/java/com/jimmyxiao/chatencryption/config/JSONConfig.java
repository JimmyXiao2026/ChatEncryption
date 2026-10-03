package com.jimmyxiao.chatencryption.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Simple Gson-based JSON configuration base, ported from No Chat Reports'
 * JSONConfig. The server-data type adapter is omitted as it is not needed for
 * the encryption feature.
 */
public abstract class JSONConfig {
	protected static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	protected static final Gson GSON = createGson();

	protected final String fileName;
	protected final Path filePath;

	protected JSONConfig(String file) {
		this.fileName = file;
		this.filePath = CONFIG_DIR.resolve(this.fileName);
	}

	public Path getFile() {
		return this.filePath;
	}

	public void saveFile() {
		writeFile(this.fileName, this);
	}

	protected void uponLoad() {
	}

	public abstract JSONConfig getDefault();

	public static <T extends JSONConfig> T loadConfig(Class<T> configClass, Supplier<T> freshInstance, String fileName) {
		T config = readFile(fileName, configClass).orElseGet(freshInstance);
		config.uponLoad();
		return config;
	}

	private static <T extends JSONConfig> Optional<T> readFile(String fileName, Class<T> configClass) {
		Path file = CONFIG_DIR.resolve(fileName);
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			return Optional.of(GSON.fromJson(reader, configClass));
		}
		catch (Exception ex) {
			throw new RuntimeException("Could not read config file: " + file, ex);
		}
	}

	private static <T> void writeFile(String fileName, T config) {
		Path file = CONFIG_DIR.resolve(fileName);
		try {
			Files.createDirectories(file.getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				GSON.toJson(config, writer);
			}
		}
		catch (Exception ex) {
			throw new RuntimeException("Could not write config file: " + file, ex);
		}
	}

	private static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.setExclusionStrategies(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes field) {
				return field.getDeclaringClass() == JSONConfig.class;
			}

			@Override
			public boolean shouldSkipClass(Class<?> theClass) {
				return false;
			}
		});
		return builder.create();
	}
}
