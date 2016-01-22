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

public class VotingCrate extends CrateBase {
	public VotingCrate() {
		super(DB.PLAYERS_FACTIONS_VOTING_CRATES, new Location(Bukkit.getWorlds().get(0), -234, 67, 278), "&cYou do not have any Voting Crate keys! &b/vote", true);
		hologram.appendTextLine(StringUtil.color("&bVoting Crate"));
		hologram.appendTextLine(StringUtil.color("&eGet one with &b/vote"));
	}
	
	@Override
	public void interact(Player player) {
		addItem(new ItemStack(Material.MONSTER_EGG, 5, (byte) 50));
		addItem(new ItemStack(Material.GOLD_INGOT, 1000));
		addItem(new ItemStack(Material.GOLDEN_APPLE, 3));
		addItem(new ItemStack(Material.IRON_INGOT, 32));
		addItem(new ItemStack(Material.EXP_BOTTLE, 20));
		addItem(new ItemStack(Material.MONSTER_EGG, 3, (byte) 50));
		addItem(new ItemStack(Material.GOLD_INGOT, 750));
		addItem(new ItemStack(Material.EXP_BOTTLE, 15));
		addItem(new ItemCreator(Material.DIAMOND_PICKAXE).addEnchantment(Enchantment.DIG_SPEED, 2).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).getItemStack());
		addItem(new ItemCreator(Material.DIAMOND_AXE).addEnchantment(Enchantment.DIG_SPEED, 2).getItemStack());
		addItem(new ItemCreator(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemCreator(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItemStack());
		addItem(new ItemStack(Material.EXP_BOTTLE, 10));
		addItem(new ItemStack(Material.GOLD_INGOT, 500));
		addItem(new ItemStack(Material.TNT, 32));
		addItem(new ItemStack(Material.OBSIDIAN, 64));
		addItem(new ItemStack(Material.EXP_BOTTLE, 5));
		addItem(new ItemStack(Material.GOLD_INGOT, 250));
		addItem(new ItemStack(Material.SULPHUR, 32));
		addItem(new ItemStack(Material.TNT, 16));
		addItem(new ItemStack(Material.OBSIDIAN, 32));
		addItem(new ItemStack(Material.SULPHUR, 16));
		addItem(new ItemStack(Material.BOOK, 16));
		addItem(new ItemStack(Material.GOLD_INGOT, 100));
		addItem(new ItemStack(Material.BOOKSHELF, 32));
		addItem(new ItemStack(Material.GOLD_INGOT, 50));
		addItem(new ItemStack(Material.BOOKSHELF, 16));
		addItem(new ItemStack(Material.GOLD_INGOT, 10));
		addItem(new ItemStack(Material.RED_ROSE));
		super.interact(player);
	}
}
