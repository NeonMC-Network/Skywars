package me.naptie.bukkit.game.skywars.tasks;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.listeners.PlayerJoin;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import me.naptie.bukkit.game.skywars.utils.CU;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class Run extends BukkitRunnable {

	public static Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
	public static HashMap<UUID, Objective> playerObjectiveMap = new HashMap<>();
	public static Map<UUID, Integer> killCount = new HashMap<>();
	private static Map<UUID, Boolean> died = new HashMap<>();
	private static int startIn = 12;
	private static int time = 0;
	private Game game;
	private Map<UUID, Boolean> started = new HashMap<>();
	private String type;
	private String map;
	private String mode;
	private Map<UUID, String> lastDate = new HashMap<>();
	private Map<UUID, Game.GameState> lastState = new HashMap<>();
	private Map<UUID, String> lastStateString = new HashMap<>();
	private Map<UUID, Integer> lastOpeningIn = new HashMap<>();
	private Map<UUID, String> lastTime = new HashMap<>();
	private Map<UUID, Integer> lastPlayerAmount = new HashMap<>();
	private Map<UUID, Integer> lastTeamAmount = new HashMap<>();
	private Map<UUID, Integer> lastSpectatorAmount = new HashMap<>();
	private Map<UUID, Integer> lastKillCount = new HashMap<>();

	public Run(Game game, boolean skip) {
		this.game = game;
		this.game.setState(Game.GameState.PREPARING);
		this.game.sendMessage("CAGES_OPENING", new String[]{}, new String[]{});
		this.game.assignSpawnPositions();
		this.game.setMovementFrozen(true);
		this.game.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

		boolean teamMode = game.getTeamMode();
		this.type = teamMode ? "TEAM" : "SOLO";
		this.map = game.getWorldName();
		this.mode = game.isInsaneMode() ? "INSANE" : "NORMAL";

		for (GamePlayer player : game.getPlayers()) {
			killCount.put(player.getPlayer().getUniqueId(), 0);
		}

		/*for (GamePlayer gamePlayer : this.game.getPlayers()) {
			Objective objective = board.registerNewObjective("Teams" + gamePlayer.getPlayer().getName(), "dummy");
			objective.setDisplayName("Teams");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
			Team self = board.registerNewTeam("self" + gamePlayer.getPlayer().getName());
			self.setPrefix(CU.t("&a"));
			self.setSuffix(CU.t("&r"));
			self.setNameTagVisibility(NameTagVisibility.ALWAYS);
			for (GamePlayer gamePlayer1 : gamePlayer.getTeam().getMembers()) {
				self.addEntry(gamePlayer1.getPlayer().getName());
			}
			Team enemy = board.registerNewTeam("enemy" + gamePlayer.getPlayer().getName());
			enemy.setPrefix(CU.t("&c"));
			enemy.setSuffix(CU.t("&r"));
			enemy.setNameTagVisibility(NameTagVisibility.ALWAYS);
			for (GamePlayer gamePlayer1 : this.game.getPlayers()) {
				if (gamePlayer1.getTeam().getMembers().contains(gamePlayer)) {
					continue;
				}
				enemy.addEntry(gamePlayer1.getPlayer().getName());
			}
		}*/

		//Objective objective = board.registerNewObjective("Health", "health");
		//objective.setDisplayName(CU.t("&c❤"));
		//objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

		if (skip) {
			startIn = 2;
		}
	}

	public static void forcestart() {
		startIn = 2;
	}

	@Override
	public void run() {

		if (game.isState(Game.GameState.ENDING)) {
			cancel();
		}

		if (startIn > 2) {
			String stringStartIn = String.valueOf(startIn - 2);
			for (GamePlayer player : game.getPlayers())
				player.sendTitle(stringStartIn, "\"color\":\"red\"", Messages.getMessage(player.getPlayer(), "PREPARE"), "\"color\":\"yellow\"", 0, 40, 10);

			game.sendActionBar("CAGES_OPEN_IN", new String[]{"%time%", "%s%"}, new String[]{stringStartIn, startIn - 2 == 1 ? "" : "s"});
			game.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
		}

		if (startIn == 2) {
			game.setState(Game.GameState.PROTECTED);
			game.sendMessage("CAGES_OPENED", new String[]{}, new String[]{});
			for (GamePlayer player : game.getPlayers())
				player.sendTitle(Messages.getMessage(player.getPlayer(), "SKYWARS"), "\"color\":\"green\",\"bold\":true", Messages.getMessage(player.getPlayer(), "STARTED"), "\"color\":\"white\"", 10, 80, 10);
			game.sendActionBar("GAME_STARTED", new String[]{}, new String[]{});
			game.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
			game.playSound(Sound.BLOCK_PORTAL_TRIGGER, 1, 1);
			game.openCages();
			game.setMovementFrozen(false);

		}

		if (startIn == 0) {
			if (!game.isState(Game.GameState.GAMING) || !game.isState(Game.GameState.DEATHMATCH)
					|| !game.isState(Game.GameState.ENDING)) {
				game.setState(Game.GameState.GAMING);
				startIn = -1;
			}
		}

		if (startIn > 0) {
			startIn -= 1;
		}

		int alive = game.getPlayers().size();
		int teams = game.getTeams().size();
		int spectators = game.getSpectators().size();

		String state;
		if (game.isState(Game.GameState.PROTECTED) || game.isState(Game.GameState.GAMING)) {
			state = "IN_GAME";
		} else if (game.isState(Game.GameState.DEATHMATCH)) {
			state = "DEATHMATCH";
		} else if (game.isState(Game.GameState.ENDING)) {
			state = "ENDING";
		} else {
			state = game.getGameState().name();
		}

		for (Player player : Bukkit.getOnlinePlayers()) {

			UUID uuid = player.getUniqueId();
			if (!playerObjectiveMap.containsKey(uuid)) {
				ScoreboardManager manager = Bukkit.getScoreboardManager();
				board = manager.getNewScoreboard();
				Objective obj = board.registerNewObjective("SkyWars", "dummy");
				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
				obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Messages.getMessage(player, "SKYWARS").toUpperCase());
				this.started.put(uuid, false);
				this.lastDate.put(uuid, getCurrentFormattedDate(Main.getInstance().getConfig().getString(PlayerJoin.pl.get(player) + ".time-format")));
				this.lastState.put(uuid, Game.GameState.PREPARING);
				this.lastStateString.put(uuid, Messages.getMessage(player, Game.GameState.PREPARING.name()));
				this.lastOpeningIn.put(uuid, 10);
				this.lastTime.put(uuid, getFormattedTime(0));
				this.lastPlayerAmount.put(uuid, 0);
				this.lastTeamAmount.put(uuid, 0);
				this.lastSpectatorAmount.put(uuid, 0);
				this.lastKillCount.put(uuid, 0);
				died.put(uuid, false);
				player.setScoreboard(board);
				playerObjectiveMap.put(uuid, obj);
				Objective objective = board.registerNewObjective("Health", "health");
				objective.setDisplayName(CU.t("&c\u2764"));
				objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
				for (Player player1 : Bukkit.getOnlinePlayers())
					objective.getScore(player1.getName()).setScore((int) player1.getHealth());
			}

			if (lastState.get(uuid) == Game.GameState.PROTECTED || lastState.get(uuid) == Game.GameState.GAMING) {
				lastStateString.put(uuid, Messages.getMessage(player, "IN_GAME"));
			} else if (lastState.get(uuid) == Game.GameState.DEATHMATCH) {
				lastStateString.put(uuid, Messages.getMessage(player, "DEATHMATCH"));
			} else if (lastState.get(uuid) == Game.GameState.ENDING) {
				lastStateString.put(uuid, Messages.getMessage(player, "ENDING"));
			} else {
				lastStateString.put(uuid, Messages.getMessage(player, lastState.get(uuid).name()));
			}

			/*
			 * if (game.getPlayers().contains(game.getGamePlayer(player))) {
			 * this.died.put(uuid, false); } else if
			 * (game.isPlayerSpectating(game.getGamePlayer(player)) &&
			 * game.getSpectators().contains(game.getGamePlayer(player))) {
			 * this.died.put(uuid, true); }
			 */

			Objective obj = playerObjectiveMap.get(uuid);

			if (!Objects.equals(lastDate.get(uuid), getCurrentFormattedDate(Main.getInstance().getConfig().getString(PlayerJoin.pl.get(player) + ".time-format")))) {
				obj.getScoreboard().resetScores(lastDate.get(uuid));
			}
			obj.getScore(CU.t("&7" + Messages.getMessage(player, type) + " " + getCurrentFormattedDate(Main.getInstance().getConfig().getString(PlayerJoin.pl.get(player) + ".time-format")))).setScore(14);
			lastDate.put(uuid, getCurrentFormattedDate(Main.getInstance().getConfig().getString(PlayerJoin.pl.get(player) + ".time-format")));

			obj.getScore(CU.t("&a")).setScore(13);

			if (lastState.get(uuid) != game.getGameState()) {
				obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "STATE") + ": &a" + lastStateString.get(uuid)));
				Main.getInstance().logger.info(CU.t(Messages.getMessage(player, "STATE") + ": &a" + lastStateString.get(uuid)));
			}
			obj.getScore(CU.t(Messages.getMessage(player, "STATE") + ": &a" + Messages.getMessage(player, state))).setScore(12);
			lastState.put(uuid, game.getGameState());

			if (game.isState(Game.GameState.PREPARING)) {
				if (lastOpeningIn.get(uuid) != startIn - 1) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "OPENING_IN").replace("%time%", "" + lastOpeningIn.get(uuid))));
				}
				obj.getScore(CU.t(Messages.getMessage(player, "OPENING_IN").replace("%time%", "" + (startIn - 1)))).setScore(11);
				lastOpeningIn.put(uuid, startIn - 1);
			} else {
				if (!started.get(uuid)) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "OPENING_IN").replace("%time%", "" + lastOpeningIn.get(uuid))));
					started.put(uuid, true);
				}
				if (!Objects.equals(lastTime.get(uuid), getFormattedTime(time))) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "TIME") + ": &a" + lastTime.get(uuid)));
				}
				obj.getScore(CU.t(Messages.getMessage(player, "TIME") + ": &a" + getFormattedTime(time))).setScore(11);
				lastTime.put(uuid, getFormattedTime(time));
			}

			obj.getScore(CU.t("&b")).setScore(10);

			if (lastPlayerAmount.get(uuid) != alive) {
				obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "PLAYERS") + ": &a" + lastPlayerAmount.get(uuid)));
			}
			obj.getScore(CU.t(Messages.getMessage(player, "PLAYERS") + ": &a" + alive)).setScore(9);
			lastPlayerAmount.put(uuid, alive);

			if (game.getTeamMode()) {
				if (lastTeamAmount.get(uuid) != teams) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "TEAMS") + ": &a" + lastTeamAmount.get(uuid)));
				}
				obj.getScore(CU.t(Messages.getMessage(player, "TEAMS") + ": &a" + teams)).setScore(8);
				lastTeamAmount.put(uuid, teams);
			} else {
				obj.getScore(CU.t("&d")).setScore(8);
			}

			if (lastSpectatorAmount.get(uuid) != spectators) {
				obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "SPECTATORS") + ": &c" + lastSpectatorAmount.get(uuid)));
			}
			obj.getScore(CU.t(Messages.getMessage(player, "SPECTATORS") + ": &c" + spectators)).setScore(7);
			lastSpectatorAmount.put(uuid, spectators);

			obj.getScore(CU.t("&c")).setScore(6);

			if (!game.isPlayerSpectating(game.getGamePlayer(player))) {
				if (killCount.containsKey(uuid)) {
					if (!Objects.equals(lastKillCount.get(uuid), killCount.get(uuid))) {
						obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "KILLS") + ": &a" + lastKillCount.get(uuid)));
					}
					obj.getScore(CU.t(Messages.getMessage(player, "KILLS") + ": &a" + killCount.get(uuid))).setScore(5);
					lastKillCount.put(uuid, killCount.get(uuid));
				} else {
					if (!Objects.equals(lastKillCount.get(uuid), 0)) {
						obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "KILLS") + ": &a" + lastKillCount.get(uuid)));
					}
					obj.getScore(CU.t(Messages.getMessage(player, "KILLS") + ": &a" + 0)).setScore(5);
				}
			} else {
				if (!died.get(uuid)) {
					obj.getScoreboard().resetScores(CU.t(Messages.getMessage(player, "KILLS") + ": &a" + lastKillCount.get(uuid)));
					died.put(uuid, true);
				}
				obj.getScore(CU.t(Messages.getMessage(player, "SERVER") + ": &a" + me.naptie.bukkit.core.Main.getInstance().getServerName())).setScore(5);
			}

			obj.getScore(CU.t("&0")).setScore(4);

			obj.getScore(CU.t(Messages.getMessage(player, "MAP") + ": &a" + map)).setScore(3);

			obj.getScore(CU.t(Messages.getMessage(player, "MODE") + ": " + Messages.getMessage(player, mode))).setScore(2);

			obj.getScore(CU.t("&1")).setScore(1);

			obj.getScore(Messages.getMessage(player, "SCOREBOARD_FOOTER")).setScore(0);
		}

		if (startIn < 2) {
			time++;
		}

		if (time == game.getDeathmatchTime() - 60) {
			game.sendMessage("DEATHMATCH_IN_1MIN", new String[]{}, new String[]{});
		}

		if (time == game.getDeathmatchTime()) {
			game.sendMessage("DEATHMATCH_STARTS", new String[]{}, new String[]{});
			game.setState(Game.GameState.DEATHMATCH);
			game.sendTitleWithKey("DEATHMATCH".replace("&e", ""), "\"color\":\"red\",\"bold\":true","DEATHMATCH_SUBTITLE", "\"color\":\"gray\"", 5, 30, 10);
			game.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
			for (GamePlayer player : game.getPlayers()) {
				player.getPlayer().teleport(game.getLobby());
				player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 100, 1));
			}
		}

		if (time == game.getEndTime() - 60) {
			game.sendMessage("END_IN_1MIN", new String[]{}, new String[]{});
		}

		if (time == game.getEndTime()) {
			game.judge();
		}
	}

	private String getCurrentFormattedDate(String format) {
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat(format);
		return ft.format(date);
	}

	private String getFormattedTime(int time) {
		int minutes = (time % 3600) / 60;
		int seconds = time % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

}