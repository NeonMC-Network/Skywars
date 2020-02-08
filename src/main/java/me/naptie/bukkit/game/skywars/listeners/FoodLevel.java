package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevel implements Listener {

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Game game = Main.getInstance().getGame();
			if (game != null && game.getGamePlayer(player) != null) {
				GamePlayer gamePlayer = game.getGamePlayer(player);

				if (game.isPlayerSpectating(gamePlayer)) {
					event.setFoodLevel(20);
					event.setCancelled(true);
				}

				if (!(game.isState(Game.GameState.GAMING) || game.isState(Game.GameState.DEATHMATCH))) {
					if (gamePlayer.getPlayer() == player) {
						event.setFoodLevel(20);
						event.setCancelled(true);
					}
				}
			}
		}
	}

}
