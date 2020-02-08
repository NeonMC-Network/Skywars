package me.naptie.bukkit.game.skywars.tasks;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class End extends BukkitRunnable {

	private int time = 10;
	private Game game;

	public End(Game game) {
		this.game = game;
	}

	@Override
	public void run() {
		time -= 1;
		if (time == 0) {

			cancel();
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					p.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
				Main.getInstance().backToLobby(p);
			}

			Main.getInstance().restart(game);
		}
	}

}
