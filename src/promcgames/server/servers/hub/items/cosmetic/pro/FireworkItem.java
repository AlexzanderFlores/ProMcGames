package promcgames.server.servers.hub.items.cosmetic.pro;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

public class FireworkItem extends HubItemBase {
	private static HubItemBase instance = null;
	private List<String> delayed = null;

	public FireworkItem() {
		super(new ItemCreator(Material.FIREWORK).setName(Ranks.PRO.getColor() + "Firework"), 3);
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
		Player player = (Player) event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO.hasRank(player)) {
				final String name = player.getName();
				if(delayed == null || !delayed.contains(name)) {
					if(delayed == null) {
						delayed = new ArrayList<String>();
					}
					delayed.add(name);
					EffectUtil.launchFirework(player);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = ProPlugin.getPlayer(name);
							if(player != null) {
								MessageHandler.sendMessage(player, "You can now use fireworks again");
							}
							delayed.remove(name);
						}
					}, Ranks.PRO_PLUS.hasRank(player) ? 20 * 3 : 20 * 5);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(delayed != null) {
			delayed.remove(event.getPlayer().getName());
		}
	}
}
