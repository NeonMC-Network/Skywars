package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItem implements Listener {

	public PlayerPickupItem() {
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		GamePlayer gamePlayer = game.getGamePlayer(player);
		if (gamePlayer.getGame().isPlayerSpectating(gamePlayer)) {
			event.setCancelled(true);
		}
		if (game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.STARTING) || game.isState(Game.GameState.PREPARING)) {
			event.setCancelled(true);
		}
	}
}