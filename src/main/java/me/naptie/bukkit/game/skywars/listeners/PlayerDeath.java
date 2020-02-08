package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import me.naptie.bukkit.game.skywars.objects.GameTeam;
import me.naptie.bukkit.game.skywars.tasks.Run;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDeath implements Listener {

	public static Map<String, String> deathMessageMap = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent event) {

		event.setDeathMessage(null);
		Player player = event.getEntity();
		Game game = Main.getInstance().getGame();
		if (game != null && game.getGamePlayer(player) != null) {
			GamePlayer gamePlayer = game.getGamePlayer(player);
			if (gamePlayer.getPlayer() == player) {
				handle(event, game);
			}
		}
	}

	private void handle(PlayerDeathEvent event, Game game) {
		Player player = event.getEntity();
		EntityDamageEvent lastDamageCause = player.getLastDamageCause();
		DamageCause dmgCause = lastDamageCause.getCause();

		if (game.isState(Game.GameState.GAMING) || game.isState(Game.GameState.DEATHMATCH)) {

			GamePlayer gamePlayer = game.getGamePlayer(player);
			Player killer = player.getKiller();

			game.getPlayers().remove(gamePlayer);
			game.reward(gamePlayer, false);
			game.activateSpectatorSettings(gamePlayer, false);

			for (int i = 0; i < game.getTeams().size(); i++) {
				GameTeam gameTeam = game.getTeams().get(i);
				boolean shouldRemove = true;
				for (GamePlayer gamePlayer1 : gameTeam.getMembers()) {
					if (game.getPlayers().contains(gamePlayer1)) {
						shouldRemove = false;
					}
				}
				if (shouldRemove)
					game.getTeams().remove(gameTeam);
			}

			if (killer != null && !Objects.equals(killer.getUniqueId(), player.getUniqueId())) {
				GamePlayer gameKiller = game.getGamePlayer(killer);
				gameKiller.addKillCount();
				player.getWorld().strikeLightningEffect(player.getLocation());
				for (String language : Main.getInstance().getConfig().getStringList("languages")) {
					String deathMessages;
					if (PvP.deadByGettingShot.contains(player)) {
						deathMessages = language + ".death-messages.killer-known.shot";
					} else if (dmgCause == DamageCause.VOID) {
						deathMessages = language + ".death-messages.killer-known.void";
					} else if (dmgCause == DamageCause.FIRE) {
						deathMessages = language + ".death-messages.killer-known.fire";
					} else if (dmgCause == DamageCause.FIRE_TICK) {
						deathMessages = language + ".death-messages.killer-known.fire-tick";
					} else if (dmgCause == DamageCause.LAVA) {
						deathMessages = language + ".death-messages.killer-known.lava";
					} else if (dmgCause == DamageCause.BLOCK_EXPLOSION) {
						deathMessages = language + ".death-messages.killer-known.block-explosion";
					} else if (dmgCause == DamageCause.FALL) {
						deathMessages = language + ".death-messages.killer-known.fall";
					} else if (dmgCause == DamageCause.DROWNING) {
						deathMessages = language + ".death-messages.killer-known.drowning";
					} else {
						deathMessages = language + ".death-messages.killer-known.pvp";
					}
					deathMessageMap.put(language, deathMessages);
				}
				int num = ThreadLocalRandom.current().nextInt(0, Main.getInstance().getConfig().getStringList(deathMessageMap.get("zh-CN")).toArray().length);
				game.sendDeathMessage(num, game.getGamePlayer(player), game.getGamePlayer(killer));
				game.sendMessageTo(gamePlayer, game.getDeathMessage(gamePlayer, num, game.getGamePlayer(player), game.getGamePlayer(killer)));
				game.sendMessageTo(gamePlayer, Messages.getMessage(gamePlayer.getPlayer(), "DEATH_MESSAGE"));

				if (Run.killCount.containsKey(killer.getUniqueId())) {
					int kills = Run.killCount.get(killer.getUniqueId());
					Run.killCount.put(killer.getUniqueId(), kills + 1);
					for (UUID uuid : Run.killCount.keySet()) {
						System.out.println(uuid.toString() + " - " + Run.killCount.get(uuid));
					}
				} else {
					Run.killCount.put(killer.getUniqueId(), 1);
				}

			} else {
				for (String language : Main.getInstance().getConfig().getStringList("languages")) {
					String deathMessages;
					if (dmgCause == DamageCause.VOID) {
						deathMessages = language + ".death-messages.killer-unknown.void";
					} else if (dmgCause == DamageCause.FIRE) {
						deathMessages = language + ".death-messages.killer-unknown.fire";
					} else if (dmgCause == DamageCause.FIRE_TICK) {
						deathMessages = language + ".death-messages.killer-unknown.fire-tick";
					} else if (dmgCause == DamageCause.LAVA) {
						deathMessages = language + ".death-messages.killer-unknown.lava";
					} else if (dmgCause == DamageCause.BLOCK_EXPLOSION) {
						deathMessages = language + ".death-messages.killer-unknown.block-explosion";
					} else if (dmgCause == DamageCause.FALL) {
						deathMessages = language + ".death-messages.killer-unknown.fall";
					} else if (dmgCause == DamageCause.DROWNING) {
						deathMessages = language + ".death-messages.killer-unknown.drowning";
					} else {
						deathMessages = language + ".death-messages.killer-unknown.pvp";
					}
					deathMessageMap.put(language, deathMessages);
				}
				int num = ThreadLocalRandom.current().nextInt(0, Main.getInstance().getConfig().getStringList(deathMessageMap.get("zh-CN")).toArray().length);
				game.sendDeathMessage(num, game.getGamePlayer(player));
				game.sendMessageTo(gamePlayer, game.getDeathMessage(gamePlayer, num, game.getGamePlayer(player)));
				game.sendMessageTo(gamePlayer, Messages.getMessage(gamePlayer.getPlayer(), "DEATH_MESSAGE"));
			}
			if (game.getPlayers().size() <= 1 || game.getTeams().size() <= 1) {
				if (!game.getTeams().contains(gamePlayer.getTeam()))
					game.sendTitleTo(gamePlayer, Messages.getMessage(gamePlayer.getPlayer(), "GAME_OVER_TITLE"), "\"color\":\"red\",\"bold\":true",
							Messages.getMessage(gamePlayer.getPlayer(), "GAME_OVER_SUBTITLE"), "\"color\":\"gray\"", 0, 100, 20);
				if (game.getTeamMode()) {
					if (game.getTeams().size() > 0) {
						for (GamePlayer winner : game.getTeams().get(0).getMembers()) {
							game.reward(winner, true);
						}
						game.judge(game.getTeams().get(0), false);
					} else {
						game.judge();
					}
				} else {
					if (game.getPlayers().size() > 0) {
						game.reward(game.getPlayers().get(0), true);
						game.judge(game.getPlayers().get(0), false);
					} else {
						game.judge();
					}
				}
			} else {
				game.sendActionBar("PLAYERS_REMAINING", new String[]{"%amount%"}, new String[]{String.valueOf(game.getPlayers().size())});
				game.sendTitleTo(gamePlayer, Messages.getMessage(gamePlayer.getPlayer(), "DEATH_TITLE"), "\"color\":\"red\",\"bold\":true",
						Messages.getMessage(gamePlayer.getPlayer(), "DEATH_SUBTITLE"), "\"color\":\"gray\"", 0, 90, 20);
			}
		}
	}
}
