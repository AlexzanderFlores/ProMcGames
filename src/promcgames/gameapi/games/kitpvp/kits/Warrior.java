package promcgames.gameapi.games.kitpvp.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Warrior extends KitBase implements Listener {
	private static KitBase instance = null;
	
	public Warrior() {
		super(new ItemCreator(Material.STONE_SWORD).setName("Warrior").setLores(new String [] {ChatColor.AQUA + "Price: 250"}).getItemStack(), 13);
		instance = this;
		EventUtil.register(this);
	}
	
	@Override
	public String getPermission() {
		return "kit_pvp.warrior";
	}
	
	@Override
	public void execute() {
		
	}
	
	public static KitBase getInstance() {
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		player.getInventory().addItem(new ItemStack(Material.FISHING_ROD));
		player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
		player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
		player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
		player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
	}
}
