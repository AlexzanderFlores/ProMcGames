package promcgames.player.account;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.AsyncPostPlayerJoinEvent;
import promcgames.customevents.timed.FifteenTickTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;
import promcgames.staff.StaffMode;

public class PlayerTracker implements Listener {
	private List<String> queue = null;
	
	public PlayerTracker() {
		queue = new ArrayList<String>();
		new CommandBase("seen", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else if(DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
							String text = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location");
							if(sender instanceof Player) {
								Player player = (Player) sender;
								String server = text.split(ChatColor.RED.toString())[1];
								ChatClickHandler.sendMessageToRunCommand(player, " &b&lCLICK TO JOIN", "Click to teleport to " + server, "/join " + server, text);
							} else {
								MessageHandler.sendMessage(sender, text);
							}
						} else {
							Player target = Bukkit.getPlayer(uuid);
							if(target == null || arguments[0].equalsIgnoreCase(Disguise.getName(target, true))) {
								MessageHandler.sendMessage(sender, "&cCould not find " + arguments[0]);
							} else if(Disguise.isDisguised(target)) {
								String text = StringUtil.color("&7" + Disguise.getName(target, false) + "&e is on &c" + ProMcGames.getServerName());
								if(sender instanceof Player) {
									Player player = (Player) sender;
									String server = text.split(ChatColor.RED.toString())[1];
									ChatClickHandler.sendMessageToRunCommand(player, " &b&lCLICK TO JOIN", "Click to teleport to " + server, "/join " + server, text);
								} else {
									MessageHandler.sendMessage(sender, text);
								}
							}
						}
						MessageHandler.sendMessage(sender, "&dFind multiple friends at the same time: &f/friends");
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("staff") {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						List<String> staffUUIDs = DB.STAFF_ONLINE.getAllStrings("uuid");
						if(staffUUIDs.isEmpty()) {
							MessageHandler.sendMessage(sender, "&cThere are currently no staff available!");
						} else {
							MessageHandler.sendMessage(sender, "&aOnline Staff (&b" + staffUUIDs.size() + "&a)");
							for(String uuid : staffUUIDs) {
								String server = DB.STAFF_ONLINE.getString("uuid", uuid, "server");
								if(sender instanceof Player) {
									Player player = (Player) sender;
									String actualServer = ChatColor.stripColor(server.split(" on ")[1]);
									if(actualServer.equalsIgnoreCase("VANISHED")) {
										MessageHandler.sendMessage(sender, server);
									} else {
										ChatClickHandler.sendMessageToRunCommand(player, " &b&lCLICK TO JOIN", "Click to join " + actualServer, "/join " + actualServer, server);
									}
								} else {
									MessageHandler.sendMessage(sender, server);
								}
							}
						}
						MessageHandler.sendMessage(sender, "&eWant to help ProMcGames? Apply here: &b/apply");
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("staffHere") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				List<String> onlineStaff = new ArrayList<String>();
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(Ranks.isStaff(online)) {
						onlineStaff.add(AccountHandler.getPrefix(online, true, true) + (StaffMode.contains(online) ? " &a(SM)" : ""));
					}
				}
				if(!onlineStaff.isEmpty()) {
					MessageHandler.sendMessage(sender, "");
					String message = "Staff on this server: (&b" + onlineStaff.size() + "&a) ";
					for(String staff : onlineStaff) {
						message += staff + "&f, ";
					}
					MessageHandler.sendMessage(sender, message.substring(0, message.length() - 2));
					MessageHandler.sendMessage(sender, "");
					MessageHandler.sendMessage(sender, "View this any time: &c/staffHere");
					MessageHandler.sendMessage(sender, "");
					onlineStaff.clear();
				}
				onlineStaff = null;
				return true;
			}
		}.setRequiredRank(Ranks.HELPER);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onFifteenTickTask(FifteenTickTaskEvent event) {
		if(!queue.isEmpty()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String name = queue.get(0);
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						UUID uuid = player.getUniqueId();
						String location = AccountHandler.getPrefix(player, false, true) + ChatColor.YELLOW + " is on " + ChatColor.RED + ProMcGames.getServerName();
						DB.PLAYERS_LOCATIONS.insert("'" + uuid.toString() + "', '" + location + "'");
					}
					queue.remove(0);
				}
			});
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		if(!Disguise.isDisguised(event.getPlayer()) && ProMcGames.getPlugin() != Plugins.SLAVE) {
			Player player = event.getPlayer();
			if(Ranks.isStaff(player)) {
				String location = AccountHandler.getPrefix(player, true, true) + ChatColor.YELLOW + " is on " + ChatColor.RED;
				if(SpectatorHandler.contains(player)) {
					location += "VANISHED";
				} else {
					location += ProMcGames.getServerName();
				}
				DB.STAFF_ONLINE.insert("'" + Disguise.getUUID(player).toString() + "', '" + location + "'");
				player.chat("/staffHere");
			} else if(AccountHandler.getRank(player) != Ranks.STREAMER && AccountHandler.getRank(player) != Ranks.YOUTUBER){
				queue.add(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		DB.STAFF_ONLINE.deleteUUID(uuid);
		DB.PLAYERS_LOCATIONS.deleteUUID(uuid);
	}
}
