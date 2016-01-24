package promcgames.gameapi.games.contention.projectilepath;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ProjectilePath implements Listener {
	private Map<String, ArcEffect> effects = null;
	
	public ProjectilePath() {
		effects = new HashMap<String, ArcEffect>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : ProPlugin.getPlayers()) {
					ArcEffect effect = effects.get(player.getName());
					ItemStack itemStack = player.getItemInHand();
					if(itemStack != null && itemStack.getType() == Material.SNOW_BALL) {
						if(effect == null) {
							effect = new ArcEffect();
						}
						effect.run(player);
					} else if(effect != null) {
						effects.remove(player.getName());
					}
				}
			}
		});
	}
}
