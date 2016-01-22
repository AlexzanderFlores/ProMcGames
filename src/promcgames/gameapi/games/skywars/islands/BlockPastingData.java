package promcgames.gameapi.games.skywars.islands;

import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class BlockPastingData {
	private Block center = null;
	private int id = 0;
	private int data = 0;
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	public BlockPastingData(Block center, int id, int data, int x, int y, int z) {
		this.center = center;
		this.id = id;
		this.data = data;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void set() {
		//Bukkit.getLogger().info(x + "," + y + "," + z + ": " + id + ":" + data);
		Block block = center.getRelative(x, y, z);
		block.setTypeId(id);
		block.setData((byte) data);
	}
}
