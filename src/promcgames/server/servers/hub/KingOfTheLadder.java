package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.EventUtil;

public class KingOfTheLadder implements Listener {
	private List<String> playing = null;
	
	public KingOfTheLadder() {
		new NPCEntity(EntityType.SKELETON, "&bKing of the Ladder", new Location(Bukkit.getWorlds().get(0), -43.5, 126, -154.5, -228.68f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "Walk in to play \"&eKing of the Ladder&a\"");
			}
		};
		playing = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	private void add(Player player) {
		player.setAllowFlight(false);
		player.teleport(new Location(player.getWorld(), -33.5, 126, -148.5, -45.0f, 0.0f));
		player.closeInventory();
		player.getInventory().clear();
		playing.add(player.getName());
	}
	
	private boolean remove(Player player, boolean teleporting) {
		if(playing.contains(player.getName())) {
			if(!teleporting) {
				player.teleport(new Location(player.getWorld(), -37.5, 126, -152.5, -225.9f, 0.0f));
			}
			if(Ranks.PRO.hasRank(player) || Events.isFriday()) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
			playing.remove(player.getName());
			HubItemBase.giveOriginalHotBar(player);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		int x = to.getBlockX();
		if(x >= -37 && x <= -35) {
			int y = to.getBlockY();
			if(y >= 126 && y <= 129) {
				int z = to.getBlockZ();
				if(z >= -152 && z <= -150) {
					Player player = event.getPlayer();
					if(!remove(player, false)) {
						if(player.isFlying()) {
							event.setTo(event.getFrom());
							MessageHandler.sendMessage(player, "&cYou cannot enter when flying");
						} else if(Parkour.isParkouring(player)) {
							event.setTo(event.getFrom());
							MessageHandler.sendMessage(player, "&cYou cannot play King of the Ladder while doing Parkour");
						} else if(SnowballFight.isPlaying(player)) {
							event.setTo(event.getFrom());
							MessageHandler.sendMessage(player, "&cYou cannot play King of the Ladder while playing Snowball fight");
						} else {
							add(player);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player) event.getEntity();
			Player damager = (Player) event.getDamager();
			if(playing.contains(player.getName()) && playing.contains(damager.getName())) {
				if(player.getLocation().getBlockY() >= 131 && damager.getLocation().getBlockY() >= 131) {
					event.setDamage(0.0d);
					event.setCancelled(false);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.PLUGIN) {
			remove(event.getPlayer(), true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		playing.remove(name);
	}
}
