package promcgames.server.servers.hub.items.cosmetic;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class MainMenuItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public MainMenuItem() {
		super(new ItemCreator(Material.ARROW).setName("&bBack"), 0);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isItem(event.getPlayer())) {
			giveOriginalHotBar(event.getPlayer());
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
