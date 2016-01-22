package promcgames.gameapi.games.factions.spawn.crates;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import promcgames.server.DB;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

public class PremiumCrate extends CrateBase {
	public PremiumCrate() {
		super(DB.PLAYERS_FACTIONS_PREMIUM_CRATES, new Location(Bukkit.getWorlds().get(0), -234, 67, 274), "&cYou do not have any Premium Crate keys! &b/buy");
		hologram.appendTextLine(StringUtil.color("&bPremium Crate"));
		hologram.appendTextLine(StringUtil.color("&eGet one with &b/buy"));
	}
	
	@Override
	public void interact(Player player) {
		addItem(new ItemStack(Material.MONSTER_EGG, 10, (byte) 50));
		addItem(new ItemStack(Material.GOLD_INGOT, 5000));
		addItem(new ItemStack(Material.GOLDEN_APPLE, 1, (byte) 1));
		addItem(new ItemStack(Material.DIAMOND, 16));
		addItem(new ItemStack(Material.IRON_INGOT, 48));
		addItem(new ItemStack(Material.EXP_BOTTLE, 300));
		addItem(new ItemStack(Material.MONSTER_EGG, 5, (byte) 50));
		addItem(new ItemStack(Material.GOLDEN_APPLE, 3));
		addItem(new ItemStack(Material.GOLD_INGOT, 3750));
		addItem(new ItemStack(Material.EXP_BOTTLE, 150));
		addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 3).addEnchantment(Enchantment.DURABILITY, 2).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_AXE).addEnchantment(Enchantment.DIG_SPEED, 2).addEnchantment(Enchantment.DURABILITY, 1).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemStack(Material.EXP_BOTTLE, 32));
		addItem(new ItemStack(Material.GOLD_INGOT, 2500));
		addItem(new ItemCreator(Material.IRON_SWORD).addEnchantment(Enchantment.FIRE_ASPECT, 2).addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3).getItemStack());
		addItem(new ItemStack(Material.OBSIDIAN, 128));
		addItem(new ItemStack(Material.EXP_BOTTLE, 16));
		addItem(new ItemStack(Material.GOLD_INGOT, 1000));
		addItem(new ItemStack(Material.SULPHUR, 64));
		addItem(new ItemStack(Material.TNT, 64));
		addItem(new ItemStack(Material.OBSIDIAN, 64));
		addItem(new ItemStack(Material.SULPHUR, 32));
		addItem(new ItemStack(Material.BOOK, 32));
		addItem(new ItemStack(Material.GOLD_INGOT, 500));
		addItem(new ItemStack(Material.BOOKSHELF, 64));
		addItem(new ItemStack(Material.GOLD_INGOT, 250));
		addItem(new ItemStack(Material.BOOKSHELF, 32));
		addItem(new ItemStack(Material.GOLD_INGOT, 100));
		super.interact(player);
	}
}
