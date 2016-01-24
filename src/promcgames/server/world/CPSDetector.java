package promcgames.server.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class CPSDetector implements Listener {
	private Map<String, Integer> clicks = null;
	private List<String> delayed = null;
	
	public CPSDetector(Location location) {
		clicks = new HashMap<String, Integer>();
		delayed = new ArrayList<String>();
		new NPCEntity(EntityType.SKELETON, "&b&lCPS Detector &c&l(CLICK)", location) {
			@Override
			public void onInteract(Player player) {
				int click = 0;
				if(clicks.containsKey(player.getName())) {
					click = clicks.get(player.getName());
				}
				clicks.put(player.getName(), ++click);
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(final String name : clicks.keySet()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, "&eCPS registered: &c" + clicks.get(name));
				if(!delayed.contains(name)) {
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * 10);
					MessageHandler.sendMessage(player, "&eNote: &aThis is what the server registers. It may not be your true CPS");
				}
			}
		}
		clicks.clear();
	}
}
