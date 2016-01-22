package promcgames.gameapi.games.arcade.games.dragonraces;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import promcgames.server.util.EventUtil;
import promcgames.server.util.Region;

public class ShortcutHandler implements Listener {
	private Region start = null;
	private Region end = null;
	
	public ShortcutHandler(World world) {
		EventUtil.register(this);
		start = new Region(world, -112, 72, 100, -89, 101, 74);
		end = new Region(world, 81, 69, 113, 52, 97, 94);
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if(event.getEntity() instanceof Fireball && event.blockList().size() > 0 && event.blockList().get(0).getType() == Material.ICE) {
			if(start.isIn(event.blockList().get(0).getLocation())) {
				EntityExplodeEvent.getHandlerList().unregister(this);
				for(Block block: start.getBlocks(Material.WATER)) {
					block.setType(Material.AIR);
				}
				for(Block block: start.getBlocks(Material.ICE)) {
					block.setType(Material.AIR);
				}
				for(Block block: end.getBlocks(Material.WATER)) {
					block.setType(Material.AIR);
				}
				for(Block block: end.getBlocks(Material.ICE)) {
					block.setType(Material.AIR);
				}
				start = null;
				end = null;
			}
		}
	}
}
