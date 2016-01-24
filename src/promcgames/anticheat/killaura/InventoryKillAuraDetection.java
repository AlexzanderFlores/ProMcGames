package promcgames.anticheat.killaura;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import promcgames.anticheat.AntiGamingChair;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.Disguise;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.PerformanceHandler;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;

public class InventoryKillAuraDetection extends AntiGamingChair implements Listener {
	private boolean hub = false;
	private Map<String, Integer> attacksPerSecond = null;
	private Map<String, Location> spawningLocation = null;
	private Map<String, Integer> secondsLived = null;
	private int maxSeconds = 5;
	
	public InventoryKillAuraDetection() {
		super("Kill Aura");
		Plugins plugin = ProMcGames.getPlugin();
		hub = plugin == Plugins.HUB;
		if(hub || plugin == Plugins.KIT_PVP || plugin == Plugins.VERSUS || plugin == Plugins.SGHUB || plugin == Plugins.UHCHUB) {
			attacksPerSecond = new HashMap<String, Integer>();
			spawningLocation = new HashMap<String, Location>();
			secondsLived = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	private int getSecondsLived(Player player) {
		return hub ? player.getTicksLived() / 20 : secondsLived.get(Disguise.getName(player));
	}
	
	private boolean ableToCheck(Player player) {
		int seconds = getSecondsLived(player);
		return hub ? seconds < maxSeconds : seconds > 1 && seconds < maxSeconds;
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(isEnabled() && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(AccountHandler.getRank(player) == Ranks.PLAYER) {
				int ping = PerformanceHandler.getPing(player);
				boolean pingOk = ping > 0 && ping < 100;
				if(player.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR && ableToCheck(player) && notIgnored(player) && pingOk) {
					if(hub && spawningLocation.containsKey(Disguise.getName(player))) {
						double x1 = player.getLocation().getX();
						double z1 = player.getLocation().getZ();
						double x2 = spawningLocation.get(Disguise.getName(player)).getX();
						double z2 = spawningLocation.get(Disguise.getName(player)).getZ();
						if(x1 != x2 || z1 != z2) {
							spawningLocation.remove(Disguise.getName(player));
							return;
						}
					}
					int attacks = 0;
					if(attacksPerSecond.containsKey(Disguise.getName(player))) {
						attacks = attacksPerSecond.get(Disguise.getName(player));
					}
					if(++attacks >= 7) {
						ban(player);
					} else {
						attacksPerSecond.put(Disguise.getName(player), attacks);
					}
				} else if(PerformanceHandler.getPing(player) > getMaxPing()) {
					attacksPerSecond.remove(Disguise.getName(player));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(isEnabled()) {
			secondsLived.put(event.getPlayer().getName(), 0);
			spawningLocation.put(event.getPlayer().getName(), event.getPlayer().getLocation());
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			attacksPerSecond.clear();
			for(Player player : Bukkit.getOnlinePlayers()) {
				secondsLived.put(Disguise.getName(player), secondsLived.get(Disguise.getName(player)) + 1);
				if(getSecondsLived(player) >= maxSeconds && spawningLocation.containsKey(Disguise.getName(player))) {
					spawningLocation.remove(Disguise.getName(player));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(isEnabled()) {
			if(hub) {
				PlayerRespawnEvent.getHandlerList().unregister(this);
			} else if(event.getPlayer() != null) {
				secondsLived.put(event.getPlayer().getName(), 0);
				spawningLocation.put(event.getPlayer().getName(), event.getRespawnLocation());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			secondsLived.remove(event.getPlayer().getName());
			spawningLocation.remove(event.getPlayer().getName());
		}
	}
}
