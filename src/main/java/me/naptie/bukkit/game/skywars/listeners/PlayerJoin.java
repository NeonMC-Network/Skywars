package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.data.DataHandler;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.Game.GameState;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import me.naptie.bukkit.game.skywars.utils.CU;
import me.naptie.bukkit.player.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJoin implements Listener {

	public static Map<Player, Objective> objMap = new HashMap<>();
	public static Map<Player, String> pl = new HashMap<>();
	private Map<UUID, Integer> lastPlayerAmount = new HashMap<>();

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		event.setJoinMessage(null);
		pl.put(player, ConfigManager.getLanguageName(player));
		player.setGameMode(GameMode.ADVENTURE);
		player.getInventory().clear();
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		player.setAllowFlight(false);
		Game game = Main.getInstance().getGame(DataHandler.i.getString("display-name"));
		assert game != null;
		if (Bukkit.getOnlinePlayers().size() <= 1) {
			game.resetWorld(game.getWorld());
		}
		game.join(new GamePlayer(player));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (game.isState(GameState.LOBBY)) {
					ScoreboardManager manager = Bukkit.getScoreboardManager();
					Scoreboard board = manager.getNewScoreboard();
					Objective obj = board.registerNewObjective(online.getName(), "dummy");
					if (!objMap.containsKey(online)) {
						lastPlayerAmount.put(online.getUniqueId(), 0);
						objMap.put(online, obj);
					}
					obj.setDisplaySlot(DisplaySlot.SIDEBAR);
					obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + Messages.getMessage(online, "SKYWARS").toUpperCase());
					player.setScoreboard(board);
					obj.getScore(CU.t("&a")).setScore(9);
					obj.getScore(CU.t(Messages.getMessage(online, "STATE") + ": &a" + Messages.getMessage(online, game.getGameState().name()))).setScore(8);
					obj.getScore(CU.t("&b")).setScore(7);
					Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
						if (lastPlayerAmount.get(online.getUniqueId()) != Bukkit.getOnlinePlayers().size()) {
							obj.getScoreboard().resetScores(CU.t(Messages.getMessage(online, "PLAYERS") + ": &a" + lastPlayerAmount.get(online.getUniqueId()) + "&r/" + game.getMaxPlayers()));
						}
						obj.getScore(CU.t(Messages.getMessage(online, "PLAYERS") + ": &a" + Bukkit.getOnlinePlayers().size() + "&r/" + game.getMaxPlayers())).setScore(6);
						lastPlayerAmount.put(online.getUniqueId(), Bukkit.getOnlinePlayers().size());
					}, 0, 20);
					obj.getScore(CU.t("&c")).setScore(5);
					obj.getScore(CU.t(Messages.getMessage(online, "SERVER") + ": &a" + me.naptie.bukkit.core.Main.getInstance().getServerName())).setScore(4);
					obj.getScore(CU.t(Messages.getMessage(online, "MAP") + ": &a" + game.getWorldName())).setScore(3);
					obj.getScore(CU.t(Messages.getMessage(online, "MODE") + ": "
							+ (game.isInsaneMode() ? CU.t("&c" + Messages.getMessage(pl.get(online), "INSANE")) : CU.t("&a" + Messages.getMessage(pl.get(online), "NORMAL")))))
							.setScore(2);
					obj.getScore(CU.t("&4")).setScore(1);
					obj.getScore(Messages.getMessage(online, "SCOREBOARD_FOOTER")).setScore(0);
				}
			}
		}, 0, 20);

	}

}