package me.naptie.bukkit.game.skywars.listeners;

import org.bukkit.entity.EntityType;
import static org.bukkit.entity.EntityType.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Arrays;
import java.util.List;

public class EntitySpawn implements Listener {

	private List<EntityType> allowedTypes = Arrays.asList(
			DROPPED_ITEM, ENDER_DRAGON, PLAYER, PRIMED_TNT,
			EXPERIENCE_ORB, EGG, ARROW, SNOWBALL, FIREBALL,
			SMALL_FIREBALL, ENDER_PEARL, SPLASH_POTION,
			THROWN_EXP_BOTTLE, FALLING_BLOCK, FIREWORK,
			DRAGON_FIREBALL, BOAT, LIGHTNING);

	@EventHandler
	public void onSpawn(EntitySpawnEvent event) {
		if (!allowedTypes.contains(event.getEntityType())) {
			event.setCancelled(true);
		}
	}

}
