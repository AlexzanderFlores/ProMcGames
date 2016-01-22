package promcgames.server.servers.hub.items.cosmetic;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.pro.ArmorSelectorItem;
import promcgames.server.servers.hub.items.cosmetic.pro.BowTeleporter;
import promcgames.server.servers.hub.items.cosmetic.pro.FireworkItem;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.PetSelectorItem;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class ProItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public ProItem() {
		super(new ItemCreator(Material.STAINED_GLASS, 5).setName(Ranks.PRO.getPrefix()), 1);
		instance = this;
		new PetSelectorItem();
		new ArmorSelectorItem();
		new FireworkItem();
		new BowTeleporter();
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
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					BackArrowItem.getInstance().giveItem(event.getPlayer());
					PetSelectorItem.getInstance().giveItem(event.getPlayer());
					ArmorSelectorItem.getInstance().giveItem(event.getPlayer());
					FireworkItem.getInstance().giveItem(event.getPlayer());
					BowTeleporter.getInstance().giveItem(event.getPlayer());
					event.getPlayer().updateInventory();
				}
			});
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
