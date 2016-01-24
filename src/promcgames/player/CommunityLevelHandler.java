package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.timed.PlayerDayOfPlaytimeEvent;
import promcgames.customevents.player.timed.PlayerHourOfPlaytimeEvent;
import promcgames.player.account.AccountHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class CommunityLevelHandler implements Listener {
	private static Map<String, Integer> communityLevels = null;
	private List<String> messages = null;
	private List<String> alreadySaid = null;
	public static int requiredForStaff = 500;
	
	public CommunityLevelHandler() {
		new CommandBase("communityLevel", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = "";
						int level = -1;
						if(arguments.length == 0) {
							if(sender instanceof Player) {
								Player player = (Player) sender;
								target = Disguise.getName(player);
								level = getCommunityLevel(player);
							}
						} else {
							target = arguments[0];
							if(target.equalsIgnoreCase("info")) {
								MessageHandler.sendLine(sender);
								MessageHandler.sendMessage(sender, "You have to have a specific amount of community level to do some things");
								MessageHandler.sendMessage(sender, "Ability to apply for staff: " + requiredForStaff);
								MessageHandler.sendMessage(sender, "Ways to gain community level: &e/communityLevel chart");
								MessageHandler.sendLine(sender);
							} else if(target.equalsIgnoreCase("chart")) {
								MessageHandler.sendLine(sender);
								MessageHandler.sendMessage(sender, "Saying \"welcome\" within 10 seconds of a new player joining: &e+3");
								MessageHandler.sendMessage(sender, "Claiming your daily gift from the Gift Giver NPC: &e+5");
								MessageHandler.sendMessage(sender, "Passing a hour of play time: &e+10");
								MessageHandler.sendMessage(sender, "Passing a day of play time: &e+25");
								MessageHandler.sendMessage(sender, "Saying \"gg\" or \"gf\" within 10 seconds of dying in game: &e+3");
								MessageHandler.sendLine(sender);
							} else if(target.equalsIgnoreCase("top")) {
								if(ProMcGames.getPlugin() == Plugins.HUB) {
									Player player = (Player) sender;
									player.teleport(new Location(player.getWorld(), -134.5, 126, -160.5, -270.0f, 0.0f));
								} else {
									MessageHandler.sendMessage(sender, "&cYou can only view the top ranked players on the hub");
								}
							} else {
								Player player = ProPlugin.getPlayer(target);
								if(player == null) {
									UUID uuid = AccountHandler.getUUID(target);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + target + " has never logged in");
									} else {
										level = DB.PLAYERS_COMMUNITY_LEVELS.getInt("uuid", uuid.toString(), "level");
									}
								} else {
									target = player.getName();
									level = getCommunityLevel(player);
								}
							}
						}
						if(level > -1) {
							MessageHandler.sendMessage(sender, target + " has a community level of &e" + level);
							MessageHandler.sendMessage(sender, "View the top 8 ranked players: &c/communityLevel top");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		messages = new ArrayList<String>();
		messages.add("gg");
		messages.add("gf");
		alreadySaid = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static int getCommunityLevel(Player player) {
		if(communityLevels == null) {
			communityLevels = new HashMap<String, Integer>();
		}
		if(!communityLevels.containsKey(Disguise.getName(player))) {
			communityLevels.put(Disguise.getName(player), DB.PLAYERS_COMMUNITY_LEVELS.getInt("uuid", Disguise.getUUID(player).toString(), "level"));
		}
		return communityLevels.get(Disguise.getName(player));
	}
	
	public static void addCommunityLevel(final Player player, final int amount) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int total = getCommunityLevel(player) + amount;
				communityLevels.put(Disguise.getName(player), total);
				MessageHandler.sendMessage(player, "Community Level +" + amount +  ", your level is now &e" + total);
				MessageHandler.sendMessage(player, "View the top 8 ranked players: &c/communityLevel top");
			}
		});
	}
	
	@EventHandler
	public void onPlayerHourOfPlaytime(PlayerHourOfPlaytimeEvent event) {
		addCommunityLevel(event.getPlayer(), 10);
	}
	
	@EventHandler
	public void onPlayerDayOfPlaytime(PlayerDayOfPlaytimeEvent event) {
		addCommunityLevel(event.getPlayer(), 25);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		int seconds = AliveTracker.getAliveSeconds(player);
		if(!event.isCancelled() && seconds >= 0 && seconds <= 10 && !alreadySaid.contains(player.getName())) {
			alreadySaid.add(player.getName());
			String msg = event.getMessage().toLowerCase();
			for(String message : messages) {
				if(msg.equals(message) || msg.startsWith(" " + message) || msg.endsWith(" " + message)) {
					addCommunityLevel(player, 3);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		String name = event.getRealName();
		if(communityLevels != null && communityLevels.containsKey(name)) {
			int level = communityLevels.get(name);
			if(DB.PLAYERS_COMMUNITY_LEVELS.isUUIDSet(uuid)) {
				DB.PLAYERS_COMMUNITY_LEVELS.updateInt("level", level, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_COMMUNITY_LEVELS.insert("'" + uuid.toString() + "', '" + level + "'");
			}
			communityLevels.remove(name);
			alreadySaid.remove(name);
		}
	}
}
