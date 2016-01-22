package promcgames.gameapi.games.factions.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.factions.CoinHandler;
import promcgames.gameapi.games.factions.spawn.SpawnHandler.WorldLocation;
import promcgames.player.MessageHandler;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class ShopHandler implements Listener {
	public ShopHandler() {
		EventUtil.register(this);
	}
	
	private void process(Player player, Action action, Sign sign, ItemStack buying) {
		if(buying != null && buying.getType() != Material.AIR) {
			String name = sign.getLine(0);
			String buy = sign.getLine(1);
			String sell = sign.getLine(2);
			String quantity = sign.getLine(3);
			if(action == Action.LEFT_CLICK_BLOCK) {
				if(buy.contains("$")) {
					int price = Integer.valueOf(buy.split("\\$")[1]);
					int coins = CoinHandler.getCoins(player);
					if(coins >= price) {
						int amount = Integer.valueOf(quantity.split(" ")[1]);
						Material type = buying.getType();
						boolean stackable = amount > 1 || type.isEdible() || type.isBlock();
						if(type.toString().contains("INGOT")) {
							stackable = true;
						}
						if(type == Material.EMERALD || type == Material.DIAMOND) {
							stackable = true;
						}
						boolean gave = false;
						for(int a = 0; a < player.getInventory().getContents().length; ++a) {
							ItemStack item = player.getInventory().getItem(a);
							if(item == null) {
								item = new ItemStack(Material.AIR);
							}
							boolean canStack = item.getType() == type && stackable;
							if(item.getType() == Material.AIR || canStack) {
								if(canStack && item.getAmount() + amount > item.getType().getMaxStackSize()) {
									continue;
								}
								if(buying.getType() == Material.MOB_SPAWNER) {
									buying = new ItemCreator(Material.MOB_SPAWNER).setName("&r" + name + " Spawner").getItemStack();
								}
								player.getInventory().addItem(new ItemCreator(buying).setAmount(amount).getItemStack());
								gave = true;
								break;
							}
						}
						if(gave) {
							CoinHandler.addCoins(player, price * -1);
							MessageHandler.sendMessage(player, "You purchased &b" + name + " &7(&cx" + amount + "&7)");
							EffectUtil.playSound(player, Sound.LEVEL_UP);
						} else {
							MessageHandler.sendMessage(player, "&cYou do not have room in your inventory for this item");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have enough Coins");
						MessageHandler.sendMessage(player, "Get &e50 &afree coins daily by voting! &b/vote");
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou cannot buy this item");
				}
			} else {
				if(player.isOp()) Bukkit.getLogger().info("8");
				ItemStack item = player.getItemInHand();
				if(item == null || item.getType() != buying.getType() || item.getData().getData() != buying.getData().getData()) {
					if(player.isOp()) Bukkit.getLogger().info("9");
					MessageHandler.sendMessage(player, "&cYou must be holding the item you are trying to sell");
				} else if(sell.contains("$")) {
					if(player.isOp()) Bukkit.getLogger().info("10");
					int amount = Integer.valueOf(quantity.split(" ")[1]);
					if(item.getAmount() >= amount) {
						if(player.isOp()) Bukkit.getLogger().info("11");
						item.setAmount(item.getAmount() - amount);
						if(item.getAmount() <= 0) {
							player.setItemInHand(new ItemStack(Material.AIR));
						}
						int price = Integer.valueOf(sell.split("\\$")[1]);
						CoinHandler.addCoins(player, price);
						MessageHandler.sendMessage(player, "You sold &b" + name + " &7(&cx" + amount + "&7)");
						EffectUtil.playSound(player, Sound.LEVEL_UP);
					} else {
						if(player.isOp()) Bukkit.getLogger().info("12");
						MessageHandler.sendMessage(player, "&cYou do not have enough of this item to sell it");
					}
				} else {
					if(player.isOp()) Bukkit.getLogger().info("13");
					MessageHandler.sendMessage(player, "&cYou cannot sell this item");
				}
			}
		}
	}
	
	/*1
	 2
	 3
	 6
	 8
	 9
	 7*/
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getPlayer().isOp()) Bukkit.getLogger().info("1");
		Block block = event.getClickedBlock();
		if(block == null) {
			if(event.getPlayer().isOp()) Bukkit.getLogger().info("a");
		}
		if(block != null && block.getType() != Material.WALL_SIGN) {
			if(event.getPlayer().isOp()) Bukkit.getLogger().info("b");
		}
		if(block != null && SpawnHandler.getSpawnLevel(block.getLocation()) != WorldLocation.SAFEZONE) {
			if(event.getPlayer().isOp()) Bukkit.getLogger().info("c");
		}
		if(block != null && block.getType() == Material.WALL_SIGN && SpawnHandler.getSpawnLevel(block.getLocation()) == WorldLocation.SAFEZONE) {
			if(event.getPlayer().isOp()) Bukkit.getLogger().info("2");
			ItemFrame itemFrame = null;
			int x1 = block.getX();
			int y1 = block.getY() + 1;
			int z1 = block.getZ();
			for(Entity entity : block.getWorld().getEntities()) {
				if(entity instanceof ItemFrame) {
					Location location = entity.getLocation();
					int x2 = location.getBlockX();
					int y2 = location.getBlockY();
					int z2 = location.getBlockZ();
					if(x1 == x2 && y1 == y2 && z1 == z2) {
						if(event.getPlayer().isOp()) Bukkit.getLogger().info("3");
						itemFrame = (ItemFrame) entity;
						break;
					}
				}
			}
			Player player = event.getPlayer();
			ItemStack buying = null;
			if(itemFrame == null) {
				if(event.getPlayer().isOp()) Bukkit.getLogger().info("4");
				org.bukkit.material.Sign sign = (org.bukkit.material.Sign) block.getState().getData();
				Block spawner = block.getRelative(sign.getAttachedFace()).getRelative(0, 1, 0);
				if(spawner.getType() == Material.MOB_SPAWNER) {
					if(event.getPlayer().isOp()) Bukkit.getLogger().info("5");
					buying = new ItemStack(spawner.getType());
				}
			} else {
				if(event.getPlayer().isOp()) Bukkit.getLogger().info("6");
				buying = itemFrame.getItem();
			}
			process(player, event.getAction(), (Sign) block.getState(), buying);
			event.setCancelled(true);
			if(event.getPlayer().isOp()) Bukkit.getLogger().info("7");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof ItemFrame && event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			if(SpawnHandler.getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE) {
				ItemFrame itemFrame = (ItemFrame) event.getEntity();
				if(itemFrame != null) {
					Block block = itemFrame.getLocation().getBlock().getRelative(0, -1, 0);
					if(block.getType() == Material.WALL_SIGN) {
						Sign sign = (Sign) block.getState();
						process(player, Action.LEFT_CLICK_BLOCK, sign, itemFrame.getItem());
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame) {
			Player player = event.getPlayer();
			if(SpawnHandler.getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE) {
				ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
				if(itemFrame != null) {
					Block block = itemFrame.getLocation().getBlock().getRelative(0, -1, 0);
					if(block.getType() == Material.WALL_SIGN) {
						Sign sign = (Sign) block.getState();
						process(player, Action.RIGHT_CLICK_BLOCK, sign, itemFrame.getItem());
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
