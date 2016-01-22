package promcgames.anticheat.killaura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.anticheat.AntiGamingChair;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.server.PerformanceHandler;
import promcgames.server.util.EventUtil;

public class DamageInInventoryDetector extends AntiGamingChair implements Listener {
	private List<String> inInventory = null;
	private Map<String, Integer> damagesWhileInInventory = null;
	private Map<String, Integer> wouldBan = null;
	
	public DamageInInventoryDetector() {
		super("Kill Aura-I");
		inInventory = new ArrayList<String>();
		damagesWhileInInventory = new HashMap<String, Integer>();
		wouldBan = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private void remove(String name) {
		inInventory.remove(name);
		damagesWhileInInventory.remove(name);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(isEnabled() && !event.isCancelled() && !inInventory.contains(event.getPlayer().getName())) {
			inInventory.add(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			if(damager.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && notIgnored(damager) && inInventory.contains(damager.getName())) {
				int times = 0;
				if(damagesWhileInInventory.containsKey(damager.getName())) {
					times = damagesWhileInInventory.get(damager.getName());
				}
				if(++times >= 5) {
					int counter = 0;
					if(wouldBan.containsKey(damager.getName())) {
						counter = wouldBan.get(damager.getName());
					}
					if(++counter >= 2) {
						ban(damager);
					} else {
						wouldBan.put(damager.getName(), counter);
					}
				} else {
					damagesWhileInInventory.put(damager.getName(), times);
				}
			} else if(PerformanceHandler.getPing(damager) > getMaxPing()) {
				inInventory.remove(damager.getName());
				damagesWhileInInventory.remove(damager.getName());
				wouldBan.remove(damager.getName());
			}
		}
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		if(isEnabled()) {
			damagesWhileInInventory.clear();
			wouldBan.clear();
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(isEnabled()) {
			remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			double x1 = event.getFrom().getX();
			double y1 = event.getFrom().getY();
			double z1 = event.getFrom().getZ();
			double x2 = event.getTo().getX();
			double y2 = event.getTo().getY();
			double z2 = event.getTo().getZ();
			if(x1 != x2 && y1 != y2 && z1 != z2) {
				remove(event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			remove(event.getPlayer().getName());
		}
	}
}
