package me.naptie.bukkit.game.skywars.objects;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.RollbackHandler;
import me.naptie.bukkit.game.skywars.data.DataHandler;
import me.naptie.bukkit.game.skywars.listeners.PlayerDeath;
import me.naptie.bukkit.game.skywars.listeners.PlayerJoin;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.tasks.Countdown;
import me.naptie.bukkit.game.skywars.tasks.End;
import me.naptie.bukkit.game.skywars.tasks.Run;
import me.naptie.bukkit.game.skywars.utils.CU;
import me.naptie.bukkit.game.skywars.utils.MapUtil;
import me.naptie.bukkit.inventory.utils.ConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.*;

public class Game {

	private final JavaPlugin plugin;
	private int maxPlayers;
	private int minPlayers;
	private int playersPerTeam;
	private int deathmatch, end;
	private Location lobby;
	private List<GamePlayer> players;
	private List<GameTeam> teams;
	private GameState gameState = GameState.LOBBY;
	private int id;
	private String displayName;
	private List<Location> spawnpoints;
	private World world;
	private boolean teamMode;
	private List<ItemStack> normalItems;
	private List<ItemStack> rareItems;
	private List<Location> normalChests;
	private List<Location> rareChests;
	private boolean insaneMode;
	private Set<GamePlayer> spectators;
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private Map<GamePlayer, Location> gamePlayerToSpawnPoint = new HashMap<>();
	private Map<GamePlayer, Boolean> gamePlayerToSpectator = new HashMap<>();
	private Set<Chest> opened;
	private boolean movementFrozen = false;

	public Game(int id, JavaPlugin plugin) {

		this.plugin = plugin;
		this.displayName = DataHandler.i.getString("display-name");
		this.maxPlayers = DataHandler.i.getInt("max-players");
		this.minPlayers = DataHandler.i.getInt("min-players");
		this.deathmatch = DataHandler.i.getInt("deathmatch");
		this.end = DataHandler.i.getInt("end");
		this.insaneMode = DataHandler.i.getBoolean("insane-mode");
		this.teamMode = DataHandler.i.getBoolean("team-mode");
		this.id = id;

		String worldName = DataHandler.i.getString("world");
		Bukkit.unloadWorld(worldName + "_active", false);
		RollbackHandler.get().rollback(worldName);

		this.world = Bukkit.createWorld(new WorldCreator(worldName + "_active"));
		resetWorld(this.world);

		try {
			String[] values = DataHandler.i.getString("lobby").split(", ");
			double x = Double.parseDouble(values[0]);
			double y = Double.parseDouble(values[1]);
			double z = Double.parseDouble(values[2]);
			lobby = new Location(world, x, y, z);
		} catch (Exception e) {
			Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA").replace("%string%", "lobby")
					.replace("%metadata%", DataHandler.i.getString("lobby") + "Exception: " + e));
		}

		this.spawnpoints = new ArrayList<>();

