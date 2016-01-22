package promcgames.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.player.ChestOpenEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.UnicodeUtil;

public class ChestHandler implements Listener {
	private List<Block> oppenedChests = null;
	private List<Material> giveOnce = null;
	private Map<String, Integer> chestsOpened = null;
	private Map<Integer, List<ItemStack>> possibleItems = null;
	private Map<String, List<Material>> alreadyGotten = null;
	private ItemStack cloud = null;
	//private boolean cloudEnabled = false;
	private static int powerLevel = 1;
	
	private enum Rarity {
		COMMON, UNCOMMON, RARE
	}
	
	public ChestHandler() {
		oppenedChests = new ArrayList<Block>();
		giveOnce = new ArrayList<Material>();
		chestsOpened = new HashMap<String, Integer>();
		possibleItems = new HashMap<Integer, List<ItemStack>>();
		alreadyGotten = new HashMap<String, List<Material>>();
		if(new Random().nextInt(100) <= 25) {
			//cloudEnabled = true;
		}
		cloud = new ItemCreator(Material.QUARTZ).setName("&bCloud Spawner &7(Click if you fall)").getItemStack();
		// Items to give to players once
		giveOnce.add(Material.LEATHER_BOOTS);
		giveOnce.add(Material.LEATHER_LEGGINGS);
		giveOnce.add(Material.LEATHER_CHESTPLATE);
		giveOnce.add(Material.LEATHER_HELMET);
		giveOnce.add(Material.STONE_SWORD);
		giveOnce.add(Material.IRON_AXE);
		giveOnce.add(Material.STONE_PICKAXE);
		giveOnce.add(Material.FISHING_ROD);
		giveOnce.add(Material.LAVA_BUCKET);
		giveOnce.add(Material.IRON_PICKAXE);
		giveOnce.add(Material.GOLD_BOOTS);
		giveOnce.add(Material.GOLD_LEGGINGS);
		giveOnce.add(Material.GOLD_CHESTPLATE);
		giveOnce.add(Material.GOLD_HELMET);
		giveOnce.add(Material.CHAINMAIL_BOOTS);
		giveOnce.add(Material.CHAINMAIL_LEGGINGS);
		giveOnce.add(Material.CHAINMAIL_CHESTPLATE);
		giveOnce.add(Material.CHAINMAIL_HELMET);
		giveOnce.add(Material.IRON_SWORD);
		giveOnce.add(Material.DIAMOND_PICKAXE);
		giveOnce.add(Material.DIAMOND_AXE);
		giveOnce.add(Material.IRON_BOOTS);
		giveOnce.add(Material.IRON_LEGGINGS);
		giveOnce.add(Material.IRON_CHESTPLATE);
		giveOnce.add(Material.IRON_HELMET);
		giveOnce.add(Material.DIAMOND_SWORD);
		giveOnce.add(Material.DIAMOND_BOOTS);
		giveOnce.add(Material.DIAMOND_LEGGINGS);
		giveOnce.add(Material.DIAMOND_CHESTPLATE);
		giveOnce.add(Material.DIAMOND_HELMET);
		// Power level 1
		addItem(Material.LEATHER_BOOTS, 1, Rarity.COMMON);
		addItem(Material.LEATHER_LEGGINGS, 1, Rarity.COMMON);
		addItem(Material.LEATHER_CHESTPLATE, 1, Rarity.COMMON);
		addItem(Material.LEATHER_HELMET, 1, Rarity.COMMON);
		addItem(Material.COOKED_BEEF, 1, Rarity.COMMON);
		addItem(Material.GRILLED_PORK, 1, Rarity.COMMON);
		addItem(Material.STONE_SWORD, 1, Rarity.COMMON);
		addItem(Material.IRON_AXE, 1, Rarity.COMMON);
		addItem(Material.STONE_PICKAXE, 1, Rarity.COMMON);
		addItem(Material.WOOD, 8, 1, Rarity.COMMON);
		addItem(Material.WOOD, 16, 1, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 8, 1, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 16, 1, Rarity.COMMON);
		addItem(Material.GOLD_INGOT, 4, 1, Rarity.UNCOMMON);
		addItem(Material.FISHING_ROD, 1, Rarity.UNCOMMON);
		addItem(Material.WATER_BUCKET, 1, Rarity.UNCOMMON);
		addItem(Material.LAVA_BUCKET, 1, Rarity.UNCOMMON);
		addItem(Material.EGG, 16, 1, Rarity.UNCOMMON);
		addItem(Material.SNOW_BALL, 16, 1, Rarity.UNCOMMON);
		addItem(Material.GOLD_BOOTS, 1, Rarity.UNCOMMON);
		addItem(Material.GOLD_LEGGINGS, 1, Rarity.UNCOMMON);
		addItem(Material.GOLD_CHESTPLATE, 1, Rarity.UNCOMMON);
		addItem(Material.GOLD_HELMET, 1, Rarity.UNCOMMON);
		addItem(Material.IRON_PICKAXE, 1, Rarity.RARE);
		addItem(Material.CHAINMAIL_BOOTS, 1, Rarity.RARE);
		addItem(Material.CHAINMAIL_LEGGINGS, 1, Rarity.RARE);
		addItem(Material.CHAINMAIL_CHESTPLATE, 1, Rarity.RARE);
		addItem(Material.CHAINMAIL_HELMET, 1, Rarity.RARE);
		addItem(Material.WEB, 4, 1, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, 1, Rarity.RARE);
		// Power level 2
		addItem(Material.IRON_SWORD, 2, Rarity.COMMON);
		//addItem(new ItemCreator(Material.STONE_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack(), 2, Rarity.COMMON);
		addItem(Material.DIAMOND_AXE, 2, Rarity.COMMON);
		addItem(Material.WOOD, 8, 2, Rarity.COMMON);
		addItem(Material.WOOD, 16, 2, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 8, 2, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 16, 2, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 5, 2, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, 10, 2, Rarity.COMMON);
		addItem(Material.WATER_BUCKET, 2, Rarity.UNCOMMON);
		addItem(Material.LAVA_BUCKET, 2, Rarity.UNCOMMON);
		addItem(Material.FISHING_ROD, 2, Rarity.UNCOMMON);
		addItem(Material.ARROW, 2, Rarity.UNCOMMON);
		addItem(Material.BOW, 2, Rarity.UNCOMMON);
		addItem(Material.COOKED_BEEF, 2, Rarity.UNCOMMON);
		addItem(Material.GRILLED_PORK, 2, Rarity.UNCOMMON);
		addItem(Material.EGG, 32, 2, Rarity.UNCOMMON);
		addItem(Material.SNOW_BALL, 32, 2, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_PICKAXE, 2, Rarity.UNCOMMON);
		addItem(Material.IRON_BOOTS, 2, Rarity.UNCOMMON);
		addItem(Material.IRON_LEGGINGS, 2, Rarity.UNCOMMON);
		addItem(Material.IRON_HELMET, 2, Rarity.UNCOMMON);
		addItem(Material.IRON_CHESTPLATE, 2, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, 2, Rarity.RARE);
		addItem(Material.GOLDEN_APPLE, 3, Rarity.RARE);
		// Power level 3
		addItem(Material.WOOD, 8, 3, Rarity.COMMON);
		addItem(Material.WOOD, 16, 3, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 8, 3, Rarity.COMMON);
		addItem(Material.COBBLESTONE, 16, 3, Rarity.COMMON);
		addItem(Material.DIAMOND_SWORD, 3, Rarity.COMMON);
		addItem(Material.ENDER_PEARL, 3, Rarity.COMMON);
		addItem(Material.GOLDEN_APPLE, 3, Rarity.COMMON);
		addItem(Material.ARROW, 3, Rarity.UNCOMMON);
		//addItem(new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_DAMAGE).getItemStack(), 3, Rarity.UNCOMMON);
		//addItem(new ItemCreator(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack(), 3, Rarity.COMMON);
		addItem(Material.IRON_BOOTS, 3, Rarity.COMMON);
		addItem(Material.IRON_LEGGINGS, 3, Rarity.COMMON);
		addItem(Material.IRON_CHESTPLATE, 3, Rarity.COMMON);
		addItem(Material.IRON_HELMET, 3, Rarity.COMMON);
		addItem(Material.WATER_BUCKET, 3, Rarity.UNCOMMON);
		addItem(Material.LAVA_BUCKET, 3, Rarity.UNCOMMON);
		addItem(Material.FISHING_ROD, 3, Rarity.UNCOMMON);
		addItem(Material.EGG, 32, 3, Rarity.UNCOMMON);
		addItem(Material.SNOW_BALL, 32, 3, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_BOOTS, 3, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_LEGGINGS, 3, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_CHESTPLATE, 3, Rarity.UNCOMMON);
		addItem(Material.DIAMOND_HELMET, 3, Rarity.UNCOMMON);
		//addItem(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack(), 3, Rarity.RARE);
		//addItem(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack(), 3, Rarity.RARE);
		//addItem(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack(), 3, Rarity.RARE);
		//addItem(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack(), 3, Rarity.RARE);
		addItem(Material.COOKED_BEEF, 3, Rarity.RARE);
		addItem(Material.GRILLED_PORK, 3, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, 3, Rarity.RARE);
		EventUtil.register(this);
	}
	
