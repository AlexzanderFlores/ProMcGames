package promcgames.gameapi.games.survivalgames.deathmatch;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import promcgames.customevents.game.DeathmatchStartEvent;
import promcgames.gameapi.SpawnPointHandler;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.FileHandler;

public class DeathmatchHandler implements Listener {
	private static boolean enabled = false;
	
	public DeathmatchHandler() {
		EventUtil.register(this);
		String worldName = "Deathmatch";
		File deathmatchWorld = new File(Bukkit.getWorldContainer().getPath() + "/" + worldName);
		if(deathmatchWorld.exists()) {
			FileHandler.delete(deathmatchWorld);
		}
		File deathmatchResource = new File(Bukkit.getWorldContainer().getPath() + "/../resources/maps/" + worldName);
		FileHandler.copyFolder(deathmatchResource, deathmatchWorld);
		World world = Bukkit.createWorld(new WorldCreator(worldName));
		List<Player> players = ProPlugin.getPlayers();
		if(players.size() <= 4) {
			List<Location> cageSpawns = null;
			/*ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/authors.yml");
			if(config.getFile().exists() && config.getConfig().contains("authors")) {
				if(config.getConfig().getString("authors").contains("ChaosMocha")) {*/
					cageSpawns = Arrays.asList(
						new Location(world, -44.5, 102, 0.5, -90.0f, 0.0f), new Location(world, 0.5, 102, 44.5, -180.0f, 0.0f),
						new Location(world, 44.5, 102, 0.5, -280.0f, 0.0f), new Location(world, 0.5, 102, -44.5, 0.0f, 0.0f)
					);
				/*} else {
					cageSpawns = Arrays.asList(
						new Location(world, -53.5, 4.0, 0.5, -90.0f, -16.8f), new Location(world, 0.5, 4.0, 54.5, -180.0f, -17.69f),
						new Location(world, 54.5, 4.0, 0.5, -270.0f, -22.19f), new Location(world, 0.5, 4.0, -53.5, 0.0f, 16.65f)
					);
				}
			}*/
			int counter = 0;
			for(Player player : ProPlugin.getPlayers()) {
				player.teleport(cageSpawns.get(counter++));
			}
			new CageHandler(world);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : ProPlugin.getPlayers()) {
						player.teleport(player);
					}
				}
			}, 20);
		} else {
			List<Location> spawns = new SpawnPointHandler(world).getSpawns();
			for(int a = 0; a < players.size(); ++a) {
				players.get(a).teleport(spawns.get(a));
			}
			players = null;
			new DeathmatchStartingHandler();
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(SpectatorHandler.contains(player)) {
				player.teleport(world.getSpawnLocation().add(0, 5, 0));
			} else {
				player.eject();
				player.getInventory().remove(Material.ENDER_PEARL);
			}
		}
		enabled = true;
		Bukkit.getPluginManager().callEvent(new DeathmatchStartEvent(world));
	}
	
	public static boolean isRunning() {
		return enabled;
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.ENDER_PEARL) {
			MessageHandler.sendMessage(event.getPlayer(), "&cCannot use ender pearls during deathmatch");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Material material = event.getBlock().getType();
		if((material == Material.MELON_BLOCK || material == Material.CAKE) && event.getBlock().getLocation().getY() >= 8) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot place this block at this height");
			event.setCancelled(true);
		}
	}
}
