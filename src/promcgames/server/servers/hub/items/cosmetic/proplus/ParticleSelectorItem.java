package promcgames.server.servers.hub.items.cosmetic.proplus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.Particles;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class ParticleSelectorItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public ParticleSelectorItem() {
		super(new ItemCreator(Material.BLAZE_ROD).setName(Particles.getName()), 2);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = (Player) event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				player.openInventory(Particles.getParticlesMenu(player, null));
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}