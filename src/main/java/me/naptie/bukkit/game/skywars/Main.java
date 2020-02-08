package me.naptie.bukkit.game.skywars;

import me.naptie.bukkit.game.skywars.commands.SkywarsCommand;
import me.naptie.bukkit.game.skywars.data.DataHandler;
import me.naptie.bukkit.game.skywars.listeners.*;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.Game.GameState;
import me.naptie.bukkit.game.skywars.tasks.Run;
import me.naptie.bukkit.game.skywars.utils.MySQLManager;
import me.naptie.bukkit.game.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

	public static MySQLManager mysql;
	private static Main instance;
	public Logger logger = getLogger();
	private Game game;
	private String restartCmd;

	public static Main getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		getConfig().options().copyDefaults(true);
		getConfig().options().copyHeader(true);
		saveDefaultConfig();
		for (String language : getConfig().getStringList("languages")) {
			File localeFile = new File(getDataFolder(), language + ".yml");
			if (localeFile.exists()) {
				if (getConfig().getBoolean("update-language-files")) {
					saveResource(language + ".yml", true);
				}
			} else {
				saveResource(language + ".yml", false);
			}
		}

		mysql = new MySQLManager();

		restartCmd = getConfig().getString("restart-command");

		if (DataHandler.i != null) {

			int id = DataHandler.i.getInt("id");
			game = new Game(id, this);

		} else {
			getLogger().severe(Messages.getMessage("zh-CN", "NULL_GAME_INFO"));
		}

		getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
		getServer().getPluginManager().registerEvents(new PlayerLeave(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeath(), this);
		getServer().getPluginManager().registerEvents(new FoodLevel(), this);
		getServer().getPluginManager().registerEvents(new LanguageChange(), this);
		getServer().getPluginManager().registerEvents(new ChestInteract(), this);
		getServer().getPluginManager().registerEvents(new BlockInteract(), this);
		getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
		getServer().getPluginManager().registerEvents(new PlayerMove(), this);
		getServer().getPluginManager().registerEvents(new PlayerDamage(), this);
		getServer().getPluginManager().registerEvents(new EntitySpawn(), this);
		getServer().getPluginManager().registerEvents(new PlayerPickupItem(), this);
		getServer().getPluginManager().registerEvents(new PlayerDropItem(), this);
		getServer().getPluginManager().registerEvents(new PvP(), this);
		getServer().getPluginManager().registerEvents(new WeatherChange(), this);

		getCommand("skywars").setExecutor(new SkywarsCommand());

		Logger logger = getLogger();
		logger.info(Messages.ENABLED);

	}

	@Override
	public void onDisable() {

		endGame(game);

		Logger logger = getLogger();
		logger.info(Messages.DISABLED);

	}

	private void endGame(Game game) {

		for (Player p : Bukkit.getOnlinePlayers()) {
			Main.getInstance().backToLobby(p);
		}

		game.setState(GameState.ENDING);

		RollbackHandler.get().rollback(game.getWorld());

		instance = null;
	}

	public void backToLobby(Player player) {
		me.naptie.bukkit.core.Main.getInstance().connectWithoutChecking(player, Objects.requireNonNull(ServerManager.getLobby(getServer().getPort())), false);
	}

	public Game getGame(String gameName) {
		if (game.getDisplayName().equalsIgnoreCase(gameName)) {
			return game;
		}
		return null;
	}

	public Game getGame() {
		return game;
	}

	public void restart(final Game game) {

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
			game.getPlayers().clear();
			game.getSpectators().clear();

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), restartCmd);
		}, 20);

	}

	@SuppressWarnings("unused")
	public void forceRun() {

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, Run::forcestart, 20);

	}

}
