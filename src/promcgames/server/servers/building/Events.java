package promcgames.server.servers.building;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		event.getWorld().setGameRuleValue("doDaylightCycle", "false");
		event.getWorld().setTime(6000);
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		if(event.getBlock().getType() == Material.FIRE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getBlock().getType() != Material.NETHERRACK) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "save-all");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(ChatColor.YELLOW + event.getPlayer().getName() + " joined the server");
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		event.setLeaveMessage(ChatColor.YELLOW + event.getPlayer().getName() + " left the server");
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		if(event.getMessage().toLowerCase().contains("/mv import ")) {
			String worldName = event.getMessage().toLowerCase().replace("/mv import ", "");
			worldName = worldName.replace(" normal", "");
			worldName = worldName.replace(" nether", "");
			worldName = worldName.replace(" end", "");
			final String world = worldName;
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					event.getPlayer().chat("/mvtp " + world);
				}
			}, 5);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
			Block block = event.getClickedBlock();
			MessageHandler.sendMessage(event.getPlayer(), "Type: " + block.getType().toString());
			MessageHandler.sendMessage(event.getPlayer(), "Data: " + block.getData());
		}
	}
	
	@EventHandler
	public void onBlockGrow(BlockSpreadEvent event) {
		if(event.getNewState().getType() == Material.VINE) {
			event.setCancelled(true);
		}
	}
}
