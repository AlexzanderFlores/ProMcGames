package promcgames.server.servers.hub.items.cosmetic.elite;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.Particles;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader.Perk;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ItemCreator;

public class SpiralParticles extends HubItemBase {
	private static HubItemBase instance = null;
	private static Map<String, ParticleTypes> spiralParticles = null;
	
	public SpiralParticles() {
		super(new ItemCreator(Material.BLAZE_ROD).setName(Ranks.ELITE.getColor() + "Spiral Particle Selector"), 1);
		instance = this;
		spiralParticles = new HashMap<String, ParticleTypes>();
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.ELITE.hasRank(player)) {
			PerkLoader.addPerkToQueue(player, Perk.SPIRAL_PARTICLES);
		}
	}
	
	public static void setType(Player player, ParticleTypes type) {
		spiralParticles.put(player.getName(), type);
	}
	
	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = (Player) event.getPlayer();
		if(isItem(player)) {
			if(Ranks.ELITE.hasRank(player)) {
				player.openInventory(Particles.getParticlesMenu(player, ChatColor.stripColor(getName())));
			} else {
				MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
			}
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			if(event.getItem().getType() == Material.WATER_BUCKET) {
				spiralParticles.remove(player.getName());
				MessageHandler.sendMessage(player, "You have disabled your spiral particles");
				if(DB.HUB_SPIRAL_PARTICLES.isUUIDSet(player.getUniqueId())) {
					DB.HUB_SPIRAL_PARTICLES.deleteUUID(player.getUniqueId());
				}
			} else {
				String name = event.getItemTitle();
				ParticleTypes type = ParticleTypes.valueOf(ChatColor.stripColor(name).toUpperCase().replace(" ", "_"));
				spiralParticles.put(player.getName(), type);
				MessageHandler.sendMessage(player, "You selected &e" + name);
				final String finalType = type.toString();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.HUB_SPIRAL_PARTICLES.isUUIDSet(uuid)) {
							DB.HUB_SPIRAL_PARTICLES.updateString("particle_type", finalType, "uuid", uuid.toString());
						} else {
							DB.HUB_SPIRAL_PARTICLES.insert("'" + uuid.toString() + "', '" + finalType + "'");
						}
					}
				});
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		for(String name : spiralParticles.keySet()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && !PlayerAFKEvent.isAFK(player)) {
				spiralParticles.get(name).displaySpiral(player.getLocation());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		spiralParticles.remove(event.getPlayer().getName());
	}
}
