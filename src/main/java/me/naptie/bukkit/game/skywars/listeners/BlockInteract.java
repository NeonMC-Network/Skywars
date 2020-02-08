package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockInteract implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		handle(event, player);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		handle(event, player);
	}

	private void handle(Cancellable event, Player player) {
		Game game = Main.getInstance().getGame();
		if (game != null) {
			if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.PREPARING)
					|| game.isState(Game.GameState.ENDING) || game.isState(Game.GameState.STARTING)) {
				event.setCancelled(true);
				return;
			}

			GamePlayer gamePlayer = game.getGamePlayer(player);
			if (gamePlayer != null) {
				if (game.getSpectators().contains(gamePlayer)) {
					event.setCancelled(true);
				}
				if (gamePlayer.getPlayer() == player) {
					if (!game.getPlayers().contains(gamePlayer)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

}
