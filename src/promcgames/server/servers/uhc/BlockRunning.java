package promcgames.server.servers.uhc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

public class BlockRunning extends HubItemBase implements Listener {
	private static BlockRunning instance = null;
	
	public BlockRunning() {
		super(new ItemCreator(Material.COBBLESTONE).setName("&aBlock Running"), 3);
		new NPCEntity(EntityType.SKELETON, "&bBlock Running", new Location(Bukkit.getWorlds().get(0), 0.5, 6, -57.5)) {
			@Override
			public void onInteract(Player player) {
				ItemStack item = player.getInventory().getItem(4);
				if(item != null && item.getType() == Material.COBBLESTONE) {
					player.getInventory().setItem(4, new ItemStack(Material.AIR));
					MessageHandler.sendMessage(player, "&cYou are no longer playing \"&bBlock Running&c\"");
				} else {
					player.getInventory().setItem(4, new ItemStack(Material.COBBLESTONE, 64));
					MessageHandler.sendMessage(player, "You are now playing \"&bBlock Running&a\"");
				}
			}
		};
		instance = this;
	}
	
	public static BlockRunning getInstance() {
		if(instance == null) {
			new BlockRunning();
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
			player.teleport(new Location(player.getWorld(), 0.5, 6, -55.5, -180.0f, 0.0f));
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getItem().getType() == Material.COBBLESTONE) {
			event.getPlayer().closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getBlock().getType() == Material.COBBLESTONE && !isItem(event.getPlayer())) {
			if(event.getBlock().getZ() <= -85) {
				final int x = event.getBlock().getX();
				final int y = event.getBlock().getY();
				final int z = event.getBlock().getZ();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Block block = Bukkit.getWorlds().get(0).getBlockAt(x, y, z);
						if(x >= 1 && x <= 7 && y == 5 && z <= -86 && z >= -187) {
							block.setType(Material.STATIONARY_WATER);
						} else {
							block.setType(Material.AIR);
						}
					}
				}, 20 * 1);
				event.getPlayer().getInventory().addItem(new ItemStack(Material.COBBLESTONE));
				event.setCancelled(false);
			} else {
				MessageHandler.sendMessage(event.getPlayer(), "&cYou cannot place this here, go down the path &enorth &cof spawn");
			}
		}
	}
}
