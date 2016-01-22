package promcgames.server.servers.hub.items.cosmetic;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.elite.DNABlaster;
import promcgames.server.servers.hub.items.cosmetic.elite.HatItem;
import promcgames.server.servers.hub.items.cosmetic.elite.SpiralParticles;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class EliteItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public EliteItem() {
		super(new ItemCreator(Material.STAINED_GLASS, 2).setName(Ranks.ELITE.getPrefix()), 3);
		instance = this;
		new SpiralParticles();
		new HatItem();
		new DNABlaster();
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	@EventHandler
	public void onMouseClick(final MouseClickEvent event) {
		if(isItem(event.getPlayer())) {
			event.getPlayer().getInventory().clear();
			event.getPlayer().getInventory().setHeldItemSlot(1);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					BackArrowItem.getInstance().giveItem(event.getPlayer());
					SpiralParticles.getInstance().giveItem(event.getPlayer());
					HatItem.getInstance().giveItem(event.getPlayer());
					DNABlaster.getInstance().giveItem(event.getPlayer());
					event.getPlayer().updateInventory();
				}
			});
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
