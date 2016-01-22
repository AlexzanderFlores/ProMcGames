package promcgames.server.world;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.Parkour;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;

public class AngryBob {
	private List<String> delayed = null;
	
	public AngryBob(Location location) {
		delayed = new ArrayList<String>();
		new NPCEntity(EntityType.IRON_GOLEM, "&c&lAngry Bob", location) {
			@Override
			public void onInteract(Player player) {
				if(Parkour.isParkouring(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot use this while playing parkour");
				} else if(!delayed.contains(player.getName())){
					final String name = player.getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * 5);
					EffectUtil.playSound(player, Sound.IRONGOLEM_HIT);
					player.setVelocity(player.getLocation().getDirection().multiply(-4.0));
				}
			}
		};
	}
}
