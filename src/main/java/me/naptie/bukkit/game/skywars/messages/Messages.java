package me.naptie.bukkit.game.skywars.messages;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.data.DataHandler;
import me.naptie.bukkit.game.skywars.listeners.PlayerJoin;
import me.naptie.bukkit.game.skywars.utils.CU;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messages {

	private static String pluginName = Main.getInstance().getDescription().getName();
	private static String pluginVersion = Main.getInstance().getDescription().getVersion();

	public static final String ENABLED = "Enabled " + pluginName + " v" + pluginVersion;
	public static final String DISABLED = "Disabled " + pluginName + " v" + pluginVersion;
	public static final String COMMAND_HYPHEN = CU.t("&b-----------------------------");
	public static final String ENDING_MESSAGE_HYPHEN = CU.t("&a&l---------------------------------------------");

	public static String getMessage(FileConfiguration language, String message) {
		return CU.t(language.getString(message));
	}

	public static String getMessage(String language, String message) {
		return CU.t(DataHandler.getInstance().getLanguageConfig(language).getString(message));
	}

	public static String getMessage(Player player, String message) {
		return getMessage(PlayerJoin.pl.get(player), message);
	}

	public static Map<String, String> getMessagesInDifLangs(String message) {
		Map<String, String> output = new HashMap<>();
		for (String language : Main.getInstance().getConfig().getStringList("languages")) {
			output.put(language, getMessage(language, message));
		}
		return output;
	}

	public static List<String> getMessages(Player player, String message) {
		return CU.t(getLanguage(PlayerJoin.pl.get(player)).getStringList(message));
	}

	public static List<String> getMessages(String language, String message) {
		return CU.t(getLanguage(language).getStringList(message));
	}

	private static FileConfiguration getLanguage(String language) {
		return DataHandler.getInstance().getLanguageConfig(language);
	}
}