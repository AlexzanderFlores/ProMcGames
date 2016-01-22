package promcgames.server;

import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

public class AntiAboveNether implements Listener {
	public AntiAboveNether() {
		EventUtil.register(this);
	}
	
	//@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getTo().getWorld().getEnvironment() == Environment.NETHER && event.getTo().getBlockY() >= 127) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot be at this height in the nether");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getWorld().getEnvironment() == Environment.NETHER && player.getLocation().getY() >= 127) {
				//player.damage(2.0d);
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&cYou cannot be at this height in the nether, this will be patched soon. Please move all items out of here ASAP");
				MessageHandler.sendMessage(player, "");
			}
		}
	}
	
	//@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getWorld().getEnvironment() == Environment.NETHER && event.getBlock().getY() >= 127) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot build at this height in the nether");
			event.setCancelled(true);
		}
	}
	
	//@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(event.getPlayer().getWorld().getEnvironment() == Environment.NETHER && event.getItemDrop().getLocation().getY() >= 127) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot build at this height in the nether");
			event.setCancelled(true);
		}
	}
}
