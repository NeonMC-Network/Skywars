package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import me.naptie.bukkit.inventory.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

	public PlayerInteract() {
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();
		assert game != null;
		GamePlayer gamePlayer = game.getGamePlayer(player);
		if (gamePlayer != null && gamePlayer.getPlayer() != null)
			if (player.getItemInHand().getType() == Material.RED_BED && ConfigManager.isMatching(player.getItemInHand(), ConfigManager.getItem(player, "leave")) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (game.isPlayerSpectating(gamePlayer)) {
					game.leaveFromSpectating(gamePlayer);
				} else {
					game.leave(gamePlayer);
				}
				player.sendMessage(Messages.getMessage(player, "QUIT_GAME"));
			} else if (game.isPlayerSpectating(gamePlayer)) {
				event.setCancelled(true);
			}

	}
}