	public static int getPowerLevel() {
		return powerLevel;
	}
	
	private void addItem(Material material, int powerLevel, Rarity rarity) {
		addItem(material, 1, powerLevel, rarity);
	}
	
	private void addItem(Material material, int amount, int powerLevel, Rarity rarity) {
		addItem(material, amount, 0, powerLevel, rarity);
	}
	
	private void addItem(Material material, int amount, int data, int powerLevel, Rarity rarity) {
		addItem(new ItemStack(material, amount, (byte) data), powerLevel, rarity);
	}
	
	private void addItem(ItemStack itemStack, int powerLevel, Rarity rarity) {
		List<ItemStack> items = possibleItems.get(powerLevel);
		if(items == null) {
			items = new ArrayList<ItemStack>();
		}
		for(int a = 0; a < (rarity == Rarity.COMMON ? 4 : rarity == Rarity.UNCOMMON ? 3 : rarity == Rarity.RARE ? 2 : 1); ++a) {
			items.add(itemStack);
		}
		possibleItems.put(powerLevel, items);
	}
	
	private String getPowerString() {
		String block = UnicodeUtil.getUnicode(2588);
		return (powerLevel == 1 ? "&a" + block + " &c" + block + " " + block :
			powerLevel == 2 ? "&a" + block + " " + block + " &c" + block :
				"&a" + block + " " + block + " " + block);
	}
	
