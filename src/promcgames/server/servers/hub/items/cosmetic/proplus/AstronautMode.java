package promcgames.server.servers.hub.items.cosmetic.proplus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class AstronautMode extends HubItemBase {
	private static HubItemBase instance = null;
	private List<String> inAstronautMode = null;

	public AstronautMode() {
		super(new ItemCreator(Material.STAINED_GLASS).setName(Ranks.PRO_PLUS.getColor() + "Astronaut Mode"), 3);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	private void remove(Player player) {
		if(inAstronautMode != null) {
			inAstronautMode.remove(Disguise.getName(player));
			player.getInventory().setHelmet(new ItemStack(Material.AIR));
			player.getInventory().setChestplate(new ItemStack(Material.AIR));
			player.getInventory().setLeggings(new ItemStack(Material.AIR));
			player.getInventory().setBoots(new ItemStack(Material.AIR));
			player.removePotionEffect(PotionEffectType.JUMP);
		}
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				if(inAstronautMode != null && inAstronautMode.contains(Disguise.getName(player))) {
					remove(player);
				} else {
					if(inAstronautMode == null) {
						inAstronautMode = new ArrayList<String>();
					}
					inAstronautMode.add(Disguise.getName(player));
					player.getInventory().setHelmet(new ItemStack(Material.STAINED_GLASS));
					player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
					player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
					player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
					player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 4));
				}
				player.updateInventory();
				event.setCancelled(true);
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
