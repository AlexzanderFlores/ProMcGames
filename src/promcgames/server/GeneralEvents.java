package promcgames.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.spigotmc.ProtocolInjector.PacketTabHeader;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.player.PlayerRankChangeEvent;
import promcgames.customevents.player.timed.PlayerFirstThirtyMinutesOfPlaytimeEvent;
import promcgames.gameapi.games.uhc.HostedEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubSponsor;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;
import promcgames.server.util.TextConverter;
import promcgames.server.util.UnicodeUtil;

public class GeneralEvents implements Listener {
	private List<String> blockedCommands = null;
	private List<String> delayed = null;
	private int delay = 3;
	
	public GeneralEvents() {
		blockedCommands = new ArrayList<String>();
		blockedCommands.add("/me");
		blockedCommands.add("/tell");
		blockedCommands.add("/w");
		blockedCommands.add("/kill");
		blockedCommands.add("/suicide");
		blockedCommands.add("/afk");
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	private void colorPlayerTab(Player player) {
		String name = AccountHandler.getRank(player).getColor() + Disguise.getName(player);
		if(name.length() > 16) {
			name = name.substring(0, 16);
		}
		try {
			player.setPlayerListName(name);
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setTicksLived(1);
		colorPlayerTab(player);
		if(!(ProMcGames.getPlugin() == Plugins.BUILDING || ProMcGames.getPlugin() == Plugins.TESTING) && (player.isOp() || player.hasPermission("bukkit.command.op.give") || player.hasPermission("bukkit.command.op.take")) && !Disguise.getUUID(player).toString().equals("c5f7f0fe-b3f7-443b-850d-dd2561caea71")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "deop " + Disguise.getName(player));
		}
		if(ProMcGames.getPlugin() != Plugins.BUILDING && ProMcGames.getPlugin() != Plugins.TESTING) {
			event.setJoinMessage(null);
		}
		String top = "&aWelcome to &bplay.ProMcGames.com";
		String bottom = "&aVisit our store &c/buy";
		CraftPlayer craftPlayer = (CraftPlayer) player;
		if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
			IChatBaseComponent header = ChatSerializer.a(TextConverter.convert(StringUtil.color(top)));
			IChatBaseComponent footer = ChatSerializer.a(TextConverter.convert(StringUtil.color(bottom)));
			craftPlayer.getHandle().playerConnection.sendPacket(new PacketTabHeader(header, footer));
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					update(player);
				}
			}
		}, 20 * 2);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if(ProMcGames.getPlugin() != Plugins.BUILDING && ProMcGames.getPlugin() != Plugins.TESTING) {
			event.setLeaveMessage(null);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if(ProMcGames.getPlugin() != Plugins.BUILDING && ProMcGames.getPlugin() != Plugins.TESTING) {
			event.setQuitMessage(null);
		}
	}
	
	@EventHandler
	public void onPlayerRankChange(PlayerRankChangeEvent event) {
		if(event.getPlayer().isOnline()) {
			colorPlayerTab(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			player.setSaturation(4.0f);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent event) {
		if(event.getPlayer().isOp()) {
			for(int a = 0; a < 4; ++a) {
				event.setLine(a, StringUtil.color(event.getLine(a)));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		
		if(delayed.contains(player.getName())) {
			MessageHandler.sendMessage(player, "&cYou can only talk once every &e" + delay + " &cseconds");
			MessageHandler.sendMessage(player, "You can bypass this with the " + Ranks.PRO.getPrefix() + "&arank &b/buy");
			event.setCancelled(true);
			return;
		} else if(AccountHandler.getRank(player) == Ranks.PLAYER) {
			final String name = player.getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * delay);
		}
		
		if(event.getMessage().contains("<3") && Ranks.PRO.hasRank(player)) {
			event.setMessage(event.getMessage().replace("<3", (Ranks.ELITE.hasRank(player) ? ChatColor.DARK_RED + UnicodeUtil.getHeart() : UnicodeUtil.getHeart())) + ChatColor.WHITE);
		}
		
		event.setMessage(event.getMessage().replace("%", "%%"));
		if(player.isOp()) {
			event.setMessage(StringUtil.color(event.getMessage()));
			event.setFormat(AccountHandler.getPrefix(player, false) + ": " + event.getMessage()); // Reformat to allow colors to work
			return;
		}
		
		if(event.getMessage().contains("&") && Ranks.PRO.hasRank(player) && !event.getMessage().contains("->")) {
			if(AccountHandler.getRank(player) == Ranks.PRO) {
				char [] characters = {
					'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
				};
				for(char color : characters) {
					event.setMessage(event.getMessage().replace("&" + color, "\u00a7" + color));
				}
			} else if(AccountHandler.getRank(player) == Ranks.PRO_PLUS){
				char [] characters = {
					'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'm', 'n', 'o'
				};
				for(char color : characters) {
					event.setMessage(event.getMessage().replace("&" + color, "\u00a7" + color));
				}
			} else {
				char [] characters = {
					'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'l', 'm', 'n', 'o'
				};
				for(char color : characters) {
					event.setMessage(event.getMessage().replace("&" + color, "\u00a7" + color));
				}
			}
			event.setFormat(AccountHandler.getPrefix(player, false) + ": " + event.getMessage());
		}
		
		if(!Ranks.isStaff(player)) {
			int numberOfCaps = 0;
			for(char character : event.getMessage().toCharArray()) {
				if(character >= 65 && character <= 90) {
					if(++numberOfCaps >= 5) {
						event.setMessage(event.getMessage().toLowerCase());
						break;
					}
				}
			}
		}
		
		event.setFormat(AccountHandler.getPrefix(player, false) + ": " + event.getMessage());
	}
	
	@EventHandler
	public void onPlayerFirstThirtyMinutesOfPlaytime(PlayerFirstThirtyMinutesOfPlaytimeEvent event) {
		Player player = event.getPlayer();
		HubSponsor.add(Disguise.getUUID(player), 1, false);
		if(!(ProMcGames.getPlugin() == Plugins.UHC && HostedEvent.isEvent())) {
			if(Disguise.isDisguised(player)) {
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, AccountHandler.getPrefix(player, true) + " &a+&c1 &aHub Sponsor");
				MessageHandler.sendMessage(player, "&6First 30 minutes of playtime!");
				MessageHandler.sendMessage(player, "&b(Place the redstone torch in the hub)");
				MessageHandler.sendMessage(player, "");
			} else {
				MessageHandler.alert("");
				MessageHandler.alert(AccountHandler.getPrefix(player, true) + " &a+&c1 &aHub Sponsor");
				MessageHandler.alert("&6First 30 minutes of playtime!");
				MessageHandler.sendMessage(player, "&b(Place the redstone torch in the hub)");
				MessageHandler.alert("");
			}
		}
	}
	
	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		if(event.getCommand().equalsIgnoreCase("stop")) {
			MessageHandler.alert("Server shut down by console!");
			ProPlugin.restartServer();
			event.setCommand("");
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(event.getPlayer().isOp() && event.getMessage().equalsIgnoreCase("/stop")) {
			MessageHandler.alert("Server shut down by " + AccountHandler.getPrefix(event.getPlayer()));
			ProPlugin.restartServer();
			event.setCancelled(true);
		} else if(isBlockedCommand(event.getMessage())) {
			MessageHandler.sendUnknownCommand(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	private boolean isBlockedCommand(String message) {
		message = message.toLowerCase();
		String command = "";
		for(char character : message.toCharArray()) {
			if(character == '\0' || character == ' ') {
				break;
			} else {
				command += character;
			}
		}
		return blockedCommands.contains(command) || command.contains(":");
	}
	
	private void update(Player player) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(online.canSee(player)) {
				online.hidePlayer(player);
				online.showPlayer(player);
			}
		}
	}
}
