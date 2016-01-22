package promcgames.server.servers.hub.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.server.util.ItemCreator;

public class CosmeticsItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public CosmeticsItem() {
		super(new ItemCreator(Material.DIAMOND).setName("&aCosmetic Items").addEnchantment(Enchantment.DURABILITY), 1);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isItem(event.getPlayer())) {
			giveCosmeticItems(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
