package promcgames.gameapi;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;

public class TeleportCoolDown implements Listener {
	private String name = null;
	private Location target = null;
	private boolean cancel = false;
	private int counter = 0;
	
	public TeleportCoolDown(Player player, Location target) {
		this(player, target, 5);
	}
	
	public TeleportCoolDown(Player player, Location target, int counter) {
		name = player.getName();
		this.target = target;
		this.counter = counter;
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		Player player = ProPlugin.getPlayer(name);
		if(--counter < 0 || cancel) {
			if(!cancel && player != null) {
				player.teleport(target);
			}
			cancel = true;
			name = null;
			target = null;
			HandlerList.unregisterAll(this);
		}
		if(!cancel && player != null) {
			MessageHandler.sendMessage(player, "Teleporting in &e" + (counter + 1) + " &aseconds, do not move!");
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!cancel && name != null && name.equals(event.getPlayer().getName())) {
			Location to = event.getTo();
			Location from = event.getFrom();
			if(to.getBlockX() != from.getBlockX() || to.getBlockY() != from.getBlockY() || to.getBlockZ() != from.getBlockZ()) {
				cancel = true;
				MessageHandler.sendMessage(event.getPlayer(), "&cTeleportation Cancelled");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(!cancel && name != null && name.equals(event.getPlayer().getName())) {
			cancel = true;
		}
	}
}
