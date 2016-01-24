package promcgames.server.servers.hub.items.cosmetic.proplus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.DragonLocationEffect;

public class DragonRoar extends HubItemBase {
	private static HubItemBase instance = null;
	private List<String> delayed = null;
	private EffectManager manager = null;
	
	public DragonRoar() {
		super(new ItemCreator(Material.DRAGON_EGG).setName(Ranks.PRO_PLUS.getColor() + "Dragon Roar"), 6);
		instance = this;
		manager = new EffectManager(ProMcGames.getInstance());
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
			if(Ranks.PRO_PLUS.hasRank(player)) {
				final String name = player.getName();
				if(delayed == null || !delayed.contains(name)) {
					if(delayed == null) {
						delayed = new ArrayList<String>();
					}
					delayed.add(name);
					DragonLocationEffect effect = new DragonLocationEffect(manager, player.getLocation().add(0, 1, 0));
					effect.particles = effect.particles / 3;
					effect.iterations = 21;
					effect.visibleRange = 25.0f;
					effect.start();
					EffectUtil.playSound(player, Sound.ENDERDRAGON_GROWL);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = ProPlugin.getPlayer(name);
							if(player != null) {
								MessageHandler.sendMessage(player, "You can now use Dragon Roar again");
							}
							delayed.remove(name);
						}
					}, 20 * 3);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
