package promcgames.server.effects.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class CylinderUtil {
	public CylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public CylinderUtil(String world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		this(Bukkit.getWorld(world), centerX, centerY, centerZ, radius, height, type, data);
	}
	
	public CylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type) {
		this(world, centerX, centerY, centerZ, radius, height, type, (byte) 0);
	}
	
	public CylinderUtil(World world, int centerX, int centerY, int centerZ, int radius, int height, Material type, byte data) {
		int x1 = centerX - radius;
		int y1 = centerY;
		int z1 = centerZ - radius;
		int x2 = centerX + radius;
		int y2 = centerY + height - 1;
		int z2 = centerZ + radius;
		for(int x = x1; x <= x2; ++x) {
			for(int y = y1; y <= y2; ++y) {
				for(int z = z1; z <= z2; ++z) {
					if(Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ)) <= radius) {
						Block block = world.getBlockAt(x, y, z);
						block.setType(type);
						block.setData(data);
					}
				}
			}
		}
	}
}