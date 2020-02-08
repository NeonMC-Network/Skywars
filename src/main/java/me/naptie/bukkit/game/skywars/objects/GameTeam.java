package me.naptie.bukkit.game.skywars.objects;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.tasks.Run;
import me.naptie.bukkit.game.skywars.utils.CU;
import org.bukkit.Location;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class GameTeam {

	private Set<GamePlayer> members = new HashSet<>(Main.getInstance().getGame().getPlayersPerTeam());
	private Team team;
	private int id;

	GameTeam(int id) {
		this.id = id;
		registerTeam();
	}

	public GameTeam(int id, GamePlayer[] gamePlayers) {
		this.id = id;
		registerTeam();
		for (GamePlayer gamePlayer : gamePlayers) {
			addMember(gamePlayer);
		}
	}

	private void registerTeam() {
		this.team = Run.board.registerNewTeam(id + "");
		this.team.setPrefix(getColor(id, true));
		this.team.setSuffix(CU.t("&r"));
		this.team.setNameTagVisibility(NameTagVisibility.ALWAYS);
		this.team.setDisplayName(getName());
		this.team.setAllowFriendlyFire(false);
		this.team.setCanSeeFriendlyInvisibles(true);
		for (GamePlayer gamePlayer : members) {
			this.team.addEntry(gamePlayer.getPlayer().getName());
			gamePlayer.getPlayer().setPlayerListName(getColor(id, true) + gamePlayer.getPlayer().getName());
			gamePlayer.setDisplayName(getColor(id, true) + gamePlayer.getPlayer().getName() + CU.t("&r"));
		}
	}

	public void sendMessage(String message) {
		for (GamePlayer gamePlayer : members) {
			gamePlayer.sendMessage(message);
		}
	}

	public void sendTitle(String sTt, String ttI, String sStt, String sttI, int ttFIT, int ttST, int ttFOT) {
		for (GamePlayer gamePlayer : members) {
			gamePlayer.sendTitle(sTt, ttI, sStt, sttI, ttFIT, ttST, ttFOT);
		}
	}

	public void teleport(Location location) {
		for (GamePlayer gamePlayer : members) {
			gamePlayer.teleport(location);
		}
	}

	public String getName() {
		String name = "";
		for (GamePlayer gamePlayer : members) {
			Game game = gamePlayer.getGame();
			if (name.equals("")) {
				if (!game.isPlayerSpectating(gamePlayer))
					name = gamePlayer.getName();
				else
					name = CU.t("&m" + gamePlayer.getName());
			} else {
				if (!game.isPlayerSpectating(gamePlayer))
					name = CU.t(name + "&7, " + gamePlayer.getName());
				else
					name = CU.t(name + "&7, &m" + gamePlayer.getName());
			}
		}
		return name;
	}

	void addMember(GamePlayer gamePlayer) {
		members.add(gamePlayer);
		this.team.addEntry(gamePlayer.getPlayer().getName());
		gamePlayer.getPlayer().setPlayerListName(getColor(id, true) + gamePlayer.getPlayer().getName());
		gamePlayer.setDisplayName(getColor(id, true) + gamePlayer.getPlayer().getName() + CU.t("&r"));
	}

	public void removeMember(GamePlayer gamePlayer) {
		members.remove(gamePlayer);
		this.team.removeEntry(gamePlayer.getPlayer().getName());
		gamePlayer.getPlayer().setPlayerListName(gamePlayer.getPlayer().getName());
		gamePlayer.setDisplayName(gamePlayer.getPlayer().getName());
	}

	public Set<GamePlayer> getMembers() {
		return members;
	}

	public void clearMembers() {
		for (GamePlayer gamePlayer : members) {
			gamePlayer.getPlayer().setPlayerListName(gamePlayer.getPlayer().getName());
			gamePlayer.setDisplayName(gamePlayer.getPlayer().getName());
		}
		members.clear();
		for (String entry : this.team.getEntries())
			this.team.removeEntry(entry);
	}

	private String getColor(int i, boolean reverse) {
		while (i > 15) {
			i -= 15;
		}
		if (reverse)
			i = 15 - i;
		if (i >= 0 && i <= 9) {
			return CU.t("&" + i);
		} else {
			if (i == 10)
				return CU.t("&f");
			if (i == 11)
				return CU.t("&e");
			if (i == 12)
				return CU.t("&d");
			if (i == 13)
				return CU.t("&c");
			if (i == 14)
				return CU.t("&b");
			if (i == 15)
				return CU.t("&a");
			return "";
		}
	}
}