		for (String point : DataHandler.i.getStringList("spawnpoints")) {
			try {
				String[] values = point.split(", ");
				double x = Double.parseDouble(values[0]);
				double y = Double.parseDouble(values[1]);
				double z = Double.parseDouble(values[2]);
				Location location = new Location(world, x, y, z);
				spawnpoints.add(location);
				setCage(location, Material.GLASS);
			} catch (Exception e) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA")
						.replace("%string%", "spawnpoints and cages").replace("%metadata%", point) + "Exception: " + e);
			}
		}

		this.opened = new HashSet<>();

		this.normalItems = new ArrayList<>();
		this.rareItems = new ArrayList<>();

		for (String item : DataHandler.i.getConfigurationSection("normal-items").getKeys(false)) {
			try {
				Material material = Material.valueOf(item);
				int count = DataHandler.i.getInt("normal-items." + item + ".count");
				this.normalItems.add(new ItemStack(material, count));
			} catch (Exception e) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA")
						.replace("%string%", "normal items").replace("%metadata%", item) + "Exception: " + e);
			}
		}

		for (String item : DataHandler.i.getConfigurationSection("rare-items").getKeys(false)) {
			try {
				Material material = Material.valueOf(item);
				int count = DataHandler.i.getInt("rare-items." + item + ".count");
				this.rareItems.add(new ItemStack(material, count));
			} catch (Exception e) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA")
						.replace("%string%", "rare items").replace("%metadata%", item) + "Exception: " + e);
			}
		}

		this.normalChests = new ArrayList<>();
		this.rareChests = new ArrayList<>();

		for (String chestsStr : DataHandler.i.getStringList("normal-chests")) {
			try {
				String[] values = chestsStr.split(", ");
				double x = Double.parseDouble(values[0]);
				double y = Double.parseDouble(values[1]);
				double z = Double.parseDouble(values[2]);
				Location location = new Location(world, x, y, z);
				this.normalChests.add(location);
			} catch (Exception e) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA")
						.replace("%string%", "normal chests").replace("%metadata%", chestsStr) + "Exception: " + e);
			}
		}
		for (String chestsStr : DataHandler.i.getStringList("rare-chests")) {
			try {
				String[] values = chestsStr.split(", ");
				double x = Double.parseDouble(values[0]);
				double y = Double.parseDouble(values[1]);
				double z = Double.parseDouble(values[2]);
				Location location = new Location(world, x, y, z);
				this.rareChests.add(location);
			} catch (Exception e) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "FAILED_TO_LOAD_STH_WITH_METADATA")
						.replace("%string%", "rare chests").replace("%metadata%", chestsStr) + "Exception: " + e);
			}
		}

		this.players = new ArrayList<>();
		this.spectators = new HashSet<>();

		int playersPerTeam = DataHandler.i.getInt("players-per-team");
		if (playersPerTeam == 0) {
			Main.getInstance().getLogger().warning(Messages.getMessage("zh-CN", "PLAYERPERTEAM_IS_0"));
			playersPerTeam = 1;
			DataHandler.i.set("players-per-team", 1);
			DataHandler.getInstance().saveGameInfo();
		}
		this.playersPerTeam = playersPerTeam;
		this.teams = new ArrayList<>();

		Main.mysql.editor.set(this.id, "Skywars", me.naptie.bukkit.core.Main.getInstance().getServerName(), this.getWorldName(), this.minPlayers, this.maxPlayers, this.players.size(), this.spectators.size(), this.gameState.name(), this.playersPerTeam);

	}

	public void join(final GamePlayer gamePlayer) {
		if (isState(GameState.LOBBY) || isState(GameState.STARTING)) {
			if (getPlayers().size() == getMaxPlayers()) {
				gamePlayer.sendMessage(Messages.getMessage(gamePlayer.getPlayer(), "GAME_FULL"));
				Main.getInstance().backToLobby(gamePlayer.getPlayer());
				return;
			}

			getPlayers().add(gamePlayer);
			String size = String.valueOf(getPlayers().size());
			Main.mysql.editor.set(this.id, "players", this.players.size());
			gamePlayer.teleport(isState(GameState.LOBBY) || isState(GameState.STARTING) ? lobby : null);

			Player player = gamePlayer.getPlayer();
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			giveInteractiveItems(player);
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(gamePlayer.getPlayer().getMaxHealth());
			player.setFoodLevel(20);
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				player.removePotionEffect(potionEffect.getType());
			}
			player.setAllowFlight(false);
			for (GamePlayer eachGamePlayer : getPlayers()) {
				eachGamePlayer.getPlayer().showPlayer(gamePlayer.getPlayer());
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> sendMessage("JOINED", new String[]{"%player%", "%size%", "%max%"}, new String[]{gamePlayer.getName(), size, String.valueOf(getMaxPlayers())}), 1);

			if (getPlayers().size() == getMinPlayers() && !isState(GameState.STARTING)) {
				setState(GameState.STARTING);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
					playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					start();
				}, 1);
			}

		} else {
			activateSpectatorSettings(gamePlayer, true);
			gamePlayer.getPlayer().setScoreboard(Run.board);
		}
	}

	public void leave(GamePlayer gamePlayer) {

		getPlayers().remove(gamePlayer);
		Main.mysql.editor.set(this.id, "players", this.players.size());
		if (isPlayerSpectating(gamePlayer)) {
			getSpectators().remove(gamePlayer);
			Main.mysql.editor.set(this.id, "spectators", this.spectators.size());
		}

		for (int i = 0; i < teams.size(); i++) {
			GameTeam gameTeam = teams.get(i);
			boolean shouldRemove = true;
			for (GamePlayer gamePlayer1 : gameTeam.getMembers()) {
				if (players.contains(gamePlayer1)) {
					shouldRemove = false;
					break;
				}
			}
			if (shouldRemove)
				teams.remove(gameTeam);
		}

		if (players.size() != 0)
			sendMessage("QUIT", new String[]{"%player%"}, new String[]{gamePlayer.getName()});

		Player player = gamePlayer.getPlayer();
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}

		Run.playerObjectiveMap.remove(player.getUniqueId());
		Run.killCount.remove(player.getUniqueId());

		if (getGamePlayer(player) != null && isState(GameState.PROTECTED)
				|| isState(GameState.GAMING) || isState(GameState.DEATHMATCH)) {
			if (getPlayers().size() == 1 || getTeams().size() == 1) {
				if (teamMode) {
					for (GamePlayer winner : getTeams().get(0).getMembers()) {
						reward(winner, true);
					}
					judge(getTeams().get(0), false);
				} else {
					reward(getPlayers().get(0), true);
					judge(getPlayers().get(0), false);
				}
			} else if (getPlayers().size() == 0 || getTeams().size() == 0) {
				judge();
			}
		}
		Main.getInstance().backToLobby(player);

	}

	public void leaveFromSpectating(GamePlayer gamePlayer) {
		getSpectators().remove(gamePlayer);
		Main.mysql.editor.set(this.id, "spectators", this.spectators.size());
		Player player = gamePlayer.getPlayer();
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		Run.playerObjectiveMap.remove(player.getUniqueId());
		Main.getInstance().backToLobby(player);
	}

	public void activateSpectatorSettings(GamePlayer gamePlayer, boolean spawn) {

		getSpectators().add(gamePlayer);
		Main.mysql.editor.set(this.id, "spectators", this.spectators.size());
		gamePlayerToSpectator.put(gamePlayer, true);
		final Player player = gamePlayer.getPlayer();
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.hidePlayer(player);
		}
		player.spigot().setCollidesWithEntities(false);
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
		player.getEquipment().clear();
		player.getInventory().clear();
		giveInteractiveItems(player);
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);

		if (spawn) {
			Location spectatorSpawn = new Location(lobby.getWorld(), lobby.getX(),
					lobby.getY() - 5, lobby.getZ());
			player.teleport(spectatorSpawn);
		}

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> player.addPotionEffect(
				new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false)), 1);

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
			if (!player.getAllowFlight())
				player.setAllowFlight(true);
			player.setFlying(true);
		}, 1L, 1L);

	}

	private void start() {
		sendMessage("GAME_STARTING", new String[]{}, new String[]{});
		new Countdown(this).runTaskTimer(Main.getInstance(), 0, 20);
	}

	public void assignSpawnPositions() {
		int id = 0;
		for (GamePlayer gamePlayer : getPlayers()) {
			try {
				int i = id / playersPerTeam;
				gamePlayerToSpawnPoint.put(gamePlayer, spawnpoints.get(i));
				GameTeam gameTeam;
				try {
					gameTeam = this.teams.get(i);
				} catch (IndexOutOfBoundsException ex) {
					gameTeam = new GameTeam(i);
					this.teams.add(gameTeam);
				}
				gameTeam.addMember(gamePlayer);
				gamePlayer.teleport(spawnpoints.get(i));
				id++;
				Player player = gamePlayer.getPlayer();
				player.setGameMode(GameMode.SURVIVAL);
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
				for (PotionEffect potionEffect : player.getActivePotionEffects()) {
					player.removePotionEffect(potionEffect.getType());
				}
				player.getInventory().clear();
			} catch (Exception ex) {
				Main.getInstance().getLogger().severe(Messages.getMessage("zh-CN", "GAME_START_FAILURE").replace("%game%", getDisplayName()).replace("%ex%", ex + ""));
				ex.printStackTrace();
			}
		}
	}

	public boolean isState(GameState state) {
		return this.getGameState() == state;
	}

	public void setState(GameState gameState) {
		Main.mysql.editor.set(this.id, "state", gameState.name());
		this.gameState = gameState;
	}

	public GameState getGameState() {
		return gameState;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<GamePlayer> getPlayers() {
		return players;
	}

	public Set<GamePlayer> getSpectators() {
		return spectators;
	}

	public boolean getTeamMode() {
		return teamMode;
	}

	public GamePlayer getGamePlayer(Player player) {
		for (GamePlayer gamePlayer : getPlayers()) {
			if (gamePlayer.getPlayer() == player) {
				return gamePlayer;
			}
		}

		for (GamePlayer gamePlayer : getSpectators()) {
			if (gamePlayer.getPlayer() == player) {
				return gamePlayer;
			}
		}
		return null;
	}

	public boolean isPlayerSpectating(GamePlayer gamePlayer) {
		for (GamePlayer spectator : spectators) {
			if (spectator.getPlayer().getUniqueId() == gamePlayer.getPlayer().getUniqueId()) {
				return true;
			}
		}
		return (gamePlayerToSpectator.getOrDefault(gamePlayer, false));
	}

	public void sendMessage(String key, String[] targets, String[] replacements) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String message = Messages.getMessage(gamePlayer.getPlayer(), key);
			for (int i = 0; i < targets.length; i++) {
				if (message.contains(targets[i]))
					message = message.replace(targets[i], replacements[i]);
			}
			gamePlayer.sendMessage(message);
		}
	}

	private void sendGlobalMessage(String key, String[] targets, String[] replacements) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			String message = Messages.getMessage(gamePlayer.getPlayer(), key);
			for (int i = 0; i < targets.length; i++) {
				if (message.contains(targets[i]))
					message = message.replace(targets[i], replacements[i]);
			}
			gamePlayer.sendMessage(message);
		}
	}

	private void sendGlobalMessage(List<String> keyList, String[] targets, String[] replacements) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			for (String key : keyList) {
				String message = Messages.getMessage(gamePlayer.getPlayer(), key);
				for (int i = 0; i < targets.length; i++) {
					if (message.contains(targets[i]))
						message = message.replace(targets[i], replacements[i]);
				}
				gamePlayer.sendMessage(message);
			}
		}
	}

	public void sendMessage(String message) {
		for (GamePlayer gamePlayer : getPlayers()) {
			gamePlayer.sendMessage(message);
		}
	}

	public void sendDeathMessage(int num, GamePlayer player, GamePlayer killer) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String language = PlayerJoin.pl.get(gamePlayer.getPlayer());
			String originalMessage = ChatColor.YELLOW + Main.getInstance().getConfig().getStringList(PlayerDeath.deathMessageMap.get(language)).get(num);
			String message = originalMessage.replace("player", player.getName() + ChatColor.YELLOW).replace("killer", killer.getName() + ChatColor.YELLOW);
			if (me.naptie.bukkit.player.utils.ConfigManager.getData(originalMessage.startsWith("player") ? player.getPlayer() : killer.getPlayer()).getString("gender").equals("FEMALE")) {
				if (originalMessage.contains(" he ") || originalMessage.contains(" he's ") || originalMessage.contains(" his ")) {
					message = message.replaceAll(" he ", " she ");
					message = message.replaceAll(" he's ", " she's ");
					message = message.replaceAll(" his ", " her ");
				}
				if (originalMessage.contains("他")) {
					message = message.replaceAll("他", "她");
				}
			}
			gamePlayer.sendMessage(message);
		}
	}

	public void sendDeathMessage(int num, GamePlayer player) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String language = PlayerJoin.pl.get(gamePlayer.getPlayer());
			String originalMessage = ChatColor.YELLOW + Main.getInstance().getConfig().getStringList(PlayerDeath.deathMessageMap.get(language)).get(num);
			String message = originalMessage.replace("player", player.getName() + ChatColor.YELLOW);
			if (me.naptie.bukkit.player.utils.ConfigManager.getData(player.getPlayer()).getString("gender").equals("FEMALE")) {
				if (originalMessage.contains(" he ") || originalMessage.contains(" he's ") || originalMessage.contains(" his ")) {
					message = message.replaceAll(" he ", " she ");
					message = message.replaceAll(" he's ", " she's ");
					message = message.replaceAll(" his ", " her ");
				}
				if (originalMessage.contains("他")) {
					message = message.replaceAll("他", "她");
				}
			}
			gamePlayer.sendMessage(message);
		}
	}

	public String getDeathMessage(GamePlayer receiver, int num, GamePlayer player, GamePlayer killer) {
		String language = PlayerJoin.pl.get(receiver.getPlayer());
		String originalMessage = ChatColor.YELLOW + Main.getInstance().getConfig().getStringList(PlayerDeath.deathMessageMap.get(language)).get(num);
		String message = originalMessage.replace("player", player.getName() + ChatColor.YELLOW).replace("killer", killer.getName() + ChatColor.YELLOW);
		if (me.naptie.bukkit.player.utils.ConfigManager.getData(originalMessage.startsWith("player") ? player.getPlayer() : killer.getPlayer()).getString("gender").equals("FEMALE")) {
			if (originalMessage.contains(" he ") || originalMessage.contains(" he's ") || originalMessage.contains(" his ")) {
				message = message.replaceAll(" he ", " she ");
				message = message.replaceAll(" he's ", " she's ");
				message = message.replaceAll(" his ", " her ");
			}
			if (originalMessage.contains("他")) {
				message = message.replaceAll("他", "她");
			}
		}
		return message;
	}

	public String getDeathMessage(GamePlayer receiver, int num, GamePlayer player) {
		String language = PlayerJoin.pl.get(receiver.getPlayer());
		String originalMessage = ChatColor.YELLOW + Main.getInstance().getConfig().getStringList(PlayerDeath.deathMessageMap.get(language)).get(num);
		String message = originalMessage.replace("player", player.getName() + ChatColor.YELLOW);
		if (me.naptie.bukkit.player.utils.ConfigManager.getData(player.getPlayer()).getString("gender").equals("FEMALE")) {
			if (originalMessage.contains(" he ") || originalMessage.contains(" he's ") || originalMessage.contains(" his ")) {
				message = message.replaceAll(" he ", " she ");
				message = message.replaceAll(" he's ", " she's ");
				message = message.replaceAll(" his ", " her ");
			}
			if (originalMessage.contains("他")) {
				message = message.replaceAll("他", "她");
			}
		}
		return message;
	}

	private void sendGlobalMessage(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			gamePlayer.sendMessage(message);
		}
	}

	private void sendGlobalMessage(List<String> messageList) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			gamePlayer.sendMessage(messageList);
		}
	}

	private void sendGlobalMessageOfKillers(Map<Integer, Map<String, String>> killerListMap) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			for (int i : killerListMap.keySet()) {
				for (String language : killerListMap.get(i).keySet()) {
					if (PlayerJoin.pl.get(player).equals(language))
						gamePlayer.sendMessage(killerListMap.get(i).get(language));
				}
			}
		}
	}

	private void sendGlobalMessage(Map<String, String> messageMap) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = new GamePlayer(player);
			gamePlayer.sendMessage(messageMap.get(PlayerJoin.pl.get(gamePlayer.getPlayer())));
		}
	}

	public void sendMessageToSpectators(String message) {
		for (GamePlayer spectator : getSpectators()) {
			spectator.sendMessage(message);
		}
	}

	public void sendMessageTo(GamePlayer gamePlayer, String message) {
		gamePlayer.sendMessage(message);
	}

	public void sendTitle(String sTt, String ttI, String sStt, String sttI, int ttFIT, int ttST, int ttFOT) {
		for (GamePlayer gamePlayer : getPlayers()) {
			gamePlayer.sendTitle(sTt, ttI, sStt, sttI, ttFIT, ttST, ttFOT);
		}
	}

	public void sendTitleWithKey(String sTtKey, String ttI, String sSttKey, String sttI, int ttFIT, int ttST, int ttFOT) {
		for (GamePlayer gamePlayer : getPlayers()) {
			gamePlayer.sendTitle(Messages.getMessage(gamePlayer.getPlayer(), sTtKey), ttI, Messages.getMessage(gamePlayer.getPlayer(), sSttKey), sttI, ttFIT, ttST, ttFOT);
		}
	}


	public void sendTitleTo(GamePlayer gamePlayer, String sTt, String ttI, String sStt, String sttI, int ttFIT,
	                        int ttST, int ttFOT) {
		gamePlayer.sendTitle(sTt, ttI, sStt, sttI, ttFIT, ttST, ttFOT);
	}

	public void sendActionBar(String key, String[] targets, String[] replacements) {
		for (GamePlayer gamePlayer : getPlayers()) {
			String message = Messages.getMessage(gamePlayer.getPlayer(), key);
			for (int i = 0; i < targets.length; i++) {
				if (message.contains(targets[i]))
					message = message.replace(targets[i], replacements[i]);
			}
			gamePlayer.sendActionBar(message);
		}
	}

	public void sendActionBar(String sTt) {
		for (GamePlayer gamePlayer : getPlayers()) {
			gamePlayer.sendActionBar(sTt);
		}
	}

	public void playSound(Sound sound, int volume, int pitch) {
		for (GamePlayer gamePlayer : getPlayers())
			gamePlayer.playSound(sound, volume, pitch);
	}

	public Set<Chest> getOpened() {
		return opened;
	}

	public List<ItemStack> getRareItems() {
		return rareItems;
	}

	public List<ItemStack> getNormalItems() {
		return normalItems;
	}

	public boolean isMovementFrozen() {
		return movementFrozen;
	}

	public void setMovementFrozen(boolean movementFrozen) {
		this.movementFrozen = movementFrozen;
	}

	public World getWorld() {
		return world;
	}

	private void sendEndingMessages(GamePlayer winner) {
		Map<String, String> textMap1 = Messages.getMessagesInDifLangs("SKYWARS");
		Map<String, String> skywarsTitleMap = new HashMap<>();
		for (String language : textMap1.keySet()) {
			skywarsTitleMap.put(language, center(textMap1.get(language), 70));
		}
		Map<String, String> winnerMap = new HashMap<>();
		if (winner != null) {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("WINNER");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language) + " &7-&r " + winner.getName()), 70));
			}
		} else {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("NOBODY_WON");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language)), 70));
			}
		}

		/*
		List<String> killerList = new ArrayList<>();
		List<Integer> list = new ArrayList<>(Run.killCount.values());
		Collections.sort(list, Collections.reverseOrder());
		List<Integer> top;
		try {
			top = list.subList(0, 3);
		} catch (IndexOutOfBoundsException e) {
			top = list.subList(0, list.size());
		}
		int pos = 1;
		UUID uuid = null;
		Set<UUID> alreadyDisplayed = new HashSet<>();
		for (int kills : top) {
			for (UUID uuid1 : Run.killCount.keySet()) {
				if (alreadyDisplayed.contains(uuid1)) {
					System.out.println("alreadyDisplayed contains " + getGamePlayer(Bukkit.getPlayer(uuid1)).getName());
					continue;
				} else {
					System.out.println("alreadyDisplayed doesn't contain " + getGamePlayer(Bukkit.getPlayer(uuid1)).getName());
				}
				if (Run.killCount.get(uuid1) == kills) {
					uuid = uuid1;
					alreadyDisplayed.add(uuid1);
				}
			}
			String posStr;
			if (pos == 1) {
				posStr = "&e1st Killer";
			} else if (pos == 2) {
				posStr = "&62nd Killer";
			} else if (pos == 3) {
				posStr = "&c3rd Killer";
			} else {
				posStr = "&7" + pos + "th Killer";
			}
			killerList.add(center(CU.t(posStr + " &7- " + getGamePlayer(Bukkit.getPlayer(uuid)).getName() + " &7- &6" + getGamePlayer(Bukkit.getPlayer(uuid)).getKills()), 70));
			pos++;
		}
		*/

		Map<Integer, Map<String, String>> killerListMap = new HashMap<>();
		int i = 0;
		for (Iterator<Map<UUID, Integer>> it = MapUtil.sortKills(Run.killCount).iterator(); it.hasNext() && i < 3; ++i) {
			Map<UUID, Integer> each = it.next();
			Map<String, String> textMap2 = new HashMap<>();
			if (i == 0) {
				textMap2 = Messages.getMessagesInDifLangs("1ST_KILLER");
			} else if (i == 1) {
				textMap2 = Messages.getMessagesInDifLangs("2ND_KILLER");
			} else if (i == 2) {
				textMap2 = Messages.getMessagesInDifLangs("3RD_KILLER");
			}
			Map<String, String> killerMap = new HashMap<>();
			for (UUID uuid : each.keySet()) {
				GamePlayer killer = getGamePlayer(Bukkit.getPlayer(uuid));
				for (String language : textMap2.keySet()) {
					killerMap.put(language, center(CU.t(textMap2.get(language) + " &7- " + killer.getName() + " &7- &6" + killer.getKills()), 70));
				}
			}
			killerListMap.put(i, killerMap);

		}

		sendGlobalMessage("GAME_ENDED", new String[]{}, new String[]{});
		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);
		sendGlobalMessage("");
		sendGlobalMessage(skywarsTitleMap);
		sendGlobalMessage("");
		sendGlobalMessage(winnerMap);
		sendGlobalMessage("");
		sendGlobalMessageOfKillers(killerListMap);
		sendGlobalMessage("");
		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);

		sendActionBar("GAME_ENDED", new String[]{}, new String[]{});

		for (GamePlayer gamePlayer : getPlayers()) {
			if (gamePlayer == winner) {
				if (winner != null) {
					winner.sendTitle(Messages.getMessage(winner.getPlayer(), "VICTORY_TITLE"), "\"color\":\"gold\",\"bold\":true",
							Messages.getMessage(winner.getPlayer(), "VICTORY_SUBTITLE"), "\"color\":\"gray\"", 0,
							100, 20);
				}
			} else {
				gamePlayer.sendTitle(Messages.getMessage(gamePlayer.getPlayer(), "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(gamePlayer.getPlayer(), "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100,
						20);
			}
		}

		for (GamePlayer spectator : spectators) {
			spectator.sendTitle(Messages.getMessage(spectator.getPlayer(), "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(spectator.getPlayer(), "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
		}

	}

	private void sendEndingMessages(GameTeam team) {
		Map<String, String> textMap1 = Messages.getMessagesInDifLangs("SKYWARS");
		Map<String, String> skywarsTitleMap = new HashMap<>();
		for (String language : textMap1.keySet()) {
			skywarsTitleMap.put(language, center(textMap1.get(language), 70));
		}
		Map<String, String> winnerMap = new HashMap<>();
		if (team != null) {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("WINNER");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language) + (language.contains("en") ? (team.getMembers().size() > 1 ? "s" : "") : "") + " &7-&r " + team.getName()), 70));
			}
		} else {
			Map<String, String> textMap2 = Messages.getMessagesInDifLangs("NOBODY_WON");
			for (String language : textMap2.keySet()) {
				winnerMap.put(language, center(CU.t("&e" + textMap2.get(language)), 70));
			}
		}

		/*
		List<String> killerList = new ArrayList<>();
		List<Integer> list = new ArrayList<>(Run.killCount.values());
		Collections.sort(list, Collections.reverseOrder());
		List<Integer> top;
		try {
			top = list.subList(0, 3);
		} catch (IndexOutOfBoundsException e) {
			top = list.subList(0, list.size());
		}
		int pos = 1;
		UUID uuid = null;
		Set<UUID> alreadyDisplayed = new HashSet<>();
		for (int kills : top) {
			for (UUID uuid1 : Run.killCount.keySet()) {
				if (alreadyDisplayed.contains(uuid1)) {
					System.out.println("alreadyDisplayed contains " + getGamePlayer(Bukkit.getPlayer(uuid1)).getName());
					continue;
				} else {
					System.out.println("alreadyDisplayed doesn't contain " + getGamePlayer(Bukkit.getPlayer(uuid1)).getName());
				}
				if (Run.killCount.get(uuid1) == kills) {
					uuid = uuid1;
					alreadyDisplayed.add(uuid1);
				}
			}
			String posStr;
			if (pos == 1) {
				posStr = "&e1st Killer";
			} else if (pos == 2) {
				posStr = "&62nd Killer";
			} else if (pos == 3) {
				posStr = "&c3rd Killer";
			} else {
				posStr = "&7" + pos + "th Killer";
			}
			killerList.add(center(CU.t(posStr + " &7- " + getGamePlayer(Bukkit.getPlayer(uuid)).getName() + " &7- &6" + getGamePlayer(Bukkit.getPlayer(uuid)).getKills()), 70));
			pos++;
		}
		*/

		Map<Integer, Map<String, String>> killerListMap = new HashMap<>();
		int i = 0;
		for (Iterator<Map<UUID, Integer>> it = MapUtil.sortKills(Run.killCount).iterator(); it.hasNext() && i < 3; ++i) {
			Map<UUID, Integer> each = it.next();
			Map<String, String> textMap2 = new HashMap<>();
			if (i == 0) {
				textMap2 = Messages.getMessagesInDifLangs("1ST_KILLER");
			} else if (i == 1) {
				textMap2 = Messages.getMessagesInDifLangs("2ND_KILLER");
			} else if (i == 2) {
				textMap2 = Messages.getMessagesInDifLangs("3RD_KILLER");
			}
			Map<String, String> killerMap = new HashMap<>();
			for (UUID uuid : each.keySet()) {
				GamePlayer killer = getGamePlayer(Bukkit.getPlayer(uuid));
				for (String language : textMap2.keySet()) {
					killerMap.put(language, center(CU.t(textMap2.get(language) + " &7- " + killer.getName() + " &7- &6" + killer.getKills()), 70));
				}
			}
			killerListMap.put(i, killerMap);

		}

		sendGlobalMessage("GAME_ENDED", new String[]{}, new String[]{});
		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);
		sendGlobalMessage("");
		sendGlobalMessage(skywarsTitleMap);
		sendGlobalMessage("");
		sendGlobalMessage(winnerMap);
		sendGlobalMessage("");
		sendGlobalMessageOfKillers(killerListMap);
		sendGlobalMessage("");
		sendGlobalMessage(Messages.ENDING_MESSAGE_HYPHEN);

		sendActionBar("GAME_ENDED", new String[]{}, new String[]{});

		if (team != null) {
			for (GamePlayer winner : team.getMembers()) {
				winner.sendTitle(Messages.getMessage(winner.getPlayer(), "VICTORY_TITLE"), "\"color\":\"gold\",\"bold\":true", PlayerJoin.pl.get(winner.getPlayer()).contains("en") ? Messages.getMessage(winner.getPlayer(), "VICTORY_SUBTITLE").replace(" man ", " men ") : Messages.getMessage(winner.getPlayer(), "VICTORY_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
			}
			for (GamePlayer spectator : spectators) {
				if (team.getName().contains(spectator.getPlayer().getName())) {
					spectator.sendTitle(Messages.getMessage(spectator.getPlayer(), "VICTORY_TITLE"), "\"color\":\"gold\",\"bold\":true", PlayerJoin.pl.get(spectator.getPlayer()).contains("en") ? Messages.getMessage(spectator.getPlayer(), "VICTORY_SUBTITLE").replace(" man ", " men ") : Messages.getMessage(spectator.getPlayer(), "VICTORY_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				} else {
					spectator.sendTitle(Messages.getMessage(spectator.getPlayer(), "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(spectator.getPlayer(), "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				}
			}
		}

	}

	public void judge(Object winner, boolean force) {
		if (getTeamMode()) {
			if (getTeams().size() == 1) {
				sendEndingMessages((GameTeam) winner);
				setState(Game.GameState.ENDING);
				new End(this).runTaskTimer(Main.getInstance(), 0, 20);
			} else if (force) {
				sendEndingMessages((GameTeam) winner);
				getTeams().clear();
				getTeams().add((GameTeam) winner);
				setState(Game.GameState.ENDING);
				new End(this).runTaskTimer(Main.getInstance(), 0, 20);
			}
		} else {
			if (getPlayers().size() == 1) {
				sendEndingMessages((GamePlayer) winner);
				setState(Game.GameState.ENDING);
				new End(this).runTaskTimer(Main.getInstance(), 0, 20);
			} else if (force) {
				sendEndingMessages((GamePlayer) winner);
				getPlayers().clear();
				reward((GamePlayer) winner, false);
				getPlayers().add((GamePlayer) winner);
				setState(Game.GameState.ENDING);
				new End(this).runTaskTimer(Main.getInstance(), 0, 20);
			}
		}
	}

	public void judge() {
		sendEndingMessages((GamePlayer) null);
		getPlayers().clear();
		setState(Game.GameState.ENDING);
		new End(this).runTaskTimer(Main.getInstance(), 0, 20);
	}

	private void giveInteractiveItems(Player player) {
		ConfigManager.getItem(player, player.getInventory(), "leave");
	}

	private void setCage(Location sp, Material material) {
		if (this.teamMode) {
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ() + 1).getBlock().setType(material);

			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY(), sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY(), sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ() + 2).getBlock().setType(material);

			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ() + 2).getBlock().setType(material);

			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ() - 2).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ() + 2).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ() + 2).getBlock().setType(material);

			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ() + 1).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ() + 1).getBlock().setType(material);
		} else {
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ()).getBlock().setType(material);

			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY(), sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY(), sp.getZ() + 1).getBlock().setType(material);

			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() + 1).getBlock().setType(material);

			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ()).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() - 1).getBlock().setType(material);
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() + 1).getBlock().setType(material);

			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ()).getBlock().setType(material);
		}
	}

	private void breakCage(Location sp) {
		if (this.teamMode) {
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() - 1, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() - 1, sp.getZ() + 1).getBlock().breakNaturally();

			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY(), sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY(), sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY(), sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY(), sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ() + 2).getBlock().breakNaturally();

			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY() + 1, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 1, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ() + 2).getBlock().breakNaturally();

			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 2, sp.getY() + 2, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 2, sp.getY() + 2, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ() - 2).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ() + 2).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ() + 2).getBlock().breakNaturally();

			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX() - 1, sp.getY() + 3, sp.getZ() + 1).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 3, sp.getZ() + 1).getBlock().breakNaturally();
		} else {
			new Location(world, sp.getX(), sp.getY() - 1, sp.getZ()).getBlock().breakNaturally();

			new Location(world, sp.getX() - 1, sp.getY(), sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY(), sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY(), sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY(), sp.getZ() + 1).getBlock().breakNaturally();

			new Location(world, sp.getX() - 1, sp.getY() + 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 1, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 1, sp.getZ() + 1).getBlock().breakNaturally();

			new Location(world, sp.getX() - 1, sp.getY() + 2, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX() + 1, sp.getY() + 2, sp.getZ()).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() - 1).getBlock().breakNaturally();
			new Location(world, sp.getX(), sp.getY() + 2, sp.getZ() + 1).getBlock().breakNaturally();

			new Location(world, sp.getX(), sp.getY() + 3, sp.getZ()).getBlock().breakNaturally();
		}
	}

	public void openCages() {
		for (Location spawnpoint : this.spawnpoints) {
			breakCage(spawnpoint);
		}
	}

	public void resetWorld(World world) {
		world.setDifficulty(Difficulty.NORMAL);
		world.setPVP(true);
		world.setTime(6000);
		world.setStorm(false);
		world.setThundering(false);
		world.setSpawnFlags(false, false);
		world.setAutoSave(false);
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
	}

	public void reward(GamePlayer player, boolean won) {
		YamlConfiguration data = me.naptie.bukkit.player.utils.ConfigManager.getData(player.getPlayer());
		if (won) {
			data.set("skywars.wins", data.getInt("skywars.wins") + 1);
			data.set("point", data.getInt("point") + 3);
			player.sendMessage(ChatColor.GREEN + "+3" + (me.naptie.bukkit.player.utils.ConfigManager.getLanguageName(player.getPlayer()).contains("en") ? Messages.getMessage(player.getPlayer(), "ADD_POINTS").replace("%s%", "") : Messages.getMessage(player.getPlayer(), "ADD_POINTS")));
		} else
			data.set("skywars.deaths", data.getInt("skywars.deaths") + 1);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isTeamMate(GamePlayer player1, GamePlayer player2) {
		if (teamMode)
			return player1.getTeam() == player2.getTeam() || player1.getTeam().getMembers().contains(player2) || player2.getTeam().getMembers().contains(player1);
		else return false;
	}

	public List<GameTeam> getTeams() {
		return teams;
	}

	public Location getLobby() {
		return lobby;
	}

	public String getWorldName() {
		return this.world.getName().replace("_active", "");
	}

	int getPlayersPerTeam() {
		return playersPerTeam;
	}

	public List<Location> getNormalChests() {
		return normalChests;
	}

	public List<Location> getRareChests() {
		return rareChests;
	}

	public boolean isInsaneMode() {
		return insaneMode;
	}

	private String center(String input, int size) {
		int count = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == '\u00a7')
				count++;
		}
		String centralized = StringUtils.center(input, size + count * 2);
		return centralized.substring(0, centralized.length() - centralized.split(input)[1].length());
	}

	public int getDeathmatchTime() {
		return deathmatch;
	}

	public int getEndTime() {
		return end;
	}

	public enum GameState {
		LOBBY, STARTING, PREPARING, PROTECTED, GAMING, DEATHMATCH, ENDING
	}

}
