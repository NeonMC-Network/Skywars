package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.naptie.bukkit.game.skywars.objects.Game;

public class PlayerMove implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		if (game != null) {
			if (game.isMovementFrozen() && !game.getTeamMode()) {
				if (!game.isPlayerSpectating(game.getGamePlayer(player))
						&& !game.getSpectators().contains(game.getGamePlayer(player))) {
					if (event.getFrom().getBlockX() != event.getTo().getBlockX()
							|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
						player.teleport(event.getFrom());
					}
				}
			}
		}
	}

}
