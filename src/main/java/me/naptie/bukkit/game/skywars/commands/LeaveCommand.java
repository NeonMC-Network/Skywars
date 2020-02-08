package me.naptie.bukkit.game.skywars.commands;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand extends SubCommand {

	@Override
	public void execute(CommandSender commandSender, String[] args) {
		if (commandSender instanceof Player) {
			Player player = (Player) commandSender;

			Game game = Main.getInstance().getGame();
			for (GamePlayer gamePlayer : game.getPlayers()) {
				if (gamePlayer.getPlayer() == player) {
					game.leave(gamePlayer);
					player.sendMessage(Messages.getMessage(player, "QUIT_GAME"));
					return;
				}
			}
			for (GamePlayer spectator : game.getSpectators()) {
				if (spectator.getPlayer() == player) {
					game.leaveFromSpectating(spectator);
					return;
				}
			}

			player.sendMessage(Messages.getMessage(player, "NOT_IN_GAME"));

		} else {
			commandSender.sendMessage(Messages.getMessage("zh-CN", "NOT_A_PLAYER"));
		}
	}
}
