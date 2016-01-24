package promcgames.server.servers.hub.items.cosmetic.pro;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader.Perk;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class ArmorSelectorItem extends HubItemBase {
	private static HubItemBase instance = null;
	private static List<String> rainbowArmorEnabled = null;
	public static enum ArmorTypes {RAINBOW, DIAMOND, IRON, CHAINMAIL, GOLD, LEATHER, NONE}
	
	public ArmorSelectorItem() {
		super(new ItemCreator(Material.DIAMOND_CHESTPLATE).setName(Ranks.PRO.getColor() + "Armor Selector"), 2);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PRO.hasRank(player)) {
			PerkLoader.addPerkToQueue(player, Perk.ARMOR);
		}
	}
	
	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO.hasRank(player)) {
				Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.stripColor(getName()));
				inventory.addItem(new ItemCreator(Material.TNT).setName("&cNo Armor").getItemStack());
				inventory.addItem(new ItemCreator(getRandomlyColoredArmor()[2]).setName("&aRainbow Armor").getItemStack());
				inventory.addItem(new ItemCreator(Material.DIAMOND_CHESTPLATE).setName("&aDiamond Armor").getItemStack());
				inventory.addItem(new ItemCreator(Material.IRON_CHESTPLATE).setName("&aIron Armor").getItemStack());
				inventory.addItem(new ItemCreator(Material.CHAINMAIL_CHESTPLATE).setName("&aChain Armor").getItemStack());
				inventory.addItem(new ItemCreator(Material.GOLD_CHESTPLATE).setName("&aGold Armor").getItemStack());
				inventory.addItem(new ItemCreator(Material.LEATHER_CHESTPLATE).setName("&aLeather Armor").getItemStack());
				player.openInventory(inventory);
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
			}
			if(event.getClickType() == ClickType.RIGHT_CLICK) {
				final String name = player.getName();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						ProPlugin.getPlayer(name).updateInventory();
					}
				});
			}
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
			ArmorTypes armorType = null;
			if(name.startsWith("No Armor")) {
				armorType = ArmorTypes.NONE;
				final UUID uuid = player.getUniqueId();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.HUB_ARMOR.isUUIDSet(uuid)) {
							DB.HUB_ARMOR.deleteUUID(uuid);
						}
					}
				});
			} else if(name.startsWith("Rainbow Armor")) {
				if(Ranks.ELITE.hasRank(player)) {
					armorType = ArmorTypes.RAINBOW;
				} else {
					MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
				}
			} else if(name.startsWith("Diamond Armor")) {
				armorType = ArmorTypes.DIAMOND;
			} else if(name.startsWith("Iron Armor")) {
				armorType = ArmorTypes.IRON;
			} else if(name.startsWith("Chain Armor")) {
				armorType = ArmorTypes.CHAINMAIL;
			} else if(name.startsWith("Gold Armor")) {
				armorType = ArmorTypes.GOLD;
			} else if(name.startsWith("Leather Armor")) {
				armorType = ArmorTypes.LEATHER;
			}
			if(armorType != null) {
				applyArmor(player, armorType);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(rainbowArmorEnabled != null && !rainbowArmorEnabled.isEmpty()) {
			for(String name : rainbowArmorEnabled) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null && !PlayerAFKEvent.isAFK(player)) {
					player.getInventory().setArmorContents(getRandomlyColoredArmor());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(rainbowArmorEnabled != null) {
			rainbowArmorEnabled.remove(event.getPlayer().getName());
		}
	}
	
	public static void applyArmor(Player player, ArmorTypes type) {
		boolean removeRainbow = true;
		if(type != ArmorTypes.NONE) {
			final String finalType = type.toString();
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(DB.HUB_ARMOR.isUUIDSet(uuid)) {
						DB.HUB_ARMOR.updateString("armor_type", finalType, "uuid", uuid.toString());
					} else {
						DB.HUB_ARMOR.insert("'" + uuid.toString() + "', '" + finalType + "'");
					}
				}
			});
		}
		if(type == ArmorTypes.RAINBOW) {
			player.getInventory().setArmorContents(getRandomlyColoredArmor());
			if(rainbowArmorEnabled == null) {
				rainbowArmorEnabled = new ArrayList<String>();
			}
			rainbowArmorEnabled.add(player.getName());
			removeRainbow = false;
		} else if(type == ArmorTypes.DIAMOND) {
			player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
			player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
			player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
			player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		} else if(type == ArmorTypes.IRON) {
			player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
			player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
		} else if(type == ArmorTypes.CHAINMAIL) {
			player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
			player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
			player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
			player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
		} else if(type == ArmorTypes.GOLD) {
			player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
			player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
			player.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
			player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));
		} else if(type == ArmorTypes.LEATHER) {
			player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
			player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
			player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
			player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
		} else if(type == ArmorTypes.NONE) {
			player.getInventory().setHelmet(new ItemStack(Material.AIR));
			player.getInventory().setChestplate(new ItemStack(Material.AIR));
			player.getInventory().setLeggings(new ItemStack(Material.AIR));
			player.getInventory().setBoots(new ItemStack(Material.AIR));
		}
		if(rainbowArmorEnabled != null && removeRainbow) {
			rainbowArmorEnabled.remove(player.getName());
		}
	}
	
	public static ItemStack [] getRandomlyColoredArmor() {
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);
		return new ItemStack [] {
			ItemUtil.applyColorToArmor(new ItemStack(Material.LEATHER_BOOTS), red, green, blue),
			ItemUtil.applyColorToArmor(new ItemStack(Material.LEATHER_LEGGINGS), red, green, blue),
			ItemUtil.applyColorToArmor(new ItemStack(Material.LEATHER_CHESTPLATE), red, green, blue),
			ItemUtil.applyColorToArmor(new ItemStack(Material.LEATHER_HELMET), red, green, blue)
		};
	}
}
