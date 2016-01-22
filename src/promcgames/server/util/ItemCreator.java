package promcgames.server.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;



@SuppressWarnings("deprecation")
public class ItemCreator {
	private ItemStack itemStack = null;
	
	public ItemCreator(Material material) {
		this(material, 0);
	}
	
	public ItemCreator(Material material, int data) {
		this(material, (byte) data);
	}
	
	public ItemCreator(Material material, byte data) {
		this(new ItemStack(material, 1, data));
	}
	
	public ItemCreator(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public ItemCreator setName(String name) {
		ItemMeta meta = getItemStack().getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		getItemStack().setItemMeta(meta);
		return this;
	}
	
	public ItemCreator setType(Material type) {
		getItemStack().setType(type);
		return this;
	}
	
	public Material getType() {
		return getItemStack().getType();
	}
	
	public ItemCreator setAmount(int amount) {
		getItemStack().setAmount(amount);
		return this;
	}
	
	public int getAmount() {
		return getItemStack().getAmount();
	}
	
	public ItemCreator setData(int data) {
		return setData((byte) data);
	}
	
	public ItemCreator setData(byte data) {
		getItemStack().getData().setData(data);
		return this;
	}
	
	public ItemCreator setLores(String [] lores) {
		ItemMeta meta = getItemStack().getItemMeta();
		meta.setLore(new ArrayList<String>());
		getItemStack().setItemMeta(meta);
		for(String lore : lores) {
			addLore(lore);
		}
		return this;
	}
	
	public ItemCreator setLores(List<String> lores) {
		ItemMeta meta = getItemStack().getItemMeta();
		meta.setLore(new ArrayList<String>());
		getItemStack().setItemMeta(meta);
		for(String lore : lores) {
			addLore(lore);
		}
		return this;
	}
	
	public ItemCreator addLore(String lore) {
		ItemMeta meta = getItemStack().getItemMeta();
		List<String> lores = meta.getLore();
		if(lores == null) {
			lores = new ArrayList<String>();
		}
		lores.add(StringUtil.color(lore));
		meta.setLore(lores);
		getItemStack().setItemMeta(meta);
		return this;
	}
	
	public ItemCreator addEnchantment(Enchantment enchantment) {
		return addEnchantment(enchantment, 1);
	}
	
	public ItemCreator addEnchantment(Enchantment enchantment, int level) {
		getItemStack().addUnsafeEnchantment(enchantment, level);
		return this;
	}
	
	public ItemCreator setDurability(int durability) {
		getItemStack().setDurability((short) durability);
		return this;
	}
	
	public ItemStack getItemStack() {
		return this.itemStack;
	}
	
	public ItemCreator setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		return this;
	}
	
	public String getName() {
		return getItemStack().getItemMeta().getDisplayName();
	}
}
