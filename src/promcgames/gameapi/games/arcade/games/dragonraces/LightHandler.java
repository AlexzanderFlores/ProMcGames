package promcgames.gameapi.games.arcade.games.dragonraces;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameStartEvent;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.Region;

@SuppressWarnings("deprecation")
public class LightHandler implements Listener {
	private static Region top = null;
	private static Region bottom = null;
	
	public LightHandler(World world) {
		top = new Region(world, -217, 87, 105, -207, 97, 102);
		bottom = new Region(world, -217, 86, 105, -207, 76, 102);
		EventUtil.register(this);
		for(Block block: top.getBlocks(Material.WOOL)) {
			block.setData(DyeColor.ORANGE.getData());
		}
		for(Block block: bottom.getBlocks(Material.WOOL)) {
			block.setData(DyeColor.GREEN.getData());
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Block block: top.getBlocks(Material.WOOL)) {
			block.setData(DyeColor.RED.getData());
		}
		for(Block block: bottom.getBlocks(Material.WOOL)) {
			block.setData(DyeColor.LIME.getData());
		}
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Block block: top.getBlocks()) {
					block.setType(Material.AIR);
				}
				for(Block block: bottom.getBlocks()) {
					block.setType(Material.AIR);
				}
				top = null;
				bottom = null;
			}
		}, 20);
	}
}
