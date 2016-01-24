package promcgames.server.servers.uhc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

public class WaterBucket extends HubItemBase implements Listener {
	private static WaterBucket instance = null;
	private Map<String, Integer> players = null;
	
	public WaterBucket() {
		super(new ItemCreator(Material.WATER_BUCKET).setName("&bMLG Water Bucket"), 2);
		players = new HashMap<String, Integer>();
		instance = this;
	}
	
	public static WaterBucket getInstance() {
		if(instance == null) {
			new WaterBucket();
		}
		return instance;
	}
	
	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}
	
	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(players.containsKey(player.getName())) {
				
			} else {
				Inventory inventory = Bukkit.createInventory(player, 9, getName());
				int [] blocks = new int [] {50, 100, 150, 200, 250};
				int [] slots = new int [] {0, 2, 4, 6, 8};
				for(int a = 0; a < blocks.length; ++a) {
					int amount = a + 1;
					inventory.setItem(slots[a], new ItemCreator(Material.WATER_BUCKET).setAmount(amount).setName("&aJump from &e" + blocks[a] + " &ablocks").getItemStack());
				}
				player.openInventory(inventory);
				event.setCancelled(true);
			}
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItemTitle()).split("Jump from ")[1].split(" blocks")[0];
			int blocks = Integer.valueOf(name);
			switch(blocks) {
			case 50:
				player.teleport(new Location(player.getWorld(), 37.5, 57, 13.5, -270.0f, 50.0f));
				break;
			case 100:
				player.teleport(new Location(player.getWorld(), 37.5, 107, 15.5, -270.0f, 50.0f));
				break;
			case 150:
				player.teleport(new Location(player.getWorld(), 37.5, 157, 17.5, -270.0f, 50.0f));
				break;
			case 200:
				player.teleport(new Location(player.getWorld(), 37.5, 207, 19.5, -270.0f, 50.0f));
				break;
			case 250:
				player.teleport(new Location(player.getWorld(), 37.5, 256, 21.5, -270.0f, 50.0f));
				break;
			}
			players.put(player.getName(), blocks);
			if(player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			Player player = (Player) event.getEntity();
			if(players.containsKey(player.getName())) {
				remove(player);
				MessageHandler.sendMessage(player, "&cYou failed to " + getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if(players.containsKey(player.getName()) && event.getBucket() == Material.WATER_BUCKET) {
			int blocks = players.get(player.getName());
			remove(player);
			if(event.getBlockClicked().getLocation().getBlockY() == 5) {
				MessageHandler.sendMessage(player, "&eYou have successfully " + getName() + "ed &efrom &b" + blocks + " &eblocks!");
				final String name = player.getName();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(name);
						if(player != null) {
							giveItem(player);
						}
					}
				}, 5);
			} else {
				MessageHandler.sendMessage(player, "&cYou failed to " + getName());
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	private void remove(Player player) {
		if(players.containsKey(player.getName())) {
			players.remove(player.getName());
			if(Ranks.PRO.hasRank(player)) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}
	}
}
