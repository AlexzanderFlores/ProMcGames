package promcgames.gameapi.games.survivalgames.deathmatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.bossbar.BossBar;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class CageHandler extends CountDownUtil implements Listener {
	private Map<Integer, List<Location>> bars = null;
	
	public CageHandler(World world) {
		bars = new HashMap<Integer, List<Location>>();
		ProMcGames.getSidebar().update("&aCages Raising");
		for(int a = 0; a < 4; ++a) {
			List<Location> levels = new ArrayList<Location>();
			levels.add(new Location(world, -1, 102 + a, -38));
			levels.add(new Location(world, 0, 102 + a, -38));
			levels.add(new Location(world, 1, 102 + a, -38));
			levels.add(new Location(world, 38, 102 + a, -1));
			levels.add(new Location(world, 38, 102 + a, 0));
			levels.add(new Location(world, 38, 102 + a, 1));
			levels.add(new Location(world, 1, 102 + a, 38));
			levels.add(new Location(world, 0, 102 + a, 38));
			levels.add(new Location(world, -1, 102 + a, 38));
			levels.add(new Location(world, -38, 102 + a, 1));
			levels.add(new Location(world, -38, 102 + a, 0));
			levels.add(new Location(world, -38, 102 + a, -1));
			bars.put(a, levels);
		}
		final CageHandler instance = this;
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				EventUtil.register(instance);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onOneSecond(OneSecondTaskEvent event) {
		if(getCounter() >= 3) {
			HandlerList.unregisterAll(this);
			new DeathmatchStartedHandler();
			for(int a = 0; a < bars.keySet().size(); ++a) {
				bars.get(a).clear();
			}
			bars.clear();
			bars = null;
		} else {
			EffectUtil.playSound(Sound.ANVIL_USE);
			for(Location bar : bars.get(getCounter())) {
				bar.getBlock().setType(Material.AIR);
			}
			incrementCounter();
		}
		String message = "&cDeathmatch Cages Raising";
		for(int a = 0; a < getCounter(); ++a) {
			message += " .";
		}
		BossBar.display(message);
	}
}
