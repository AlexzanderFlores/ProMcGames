package promcgames.gameapi.games.skywars.cages.effects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.skywars.cages.CageHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class BottomUpChangingColor extends KitBase implements Listener {
	private Map<String, List<Block>> playersBlocks = null;
	private Random random = null;
	private int y = 63;
	private byte data = 0;
	
	public BottomUpChangingColor() {
		super(new ItemCreator(Material.QUARTZ_BLOCK).setAmount(3).setName("&6Bottom Up Changing Color").setLores(new String [] {
			"&aA color will be set on your",
			"&acage starting from the bottom",
			"&aand moving to the top",
			"&aonce it fills your cage it",
			"&awill repeat",
			"",
			"&fUnlock this in &eHub Sponsors &b/vote"
		}).getItemStack(), 3);
		playersBlocks = new HashMap<String, List<Block>>();
		random = new Random();
		EventUtil.register(this);
	}

	@Override
	public String getPermission() {
		return "sky_wars_effect_bottom_up_changing_color";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		playersBlocks.put(player.getName(), CageHandler.getCage(player));
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(playersBlocks != null && random != null) {
			if(y == 63) {
				data = DyeColor.values()[random.nextInt(DyeColor.values().length)].getData();
			}
			for(List<Block> blocks : playersBlocks.values()) {
				for(Block block : blocks) {
					if(block.getY() == y) {
						block.setType(Material.STAINED_GLASS);
						block.setData(data);
					} else if(y == 63) {
						block.setType(Material.GLASS);
						block.setData((byte) 0);
					}
				}
			}
			if(++y >= 67) {
				y = 63;
			}
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
