package me.naptie.bukkit.game.skywars.commands;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.objects.GamePlayer;
import me.naptie.bukkit.game.skywars.objects.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWinnerCommand extends SubCommand {

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(sender instanceof Player ? Messages.getMessage((Player) sender, "NOT_SPECIFIED") : Messages.getMessage("zh-CN", "NOT_SPECIFIED"));
			return;
		}
		Game game = Main.getInstance().getGame();
		if (game.getTeamMode()) {
			for (GameTeam gameTeam : game.getTeams()) {
				for (GamePlayer gamePlayer : gameTeam.getMembers()) {
					if (args[0].equalsIgnoreCase(gamePlayer.getPlayer().getName())) {
						Player winnerPlayer = Bukkit.getPlayer(args[0]);
						GameTeam winner = game.getGamePlayer(winnerPlayer).getTeam();
						for (GamePlayer winner1 : winner.getMembers())
							game.reward(winner1, true);
						game.judge(winner, true);
						return;
					}
				}
			}
			sender.sendMessage(sender instanceof Player ? Messages.getMessage((Player) sender, "PLAYER_NOT_FOUND").replace("%player%", args[0]) : Messages.getMessage("zh-CN", "PLAYER_NOT_FOUND").replace("%player%", args[0]));
		} else {
			for (GamePlayer gamePlayer : game.getPlayers()) {
				if (args[0].equalsIgnoreCase(gamePlayer.getPlayer().getName())) {
					Player winnerPlayer = Bukkit.getPlayer(args[0]);
					GamePlayer winner = game.getGamePlayer(winnerPlayer);
					game.reward(winner, true);
					game.judge(winner, true);
					return;
				}
			}
			sender.sendMessage(sender instanceof Player ? Messages.getMessage((Player) sender, "PLAYER_NOT_FOUND").replace("%player%", args[0]) : Messages.getMessage("zh-CN", "PLAYER_NOT_FOUND").replace("%player%", args[0]));
		}
	}
}
