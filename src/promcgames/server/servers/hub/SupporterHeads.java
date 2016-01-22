package promcgames.server.servers.hub;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.PurchaseEvent;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class SupporterHeads implements Listener {
	private World world = null;
	
	public SupporterHeads() {
		world = Bukkit.getWorlds().get(0);
		update();
		EventUtil.register(this);
	}
	
	private void updateSkull(int x, int y, int z, String name) {
		Block block = world.getBlockAt(x, y, z);
		block.setType(Material.SKULL);
		block.setData((byte) 1);
		Skull skull = (Skull) block.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setRotation(BlockFace.SOUTH);
		skull.setOwner(name);
		skull.update();
		block = block.getRelative(0, -1, 1);
		if(block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			sign.setLine(2, name);
			sign.update();
		}
	}
	
	private void update() {
		List<String> recent = RecentPurchaseDisplayer.getRecentCustomers();
		updateSkull(-106, 127, -178, recent.get(0));
		updateSkull(-103, 127, -178, recent.get(1));
		updateSkull(-100, 127, -178, recent.get(2));
	}
	
	@EventHandler
	public void onPurchase(PurchaseEvent event) {
		update();
	}
}
