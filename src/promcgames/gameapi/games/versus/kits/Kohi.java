package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.Potion.Tier;
import org.bukkit.potion.PotionType;

import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class Kohi extends VersusKit {
	public Kohi() {
		super("Kohi", new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1));
		setArmor(new ItemCreator(Material.DIAMOND_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
		setArmor(new ItemCreator(Material.DIAMOND_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
		setArmor(new ItemCreator(Material.DIAMOND_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
		setArmor(new ItemCreator(Material.DIAMOND_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).addEnchantment(Enchantment.DURABILITY, 3).addEnchantment(Enchantment.PROTECTION_FALL, 4).getItemStack());
		setItem(0, new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL).addEnchantment(Enchantment.FIRE_ASPECT, 2).addEnchantment(Enchantment.DURABILITY, 3).getItemStack());
		setItem(1, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).addEnchantment(Enchantment.ARROW_FIRE).addEnchantment(Enchantment.ARROW_DAMAGE, 4).getItemStack());
		setItem(2, new ItemCreator(Material.ENDER_PEARL).setAmount(16).getItemStack());
		setItem(9, new ItemStack(Material.ARROW, 64));
		Potion health = new Potion(PotionType.INSTANT_HEAL, 1, true);
		health.setTier(Tier.TWO);
		for(int a = 3; a <= 6; ++a) {
			setItem(a, health.toItemStack(1));
		}
		Potion fireResistance = new Potion(PotionType.FIRE_RESISTANCE, 1, false);
		fireResistance.setHasExtendedDuration(true);
		setItem(7, fireResistance.toItemStack(1));
		Potion speed = new Potion(PotionType.SPEED, 1, false);
		speed.setTier(Tier.TWO);
		for(int slot : new int [] {8, 33, 34, 35}) {
			setItem(slot, speed.toItemStack(1));
		}
		Potion slowness = new Potion(PotionType.SLOWNESS, 1, true);
		for(int slot : new int [] {16, 17, 25, 26}) {
			setItem(slot, slowness.toItemStack(1));
		}
		for(int slot : new int [] {10, 11, 12, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 30, 31, 32}) {
			setItem(slot, health.toItemStack(1));
		}
	}
}
