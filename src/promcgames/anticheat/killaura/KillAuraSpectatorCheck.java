package promcgames.anticheat.killaura;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.org.apache.commons.lang3.RandomStringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.anticheat.AntiGamingChair;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.uhc.HostHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.staff.StaffMode;

public class KillAuraSpectatorCheck extends AntiGamingChair implements Listener {
	private static Map<String, String> watching = null;
	private Map<String, String> oldListNames = null;
	private Map<String, Integer> lookingUpCounter = null;
	private float lookingUp = -45;
	
	public KillAuraSpectatorCheck() {
		super("KillAura-S");
		watching = new HashMap<String, String>();
		oldListNames = new HashMap<String, String>();
		lookingUpCounter = new HashMap<String, Integer>();
		new CommandBase("killAura", 1, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(isEnabled()) {
					if(ProMcGames.getPlugin() == Plugins.SKY_WARS && !Ranks.isStaff(sender)) {
						MessageHandler.sendMessage(sender, "&cCannot run this command in " + ProMcGames.getServerName());
						return true;
					}
					if(ProMcGames.getPlugin() == Plugins.UHC && sender instanceof Player) {
						Player player = (Player) sender;
						if(!Ranks.isStaff(sender) && !HostHandler.isHost(player.getUniqueId())) {
							MessageHandler.sendMessage(sender, "&cCannot run this command in " + ProMcGames.getServerName());
							return true;
						}
					}
					final Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online!");
					} else if(SpectatorHandler.contains(player)) {
						MessageHandler.sendMessage(sender, "&c" + player.getName() + " is spectating!");
					} else {
						final Player viewer = (Player) sender;
						if(SpectatorHandler.contains(viewer) || StaffMode.contains(player)) {
							if(viewer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
								if(viewer.getItemInHand() == null || viewer.getItemInHand().getType() == Material.AIR) {
									if(ProMcGames.getPlugin() == Plugins.FACTIONS && !Ranks.MODERATOR.hasRank(player)) {
										MessageHandler.sendMessage(player, Ranks.MODERATOR.getNoPermission());
										return true;
									}
									if(arguments.length == 1) {
										if(watching.containsKey(viewer.getName())) {
											MessageHandler.sendMessage(viewer, "&cYou are no longer watching " + watching.get(viewer.getName()));
											watching.remove(viewer.getName());
										} else {
											watching.put(viewer.getName(), player.getName());
											MessageHandler.sendMessage(sender, "You are now watching " + player.getName());
										}
									} else if(arguments.length == 2) {
										if(arguments[1].equalsIgnoreCase("tp")) {
											final String oldName;
											if(oldListNames.containsKey(viewer.getName())) {
												oldName = oldListNames.get(viewer.getName());
											} else {
												oldName = viewer.getPlayerListName();
												oldListNames.put(viewer.getName(), oldName);
											}
											viewer.setPlayerListName(Ranks.PLAYER.getColor() + RandomStringUtils.randomAlphanumeric(new Random().nextInt(5) + 3));
											viewer.teleport(player.getLocation().add(0, 2, 0));
											viewer.setAllowFlight(true);
											viewer.setFlying(true);
											Location location = viewer.getLocation();
											location.setPitch(90.0f);
											viewer.teleport(location);
											if(player.getLocation().getPitch() > lookingUp) {
												player.showPlayer(viewer);
												new DelayedTask(new Runnable() {
													@Override
													public void run() {
														float pitch = player.getLocation().getPitch();
														if(pitch <= lookingUp && Ranks.isStaff(viewer)) {
															int counter = 0;
															if(lookingUpCounter.containsKey(player.getName())) {
																counter = lookingUpCounter.get(player.getName());
															}
															if(++counter >= 5) {
																ban(player, viewer);
															} else {
																lookingUpCounter.put(player.getName(), counter);
															}
														}
														player.hidePlayer(viewer);
														viewer.setPlayerListName(oldName);
													}
												}, 5);
											}
										} else {
											return false;
										}
									}
								} else {
									MessageHandler.sendMessage(viewer, "&cYou must not be holding anything to use this command");
								}
							} else {
								MessageHandler.sendMessage(viewer, "&cYou do not have invisibility... Giving now");
								viewer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
							}
						} else {
							MessageHandler.sendMessage(viewer, "&cYou must be a spectator or in staff mode to run this command");
						}
					}
				} else {
					MessageHandler.sendMessage(sender, "&cThe Anti Cheat is currently &4DISABLED");
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static boolean isWatching(Player player) {
		return watching != null && watching.containsKey(player.getName());
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(isEnabled() && watching != null) {
			Iterator<String> iterator = watching.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					iterator.remove();
				} else {
					String targetName = watching.get(name);
					Player target = ProPlugin.getPlayer(targetName);
					if(target == null || SpectatorHandler.contains(target)) {
						iterator.remove();
						MessageHandler.sendMessage(player, "&c" + targetName + "is no longer playing");
					} else {
						player.chat("/killAura " + targetName + " tp");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(isEnabled()) {
			lookingUpCounter.clear();
		}
	}
	
	//@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(SpectatorHandler.contains(player)) {
				Bukkit.getLogger().info("Spectator " + player.getName() + " was damaged");
				Player damager = (Player) event.getDamager();
				if(damager.canSee(player)) {
					Bukkit.getLogger().info("Damager " + damager.getName() + " can see the spectator");
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(damager) + " &e&lhas damaged you");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(isEnabled()) {
			watching.remove(event.getPlayer().getName());
			oldListNames.remove(event.getPlayer().getName());
		}
	}
}
