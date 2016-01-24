package promcgames.gameapi.games.survivalgames.mapeffects;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitTask;

import promcgames.ProMcGames;
import promcgames.customevents.game.GameStartEvent;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Highway extends MapEffectsBase implements Listener {
	private BukkitTask task = null;
	private int y = 79;
	
	public Highway() {
		super("Highway");
		EventUtil.register(this);
	}

	@Override
	public void execute(final World world) {
		final long delays [] = {2, 15};
		cancel();
		task = Bukkit.getScheduler().runTaskTimer(ProMcGames.getInstance(), new Runnable() {
			@Override
			public void run() {
				new Location(world, 45, y--, 46).getBlock().setType(Material.FENCE);
				if(y < 70) {
					cancel();
					Bukkit.getScheduler().scheduleSyncDelayedTask(ProMcGames.getInstance(), new Runnable() {
						@Override
						public void run() {
							ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/chests.yml");
							for(String key : config.getConfig().getKeys(false)) {
								int x = config.getConfig().getInt(key + ".x");
								int y = config.getConfig().getInt(key + ".y");
								int z = config.getConfig().getInt(key + ".z");
								FallingBlock fallingChest = world.spawnFallingBlock(new Location(world, x, y, z), Material.CHEST, (byte) 0);
								fallingChest.setDropItem(false);
							}
							Bukkit.getScheduler().scheduleSyncDelayedTask(ProMcGames.getInstance(), new Runnable() {
								@Override
								public void run() {
									task = Bukkit.getScheduler().runTaskTimer(ProMcGames.getInstance(), new Runnable() {
										@Override
										public void run() {
											new Location(world, 45, y++, 46).getBlock().setType(Material.AIR);
											if(y > 79) {
												cancel();
											}
										}
									}, 1, delays[0]);
								}
							}, delays[1]);
						}
					}, delays[1]);
				}
			}
		}, 1, delays[0]);
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		EffectUtil.playSound(Sound.WOOD_CLICK);
		EffectUtil.playEffect(Effect.MOBSPAWNER_FLAMES, event.getBlock().getLocation());
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		HandlerList.unregisterAll(this);
	}
	
	private void cancel() {
		if(task != null) {
			task.cancel();
			task = null;
		}
	}
}
