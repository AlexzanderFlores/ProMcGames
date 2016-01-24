package promcgames.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.WhitelistDisabledEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.Tweeter;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;
import twitter4j.Status;

public class WhitelistHandler implements Listener {
	private static boolean enabled = true;
	private static List<UUID> whitelisted = null;
	private static List<UUID> manualWhitelisted = null;
	
	public WhitelistHandler() {
		whitelisted = new ArrayList<UUID>();
		manualWhitelisted = new ArrayList<UUID>();
		new CommandBase("wl", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!Ranks.MODERATOR.hasRank(player) && !HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
				} else if(manualWhitelisted.contains(uuid)) {
					manualWhitelisted.remove(uuid);
					MessageHandler.sendMessage(sender, "&cRemoved &e" + arguments[0] + " &cfrom the whitelist");
				} else {
					manualWhitelisted.add(uuid);
					MessageHandler.sendMessage(sender, "Added &e" + arguments[0] + " &ato the whitelist");
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static boolean isWhitelisted() {
		return enabled;
	}
	
	public static boolean isWhitelisted(UUID uuid) {
		return whitelisted.contains(uuid);
	}
	
	public static void unWhitelist() {
		enabled = false;
		Bukkit.getPluginManager().callEvent(new WhitelistDisabledEvent());
		MessageHandler.alert("Game opened!");
	}
	
	public static void unWhitelist(UUID uuid) {
		whitelisted.remove(uuid);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(enabled) {
			UUID uuid = event.getPlayer().getUniqueId();
			if(HostedEvent.isEvent() && TeamHandler.getTeam(event.getPlayer().getName()) != null) {
				if(!manualWhitelisted.contains(uuid) && ProMcGames.getMiniGame().getGameState() != GameStates.STARTED) {
					manualWhitelisted.add(event.getPlayer().getUniqueId());
				}
			}
			if(whitelisted.contains(uuid) || Ranks.isStaff(event.getPlayer()) || HostHandler.isHost(uuid)) {
				event.setResult(Result.ALLOWED);
			} else {
				event.setResult(Result.KICK_OTHER);
				if(ProMcGames.getMiniGame().getGameState() == GameStates.WAITING && !TweetHandler.getURL().endsWith("/0")) {
					event.setKickMessage("This server is currently whitelisted. " + ChatColor.GREEN + "Get whitelisted:" + StringUtil.color(TweetHandler.getURL()));
				} else {
					event.setKickMessage("This server is currently whitelisted");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerLogin(PlayerLoginEvent event) {
		if(manualWhitelisted.contains(event.getPlayer().getUniqueId())) {
			event.setResult(Result.ALLOWED);
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			whitelisted.add(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		manualWhitelisted.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(enabled && ProMcGames.getMiniGame().getGameState() == GameStates.WAITING && !HostedEvent.isEvent()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Status status : Tweeter.getReplies()) {
						String text = status.getText();
						for(String word : text.split(" ")) {
							if(!word.contains("@")) {
								Bukkit.getLogger().info("\"" + word + "\"");
								UUID uuid = AccountHandler.getUUID(word);
								if(uuid != null && !whitelisted.contains(uuid)) {
									MessageHandler.alert("&c" + word + " &aRetweeted & Replied with their IGN for Pre-Whitelist" + TweetHandler.getURL());
									whitelisted.add(uuid);
								}
							}
						}
					}
				}
			});
		}
	}
}
