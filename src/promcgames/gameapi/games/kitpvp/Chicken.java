package promcgames.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.kitpvp.trophies.ChickenHunt;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EventUtil;

public class Chicken implements Listener {
	private List<String> clicked = null;
	
	public Chicken(double x, double y, double z) {
		new NPCEntity(EntityType.CHICKEN, null, new Location(Bukkit.getWorlds().get(0), x, y, z)) {
			@Override
			public void onInteract(Player player) {
				if(!clicked.contains(player.getName())) {
					clicked.add(player.getName());
					ChickenHunt.getInstance().setAchieved(player);
				}
			}
		};
		clicked = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		clicked.remove(event.getPlayer().getName());
	}
}
