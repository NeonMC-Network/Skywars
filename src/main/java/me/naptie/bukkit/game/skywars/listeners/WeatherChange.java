package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.objects.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChange implements Listener {

	public WeatherChange() {
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWeatherChange(WeatherChangeEvent event) {
		Game game = Main.getInstance().getGame();
			if (event.getWorld().equals(game.getWorld())) {
				event.setCancelled(event.toWeatherState());
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onThunderChange(ThunderChangeEvent event) {
		Game game = Main.getInstance().getGame();
			if (event.getWorld().equals(game.getWorld())) {
				event.setCancelled(event.toThunderState());
			}
	}
}