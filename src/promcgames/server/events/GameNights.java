package promcgames.server.events;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AlertHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.Tweeter;
import promcgames.server.tasks.AsyncDelayedTask;

public class GameNights {
	private boolean canRun = true;
	private Map<UUID, String> hosts = null;
	
	public GameNights() {
		hosts = new HashMap<UUID, String>();
		for(String uuidString : DB.NETWORK_GAME_NIGHT_HOSTS.getAllStrings("uuid")) {
			UUID uuid = UUID.fromString(uuidString);
			if(uuid == null) {
				DB.NETWORK_GAME_NIGHT_HOSTS.delete("uuid", uuidString);
				continue;
			}
			String prefix = AccountHandler.getPrefix(uuid);
			if(prefix == null) {
				DB.NETWORK_GAME_NIGHT_HOSTS.delete("uuid", uuidString);
				continue;
			}
			hosts.put(uuid, prefix);
		}
		new CommandBase("gameNight", 0, 2) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(sender, "/gameNight alert");
					MessageHandler.sendMessage(sender, "/gameNight promote <name>");
					MessageHandler.sendMessage(sender, "/gameNight demote <name>");
					MessageHandler.sendMessage(sender, "/gameNight list");
				} else {
					String command = arguments[0];
					if(command.equalsIgnoreCase("alert")) {
						if(ProMcGames.getServerName().equals("HUB1")) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									if(sender instanceof Player) {
										Player player = (Player) sender;
										if(DB.NETWORK_GAME_NIGHT_HOSTS.isUUIDSet(player.getUniqueId())) {
											if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 7) {
												if(canRun) {
													canRun = false;
													String tweet = "Come join us for Game Night on our Teamspeak server! \n\nts.ProMcGames.com";
													String alert = "Come join us for Game Night on our TS server! Run /ts";
													long id = Tweeter.tweet(tweet, "game_night.jpg");
													if(id == -1) {
														MessageHandler.sendMessage(sender, "&cFailed to send tweet! Possible duplicate tweet");
													} else {
														AlertHandler.alert(alert + " &ehttps://twitter.com/ProMcGames/status/" + id);
													}
												} else {
													MessageHandler.sendMessage(sender, "&cYou cannot run this command currently");
												}
											} else {
												MessageHandler.sendMessage(sender, "&cYou cannot run this command today");
											}
										} else {
											MessageHandler.sendUnknownCommand(sender);
										}
									} else {
										MessageHandler.sendUnknownCommand(sender);
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, "&cYou can only run this command on HUB1 &e/hub 1");
						}
					} else if(command.equalsIgnoreCase("promote")) {
						if(Ranks.SENIOR_MODERATOR.hasRank(sender)) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									String name = arguments[1];
									UUID uuid = AccountHandler.getUUID(name);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
									} else if(hosts.containsKey(uuid) || DB.NETWORK_GAME_NIGHT_HOSTS.isUUIDSet(uuid)){
										MessageHandler.sendMessage(sender, "&c" + name + " is already a Game Night host");
									} else {
										String prefix = AccountHandler.getPrefix(uuid);
										if(prefix == null) {
											MessageHandler.sendMessage(sender, "&cAn error occurred when trying to add " + name + " as a Game Night host");
										} else {
											DB.NETWORK_GAME_NIGHT_HOSTS.insert("'" + uuid.toString() + "'");
											hosts.put(uuid, prefix);
											MessageHandler.sendMessage(sender, "You have promoted " + prefix + " &ato Game Night host");
										}
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, Ranks.OWNER.getNoPermission());
						}
					} else if(command.equalsIgnoreCase("demote")) {
						if(Ranks.SENIOR_MODERATOR.hasRank(sender)) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									String name = arguments[1];
									UUID uuid = AccountHandler.getUUID(name);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
									} else if(hosts.containsKey(uuid) || DB.NETWORK_GAME_NIGHT_HOSTS.isUUIDSet(uuid)){
										DB.NETWORK_GAME_NIGHT_HOSTS.deleteUUID(uuid);
										hosts.remove(uuid);
										MessageHandler.sendMessage(sender, "You have &cdemoted &a" + name + " from Game Night host");
									} else {
										MessageHandler.sendMessage(sender, "&c" + name + " is not a Game Night host");
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, Ranks.OWNER.getNoPermission());
						}
					} else if(command.equalsIgnoreCase("list")) {
						String message = "";
						for(UUID uuid : hosts.keySet()) {
							message += hosts.get(uuid) + ", ";
						}
						MessageHandler.sendMessage(sender, "Game Night Hosts: &e(&a" + hosts.size() + "&e) &a" + message.substring(0, message.length() - 2));
					} else {
						return false;
					}
				}
				return true;
			}
		}.enableDelay(2);
		if(ProMcGames.getServerName().equals("HUB1")) {
			new Tweeter("XwNjW1ZJTQadiRfkJwvSjd5l5", "fo3P1xcrAWtaGItKPQnuChd3CVJJhu0n0B1DhWmeMe0Y90VUJa", "2395173859-N85rJm20JEN0uVTFzdVFHRQGtU94GRjmnXXusUt", "UB8b9VoCfpurco5y6pv5q7CW2uF4eoCeyMq0DsXwXMkbV");
		}
	}
}
