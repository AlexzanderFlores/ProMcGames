package promcgames.gameapi.games.factions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.player.MessageHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class BorderHandler implements Listener {
	private static int radius = 15000;
	private Location spawn = null;
	
	public BorderHandler() {
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		EventUtil.register(this);
	}
	
	public static int getRadius() {
		return radius;
	}
	
	private double getDistance(Location location) {
		int x = location.getBlockX();
		int z = location.getBlockZ();
		return Math.sqrt((spawn.getBlockX() - x) * (spawn.getBlockX() - x) + (spawn.getBlockZ() - z) * (spawn.getBlockZ() - z));
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(getDistance(event.getTo()) > radius) {
			Player player = event.getPlayer();
			if(player.getVehicle() != null) {
				final Entity entity = player.getVehicle();
				final Location location = player.getLocation();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						entity.teleport(location);
					}
				}, 3);
				player.leaveVehicle();
			}
			event.setTo(event.getFrom());
			MessageHandler.sendMessage(player, "&cYou have reached the world border");
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(getDistance(event.getBlock().getLocation()) > radius) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(getDistance(event.getBlock().getLocation()) > radius) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(getDistance(event.getLocation()) > radius) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(getDistance(event.getPlayer().getLocation()) > radius) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot drop items outside of the border");
			event.setCancelled(true);
		}
	}
}
