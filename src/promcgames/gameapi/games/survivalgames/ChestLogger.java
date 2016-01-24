package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.player.ChestOpenEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ChestLogger implements Listener {
	private List<Block> chests = null;
	private static Map<String, List<Block>> opened = null;
	
	public ChestLogger() {
		chests = new ArrayList<Block>();
		opened = new HashMap<String, List<Block>>();
		EventUtil.register(this);
	}
	
	public static int getNumberOfChestsOpened(Player player) {
		return opened == null || !opened.containsKey(Disguise.getName(player)) ? -1 : opened.get(Disguise.getName(player)).size();
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Block block : chests) {
					String map = block.getWorld().getName();
					String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
					String [] keys = new String [] {"map_name", "location"};
					String [] values = new String [] {map, location};
					if(DB.NETWORK_SG_CHESTS.isKeySet(keys, values)) {
						int opened = DB.NETWORK_SG_CHESTS.getInt(keys, values, "times_opened") + 1;
						DB.NETWORK_SG_CHESTS.updateInt("times_opened", opened, keys, values);
					} else {
						DB.NETWORK_SG_CHESTS.insert("'" + map + "', '" + location + "', '1'");
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer()) && ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block block = event.getClickedBlock();
				Material type = block.getType();
				if(block.getRelative(0, 1, 0).getType() == Material.AIR && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) {
					if(!chests.contains(event.getClickedBlock())) {
						chests.add(event.getClickedBlock());
					}
					Player player = event.getPlayer();
					List<Block> chestsOpened = new ArrayList<Block>();
					if(opened.containsKey(Disguise.getName(event.getPlayer()))) {
						chestsOpened = opened.get(Disguise.getName(event.getPlayer()));
					}
					if(!chestsOpened.contains(block)) {
						chestsOpened.add(block);
						opened.put(Disguise.getName(event.getPlayer()), chestsOpened);
						Bukkit.getPluginManager().callEvent(new ChestOpenEvent(player, block));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(opened.containsKey(Disguise.getName(event.getPlayer()))) {
			opened.get(Disguise.getName(event.getPlayer())).clear();
			opened.remove(Disguise.getName(event.getPlayer()));
		}
	}
}
