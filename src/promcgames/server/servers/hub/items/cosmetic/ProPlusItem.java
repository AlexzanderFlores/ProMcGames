package promcgames.server.servers.hub.items.cosmetic;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.proplus.AstronautMode;
import promcgames.server.servers.hub.items.cosmetic.proplus.BouncingBlockItem;
import promcgames.server.servers.hub.items.cosmetic.proplus.DragonRoar;
import promcgames.server.servers.hub.items.cosmetic.proplus.FlyCartItem;
import promcgames.server.servers.hub.items.cosmetic.proplus.Head;
import promcgames.server.servers.hub.items.cosmetic.proplus.ParticleSelectorItem;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class ProPlusItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public ProPlusItem() {
		super(new ItemCreator(Material.STAINED_GLASS, 3).setName(Ranks.PRO_PLUS.getPrefix()), 2);
		instance = this;
		new BouncingBlockItem();
		new ParticleSelectorItem();
		new AstronautMode();
		new FlyCartItem();
		new Head();
		new DragonRoar();
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
					BouncingBlockItem.getInstance().giveItem(event.getPlayer());
					ParticleSelectorItem.getInstance().giveItem(event.getPlayer());
					AstronautMode.getInstance().giveItem(event.getPlayer());
					FlyCartItem.getInstance().giveItem(event.getPlayer());
					Head.getInstance().giveItem(event.getPlayer());
					DragonRoar.getInstance().giveItem(event.getPlayer());
					event.getPlayer().updateInventory();
				}
			});
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
