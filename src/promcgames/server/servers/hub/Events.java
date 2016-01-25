package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.util.Vector;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.player.WaterSplashEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.PerformanceHandler;
import promcgames.server.servers.hub.events.PlayerRidePlayerEvent;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.staff.ban.BanHandler;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.TextLocationEffect;
import de.slikey.effectlib.util.ParticleEffect;

public class Events implements Listener {
	private static int day = 0;
	private List<String> updates = null;
	private final int length = 60;
	private int maxTicks = 0;
	private World world = null;
	
	public Events() {
		updates = new ArrayList<String>();
		updates.add("&e&lFactions!");
		updates.add("&e&lHosted UHC!");
		maxTicks = 20 + updates.size() * length;
		world = Bukkit.getWorlds().get(0);
		EffectManager manager = new EffectManager(ProMcGames.getInstance());
		TextLocationEffect effect = new TextLocationEffect(manager, new Location(world, -103.5, 131, -174.5, -180.0f, 0.0f));
		effect.text = "Welcome!";
		effect.visibleRange = 40.0f;
		effect.infinite();
		effect.start();
		EventUtil.register(this);
	}
	
	public static boolean isFriday() {
		return false;
		//return day == 6;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(false);
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(block.getType() == Material.WALL_SIGN) {
				if(block.getX() == -127 && block.getY() == 128 && block.getZ() == -201) {
					Sign sign = (Sign) block.getState();
					String name = player.getName();
					if(name.toLowerCase().endsWith("s")) {
						if(name.length() < 14) {
							name += "'";
						} else {
							name = name.substring(0, 14) + "'";
						}
					} else if(name.length() < 13) {
						name += "'s";
					}
					sign.setLine(1, name);
					sign.update();
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Ranks rank = AccountHandler.getRank(player);
		if(Ranks.PRO.hasRank(player) || isFriday()) {
			player.setAllowFlight(true);
			if((rank == Ranks.ELITE || rank == Ranks.YOUTUBER) && !BanHandler.checkForBanned(player)) {
				MessageHandler.alert(AccountHandler.getPrefix(player) + " has joined the hub!");
			}
		}
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		player.setLevel(Hub.hubNumber);
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		MessageHandler.sendLine(player, "&6");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "   &d&l&kabc&b Welcome to &aProMcGames&b, " + player.getName() + "! &d&l&kabc");
		MessageHandler.sendMessage(player, "   &bDo you need help? Tweet us: &chttp://twitter.com/ProMcGames");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "   &eWe hope you enjoy the server!");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendLine(player, "&6");
		if(isFriday()) {
			MessageHandler.sendMessage(player, "&e&lFlying Fridays! Everyone can fly in hub!");
		} else if(day == 7 && AccountHandler.getRank(player) == Ranks.PLAYER) {
			MessageHandler.sendMessage(player, "&c&lFlying Fridays is over :(");
			MessageHandler.sendMessage(player, "&eRanks allow you to fly all the time! &b/buy");
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		EffectUtil.playSound(player, Sound.NOTE_PLING);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if(player.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
			if(event.getSlotType() == SlotType.ARMOR) {
				player.closeInventory();
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		if(event.getCommand().toLowerCase().equals("/stop")) {
			ProPlugin.restartServer();
			event.setCommand(null);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getTo().equals(event.getPlayer().getWorld().getSpawnLocation())) {
			Location spawn = event.getTo();
			Random random = new Random();
			int range = 5;
			spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
			spawn.setY(spawn.getY() + 2.5d);
			spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
			spawn.setYaw(-180.0f);
			spawn.setPitch(-0.0f);
			event.setTo(spawn);
		}
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		boolean old = isFriday();
		day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(AccountHandler.getRank(player) == Ranks.PLAYER && player.getTicksLived() >= (20 * 30) && PerformanceHandler.getPing(player) == 0) {
				player.kickPlayer(ChatColor.RED + "You have been kicked for possibly being a bot. This kick is not logged.");
			} else if(old && !isFriday() && AccountHandler.getRank(player) == Ranks.PLAYER) {
				player.setFlying(false);
				player.setAllowFlight(false);
				MessageHandler.sendMessage(player, "&e&lFlying Fridays is now &c&ldisabled");
			}
		}
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		if(updates != null && !updates.isEmpty()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				int ticks = player.getTicksLived();
				if(ticks >= 20 && ticks < maxTicks) {
					int update = ticks / length;
					if(update < updates.size()) {
						new TitleDisplayer(player, "&bplay.ProMcGames.com", updates.get(update)).setFadeIn(0).setStay(length).setFadeOut(0).display();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if(event.getRightClicked() instanceof Player && Ranks.ELITE.hasRank(player)) {
			Player clicked = (Player) event.getRightClicked();
			PlayerRidePlayerEvent playerRidePlayerEvent = new PlayerRidePlayerEvent(player, clicked);
			Bukkit.getPluginManager().callEvent(playerRidePlayerEvent);
			if(!playerRidePlayerEvent.isCancelled()) {
				CraftPlayer craftPlayer = (CraftPlayer) clicked;
				if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
					MessageHandler.sendMessage(player, "&cCannot ride this player: They are on 1.8");
					MessageHandler.sendMessage(player, "&7You'll get in the way of their view :(");
				} else if(Parkour.isParkouring(clicked)) {
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(clicked) + " &cis playing parkour");
				} else if(Parkour.isParkouring(player)) {
					MessageHandler.sendMessage(player, "&cYou are playing parkour");
				} else if(clicked.getPassenger() == null) {
					clicked.setPassenger(player);
				} else {
					MessageHandler.sendMessage(player, AccountHandler.getPrefix(clicked) + " &cis already being ridden");
				}
			}
		}
	}
	
	@EventHandler
	public void onWaterSplash(WaterSplashEvent event) {
		Player player = event.getPlayer();
		if(!Parkour.isParkouring(player)) {
			player.setVelocity(new Vector(0, 4, 0));
			EffectUtil.playSound(player, Sound.WATER);
			EffectUtil.playSound(player, Sound.SLIME_WALK);
		}
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		Location spawn = world.getSpawnLocation();
		for(int a = 0; a < 5; ++a) {
			Random random = new Random();
			int value = random.nextInt(7) + 2;
			ParticleEffect.FIREWORKS_SPARK.display(spawn, (float) (random.nextBoolean() ? value : value * -1), 4, (float) (random.nextBoolean() ? value : value * -1), 0, 1);
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if(event.getLeaveMessage().contains("We do not allow connections from VPNs") && DB.PLAYERS_VPN.isUUIDSet(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}
}
