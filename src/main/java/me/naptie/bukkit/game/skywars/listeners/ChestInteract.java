package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ChestInteract implements Listener {

	@EventHandler
	public void onChestOpen(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGame();

		GamePlayer gamePlayer = game.getGamePlayer(player);

		if (game.getGamePlayer(player) != null) {
			if ((game.isState(Game.GameState.LOBBY) || game.isState(Game.GameState.PREPARING)
					|| game.isState(Game.GameState.STARTING)) || game.getSpectators().contains(gamePlayer)) {
				event.setCancelled(true);
				return;
			}
			if (gamePlayer.getPlayer().equals(player)) {
				handle(event, game);
			}
		}
	}

	private void handle(PlayerInteractEvent event, Game game) {
		if (event.hasBlock() && event.getClickedBlock() != null
				&& event.getClickedBlock().getState() instanceof Chest) {
			Chest chest = (Chest) event.getClickedBlock().getState();

			if (game.getOpened().contains(chest) || game.getRareItems().size() == 0
					|| game.getNormalItems().size() == 0) {
				return;
			}

			chest.getBlockInventory().clear();

			for (Location rareChest : game.getRareChests()) {
				for (Location normalChest : game.getNormalChests()) {
					if (chest.getLocation().equals(rareChest)) {
						int toFill = new Random().nextInt(8);
						for (int x = 0; x < toFill; x++) {
							int selected = new Random().nextInt(game.getRareItems().size());
							if (!chest.getBlockInventory().contains(game.getRareItems().get(selected))) {
								chest.getBlockInventory().setItem(new Random().nextInt(27),
										game.getRareItems().get(selected));
							}
						}
					} else if (chest.getLocation().equals(normalChest)) {
						int toFill = new Random().nextInt(5);
						for (int x = 0; x < toFill; x++) {
							int selected = new Random().nextInt(game.getNormalItems().size());
							if (!chest.getBlockInventory().contains(game.getNormalItems().get(selected))) {
								chest.getBlockInventory().setItem(new Random().nextInt(27),
										game.getNormalItems().get(selected));
							}
							if (!chest.getBlockInventory().contains(Material.STONE_SWORD)
									&& (chest.getLocation().getBlockY() == game.getNormalChests().get(3).getBlockY()
									|| chest.getLocation().getBlockY() == game.getNormalChests().get(6)
									.getBlockY()
									|| chest.getLocation().getBlockY() == game.getNormalChests().get(9)
									.getBlockY())) {
								ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
								chest.getBlockInventory().setItem(new Random().nextInt(27), sword);
							}
						}
					}

				}
			}

			game.getOpened().add(chest);
		}
	}

}