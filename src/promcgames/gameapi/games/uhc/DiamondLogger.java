package promcgames.gameapi.games.uhc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.gameapi.games.uhc.events.DiamondMineEvent;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class DiamondLogger implements Listener {
	private Map<String, Integer> diamonds = null;
	
	public DiamondLogger() {
		diamonds = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.DIAMOND_ORE) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			if(item != null && item.getType() != Material.AIR && item.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
				return;
			}
			int amount = 0;
			if(diamonds.containsKey(player.getName())) {
				amount = diamonds.get(player.getName());
			} else {
				final String name = player.getName();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(name);
						diamonds.put(player.getName(), DB.PLAYERS_UHC_DIAMONDS.getInt("uuid", player.getUniqueId().toString(), "amount"));
					}
				});
			}
			diamonds.put(player.getName(), ++amount);
			Bukkit.getPluginManager().callEvent(new DiamondMineEvent(player, amount));
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		String name = event.getRealName();
		if(diamonds.containsKey(name)) {
			if(DB.PLAYERS_UHC_DIAMONDS.isUUIDSet(uuid)) {
				DB.PLAYERS_UHC_DIAMONDS.updateInt("amount", diamonds.get(name), "uuid", uuid.toString());
			} else {
				DB.PLAYERS_UHC_DIAMONDS.insert("'" + uuid.toString() + "', '" + diamonds.get(name) + "'");
			}
			diamonds.remove(name);
		}
	}
}
