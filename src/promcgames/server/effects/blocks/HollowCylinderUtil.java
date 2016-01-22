package promcgames.server.effects.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;


public class HollowCylinderUtil {
	public HollowCylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public HollowCylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, data);
	}
	
	public HollowCylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(world, centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public HollowCylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		new CylinderUtil(world, centerX, centerY, centerZ, radius, height, type, data);
		new CylinderUtil(world, centerX, centerY, centerZ, radius - 1, height, Material.AIR, (byte) 0);
	}
}
