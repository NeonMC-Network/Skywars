package me.naptie.bukkit.game.skywars.commands;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.objects.Game;
import me.naptie.bukkit.game.skywars.permissions.Permissions;
import me.naptie.bukkit.game.skywars.tasks.Countdown;
import me.naptie.bukkit.game.skywars.tasks.Run;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkywarsCommand implements CommandExecutor {

	private LeaveCommand leaveCommand;
	private SetWinnerCommand setWinnerCommand;

	public SkywarsCommand() {
		this.leaveCommand = new LeaveCommand();
		this.setWinnerCommand = new SetWinnerCommand();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Messages.COMMAND_HYPHEN);
			if (sender instanceof Player) {
				Player p = (Player) sender;
				for (String string : Messages.getMessages(p, "COMMANDS.COMMON")) {
					p.sendMessage(string);
				}
				if (p.hasPermission(Permissions.ADMINISTRATION)) {
					for (String string : Messages.getMessages(p, "COMMANDS.ADMINISTRATION")) {
						p.sendMessage(string);
					}
				}
			} else {
				for (String string : Messages.getMessages("zh-CN", "COMMANDS.COMMON")) {
					sender.sendMessage(string);
				}
				for (String string : Messages.getMessages("zh-CN", "COMMANDS.ADMINISTRATION")) {
					sender.sendMessage(string);
				}
			}
			sender.sendMessage(Messages.COMMAND_HYPHEN);
		} else {
			String argument = args[0];
			List<String> newArgs = new ArrayList<>();

			for (int i = 0; i < args.length; i++) {
				if (i == 0) {
					continue;
				}
				newArgs.add(args[i]);
			}

			if (argument.equalsIgnoreCase("leave")) {
				this.leaveCommand.execute(sender, newArgs.toArray(new String[0]));
			} else if (argument.equalsIgnoreCase("forcestart")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.hasPermission(Permissions.ADMINISTRATION)) {
						Game game = Main.getInstance().getGame();
						if (game.isState(Game.GameState.PREPARING)) {
							Run.forcestart();
							game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{p.getDisplayName()});
						} else if (game.isState(Game.GameState.STARTING)) {
							Countdown.getInstance().forcestart();
							game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{p.getDisplayName()});
						} else if (game.isState(Game.GameState.LOBBY)) {
							new Run(game, true).runTaskTimer(Main.getInstance(), 0, 20);
							game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{p.getDisplayName()});
						} else {
							p.sendMessage(Messages.getMessage(p, "CANNOT_USE_YET"));
						}
					} else {
						p.sendMessage(Messages.getMessage(p, "PERMISSION_DENIED"));
					}
				} else {
					Game game = Main.getInstance().getGame();
					if (game.isState(Game.GameState.PREPARING)) {
						Run.forcestart();
						game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{sender.getName()});
					} else if (game.isState(Game.GameState.STARTING)) {
						Countdown.getInstance().forcestart();
						game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{sender.getName()});
					} else if (game.isState(Game.GameState.LOBBY)) {
						new Run(game, true).runTaskTimer(Main.getInstance(), 0, 20);
						game.sendMessage("FORCE_START", new String[]{"%sender%"}, new String[]{sender.getName()});
					} else {
						sender.sendMessage(Messages.getMessage("zh-CN", "CANNOT_USE_YET"));
					}

				}
			} else if (argument.equalsIgnoreCase("setwinner")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.hasPermission(Permissions.ADMINISTRATION)) {
						this.setWinnerCommand.execute(p, newArgs.toArray(new String[0]));
					} else {
						p.sendMessage(Messages.getMessage(p, "PERMISSION_DENIED"));
					}
				} else {
					this.setWinnerCommand.execute(sender, newArgs.toArray(new String[0]));
				}
			} else if (argument.equalsIgnoreCase("reload")) {

				if (sender instanceof Player) {
					Player p = (Player) sender;
					if (p.hasPermission(Permissions.ADMINISTRATION)) {
						Main.getInstance().reloadConfig();
						p.sendMessage(Messages.getMessage(p, "CONFIG_RELOAD"));
					} else {
						p.sendMessage(Messages.getMessage(p, "PERMISSION_DENIED"));
					}
				} else {
					Main.getInstance().reloadConfig();
					sender.sendMessage(Messages.getMessage("zh-CN", "CONFIG_RELOAD"));
				}
			} else {
				sender.sendMessage(sender instanceof Player ? Messages.getMessage((Player) sender, "COMMAND_NOT_EXIST") : Messages.getMessage("zh-CN", "COMMAND_NOT_EXIST"));
			}
		}

		return true;
	}

}