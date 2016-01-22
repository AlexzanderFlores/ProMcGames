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
import promcgames.customevents.timed.FifteenTickTaskEvent;
import promcgames.gameapi.games.skywars.cages.CageHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class RapidlyChangingRandomColor extends KitBase implements Listener {
	private Map<String, List<Block>> playersBlocks = null;
	private Random random = null;
	
	public RapidlyChangingRandomColor() {
		super(new ItemCreator(Material.QUARTZ_BLOCK).setAmount(5).setName("&6Rapidly Changing Random Color").setLores(new String [] {
			"&aYour whole cage will rapidly",
			"&achange into random colors",
			"",
			"&fUnlock this in &eHub Sponsors &b/vote"
		}).getItemStack(), 5);
		playersBlocks = new HashMap<String, List<Block>>();
		random = new Random();
		EventUtil.register(this);
	}

	@Override
	public String getPermission() {
		return "sky_wars_effect_rapidly_changing_random_color";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		playersBlocks.put(player.getName(), CageHandler.getCage(player));
		for(Block block : playersBlocks.get(player.getName())) {
			block.setType(Material.STAINED_GLASS);
		}
	}
	
	@EventHandler
	public void onFifteenTickTask(FifteenTickTaskEvent event) {
		if(playersBlocks != null && random != null) {
			for(List<Block> blocks : playersBlocks.values()) {
				for(Block block : blocks) {
					byte data = DyeColor.values()[random.nextInt(DyeColor.values().length)].getData();
					block.setData(data);
				}
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
