package me.naptie.bukkit.game.skywars.data;

import me.naptie.bukkit.game.skywars.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataHandler {

	private static DataHandler instance = new DataHandler();
	public static FileConfiguration i = getInstance().getGameInfo();
	private File gameInfoFile;
	private FileConfiguration gameInfo;
	private Map<String, FileConfiguration> languageConfigs = new HashMap<>();
	public static DataHandler getInstance() {
		return instance;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private DataHandler() {
		this.gameInfoFile = new File(Main.getInstance().getDataFolder(), "game-info.yml");

		if (!this.gameInfoFile.exists()) {
			try {
				this.gameInfoFile.getParentFile().mkdirs();
				this.gameInfoFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.gameInfo = YamlConfiguration.loadConfiguration(this.gameInfoFile);
		for (String language : Main.getInstance().getConfig().getStringList("languages")) {
			File languageFile = new File(Main.getInstance().getDataFolder(), language + ".yml");
			FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
			this.languageConfigs.put(language, config);
		}
	}

	public FileConfiguration getLanguageConfig(String language) {
		return languageConfigs.get(language);
	}

	private FileConfiguration getGameInfo() {
		return gameInfo;
	}

	public void saveGameInfo() {
		try {
			this.gameInfo.save(this.gameInfoFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
