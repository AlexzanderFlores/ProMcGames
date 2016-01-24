package promcgames.server.servers.hub.items.cosmetic.elite;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.DnaLocationEffect;

public class DNABlaster extends HubItemBase {
	private static HubItemBase instance = null;
	private List<String> delayed = null;
	private EffectManager manager = null;

	public DNABlaster() {
		super(new ItemCreator(Material.BEACON).setName(Ranks.ELITE.getColor() + "DNA Blaster"), 3);
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
			if(Ranks.ELITE.hasRank(player)) {
				final String name = player.getName();
				if(delayed == null || !delayed.contains(name)) {
					if(delayed == null) {
						delayed = new ArrayList<String>();
					}
					delayed.add(name);
					DnaLocationEffect effect = new DnaLocationEffect(manager, player.getLocation().add(0, 1, 0));
					effect.iterations = effect.iterations / 20;
					effect.visibleRange = 25.0f;
					effect.start();
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = ProPlugin.getPlayer(name);
							if(player != null) {
								MessageHandler.sendMessage(player, "You can now use the DNA Blaster again");
							}
							delayed.remove(name);
						}
					}, 20 * 4);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
