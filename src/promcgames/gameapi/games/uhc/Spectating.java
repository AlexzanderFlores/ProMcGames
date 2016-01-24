package promcgames.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.PlayerSpectateCommandEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class Spectating implements Listener {
	private List<String> delayed = null;
	private int delay = 2;
	
	public Spectating() {
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static Inventory getInventory(Player player, String name, Inventory inventory) {
		if(HostHandler.isHost(player.getUniqueId()) || HostedEvent.isEvent()) {
			return inventory;
		}
		MessageHandler.sendMessage(player, "&cCannot teleport to a specific player in " + ProMcGames.getServerName());
		MessageHandler.sendMessage(player, "Sending you to 0, 0");
		return null;
	}
	
	private boolean isAwayFromSpawn(Location to) {
		return !to.toVector().isInSphere(to.getWorld().getSpawnLocation().toVector(), 150);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(HostedEvent.isEvent()) {
			PlayerMoveEvent.getHandlerList().unregister(this);
			return;
		}
		if(SpectatorHandler.contains(event.getPlayer()) && !HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.MODERATOR.hasRank(event.getPlayer()) && !delayed.contains(event.getPlayer().getName())) {
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ()) {
				Player player = event.getPlayer();
				World world = player.getWorld();
				if(world.getName().equals(WorldHandler.getWorld().getName())) {
					if(isAwayFromSpawn(event.getTo())) {
						MessageHandler.sendMessage(player, "&cYou cannot move that far away from spawn. Spectating is limited to prevent leaking player's information");
						event.setTo(event.getFrom());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateCommand(PlayerSpectateCommandEvent event) {
		if(!HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.MODERATOR.hasRank(event.getPlayer())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot use this command in " + ProMcGames.getServerName());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.PLUGIN && SpectatorHandler.contains(event.getPlayer())) {
			final String name = event.getPlayer().getName();
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * delay);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(SpectatorHandler.contains(event.getPlayer()) && !HostHandler.isHost(event.getPlayer().getUniqueId()) && !Ranks.isStaff(event.getPlayer())) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.HELPER.hasRank(player) && !HostHandler.isHost(player.getUniqueId()) && !SpectatorHandler.contains(player)) {
					event.getRecipients().remove(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(!Ranks.ELITE.hasRank(event.getPlayer()) && SpectatorHandler.wouldSpectate() && !DisconnectHandler.isDisconnected(event.getPlayer()) && !HostedEvent.isEvent()) {
			event.setKickMessage("To spectate you must have " + Ranks.ELITE.getPrefix());
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		if(!DisconnectHandler.isDisconnected(event.getPlayer()) && !HostedEvent.isEvent()) {
			if((Ranks.ELITE.hasRank(event.getPlayer()) && !HostedEvent.isEvent()) || HostHandler.isHost(event.getPlayer().getUniqueId())) {
				event.getPlayer().teleport(WorldHandler.getWorld().getSpawnLocation());
			} else {
				MessageHandler.sendMessage(event.getPlayer(), "&cTo spectate you must have " + Ranks.ELITE.getPrefix());
				ProPlugin.sendPlayerToServer(event.getPlayer(), "hub");
				event.setCancelled(true);
			}
		}
	}
}
