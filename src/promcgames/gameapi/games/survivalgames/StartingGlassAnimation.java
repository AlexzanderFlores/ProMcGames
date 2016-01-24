package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.server.ProMcGames;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class StartingGlassAnimation implements Listener {
	private int counter = 0;
	private MiniGame miniGame = null;
	private List<BlockState> originalBlocks = null;
	private List<Block> blocks = null;
	private Block lastSet = null;
	private static enum StainedGlassColor {
		WHITE(0),
		ORANGE(1),
		YELLOW(4),
		RED(14),
		BLACK(15),
		GREEN(5);
		
		private byte data = 0;
		
		private StainedGlassColor(int data) {
			this.data = (byte) data;
		}
		
		public byte getData() {
			return this.data;
		}
	}
	
	public StartingGlassAnimation(List<Location> spawns) {
		miniGame = ProMcGames.getMiniGame();
		originalBlocks = new ArrayList<BlockState>();
		blocks = new ArrayList<Block>();
		for(Location spawn : spawns) {
			spawn.getBlock().setType(Material.AIR);
			Block block = spawn.add(0, -1, 0).getBlock();
			originalBlocks.add(block.getState());
			blocks.add(block);
		}
		for(Block block : blocks) {
			block.setType(Material.STAINED_GLASS);
			block.setData(DyeColor.WHITE.getData());
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(miniGame.getCounter() > 5) {
			if(lastSet != null) {
				lastSet.setData(DyeColor.WHITE.getData());
			}
			lastSet = blocks.get(counter++);
			lastSet.setData(DyeColor.RED.getData());
			if(counter >= blocks.size()) {
				counter = 0;
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		int counter = miniGame.getCounter() + 1;
		if(counter == 5) {
			setAllGlass(StainedGlassColor.WHITE);
		} else if(counter == 4) {
			setAllGlass(StainedGlassColor.ORANGE);
		} else if(counter == 3) {
			setAllGlass(StainedGlassColor.YELLOW);
		} else if(counter == 2) {
			setAllGlass(StainedGlassColor.RED);
		} else if(counter == 1) {
			setAllGlass(StainedGlassColor.BLACK);
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		setAllGlass(StainedGlassColor.GREEN);
		HandlerList.unregisterAll(this);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(int a = 0; a < blocks.size(); ++a) {
					blocks.get(a).setType(originalBlocks.get(0).getType());
					blocks.get(a).setData(originalBlocks.get(0).getData().getData());
				}
				originalBlocks.clear();
				originalBlocks = null;
				blocks.clear();
				blocks = null;
			}
		}, 20 * 3);
	}
	
	private void setAllGlass(StainedGlassColor color) {
		for(Block block : blocks) {
			block.setData(color.getData());
		}
	}
}
