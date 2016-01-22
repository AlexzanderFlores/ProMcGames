package promcgames.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.NewPlayerJoinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.gameapi.games.kitpvp.killstreaks.ExplosiveBow;
import promcgames.gameapi.games.kitpvp.killstreaks.ExtraHealth;
import promcgames.gameapi.games.kitpvp.killstreaks.Juggernaut;
import promcgames.gameapi.games.kitpvp.killstreaks.Killstreak;
import promcgames.gameapi.games.kitpvp.killstreaks.PoisonBow;
import promcgames.gameapi.games.kitpvp.killstreaks.SlimeTime;
import promcgames.gameapi.games.kitpvp.killstreaks.SnowballFight;
import promcgames.gameapi.games.kitpvp.killstreaks.Speed;
import promcgames.gameapi.games.kitpvp.killstreaks.Strength;
import promcgames.gameapi.games.kitpvp.trophies.Enchanted;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class KillstreakHandler implements Listener {
	private Location location = null;
	private boolean beingUsed = false;
	private int requiredLevels = 5;
	private double counter = 0;
	private double maxTime = 7.5;
	private Player player = null;
	private NPCEntity npc = null;
	private List<String> usedKillStreak = null;
	private List<String> usedKillStreakEver = null;
	private static List<String> newPlayers = null;
	private Map<String, Integer> timesUsed = null;
	
	public KillstreakHandler(double x, double y, double z) {
		World world = Bukkit.getWorlds().get(0);
		location = new Location(world, x, y, z);
		location.getBlock().setType(Material.ENCHANTMENT_TABLE);
		usedKillStreak = new ArrayList<String>();
		usedKillStreakEver = new ArrayList<String>();
		newPlayers = new ArrayList<String>();
		timesUsed = new HashMap<String, Integer>();
		new PoisonBow();
		new ExtraHealth();
		new Strength();
		new Speed();
		new Juggernaut();
		new ExplosiveBow();
		new SnowballFight();
		new SlimeTime();
		EventUtil.register(this);
	}
	
	public static boolean isNew(Player player) {
		return newPlayers != null && newPlayers.contains(player.getName());
	}
	
	public static void sendMessage(Player player) {
		MessageHandler.sendLine(player, "&6");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "&bWelcome to Kit PVP, &e" + player.getName() + "&b!");
		MessageHandler.sendMessage(player, "&bDue to being new you've been given &e5 &blevels");
		MessageHandler.sendMessage(player, "&bClick the &eEnchantment Table &bfor a cool perk!");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendLine(player, "&6");
	}
	
	private void selectKillstreak() {
		if(player.isOnline() && npc != null && npc.getLivingEntity().getPassenger() != null) {
			Killstreak selected = null;
			for(Killstreak killstreak : Killstreak.getKillstreaks()) {
				Item item = (Item) npc.getLivingEntity().getPassenger();
				if(killstreak.getItemStack().equals(item.getItemStack())) {
					selected = killstreak;
					break;
				}
			}
			selected.execute(player);
			Bukkit.getPluginManager().callEvent(new KillstreakEvent(player, selected));
			if(!usedKillStreakEver.contains(player.getName())) {
				usedKillStreakEver.add(player.getName());
				Enchanted.getInstance().setAchieved(player);
			}
		}
		ParticleTypes.FLAME.displaySpiral(npc.getLivingEntity().getLocation());
		EffectUtil.playSound(player, Sound.ENDERDRAGON_WINGS);
		player = null;
		beingUsed = false;
		counter = 0;
		if(npc != null) {
			if(npc.getLivingEntity().getPassenger() != null) {
				npc.getLivingEntity().getPassenger().remove();
			}
			npc.remove();
			npc = null;
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(location != null && location.equals(event.getClickedBlock().getLocation())) {
				Player player = event.getPlayer();
				if(SpectatorHandler.contains(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot be a spectator when using this");
				} else {
					if(player.getLevel() >= requiredLevels) {
						if(beingUsed) {
							MessageHandler.sendMessage(player, "&cKillstreak selector is already in use");
						} else {
							if(usedKillStreak.contains(player.getName())) {
								MessageHandler.sendMessage(player, "&cYou can only use this once per life");
							} else {
								boolean hasKit = false;
								for(KitBase kit : KitBase.getKits()) {
									if(kit.has(player)) {
										hasKit = true;
										break;
									}
								}
								if(hasKit) {
									newPlayers.remove(player.getName());
									usedKillStreak.add(player.getName());
									beingUsed = true;
									player.setLevel(player.getLevel() - requiredLevels);
									this.player = player;
									if(npc != null) {
										npc.remove();
									}
									npc = new NPCEntity(EntityType.SILVERFISH, null, location.clone().add(0.5, 1, 0.5)) {
										@Override
										public void onInteract(Player player) {
											
										}
									};
									npc.setSpawnZombie(false);
									LivingEntity livingEntity = (LivingEntity) npc.getLivingEntity();
									livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
								} else {
									MessageHandler.sendMessage(player, "&cYou must have selected a kit before using this");
								}
							}
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou need &e" + requiredLevels + " &clevels to use the killstreak selector");
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(npc != null && counter <= maxTime) {
			LivingEntity livingEntity = (LivingEntity) npc.getLivingEntity();
			Killstreak killstreak = Killstreak.getKillstreaks().get(new Random().nextInt(Killstreak.getKillstreaks().size()));
			if(livingEntity.getPassenger() == null) {
				Item item = livingEntity.getWorld().dropItem(livingEntity.getLocation(), killstreak.getItemStack());
				livingEntity.setPassenger(item);
			} else {
				Item current = (Item) livingEntity.getPassenger();
				current.setItemStack(killstreak.getItemStack());
			}
			counter += .25;
		} else if(counter > maxTime) {
			selectKillstreak();
		}
	}
	
	@EventHandler
	public void onNewPlayerJoin(NewPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(!newPlayers.contains(player.getName())) {
			newPlayers.add(player.getName());
			player.setLevel(5);
			sendMessage(player);
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		usedKillStreak.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(timesUsed.containsKey(player.getName())) {
			int times = timesUsed.get(player.getName());
			if(DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.isUUIDSet(player.getUniqueId())) {
				DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.updateInt("purchases", times, "uuid", player.getUniqueId().toString());
			} else {
				DB.PLAYERS_KIT_PVP_LEVEL_PURCHASES.insert("'" + player.getUniqueId().toString() + "', '" + times + "'");
			}
			timesUsed.remove(player.getName());
		}
		newPlayers.remove(player.getName());
		usedKillStreak.remove(player.getName());
		usedKillStreakEver.remove(player.getName());
	}
}
