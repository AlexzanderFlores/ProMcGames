package promcgames.gameapi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.player.MessageHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;

public class AutoJoinHandler implements Listener {
	public AutoJoinHandler() {
		new CommandBase("autoJoin", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getAutoJoin()) {
					if(ProMcGames.getMiniGame().getAutoJoin()) {
						send(player);
					} else {
						MessageHandler.sendMessage(player, "&cAuto-joining is disabled for this game!");
					}
				}
				return true;
			}
		};
	}
	
	public static void send(final Player player) {
		send(player, ProMcGames.getPlugin());
	}
	
	public static void send(final Player player, final Plugins plugin) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				PreparedStatement statement = null;
				ResultSet resultSet = null;
				DB table = DB.NETWORK_SERVER_STATUS;
				try {
					int max = Bukkit.getMaxPlayers();
					statement = table.getConnection().prepareStatement("SELECT server_number FROM " + table.getName() + " WHERE game_name = '" + plugin.toString() + "' AND players < " + max + " ORDER BY listed_priority, players DESC, server_number LIMIT 1");
					resultSet = statement.executeQuery();
					if(resultSet.next() && resultSet.getInt(1) > 0) {
						ProPlugin.sendPlayerToServer(player, plugin.getServer() + resultSet.getInt("server_number"));
					} else {
						ProPlugin.sendPlayerToServer(player, "hub");
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(statement, resultSet);
				}
			}
		});
	}
}
