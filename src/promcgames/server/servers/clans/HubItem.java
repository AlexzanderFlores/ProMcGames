package promcgames.server.servers.clans;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class HubItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public HubItem() {
		super(new ItemCreator(Material.GLOWSTONE_DUST).setName("&6Return to Hub"), 8);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		if(instance == null) {
			new HubItem();
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
			ProPlugin.sendPlayerToServer(player, "hub");
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
