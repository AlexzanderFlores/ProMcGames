package promcgames.anticheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.customevents.player.ParkourCompleteEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.server.DB;
import promcgames.server.PerformanceHandler;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class SurvivalFly extends AntiGamingChair implements Listener {
	private Map<String, Integer> heightIncreasing = null;
	private Map<String, Integer> wouldBan = null;
	private Map<String, Integer> disabledCounters = null;
	private Map<String, Integer> floating = null;
	
	public SurvivalFly() {
		super("Survival Fly");
		heightIncreasing = new HashMap<String, Integer>();
		wouldBan = new HashMap<String, Integer>();
		disabledCounters = new HashMap<String, Integer>();
		floating = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	private void disable(Player player, int seconds) {
		if(isEnabled()) {
			disabledCounters.put(player.getName(), seconds);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			Iterator<String> iterator = disabledCounters.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = disabledCounters.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					disabledCounters.put(name, counter);
				}
			}
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(PerformanceHandler.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 10 && !player.getAllowFlight() && player.getVehicle() == null) {
							if(notIgnored(player) && !disabledCounters.containsKey(player.getName()) && !player.hasPotionEffect(PotionEffectType.JUMP) && SpeedFix.getSpeed(player) > 0.0d) {
								Location location = player.getLocation();
								int blocks = 0;
								for(int a = 0; a < 3; ++a) {
									Block block = location.getBlock().getRelative(0, -a, 0);
									if(block.getType() == Material.AIR) {
										++blocks;
									} else {
										blocks = 0;
										floating.remove(player.getName());
									}
								}
								if(blocks == 3) {
									int counter = 0;
									if(floating.containsKey(player.getName())) {
										counter = floating.get(player.getName());
									}
									if(++counter >= 5) {
										DB.NETWORK_ANTI_CHEAT_TESTING.insert("'" + player.getName() + "-" + ProMcGames.getServerName() + "-" + PerformanceHandler.getPing(player) + "-B'");
										floating.remove(player.getName());
									} else {
										floating.put(player.getName(), counter);
									}
								}
							}
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(event.getTo().getY() < event.getFrom().getY()) {
				floating.remove(player.getName());
			}
			if(PerformanceHandler.getPing(player) < getMaxPing() && player.getTicksLived() >= 20 * 10 && !player.getAllowFlight() && player.getVehicle() == null) {
				if(notIgnored(player) && !disabledCounters.containsKey(player.getName()) && !player.hasPotionEffect(PotionEffectType.JUMP)) {
					for(int y = 0; y >= -1; --y) {
						for(int x = 1; x >= -1; --x) {
							for(int z = 1; z >= -1; --z) {
								Material type = player.getLocation().getBlock().getRelative(x, y, z).getType();
								if(type.toString().toLowerCase().contains("lava") || type.toString().toLowerCase().contains("water") || type == Material.LADDER) {
									return;
								}
							}
						}
					}
					Location to = event.getTo();
					Location from = event.getFrom();
					if(to.getY() > from.getY()) {
						int distance = 0;
						Location location = to.clone();
				        while(location.getBlockY() >= 2 && location.getBlock().getType() == Material.AIR) {
				        	location.setY(location.getBlockY() - 1);
				        	++distance;
				        }
				        if(distance >= 3) {
				        	int counter = 0;
					        if(heightIncreasing.containsKey(player.getName())) {
					        	counter = heightIncreasing.get(player.getName());
					        }
					        if(++counter >= 15) {
					        	heightIncreasing.remove(player.getName());
					        	counter = 0;
					        	if(wouldBan.containsKey(player.getName())) {
					        		counter = wouldBan.get(player.getName());
					        	}
					        	if(++counter >= 3) {
					        		ban(player);
					        	} else {
					        		wouldBan.put(player.getName(), counter);
					        	}
					        } else {
					        	heightIncreasing.put(player.getName(), counter);
					        }
				        }
					} else {
						heightIncreasing.remove(player.getName());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			disable(player, 5);
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 10);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 10);
		}
	}
	
	@EventHandler
	public void onParkourComplete(ParkourCompleteEvent event) {
		if(isEnabled() && event.getSeconds() < 85) {
			ban(event.getPlayer());
			UUID uuid = event.getPlayer().getUniqueId();
			if(DB.HUB_PARKOUR_TIMES.isUUIDSet(uuid)) {
				DB.HUB_PARKOUR_TIMES.deleteUUID(uuid);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			heightIncreasing.remove(event.getPlayer().getName());
			wouldBan.remove(event.getPlayer().getName());
			floating.remove(event.getPlayer().getName());
		}
	}
}
