package promcgames.customevents.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.server.util.EventUtil;

public class PlayerArmorEquipEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private ItemStack armor = null;
    
    public PlayerArmorEquipEvent() {
    	EventUtil.register(this);
    }
 
    public PlayerArmorEquipEvent(Player player, ItemStack armor) {
    	this.player = player;
    	this.armor = armor;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public ItemStack getArmor() {
    	return this.armor;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public boolean isArmor(Player player, ItemStack armor) {
    	String [] split = player.getItemInHand().getType().toString().split("_");
    	if(split.length == 2) {
    		String name = split[1];
        	if(name.equals("BOOTS") && (player.getInventory().getBoots() == null || player.getInventory().getBoots().getType() == Material.AIR)) {
    			return true;
    		} else if(name.equals("LEGGINGS") && (player.getInventory().getLeggings() == null || player.getInventory().getLeggings().getType() == Material.AIR)) {
    			return true;
    		} else if(name.equals("CHESTPLATE") && (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() == Material.AIR)) {
    			return true;
    		} else if(name.equals("HELMET") && (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType() == Material.AIR)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
    	SlotType type = event.getSlotType();
    	if(!event.isCancelled() && event.getWhoClicked() instanceof Player) {
    		Player player = (Player) event.getWhoClicked();
    		if(player.getOpenInventory() == null) {
    			if(type == SlotType.ARMOR) {
        			Bukkit.getPluginManager().callEvent(new PlayerArmorEquipEvent(player, event.getCursor()));
        		} else if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
        			if(type == SlotType.QUICKBAR) {
        				Bukkit.getPluginManager().callEvent(new PlayerArmorEquipEvent(player, event.getCurrentItem()));
        			} else if(type == SlotType.CONTAINER){
        				Bukkit.getPluginManager().callEvent(new PlayerArmorEquipEvent(player, event.getCurrentItem()));
        			}
        		}
    		}
    	}
    }
    
    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
    	if(event.getClickType() == ClickType.RIGHT_CLICK) {
    		Player player = event.getPlayer();
    		if(isArmor(player, player.getItemInHand())) {
    			Bukkit.getPluginManager().callEvent(new PlayerArmorEquipEvent(player, player.getItemInHand()));
    		}
    	}
    }
}
