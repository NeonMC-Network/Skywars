package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.Game.GameState;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDamage implements Listener {

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Game game = Main.getInstance().getGame();

			if (game != null) {

				if (game.isState(GameState.GAMING) || game.isState(GameState.GAMING)
						|| game.isState(GameState.DEATHMATCH)) {
					event.setCancelled(false);
				}

				if (player.getKiller() != null) {
					GamePlayer gameKiller = game.getGamePlayer(player.getKiller());
					if (game.isTeamMate(gameKiller, game.getGamePlayer(player))) {
						event.setCancelled(true);
					}
				}

				if (game.isState(GameState.LOBBY) || game.isState(GameState.STARTING)
						|| game.isState(GameState.PREPARING)
						|| (game.isState(GameState.PROTECTED) && !event.getCause().equals(DamageCause.VOID))
						|| game.isState(GameState.ENDING)) {
					event.setCancelled(true);
				}
				
				if (game.isPlayerSpectating(game.getGamePlayer(player))) {
					event.setCancelled(true);
				}

				if (!game.isPlayerSpectating(game.getGamePlayer(player)) && player.getLocation().getY() < 0 && (game.isState(GameState.PROTECTED)
						|| game.isState(GameState.GAMING) || game.isState(GameState.DEATHMATCH))) {

					for (PotionEffect potionEffect : player.getActivePotionEffects()) {
						player.removePotionEffect(potionEffect.getType());
					}
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
					player.teleport(new Location(game.getLobby().getWorld(), game.getLobby().getX(),
							game.getLobby().getY() - 5, game.getLobby().getZ()));
					player.setAllowFlight(true);
					player.setFlying(true);
					event.setDamage(player.getMaxHealth() + (player.hasPotionEffect(PotionEffectType.ABSORPTION) ? 4 : 0));

				} else if (!game.isPlayerSpectating(game.getGamePlayer(player)) && player.getLocation().getY() < 0 && !(game.isState(GameState.PROTECTED)
						|| game.isState(GameState.GAMING) || game.isState(GameState.DEATHMATCH))) {

					player.teleport(game.getLobby());

				} else if (game.isPlayerSpectating(game.getGamePlayer(player)) && player.getLocation().getY() < 0) {
					player.teleport(new Location(game.getLobby().getWorld(), game.getLobby().getX(),
							game.getLobby().getY() - 5, game.getLobby().getZ()));
				}
			}
		}
	}

}