	private void updateScoreboard() {
		ProMcGames.getSidebar().removeScore(-6);
		ProMcGames.getSidebar().setText(new String [] {
			"  ",
			"&eChest Power:",
			getPowerString()
		}, -4);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		updateScoreboard();
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			int counter = ProMcGames.getMiniGame().getCounter();
			if(counter == 240 || counter == 360) {
				++powerLevel;
				oppenedChests.clear();
				for(String name : alreadyGotten.keySet()) {
					alreadyGotten.get(name).clear();
				}
				alreadyGotten.clear();
				MessageHandler.alertLine("&6");
				MessageHandler.alert("");
				MessageHandler.alert("&6Chests have been restocked");
				MessageHandler.alert("&6Chest Power Level: " + getPowerString());
				MessageHandler.alert("");
				MessageHandler.alertLine("&6");
				updateScoreboard();
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(oppenedChests != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !SpectatorHandler.contains(event.getPlayer())) {
			Block block = event.getClickedBlock();
			if((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) && !oppenedChests.contains(block)) {
				oppenedChests.add(block);
				Player player = event.getPlayer();
				Bukkit.getPluginManager().callEvent(new ChestOpenEvent(player, block));
				Chest chest = (Chest) block.getState();
				chest.getInventory().clear();
				Random random = ProMcGames.getProPlugin().random;
				int numberOfTimes = 0;
				if(getPowerLevel() == 1) {
					numberOfTimes = random.nextInt(5) + 5;
				} else if(getPowerLevel() == 2) {
					numberOfTimes = random.nextInt(3) + 4;
				} else if(getPowerLevel() == 3) {
					numberOfTimes = random.nextInt(2) + 3;
				}
				List<ItemStack> items = possibleItems.get(getPowerLevel());
				for(int a = 0; a < numberOfTimes; ++a) {
					ItemStack itemStack = null;
					Material type = null;
					do {
						itemStack = items.get(random.nextInt(items.size()));
						type = itemStack.getType();
						if((itemStack.getEnchantments() == null || itemStack.getEnchantments().isEmpty()) && giveOnce.contains(type)) {
							List<Material> got = alreadyGotten.get(player.getName());
							if(got == null) {
								got = new ArrayList<Material>();
								alreadyGotten.put(player.getName(), got);
							}
							if(!got.contains(type)) {
								got.add(type);
								alreadyGotten.put(player.getName(), got);
								break;
							}
						} else {
							break;
						}
					} while(true);
					if(type == Material.ARROW || type.isEdible()) {
						if(type == Material.ARROW) {
							itemStack = new ItemStack(type, random.nextInt(10) + 10);
						} else if(type != Material.GOLDEN_APPLE) {
							itemStack = new ItemStack(type, random.nextInt(5) + 5);
						}
					}
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), itemStack);
				}
				int counter = 0;
				if(chestsOpened.containsKey(player.getName())) {
					counter = chestsOpened.get(player.getName());
				}
				chestsOpened.put(player.getName(), ++counter);
				if(counter == 1) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_PICKAXE));
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.STONE_SWORD));
				} else if(counter == 2) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.WOOD, 32));
				} else if(counter == 3) {
					chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), new ItemStack(Material.SNOW_BALL, 16));
				}
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(ItemUtil.isItem(item, cloud)) {
			
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material type = event.getEntity().getItemStack().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Material type = event.getBlock().getType();
		if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
			Player player = event.getPlayer();
			player.setItemInHand(new ItemStack(Material.AIR));
			MessageHandler.sendMessage(player, "&cYou cannot place chests");
			event.setCancelled(true);
		}
	}
}
