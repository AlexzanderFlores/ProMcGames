package promcgames.gameapi.games.factions;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.TeleportCoolDown;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.CommandBase;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;

public class Teleportation implements Listener {
	public class Teleport {
		private String target = null;
		private String requesting = null;
		private int counter = 30;
		
		public Teleport(Player requesting, Player target) {
			this.target = target.getName();
			this.requesting = requesting.getName();
			playerTeleports.put(this.target, this);
			playerTeleports.put(this.requesting, this);
			teleportations.add(this);
			MessageHandler.sendMessage(target, AccountHandler.getPrefix(requesting) + " &ahas requested to teleport to you");
			MessageHandler.sendMessage(target, "Accept: &b/tpAccept &aDeny: &b/tpDeny");
			MessageHandler.sendMessage(target, "This will expire in &e" + counter + " &aseconds");
			MessageHandler.sendMessage(requesting, "Teleport request sent to " + AccountHandler.getPrefix(target));
			MessageHandler.sendMessage(requesting, "Expires in &e" + counter + " &aseconds");
		}
		
		public boolean contains(Player player) {
			return target.equals(player.getName()) || requesting.equals(player.getName());
		}
		
		public Player getTarget() {
			return ProPlugin.getPlayer(target);
		}
		
		public Player getRequesting() {
			return ProPlugin.getPlayer(requesting);
		}
		
		public void process() {
			if(--counter < 0) {
				cancel(true);
			}
		}
		
		public void cancel(boolean alert) {
			if(alert) {
				Player targetPlayer = ProPlugin.getPlayer(target);
				Player requestingPlayer = ProPlugin.getPlayer(requesting);
				if(targetPlayer != null && requestingPlayer != null) {
					MessageHandler.sendMessage(targetPlayer, "Tpa request from " + requesting + " was cancelled or denied");
					MessageHandler.sendMessage(requestingPlayer, "Tpa request to " + target + " was cancelled or denied");
				}
			}
			playerTeleports.remove(this.target);
			playerTeleports.remove(this.requesting);
			this.target = null;
			this.requesting = null;
			this.counter = 0;
			teleportations.remove(this);
		}
	}
	
	private static List<Teleport> teleportations = null;
	private static Map<String, Teleport> playerTeleports = null;
	private Map<String, Location> lastLocation = null;
	private List<String> disabled = null;
	
	public Teleportation() {
		teleportations = new ArrayList<Teleport>();
		playerTeleports = new HashMap<String, Teleport>();
		lastLocation = new HashMap<String, Location>();
		disabled = new ArrayList<String>();
		new CommandBase("tpa", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				String name = arguments[0];
				Player target = ProPlugin.getPlayer(name);
				if(target == null) {
					MessageHandler.sendMessage(player, "&c" + name + " is not online");
				} else if(playerTeleports.containsKey(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou must wait for your current request to expire or be denied");
				} else if(playerTeleports.containsKey(target.getName())) {
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &calready has a teleportation request, try again soon");
				} else if(disabled.contains(target.getName())) {
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &chas teleport requests disabled");
				} else {
					new Teleport(player, target);
					MessageHandler.sendMessage(target, "&cDo not want teleport requests? &f/tpToggle");
				}
				return true;
			}
		}.enableDelay(5);
		new CommandBase("tpaccept", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(playerTeleports.containsKey(player.getName())) {
					Player requesting = playerTeleports.get(player.getName()).getRequesting();
					if(requesting != null) {
						MessageHandler.sendMessage(requesting, "Tpa request to " + AccountHandler.getPrefix(player) + " &awas accepted");
						new TeleportCoolDown(requesting, player.getLocation());
					}
					playerTeleports.get(player.getName()).cancel(false);
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have a pending teleport request");
				}
				return true;
			}
		};
		new CommandBase("tpdeny", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(playerTeleports.containsKey(player.getName())) {
					playerTeleports.get(player.getName()).cancel(true);
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have a pending teleport request");
				}
				return true;
			}
		};
		new CommandBase("back", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIP(player)) {
					if(lastLocation.containsKey(player.getName())) {
						new TeleportCoolDown(player, lastLocation.get(player.getName()));
					} else {
						MessageHandler.sendMessage(player, "&cWe could not find your previous location");
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou must have VIP or VIP+ to use this command &b/buy");
				}
				return true;
			}
		};
		new CommandBase("tpToggle", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(disabled.contains(sender.getName())) {
					disabled.remove(sender.getName());
					MessageHandler.sendMessage(sender, "Teleport requests are now &eON");
				} else {
					disabled.add(sender.getName());
					MessageHandler.sendMessage(sender, "Teleport requests are now &cOFF");
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		try {
			for(Teleport teleport : teleportations) {
				teleport.process();
			}
		} catch(ConcurrentModificationException e) {
			
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		lastLocation.put(event.getPlayer().getName(), event.getFrom());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(playerTeleports.containsKey(player.getName())) {
			playerTeleports.get(player.getName()).cancel(true);
		}
		lastLocation.remove(player.getName());
		disabled.remove(player.getName());
	}
}
