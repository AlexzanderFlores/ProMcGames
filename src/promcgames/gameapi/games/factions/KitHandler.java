package promcgames.gameapi.games.factions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class KitHandler implements Listener {
	private String name = null;
	private Map<Ranks, Byte> bytes = null;
	
	public KitHandler() {
		name = "Daily Kits";
		bytes = new HashMap<Ranks, Byte>();
		bytes.put(Ranks.ELITE, (byte) 2);
		bytes.put(Ranks.PRO_PLUS, (byte) 3);
		bytes.put(Ranks.PRO, (byte) 5);
		new CommandBase("kit", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				open(player);
				return true;
			}
		};
		spawn(-269.5, 69, 298.5);
		spawn(-270.5, 69, 308.5);
		spawn(-264.5, 69, 314.5);
		spawn(-254.5, 69, 314.5);
		spawn(-248.5, 69, 308.5);
		spawn(-248.5, 69, 298.5);
		EventUtil.register(this);
	}
	
	private void spawn(double x, double y, double z) {
		new NPCEntity(EntityType.IRON_GOLEM, "&b" + name, new Location(Bukkit.getWorlds().get(0), x, y, z)) {
			@Override
			public void onInteract(Player player) {
				EffectUtil.playSound(player, Sound.IRONGOLEM_HIT);
				open(player);
			}
		};
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 5, name);
		Material material = Material.BEDROCK;
		if(Ranks.ELITE.hasRank(player) && canGetDailyKit(player, true)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(11, new ItemCreator(material, bytes.get(Ranks.ELITE)).setName(Ranks.ELITE.getPrefix() + "&eKit").setLores(new String [] {
			"&e+32 &aSteak",
			"&e+80 &aTNT",
			"&e+64 &aTorches",
			"&e+128 &aObsidian",
			"&e+160 &aExp Bottles",
			"&e+1 &aHorse Egg",
			"&e+1 &aSaddle",
			"&e+1 &aDiamond Horse Armor",
			"&e+1 &aProtection II Iron Helmet",
			"&e+1 &aProtection II Diamond Chestplate",
			"&e+1 &aProtection II Diamond Leggings",
			"&e+1 &aProtection II Iron Boots",
			"&e+1 &aSharpness II Diamond Sword",
			"&e+1 &aEfficiency III Unbreaking II Diamond Pickaxe",
			"&e+64 &aLogs",
			"&e+16 &aInstant Health II Splash Potions",
			"&e+3 &aSpeed II Potions",
			"&e+3 &aStrength II Potions"
		}).getItemStack());
		material = Material.BEDROCK;
		if(AccountHandler.getRank(player) == Ranks.PRO_PLUS && canGetDailyKit(player, true)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(13, new ItemCreator(material, bytes.get(Ranks.PRO_PLUS)).setName(Ranks.PRO_PLUS.getPrefix() + "&eKit").setLores(new String [] {
			"&e+32 &aSteak",
			"&e+64 &aTNT",
			"&e+64 &aTorches",
			"&e+96 &aObsidian",
			"&e+128 &aExp Bottles",
			"&e+1 &aHorse Egg",
			"&e+1 &aSaddle",
			"&e+1 &aIron Horse Armor",
			"&e+1 &aProtection I Diamond Helmet",
			"&e+1 &aProtection I Iron Chestplate",
			"&e+1 &aProtection I Iron Leggings",
			"&e+1 &aProtection I Diamond Boots",
			"&e+1 &aSharpness I Diamond Sword",
			"&e+1 &aEfficiency II Unbreaking I Diamond Pickaxe",
			"&e+64 &aLogs",
			"&e+8 &aInstant Health II Splash Potions",
			"&e+3 &aSpeed II Potions",
			"&e+2 &aStrength II Potions"
		}).getItemStack());
		material = Material.BEDROCK;
		if(AccountHandler.getRank(player) == Ranks.PRO && canGetDailyKit(player, true)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(15, new ItemCreator(material, bytes.get(Ranks.PRO)).setName(Ranks.PRO.getPrefix() + "&eKit").setLores(new String [] {
			"&e+32 &aSteak",
			"&e+32 &aTNT",
			"&e+64 &aTorches",
			"&e+32 &aObsidian",
			"&e+64 &aExp Bottles",
			"&e+1 &aHorse Egg",
			"&e+1 &aSaddle",
			"&e+1 &aFull Protection I Iron Armor",
			"&e+1 &aSharpness I Diamond Sword",
			"&e+1 &aEfficiency I Diamond Pickaxe",
			"&e+48 &aLogs",
			"&e+8 &aInstant Health I Splash Potions",
			"&e+3 &aSpeed I Potions",
			"&e+2 &aStrength I Potions"
		}).getItemStack());
		material = Material.BEDROCK;
		if(VIPHandler.isVIPPlus(player) && canGetDailyKit(player, false)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(29, new ItemCreator(material, 4).setName(VIPHandler.getVIPPlusPrefix() + " &eKit").setLores(new String [] {
			"&e+32 &aSteak",
			"&e+64 &aTNT",
			"&e+64 &aTorches",
			"&e+96 &aObsidian",
			"&e+128 &aExp Bottles",
			"&e+1 &aHorse Egg",
			"&e+1 &aSaddle",
			"&e+1 &aIron Horse Armor",
			"&e+1 &aProtection II Iron Chestplate & Leggings",
			"&e+1 &aProtection II Diamond Helmet & Boots",
			"&e+1 &aEfficiency I Unbreaking I Diamond Pickaxe",
			"&e+64 &aLogs",
			"&e+8 &aInstant Health I Splash Potions",
			"&e+3 &aSpeed I Potions",
			"&e+2 &aStrength I Potions"
		}).getItemStack());
		material = Material.BEDROCK;
		if(VIPHandler.isVIP(player) && !VIPHandler.isVIPPlus(player) && canGetDailyKit(player, false)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(31, new ItemCreator(material, 4).setName(VIPHandler.getVIPPrefix() + " &eKit").setLores(new String [] {
			"&e+32 &aSteak",
			"&e+16 &aTNT",
			"&e+64 &aTorches",
			"&e+32 &aExp Bottles",
			"&e+1 &aHorse Egg",
			"&e+1 &aFull Iron Armor",
			"&e+1 &aSharpness I Iron Sword",
			"&e+1 &aUnbreaking I Diamond Pickaxe",
			"&e+32 &aLogs",
			"&e+5 &aInstant Health I Splash Potions",
			"&e+2 &aSpeed I Potions"
		}).getItemStack());
		material = Material.BEDROCK;
		if(AccountHandler.getRank(player) == Ranks.PLAYER && !VIPHandler.isVIP(player) && canGetDailyKit(player, false)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(33, new ItemCreator(material, 7).setName("&7Default &eKit").setLores(new String [] {
			"&e+1 &aFull Iron Armor",
			"&e+1 &aDiamond Sword",
			"&e+16 &aSteak",
			"&e+16 &aLogs",
			"&e+1 &aIron Pickaxe"
		}).getItemStack());
		player.openInventory(inventory);
	}
	
	private boolean canGetDailyKit(Player player, boolean mainRank) {
		DB table = mainRank ? DB.PLAYERS_FACTIONS_MAIN_RANK_KITS : DB.PLAYERS_FACTIONS_RANK_KITS;
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		int day = -1;
		if(table.isUUIDSet(player.getUniqueId())) {
			day = table.getInt("uuid", player.getUniqueId().toString(), "day");
		}
		if(day == currentDay) {
			return false;
		}
		return true;
	}
	
	private int getOpenSlots(Player player) {
		int openSlots = 0;
		for(ItemStack item : player.getInventory().getContents()) {
			if(item == null || item.getType() == Material.AIR) {
				++openSlots;
			}
		}
		return openSlots;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			if(event.getItem().getType() == Material.BEDROCK) {
				MessageHandler.sendMessage(player, "&cYou cannot get this item at this time");
			} else {
				String item = ChatColor.stripColor(event.getItemTitle());
				int openSlots = getOpenSlots(player);
				if(item.equals("[Elite] Kit")) {
					int required = 21;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
						player.getInventory().addItem(new ItemStack(Material.TNT, 80));
						player.getInventory().addItem(new ItemStack(Material.TORCH, 64));
						player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 128));
						player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 160));
						player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (byte) 100));
						player.getInventory().addItem(new ItemStack(Material.SADDLE));
						player.getInventory().addItem(new ItemStack(Material.DIAMOND_BARDING));
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 3).addEnchantment(Enchantment.DURABILITY, 2).getItemStack());
						player.getInventory().addItem(new ItemStack(Material.LOG, 64));
						player.getInventory().addItem(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(16));
						player.getInventory().addItem(new Potion(PotionType.SPEED, 2, false).toItemStack(3));
						player.getInventory().addItem(new Potion(PotionType.STRENGTH, 2, false).toItemStack(3));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				} else if(item.equals("[Pro+] Kit")) {
					int required = 19;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
						player.getInventory().addItem(new ItemStack(Material.TNT, 64));
						player.getInventory().addItem(new ItemStack(Material.TORCH, 64));
						player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 96));
						player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 128));
						player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (byte) 100));
						player.getInventory().addItem(new ItemStack(Material.SADDLE));
						player.getInventory().addItem(new ItemStack(Material.IRON_BARDING));
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 2).addEnchantment(Enchantment.DURABILITY, 1).getItemStack());
						player.getInventory().addItem(new ItemStack(Material.LOG, 64));
						player.getInventory().addItem(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(8));
						player.getInventory().addItem(new Potion(PotionType.SPEED, 2, false).toItemStack(2));
						player.getInventory().addItem(new Potion(PotionType.STRENGTH, 2, false).toItemStack(2));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				} else if(item.equals("[Pro] Kit")) {
					int required = 16;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
						player.getInventory().addItem(new ItemStack(Material.TNT, 32));
						player.getInventory().addItem(new ItemStack(Material.TORCH, 64));
						player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 32));
						player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 32));
						player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (byte) 100));
						player.getInventory().addItem(new ItemStack(Material.SADDLE));
						player.getInventory().addItem(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 1).getItemStack());
						player.getInventory().addItem(new ItemStack(Material.LOG, 48));
						player.getInventory().addItem(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(8));
						player.getInventory().addItem(new Potion(PotionType.SPEED, 1, false).toItemStack(3));
						player.getInventory().addItem(new Potion(PotionType.STRENGTH, 1, false).toItemStack(2));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_MAIN_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				} else if(item.equals("[VIP+] Kit")) {
					int required = 16;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
						player.getInventory().addItem(new ItemStack(Material.TNT, 32));
						player.getInventory().addItem(new ItemStack(Material.TORCH, 64));
						player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 32));
						player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 32));
						player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (byte) 100));
						player.getInventory().addItem(new ItemStack(Material.SADDLE));
						player.getInventory().addItem(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 1).addEnchantment(Enchantment.DURABILITY, 1).getItemStack());
						player.getInventory().addItem(new ItemStack(Material.LOG, 48));
						player.getInventory().addItem(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(8));
						player.getInventory().addItem(new Potion(PotionType.SPEED, 1, false).toItemStack(3));
						player.getInventory().addItem(new Potion(PotionType.STRENGTH, 1, false).toItemStack(2));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				} else if(item.equals("[VIP] Kit")) {
					int required = 13;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
						player.getInventory().addItem(new ItemStack(Material.TNT, 16));
						player.getInventory().addItem(new ItemStack(Material.TORCH, 64));
						player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 32));
						player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, 1, (byte) 100));
						player.getInventory().addItem(new ItemStack(Material.IRON_HELMET));
						player.getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE));
						player.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS));
						player.getInventory().addItem(new ItemStack(Material.IRON_BOOTS));
						player.getInventory().addItem(new ItemCreator(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).getItemStack());
						player.getInventory().addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DURABILITY, 1).getItemStack());
						player.getInventory().addItem(new ItemStack(Material.LOG, 32));
						player.getInventory().addItem(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(5));
						player.getInventory().addItem(new Potion(PotionType.SPEED, 1, false).toItemStack(2));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				} else if(item.equals("Default Kit")) {
					int required = 8;
					if(openSlots >= required) {
						player.getInventory().addItem(new ItemStack(Material.IRON_HELMET));
						player.getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE));
						player.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS));
						player.getInventory().addItem(new ItemStack(Material.IRON_BOOTS));
						player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
						player.getInventory().addItem(new ItemStack(Material.LOG, 16));
						player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						if(DB.PLAYERS_FACTIONS_RANK_KITS.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_FACTIONS_RANK_KITS.updateInt("day", day, "uuid", player.getUniqueId().toString());
						} else {
							DB.PLAYERS_FACTIONS_RANK_KITS.insert("'" + player.getUniqueId().toString() + "', '" + day + "'");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + required + " &cfree slots in your inventory for this kit");
					}
				}
			}
			event.setCancelled(true);
			player.closeInventory();
		}
	}
}
