package promcgames.gameapi.games.survivalgames.mapeffects;

import java.util.ArrayList;
import java.util.List;

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

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class BasicFallingChests extends MapEffectsBase implements Listener {
	private World arena = null;
	private ConfigurationUtil config = null;
	private List<Location> locations = null;
	
	public BasicFallingChests() {
		super(null);
	}
	
	@Override
	public void execute(World arena) {
		this.arena = arena;
		config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + arena.getName() + "/chests.yml");
		locations = new ArrayList<Location>();
		for(String key : config.getConfig().getKeys(false)) {
			int x = config.getConfig().getInt(key + ".x");
			int y = config.getConfig().getInt(key + ".y");
			int z = config.getConfig().getInt(key + ".z");
			locations.add(new Location(arena, x, y, z));
		}
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(!locations.isEmpty()) {
			FallingBlock fallingBlock = arena.spawnFallingBlock(locations.get(0), Material.CHEST, (byte) 0);
			fallingBlock.setDropItem(false);
			locations.remove(0);
		}
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
		locations = null;
	}
}
