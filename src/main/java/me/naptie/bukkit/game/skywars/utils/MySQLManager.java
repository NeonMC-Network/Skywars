package me.naptie.bukkit.game.skywars.utils;

import me.naptie.bukkit.game.skywars.Main;
import me.naptie.bukkit.game.skywars.tools.MySQL;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySQLManager {

	public MySQL.Editor editor;

	public MySQLManager() {
		this.login();
	}

	private void login() {
		MySQL mySQL = new MySQL(Main.getInstance().getConfig().getString("mysql.username"), Main.getInstance().getConfig().getString("mysql.password"));
		mySQL.setAddress(Main.getInstance().getConfig().getString("mysql.address"));
		mySQL.setDatabase(Main.getInstance().getConfig().getString("mysql.database"));
		mySQL.setTable("games");
		mySQL.setTimezone(Main.getInstance().getConfig().getString("mysql.timezone"));
		mySQL.setUseSSL(Main.getInstance().getConfig().getBoolean("mysql.useSSL"));

		try {
			this.editor = mySQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				autoReconnect();
			}
		});
	}

	@SuppressWarnings("InfiniteRecursion")
	private void autoReconnect() {

		try {
			TimeUnit.MINUTES.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			this.editor = this.editor.getMySQL().reconnect();
		} catch (SQLException e) {
			this.autoReconnect();
			return;
		}
		this.autoReconnect();
	}

}
