package promcgames.server.servers.hub.items.cosmetic.proplus;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class FlyCartItem extends HubItemBase {
	private static HubItemBase instance = null;
	private List<Vehicle> vehicles = null;
	
	public FlyCartItem() {
		super(new ItemCreator(Material.MINECART).setName(Ranks.PRO_PLUS.getColor() + "Flycart"), 4);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				Vehicle vehicle = (Vehicle) player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.MINECART);
				vehicle.setPassenger(player);
				if(vehicles == null) {
					vehicles = new ArrayList<Vehicle>();
				}
				vehicles.add(vehicle);
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if(vehicles != null && vehicles.contains(event.getVehicle())) {
			event.setCollisionCancelled(false);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		if(vehicles != null && vehicles.contains(event.getVehicle())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if(vehicles != null && vehicles.contains(event.getVehicle())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		if(vehicles != null) {
			try {
				Iterator<Vehicle> list = vehicles.iterator();
				while(list.hasNext()) {
					Vehicle vehicle = list.next();
					if(vehicle.getPassenger() != null && vehicle.getPassenger() instanceof Player) {
						Player player = (Player) vehicle.getPassenger();
						vehicle.setVelocity(player.getLocation().getDirection());
					} else {
						vehicles.remove(vehicle);
						vehicle.remove();
						list.remove();
					}
				}
			} catch(ConcurrentModificationException e) {
				
			}
		}
	}
}
