package promcgames.server.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class Region {
	public World world = null;
	public int x1, y1, z1, x2, y2, z2;
	
	public Region(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.world = world;
		this.x1 = (x1 < x2 ? x1 : x2);
		this.y1 = (y1 < y2 ? y1 : y2);
		this.z1 = (z1 < z2 ? z1 : z2);
		this.x2 = (x1 < x2 ? x2 : x1);
		this.y2 = (y1 < y2 ? y2 : y1);
		this.z2 = (z1 < z2 ? z2 : z1);
	}
	
	public boolean isIn(Entity entity) {
		return isIn(entity.getLocation());
	}
	
	public boolean isIn(Location location) {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		return isIn(x, y, z);
	}
	
	public boolean isIn(int x, int y, int z) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
	}
	
	public List<Block> getBlocks() {
		List<Block> blocks = new ArrayList<Block>();
		for(int x = x1; x <= x2; x++) {
			for(int y = y1; y <= y2; y++) {
				for(int z = z1; z <= z2; z++) {
					blocks.add(world.getBlockAt(x, y, z));
				}
			}
		}
		return blocks;
	}
	
	public List<Block> getBlocks(Material material) {
		List<Block> blocks = new ArrayList<Block>();
		for(Block block : getBlocks()) {
			if(block.getType() == material) {
				blocks.add(block);
			}
		}
		return blocks;
	}
}
