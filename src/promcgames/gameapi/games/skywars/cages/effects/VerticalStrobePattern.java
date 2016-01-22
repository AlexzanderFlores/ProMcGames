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
public class VerticalStrobePattern extends KitBase implements Listener {
	private Map<String, List<Block>> playersBlocks = null;
	private Random random = null;
	private int y = 63;
	
	public VerticalStrobePattern() {
		super(new ItemCreator(Material.QUARTZ_BLOCK).setAmount(6).setName("&6Vertical Strobe Pattern").setLores(new String [] {
			"&aA strobe of a solid color",
			"&agoes from the bottom of your cage",
			"&ato the top",
			"",
			"&fUnlock this in &eHub Sponsors &b/vote"
		}).getItemStack(), 6);
		playersBlocks = new HashMap<String, List<Block>>();
		random = new Random();
		EventUtil.register(this);
	}

	@Override
	public String getPermission() {
		return "sky_wars_effect_vertical_strobe_pattern";
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
			for(List<Block> blocks : playersBlocks.values()) {
				byte data = DyeColor.values()[random.nextInt(DyeColor.values().length)].getData();
				for(Block block : blocks) {
					if(block.getY() == y) {
						block.setType(Material.STAINED_GLASS);
						block.setData(data);
					} else {
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
