package me.naptie.bukkit.game.skywars.objects;

import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PacketPlayOutTitle;
import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.messages.Messages;
import me.naptie.bukkit.game.skywars.utils.CU;
import me.naptie.bukkit.player.utils.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class GamePlayer {

	private Player player;
	private Location spawnPoint;
	private int kills;
	private String name;

	public GamePlayer(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public GameTeam getTeam() {
		for (GameTeam gameTeam : getGame().getTeams()) {
			if (gameTeam.getMembers().contains(this)) {
				return gameTeam;
			}
		}
		return null;
	}

	public Game getGame() {
		return Main.getInstance().getGame();
	}

	public void sendMessage(String message) {
		player.sendMessage(CU.t(message));
	}

	public void sendMessage(List<String> messageList) {
		for (String message : messageList)
			player.sendMessage(CU.t(message));

	}

	public void sendTitle(String titleText, String titleProperties, String subtitleText, String subtitleProperties, int titleFadeIn, int titleStay, int titleFadeOut) {
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + titleText + "\"," + titleProperties.toLowerCase() + "}");
		IChatBaseComponent chatSubtitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + subtitleText + "\"," + subtitleProperties.toLowerCase() + "}");
		PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubtitle);
		PacketPlayOutTitle titleLength = new PacketPlayOutTitle(titleFadeIn, titleStay, titleFadeOut);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titleLength);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);
	}

	void sendActionBar(String sTt) {
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + CU.t(sTt) + "\"}");
		PacketPlayOutChat title = new PacketPlayOutChat(chatTitle, ChatMessageType.GAME_INFO);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
	}

	void playSound(Sound sound, int volume, int pitch) {
		player.playSound(player.getLocation(), sound, volume, pitch);
	}

	void teleport(Location location) {
		if (location == null) {
			return;
		}
		getPlayer().teleport(location);
	}

	void setDisplayName(String name) {
		this.name = name;
		player.setDisplayName(name);
	}

	public String getName() {
		if (player.isOnline()) {
			String name = player.getDisplayName();
			if (name.contains("[") && name.contains("]")) {
				name = name.split("] ")[1];
			}
			return name;
		} else {
			return this.name;
		}
	}

	public Location getSpawnPoint() {
		return spawnPoint;
	}

	public void setSpawnPoint(Location spawnPoint) {
		this.spawnPoint = spawnPoint;
	}

	int getKills() {
		return kills;
	}

	public void addKillCount() {
		this.kills += 1;
		YamlConfiguration data = me.naptie.bukkit.player.utils.ConfigManager.getData(Objects.requireNonNull(player.getPlayer()));
		data.set("point", data.getInt("point") + 1);
		sendMessage(ChatColor.GREEN + "+1" + (ConfigManager.getLanguageName(getPlayer()).contains("en") ? Messages.getMessage(getPlayer(), "ADD_POINTS").replace("%s%", "") : Messages.getMessage(getPlayer(), "ADD_POINTS")));
		data.set("skywars.kills", data.getInt("skywars.kills") + 1);
		try {
			data.save(me.naptie.bukkit.player.utils.ConfigManager.getDataFile(player.getPlayer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
