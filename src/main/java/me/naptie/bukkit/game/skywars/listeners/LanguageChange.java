package me.naptie.bukkit.game.skywars.listeners;

import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.player.events.LanguageChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LanguageChange implements Listener {

    @EventHandler
    public void onLanguageChange(LanguageChangeEvent event) {
        if (!event.getNewLanguage().equalsIgnoreCase(PlayerJoin.pl.get(event.getPlayer()))) {
            event.getPlayer().sendMessage(Messages.getMessage(event.getNewLanguage(), "LANGUAGE_CHANGE"));
        }
    }

}
