package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.games.survivalgames.events.RestockChestEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.StringUtil;

public class ChestHandler implements Listener {
	private List<Block> oppenedChests = null;
	private List<Material> possibleItems = null;
	private List<Material> tierOneItems = null;
	public static ItemStack speedBall = null;
	
	private enum Rarity {
		COMMON, UNCOMMON, RARE
	}
	
	public ChestHandler() {
		oppenedChests = new ArrayList<Block>();
		possibleItems = new ArrayList<Material>();
		speedBall = new ItemCreator(Material.FIREWORK_CHARGE).setName("&fSpeed Ball &6(Click)").getItemStack();
		addItem(Material.GOLD_HELMET, Rarity.COMMON);
		addItem(Material.GOLD_CHESTPLATE, Rarity.COMMON);
		addItem(Material.GOLD_LEGGINGS, Rarity.COMMON);
		addItem(Material.GOLD_BOOTS, Rarity.COMMON);
		addItem(Material.MELON_BLOCK, Rarity.COMMON);
		addItem(Material.WOOD_SWORD, Rarity.COMMON);
		addItem(Material.GOLD_AXE, Rarity.COMMON);
		addItem(Material.GOLD_SWORD, Rarity.COMMON);
		addItem(Material.STONE_AXE, Rarity.COMMON);
		addItem(Material.STONE_SWORD, Rarity.COMMON);
		addItem(Material.EXP_BOTTLE, Rarity.COMMON);
		addItem(Material.FISHING_ROD, Rarity.COMMON);
		addItem(Material.RAW_FISH, Rarity.COMMON);
		addItem(Material.COOKED_FISH, Rarity.COMMON);
		addItem(Material.COOKIE, Rarity.COMMON);
		addItem(Material.PORK, Rarity.COMMON);
		addItem(Material.GRILLED_PORK, Rarity.COMMON);
		addItem(Material.RAW_BEEF, Rarity.COMMON);
		addItem(Material.COOKED_CHICKEN, Rarity.COMMON);
		addItem(Material.BAKED_POTATO, Rarity.COMMON);
		addItem(Material.BREAD, Rarity.COMMON);
		addItem(Material.APPLE, Rarity.COMMON);
		addItem(Material.PUMPKIN_PIE, Rarity.COMMON);
		addItem(Material.CARROT_ITEM, Rarity.COMMON);
		addItem(Material.CAKE, Rarity.COMMON);
		addItem(Material.ARROW, Rarity.COMMON);
		addItem(Material.FLINT, Rarity.COMMON);
		addItem(Material.STICK, Rarity.COMMON);
		addItem(Material.FEATHER, Rarity.COMMON);
		addItem(Material.GOLD_INGOT, Rarity.COMMON);
		addItem(Material.CHAINMAIL_HELMET, Rarity.UNCOMMON);
		addItem(Material.CHAINMAIL_CHESTPLATE, Rarity.UNCOMMON);
		addItem(Material.CHAINMAIL_LEGGINGS, Rarity.UNCOMMON);
		addItem(Material.CHAINMAIL_BOOTS, Rarity.UNCOMMON);
		addItem(Material.IRON_AXE, Rarity.UNCOMMON);
		addItem(Material.WEB, Rarity.UNCOMMON);
		addItem(Material.TNT, Rarity.UNCOMMON);
		addItem(Material.FIREWORK_CHARGE, Rarity.RARE);
		addItem(Material.BOW, Rarity.RARE);
		addItem(Material.STONE_SWORD, Rarity.RARE);
		addItem(Material.FLINT_AND_STEEL, Rarity.RARE);
		addItem(Material.GOLDEN_CARROT, Rarity.RARE);
		addItem(Material.IRON_HELMET, Rarity.RARE);
		addItem(Material.IRON_LEGGINGS, Rarity.RARE);
		addItem(Material.IRON_BOOTS, Rarity.RARE);
		addItem(Material.IRON_INGOT, Rarity.RARE);
		tierOneItems = new ArrayList<Material>();
		tierOneItems.add(Material.IRON_HELMET);
		tierOneItems.add(Material.IRON_CHESTPLATE);
		tierOneItems.add(Material.IRON_LEGGINGS);
		tierOneItems.add(Material.IRON_BOOTS);
		tierOneItems.add(Material.DIAMOND);
		tierOneItems.add(Material.IRON_INGOT);
		tierOneItems.add(Material.GOLDEN_APPLE);
		EventUtil.register(this);
	}
	
