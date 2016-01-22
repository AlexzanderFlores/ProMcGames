package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerShootBowEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;

public class ArrowTrails implements Listener {
	private String name = null;
	private List<String> checkedForArrows = null;
	private List<String> toldAboutArrowTrails = null;
	private Map<String, ParticleTypes> particleTypes = null;
	private Map<Arrow, ParticleTypes> arrowTracker = null;
	
	public ArrowTrails() {
		name = "Arrow Trails";
		new CommandBase("arrowTrails", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.openInventory(Particles.getParticlesMenu(player, name));
				return true;
			}
		}.setRequiredRank(Ranks.ELITE);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		if(arrowTracker != null && !arrowTracker.isEmpty()) {
			Iterator<Arrow> iterator = arrowTracker.keySet().iterator();
			while(iterator.hasNext()) {
				Arrow arrow = (Arrow) iterator.next();
				if(arrow == null || arrow.isOnGround() || arrow.isDead()) {
					iterator.remove();
				} else {
					arrowTracker.get(arrow).display(arrow.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow && arrowTracker != null) {
			Arrow arrow = (Arrow) event.getEntity();
			arrowTracker.remove(arrow);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(name)) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
			if(name.equals("Remove particles")) {
				DB.PLAYERS_ARROW_TRAILS.deleteUUID(player.getUniqueId());
				if(particleTypes != null) {
					particleTypes.remove(player.getName());
				}
			} else {
				ParticleTypes type = ParticleTypes.valueOf(name.toUpperCase().replace(" ", "_"));
				if(particleTypes == null) {
					particleTypes = new HashMap<String, ParticleTypes>();
				}
				if(DB.PLAYERS_ARROW_TRAILS.isUUIDSet(player.getUniqueId())) {
					DB.PLAYERS_ARROW_TRAILS.updateString("particles", type.toString(), "uuid", player.getUniqueId().toString());
				} else {
					DB.PLAYERS_ARROW_TRAILS.insert("'" + player.getUniqueId().toString() + "', '" + type.toString() + "'");
				}
				particleTypes.put(player.getName(), type);
				MessageHandler.sendMessage(player, "You selected &b" + name);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerShootBow(PlayerShootBowEvent event) {
		Player player = event.getPlayer();
		if(checkedForArrows == null) {
			checkedForArrows = new ArrayList<String>();
		}
		if(!checkedForArrows.contains(player.getName())) {
			checkedForArrows.add(player.getName());
			String particles = DB.PLAYERS_ARROW_TRAILS.getString("uuid", player.getUniqueId().toString(), "particles");
			if(particles != null) {
				ParticleTypes type = ParticleTypes.valueOf(particles);
				if(particleTypes == null) {
					particleTypes = new HashMap<String, ParticleTypes>();
				}
				particleTypes.put(player.getName(), type);
			}
		}
		if(particleTypes != null && particleTypes.containsKey(player.getName())) {
			if(arrowTracker == null) {
				arrowTracker = new HashMap<Arrow, ParticleTypes>();
			}
			arrowTracker.put(event.getArrow(), particleTypes.get(player.getName()));
		}
		if(toldAboutArrowTrails == null || !toldAboutArrowTrails.contains(player.getName())) {
			if(toldAboutArrowTrails == null) {
				toldAboutArrowTrails = new ArrayList<String>();
			}
			toldAboutArrowTrails.add(player.getName());
			ChatClickHandler.sendMessageToRunCommand(player, " &c&lCLICK HERE", "Click to set particle trails", "/arrowTrails", "&bYou can set arrow particle trails");
			MessageHandler.sendMessage(player, "You can access this any time with &e/arrowTrails");
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(checkedForArrows != null) {
			checkedForArrows.remove(event.getPlayer().getName());
		}
		if(toldAboutArrowTrails != null) {
			toldAboutArrowTrails.remove(event.getPlayer().getName());
		}
		if(particleTypes != null) {
			particleTypes.remove(event.getPlayer().getName());
		}
	}
}
