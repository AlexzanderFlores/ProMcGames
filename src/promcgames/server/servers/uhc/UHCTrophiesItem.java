package promcgames.server.servers.uhc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.trophies.UHCTrophies;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class UHCTrophiesItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public UHCTrophiesItem() {
		super(new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies"), 7);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		if(instance == null) {
			new UHCTrophiesItem();
		}
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
		Player player = event.getPlayer();
		if(isItem(player)) {
			UHCTrophies.open(player);
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
