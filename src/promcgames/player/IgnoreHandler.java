package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.ProPlugin;
import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class IgnoreHandler implements Listener {
	private static Map<String, List<UUID>> ignores = null;
	
	public IgnoreHandler() {
		new CommandBase("ignore", 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						final Player target = ProPlugin.getPlayer(arguments[0]);
						if(target == null) {
							MessageHandler.sendMessage(sender, ChatColor.RED + arguments[0] + " is not online");
						} else if(Ranks.isStaff(target)) {
							MessageHandler.sendMessage(sender, ChatColor.RED + "You cannot ignore a staff member");
						} else if(target.getName().equals(sender.getName())) {
							MessageHandler.sendMessage(sender, ChatColor.RED + "You cannot ignore yourself");
						} else {
							final Player player = (Player) sender;
							if(Ranks.isStaff(player)) {
								MessageHandler.sendMessage(player, ChatColor.RED + "You cannot ignore players as a staff member");
							} else {
								String [] keys = new String [] {"uuid", "ignored_uuid"};
								String [] values = new String [] {Disguise.getUUID(player).toString(), Disguise.getUUID(target).toString()};
								if(DB.PLAYERS_IGNORES.isKeySet(keys, values)) {
									DB.PLAYERS_IGNORES.delete(keys, values);
									MessageHandler.sendMessage(player, "You are no longer ignoring " + AccountHandler.getPrefix(target, false));
									if(ignores != null && ignores.containsKey(Disguise.getName(player))) {
										List<UUID> ignored = ignores.get(Disguise.getName(player));
										ignored.remove(Disguise.getUUID(target));
										ignores.put(Disguise.getName(player), ignored);
										ignored = null;
									}
								} else {
									DB.PLAYERS_IGNORES.insert("'" + Disguise.getUUID(player).toString() + "', '" + Disguise.getUUID(target).toString() + "'");
									MessageHandler.sendMessage(player, "You are now ignoring " + AccountHandler.getPrefix(target, false));
									if(ignores == null) {
										ignores = new HashMap<String, List<UUID>>();
									}
									List<UUID> ignored = ignores.get(Disguise.getName(player));
									if(ignored == null) {
										ignored = new ArrayList<UUID>();
									}
									ignored.add(Disguise.getUUID(target));
									ignores.put(Disguise.getName(player), ignored);
								}
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		EventUtil.register(this);
	}
	
	public static boolean isIgnored(Player playerOne, Player playerTwo) {
		return ignores != null && ignores.get(Disguise.getName(playerOne)) != null && ignores.get(Disguise.getName(playerOne)).contains(Disguise.getUUID(playerTwo));
	}
	
	public static List<UUID> getIgnores(Player player) {
		return ignores == null || !ignores.containsKey(player.getName()) ? null : ignores.get(player.getName());
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(!Ranks.isStaff(event.getPlayer())) {
			if(DB.PLAYERS_IGNORES.isUUIDSet(Disguise.getUUID(event.getPlayer()))) {
				if(ignores == null) {
					ignores = new HashMap<String, List<UUID>>();
				}
				List<UUID> ignored = ignores.get(event.getPlayer().getName());
				if(ignored == null) {
					ignored = new ArrayList<UUID>();
				}
				for(String uuid : DB.PLAYERS_IGNORES.getAllStrings("ignored_uuid", "uuid", Disguise.getUUID(event.getPlayer()).toString())) {
					ignored.add(UUID.fromString(uuid));
				}
				ignores.put(event.getPlayer().getName(), ignored);
				ignored = null;
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(ignores != null && ignores.containsKey(event.getPlayer().getName())) {
			ignores.get(event.getPlayer().getName()).clear();
			ignores.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(ignores != null) {
			for(String name : ignores.keySet()) {
				List<UUID> ignored = ignores.get(name);
				if(ignored != null && ignored.contains(Disguise.getUUID(event.getPlayer())) && !Ranks.isStaff(event.getPlayer())) {
					event.getRecipients().remove(ProPlugin.getPlayer(name));
					ignored = null;
				}
			}
			if(ignores.containsKey(event.getPlayer().getName())) {
				List<UUID> ignored = ignores.get(event.getPlayer().getName());
				if(ignored != null) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(ignored.contains(Disguise.getUUID(player)) && !Ranks.isStaff(player)) {
							event.getRecipients().remove(player);
						}
					}
					ignored = null;
				}
			}
		}
	}
}