	private void addItem(Material material, Rarity rarity) {
		for(int a = 0; a < (rarity == Rarity.COMMON ? 4 : rarity == Rarity.UNCOMMON ? 3 : rarity == Rarity.RARE ? 2 : 1); ++a) {
			possibleItems.add(material);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(!SurvivalGames.getChestRestockEnabled()) {
			OneSecondTaskEvent.getHandlerList().unregister(this);
			return;
		}
		if(ProMcGames.getMiniGame().getCounter() == (60 * 10)) {
			oppenedChests.clear();
			EffectUtil.playSound(Sound.CHEST_OPEN);
			MessageHandler.alert("All chests have been refilled");
		}
	}
	
	@EventHandler
	public void onRestockChest(RestockChestEvent event) {
		if(oppenedChests != null && ProMcGames.getMiniGame() != null && SurvivalGames.getChestRestockEnabled()) {
			oppenedChests.remove(event.getChest().getBlock());
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		InventoryType type = event.getInventory().getType();
		if(type != InventoryType.CHEST && type != InventoryType.WORKBENCH && type != InventoryType.ENCHANTING) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(oppenedChests != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !SpectatorHandler.contains(event.getPlayer())) {
			Block block = event.getClickedBlock();
			if((block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) && !oppenedChests.contains(block)) {
				if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
					oppenedChests.add(block);
					Chest chest = (Chest) block.getState();
					chest.getInventory().clear();
					Random random = ProMcGames.getProPlugin().random;
					boolean tierOne = TieringHandler.tierOne.contains(block.getLocation());
					if(tierOne) {
						MessageHandler.sendMessage(event.getPlayer(), StringUtil.color("&4&k&l||| &bYou Found a Tier One Chest &4&l&k|||"));
					}
					int numberOfTimes = random.nextInt(tierOne ? 3 : 7) + 2;
					for(int a = 0; a < numberOfTimes; ++a) {
						Material type = null;
						if(tierOne) {
							type = tierOneItems.get(random.nextInt(tierOneItems.size()));
						} else {
							type = possibleItems.get(random.nextInt(possibleItems.size()));
						}
						ItemStack itemStack = new ItemStack(type);
						if(!tierOne && (type == Material.ARROW || type == Material.GOLD_INGOT || type == Material.STICK || type == Material.FEATHER || type.isEdible())) {
							if(type == Material.ARROW) {
								itemStack = new ItemStack(type, random.nextInt(5) + 3);
							} else {
								itemStack = new ItemStack(type, random.nextInt(2) + 2);
							}
						} else if(type == Material.FIREWORK_CHARGE) {
							itemStack = speedBall;
						}
						if(chest instanceof DoubleChest) {
							DoubleChest doubleChest = (DoubleChest) chest;
							DoubleChestInventory inventory = (DoubleChestInventory) doubleChest.getInventory();
							inventory.setItem(random.nextInt(inventory.getSize()), itemStack);
						} else {
							chest.getInventory().setItem(random.nextInt(chest.getInventory().getSize()), itemStack);
						}
					}
				}
			}
		}
		ItemStack item = event.getItem();
		if(item != null && ItemUtil.isItem(item, speedBall)) {
			Player player = event.getPlayer();
			if(player.hasPotionEffect(PotionEffectType.SPEED)) {
				player.removePotionEffect(PotionEffectType.SPEED);
				MessageHandler.sendMessage(player, "Speed " + ChatColor.RED + "OFF");
			} else {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
				MessageHandler.sendMessage(player, "Speed " + ChatColor.YELLOW + "ON");
			}
		}
	}
}
