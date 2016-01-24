package promcgames.gameapi.games.skywars.cages.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.gameapi.games.skywars.islands.IslandHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class TopRowMovingLight extends KitBase implements Listener {
	private Map<String, List<Block>> playersBlocks = null;
	private Random random = null;
	private int counter = 0;
	
	public TopRowMovingLight() {
		super(new ItemCreator(Material.QUARTZ_BLOCK).setName("&6Top Row Moving Light").setLores(new String [] {
			"&aA light will scroll through",
			"&athe top row of your cage",
			"",
			"&fUnlock this in &eHub Sponsors &b/vote"
		}).getItemStack(), 1);
		playersBlocks = new HashMap<String, List<Block>>();
		random = new Random();
		EventUtil.register(this);
	}

	@Override
	public String getPermission() {
		return "sky_wars_effect_top_row_moving_light";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		int index = ProPlugin.getPlayers().indexOf(player) + 1;
		World world = player.getWorld();
		Block spawn = IslandHandler.getLocation(world, index).getBlock();
		Block min = spawn.getRelative(-3, -1, -3);
		int y = 66;
		int x1 = min.getX();
		int z1 = min.getZ();
		int x2 = x1 + 6;
		int z2 = z1 + 6;
		List<Block> blocks = new ArrayList<Block>();
		for(; x1 < x2; ++x1) {
			blocks.add(world.getBlockAt(x1, y, z1));
		}
		for(; z1 < z2; ++z1) {
			blocks.add(world.getBlockAt(x1, y, z1));
		}
		for(; x1 > min.getX(); --x1) {
			blocks.add(world.getBlockAt(x1, y, z1));
		}
		for(; z1 > min.getZ(); --z1) {
			blocks.add(world.getBlockAt(x1, y, z1));
		}
		playersBlocks.put(player.getName(), blocks);
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		if(playersBlocks != null && random != null) {
			for(List<Block> blocks : playersBlocks.values()) {
				if(counter > blocks.size()) {
					counter = 0;
				}
				for(int a = 0; a < blocks.size(); ++a) {
					Block block = blocks.get(a);
					if(a == counter) {
						if(block.getType() == Material.STAINED_GLASS) {
							for(Block blockTwo : blocks) {
								blockTwo.setType(Material.GLASS);
								blockTwo.setData((byte) 0);
							}
						}
						byte data = DyeColor.values()[random.nextInt(DyeColor.values().length)].getData();
						block.setType(Material.STAINED_GLASS);
						block.setData(data);
					}
				}
			}
			++counter;
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(String name : playersBlocks.keySet()) {
			playersBlocks.get(name).clear();
			playersBlocks.put(name, null);
		}
		playersBlocks.clear();
		playersBlocks = null;
		random = null;
		HandlerList.unregisterAll(this);
	}
}
