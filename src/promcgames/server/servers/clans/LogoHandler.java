package promcgames.server.servers.clans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AutoBroadcasts;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.FileHandler;

public class LogoHandler implements Listener {
	private static Map<Clan, String> paths = null;
	private static List<String> badURLs = null;
	
	public LogoHandler() {
		AutoBroadcasts.addAlert("Want to attach a logo to your clan? &e/clan logo <url>");
		paths = new HashMap<Clan, String>();
		badURLs = new ArrayList<String>();
		for(String id : DB.NETWORK_CLANS_PENDING_LOGOS.getAllStrings("clan_id")) {
			Clan clan = ClanHandler.getClan(Integer.valueOf(id));
			String path = DB.NETWORK_CLANS_PENDING_LOGOS.getString("clan_id", id, "path");
			paths.put(clan, path);
		}
		DB.NETWORK_CLANS_PENDING_LOGOS.deleteAll();
		EventUtil.register(this);
	}
	
	public static void handleCommand(CommandSender sender, String [] arguments) {
		if(arguments.length == 1 || arguments.length == 3) {
			if(Ranks.isStaff(sender)) {
				if(arguments.length == 1) {
					MessageHandler.sendMessage(sender, "Pending requests:");
					MessageHandler.sendMessage(sender, "");
					if(paths.isEmpty()) {
						MessageHandler.sendMessage(sender, "&cThere are no logo requests at this time");
					} else {
						for(Clan clan : paths.keySet()) {
							MessageHandler.sendMessage(sender, clan.getClanName() + ": &e" + paths.get(clan));
						}
					}
					MessageHandler.sendMessage(sender, "");
					MessageHandler.sendMessage(sender, "To Accept: &f/clan logo accept <url>");
					MessageHandler.sendMessage(sender, "To Deny: &f/clan logo deny <url>");
					MessageHandler.sendMessage(sender, "View an existing logo: &f/clan logo view <Clan Name>");
					MessageHandler.sendMessage(sender, "Delete an existing logo: &f/clan logo delete <Clan Name>");
				} else if(arguments[1].equalsIgnoreCase("accept") && arguments.length == 3) {
					final String path = arguments[2];
					Clan acceptedClan = null;
					for(Clan clan : paths.keySet()) {
						if(paths.get(clan).equals(path)) {
							acceptedClan = clan;
							break;
						}
					}
					if(acceptedClan == null) {
						MessageHandler.sendMessage(sender, "&c" + path + " does not match any loaded urls");
					} else {
						MessageHandler.sendMessage(sender, "Accepted " + acceptedClan.getClanName() + "'s logo request");
						for(Player player : Bukkit.getOnlinePlayers()) {
							Clan clan = ClanHandler.getClan(player);
							if(clan != null && clan == acceptedClan) {
								MessageHandler.sendMessage(player, "Your clan's logo request has been accepted!");
								MessageHandler.sendMessage(player, "Your clan's logo will be shown on twitter in battles you win");
							}
						}
						FileHandler.downloadImage(path, Bukkit.getWorldContainer().getPath() + "/../resources/clans/" + acceptedClan.getClanName() + ".png");
						paths.remove(acceptedClan);
						final int id = acceptedClan.getClanID();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								if(DB.NETWORK_CLANS_LOGOS.isKeySet("clan_id", String.valueOf(id))) {
									DB.NETWORK_CLANS_LOGOS.updateString("path", path, "clan_id", String.valueOf(id));
								} else {
									DB.NETWORK_CLANS_LOGOS.insert("'" + id + "', '" + path + "'");
								}
							}
						});
					}
				} else if(arguments[1].equalsIgnoreCase("deny") && arguments.length == 3) {
					String path = arguments[2];
					Clan acceptedClan = null;
					for(Clan clan : paths.keySet()) {
						if(paths.get(clan).equals(path)) {
							acceptedClan = clan;
							break;
						}
					}
					if(acceptedClan == null) {
						MessageHandler.sendMessage(sender, "&c" + path + " does not match any loaded urls");
					} else {
						MessageHandler.sendMessage(sender, "Denied " + acceptedClan.getClanName() + "'s logo request");
						for(Player player : Bukkit.getOnlinePlayers()) {
							Clan clan = ClanHandler.getClan(player);
							if(clan != null && clan == acceptedClan) {
								MessageHandler.sendMessage(player, "Your clan's logo request has been &cdenied");
							}
						}
						paths.remove(acceptedClan);
						badURLs.add(path);
					}
				} else if(arguments[1].equalsIgnoreCase("view") && arguments.length == 3) {
					Clan clan = ClanHandler.getClan(arguments[2]);
					if(clan == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[2] + " is not a clan");
					} else {
						int id = clan.getClanID();
						if(DB.NETWORK_CLANS_LOGOS.isKeySet("clan_id", String.valueOf(id))) {
							MessageHandler.sendMessage(sender, arguments[2] + "'s logo is " + DB.NETWORK_CLANS_LOGOS.getString("clan_id", String.valueOf(id), "path"));
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[2] + " does not have a logo");
						}
					}
				} else if(arguments[1].equalsIgnoreCase("delete") && arguments.length == 3) {
					Clan clan = ClanHandler.getClan(arguments[2]);
					if(clan == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[2] + " is not a clan");
					} else {
						int id = clan.getClanID();
						if(DB.NETWORK_CLANS_LOGOS.isKeySet("clan_id", String.valueOf(id))) {
							DB.NETWORK_CLANS_LOGOS.delete("clan_id", String.valueOf(id));
							FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/../resources/clans/" + clan.getClanName() + ".png"));
							MessageHandler.sendMessage(sender, "You have deleted " + arguments[2] + "'s logo");
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[2] + " does not have a logo");
						}
					}
				} else {
					MessageHandler.sendMessage(sender, "To Accept: &f/clan logo accept <url>");
					MessageHandler.sendMessage(sender, "To Deny: &f/clan logo deny <url>");
					MessageHandler.sendMessage(sender, "View an existing logo: &f/clan logo view <Clan Name>");
					MessageHandler.sendMessage(sender, "Delete an existing logo: &f/clan logo delete <Clan Name>");
				}
			} else {
				return;
			}
		} else if(sender instanceof Player) {
			Player player = (Player) sender;
			Clan clan = ClanHandler.getClan(player);
			if(clan == null) {
				MessageHandler.sendMessage(player, "&cYou are not in a clan");
			} else if(clan.getFounder().getName().equals(player.getName())) {
				String url = arguments[1];
				String imgur = "i.imgur.com";
				if(Pattern.compile("http[s]{0,1}://[a-zA-Z0-9\\./\\?=_%&#-+$@'\"\\|,!*]*").matcher(url).find() && url.contains(imgur)) {
					if(url.length() >= 35) {
						MessageHandler.sendMessage(player, "&cURL is too long");
					} else if(badURLs.contains(url)) {
						MessageHandler.sendMessage(player, "&cThis URL has been denied already");
					} else {
						paths.put(clan, url);
						MessageHandler.sendMessage(player, "Your clan logo has been submited for approval");
					}
				} else {
					String prntscr = "prntscr.com";
					if(Pattern.compile("http[s]{0,1}://[a-zA-Z0-9\\./\\?=_%&#-+$@'\"\\|,!*]*").matcher(url).find() && url.contains(prntscr)) {
						MessageHandler.sendMessage(player, "&cYou cannot use this URL! Please right click the image and click \"Copy Image Location\" then paste that URL");
					} else {
						MessageHandler.sendMessage(player, "&c" + url + " is not a valid url, it must contain " + imgur);
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cOnly clan founders can use this command");
			}
		} else {
			MessageHandler.sendPlayersOnly(sender);
		}
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		if(!paths.isEmpty()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(Ranks.isStaff(player)) {
					MessageHandler.sendMessage(player, "&bStaff: &aClan logos waiting approval, do &e/clan logo");
				}
			}
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		for(Clan clan : paths.keySet()) {
			String path = paths.get(clan);
			DB.NETWORK_CLANS_PENDING_LOGOS.insert("'" + clan.getClanID() + "', '" + path + "'");
		}
	}
}
