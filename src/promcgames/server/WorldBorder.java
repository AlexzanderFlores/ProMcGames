package promcgames.server;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

public class WorldBorder implements Listener {
	private int radius = 0;
	private int minX = 0;
	private int maxX = 0;
	private int minZ = 0;
	private int maxZ = 0;
	private Location center = null;
	private String world = null;
	
	public WorldBorder(World world, int radius) {
		this(world.getSpawnLocation(), radius);
	}
	
	public WorldBorder(Location center, int radius) {
		this.radius = radius;
		this.center = center;
		this.world = center.getWorld().getName();
		calculateLimits();
		EventUtil.register(this);
	}
	
	public void calculateLimits() {
		minX = center.getBlockX() - radius;
		maxX = center.getBlockX() + radius;
		minZ = center.getBlockZ() - radius;
		maxZ = center.getBlockZ() + radius;
	}
	
	public boolean inBorder(Player player) {
		return inBorder(player.getLocation());
	}
	
	public boolean inBorder(Location location) {
		int x = location.getBlockX();
		int z = location.getBlockZ();
		return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
	}
	
	public Location getTeleportLocation(Location stuck) {
		double x = stuck.getX(), y = stuck.getY(), z = stuck.getZ();
		Location newLoc = new Location(stuck.getWorld(), x + .5D, y, z, stuck.getYaw(), stuck.getPitch());
		if(inBorder(newLoc)) {
			return newLoc;
		}
		newLoc = new Location(stuck.getWorld(), x - .5D, y, z, stuck.getYaw(), stuck.getPitch());
		if(inBorder(newLoc)) {
			return newLoc;
		}
		newLoc = new Location(stuck.getWorld(), x, y, z + .5D, stuck.getYaw(), stuck.getPitch());
		if(inBorder(newLoc)) {
			return newLoc;
		}
		newLoc = new Location(stuck.getWorld(), x, y, z - .5D, stuck.getYaw(), stuck.getPitch());
		if(inBorder(newLoc)) {
			return newLoc;
		}
		return null;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		Player player = event.getPlayer();
		if(to.getWorld().getName().equals(world) && !inBorder(player)) {
			player.teleport(getTeleportLocation(event.getFrom()));
			MessageHandler.sendMessage(player, "&cYou have reached the world border!");
			MessageHandler.sendMessage(player, "&cIf you are stuck, do &b/spawn&a!");
		}
	}
	
	public int getRadius() {
		return radius;
	}
	
	public int getMinX() {
		return minX;
	}
	
	public int getMaxX() {
		return maxX;
	}
	
	public int getMinZ() {
		return minZ;
	}
	
	public int getMaxZ() {
		return maxZ;
	}
	
	public Location getCenter() {
		return center;
	}
	
	public String getWorld() {
		return world;
	}
}