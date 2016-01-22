package promcgames.gameapi.games.uhc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EventUtil;

public class SurfaceHandler implements Listener {
	private CountDownUtil countDown = null;
	
	public SurfaceHandler() {
		if(!HostedEvent.isEvent()) {
			countDown = new CountDownUtil(60 * 2);
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(countDown.canDisplay()) {
			MessageHandler.alert("Teleporting players to surface in " + countDown.getCounterAsString());
		}
		countDown.decrementCounter();
		if(countDown.getCounter() <= 0) {
			OneSecondTaskEvent.getHandlerList().unregister(this);
			for(Player player : ProPlugin.getPlayers()) {
				if(player.getWorld().getName().equals(WorldHandler.getWorld().getName()) && player.getLocation().getY() <= 55) {
					player.teleport(WorldHandler.getGround(player.getLocation()));
				}
			}
		}
	}
}
