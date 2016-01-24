package promcgames.gameapi;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateCommandEvent;
import promcgames.customevents.player.PlayerSpectateEndEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.player.WaterSplashEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.games.uhc.Spectating;
import promcgames.gameapi.games.uhc.WorldHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.staff.StaffMode;

public class SpectatorHandler implements Listener {
	private static List<String> spectators = null;
	private static ItemStack teleporter = null;
	private static ItemStack exit = null;
	private static ItemStack nextGame = null;
	private static boolean enabled = false;
	
	public SpectatorHandler() {
		spectators = new ArrayList<String>();
		teleporter = new ItemCreator(Material.WATCH).setName("&6Teleport to Player").getItemStack();
		if(ProMcGames.getMiniGame() == null && ProMcGames.getPlugin() != Plugins.UHC) {
			exit = new ItemCreator(Material.GLOWSTONE_DUST).setName("&aExit Spectating").getItemStack();
		} else {
			exit = new ItemCreator(Material.GLOWSTONE_DUST).setName("&aReturn to Hub").getItemStack();
		}
		nextGame = new ItemCreator(Material.DIAMOND).setName("&6Join Next Game").getItemStack();
		new CommandBase("spectate", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				if(contains(player)) {
					PlayerSpectateCommandEvent event = new PlayerSpectateCommandEvent(player);
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled()) {
						Player target = ProPlugin.getPlayer(arguments[0]);
						if(target == null || contains(target)) {
							MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not playing");
						} else {
							player.teleport(target);
						}
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou are not a spectator");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PRO);
		World lobby = Bukkit.getWorlds().get(0);
		Location location = null;
		if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
			location = new Location(lobby, 3.5, 114, -149.5);//1.5, 83, 1.5);
		} else if(ProMcGames.getPlugin() == Plugins.VERSUS) {
			location = new Location(lobby, 3.5, 5, 11.5, -180.0f, 0.0f);
		}
		if(location != null) {
			new NPCEntity(EntityType.CREEPER, "&eSpectate " + ChatColor.RED + "(CLICK)", location) {
				@Override
				public void onInteract(Player player) {
					if(Ranks.PRO.hasRank(player)) {
						SpectatorHandler.add(player);
					} else {
						MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
					}
				}
			};
		}
		enabled = true;
		EventUtil.register(this);
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void add(Player player) {
		if(!contains(player)) {
			PlayerSpectateStartEvent playerSpectateStartEvent = new PlayerSpectateStartEvent(player);
			Bukkit.getPluginManager().callEvent(playerSpectateStartEvent);
			if(!playerSpectateStartEvent.isCancelled()) {
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				spectators.add(Disguise.getName(player));
				player.getInventory().setItem(0, teleporter);
				if(ProMcGames.getMiniGame() == null) {
					player.getInventory().setItem(8, exit);
				} else {
					if(ProMcGames.getMiniGame().getAutoJoin()) {
						player.getInventory().setItem(7, exit);
						player.getInventory().setItem(8, nextGame);
					} else {
						player.getInventory().setItem(8, exit);
					}
				}
				player.getInventory().setHeldItemSlot(0);
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.hidePlayer(player);
					online.showPlayer(player);
					online.hidePlayer(player);
					if(contains(online)) {
						player.hidePlayer(online);
					}
				}
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 10));
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 10));
				player.setGameMode(GameMode.CREATIVE);
				player.setAllowFlight(true);
				player.setFlying(true);
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&e&lHelp catch hackers: &c&lTry /killaura <name>");
				if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getAutoJoin()) {
					ChatClickHandler.sendMessageToRunCommand(player, "&6&lCLICK TO AUTO JOIN NEXT GAME", "Click to play again", "/autoJoin");
				}
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "");
			}
		}
	}
	
	public static void remove(Player player) {
		if(contains(player)) {
			PlayerSpectateEndEvent spectateEndEvent = new PlayerSpectateEndEvent(player);
			Bukkit.getPluginManager().callEvent(spectateEndEvent);
			if(!spectateEndEvent.isCancelled()) {
				spectators.remove(Disguise.getName(player));
				ProPlugin.resetPlayer(player);
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.showPlayer(player);
				}
				player.setGameMode(GameMode.SURVIVAL);
				player.setFlying(false);
				player.setAllowFlight(false);
			}
		}
	}
	
	public static boolean contains(Player player) {
		return isEnabled() && spectators.contains(Disguise.getName(player));
	}
	
	public static int getNumberOf() {
		if(spectators.isEmpty()) {
			return 0;
		}
		int amount = 0;
		for(Player player : getPlayers()) {
			if(!Ranks.isStaff(player)) {
				++amount;
			}
		}
		return amount;
	}
	
	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(contains(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static boolean wouldSpectate() {
		MiniGame miniGame = ProMcGames.getMiniGame();
		if(miniGame == null) {
			return false;
		} else {
			GameStates gameState = (GameStates) miniGame.getGameState();
			return (gameState == GameStates.STARTING && !miniGame.getCanJoinWhileStarting()) || gameState == GameStates.STARTED || gameState == GameStates.ENDING;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(wouldSpectate()) {
			add(event.getPlayer());
		}
		for(Player spectator : getPlayers()) {
			event.getPlayer().hidePlayer(spectator);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		} else if(!event.isCancelled()) {
			int x = event.getBlock().getX();
			int y = event.getBlock().getY();
			int z = event.getBlock().getZ();
			for(Player spectator : getPlayers()) {
				Location location = spectator.getLocation();
				if(x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ()) {
					spectator.teleport(spectator.getLocation().add(0, 30, 0));
					MessageHandler.sendMessage(spectator, "&cYou have been teleported due to you getting in the way of a player placing a block");
					break;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				if(event.getCause() == DamageCause.VOID) {
					player.teleport(player.getWorld().getSpawnLocation());
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
		if(event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			if(contains(damager)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(contains(player)) {
			ItemStack item = player.getItemInHand();
			if(item != null) {
				if(item.getType() == Material.WATCH) {
					Inventory inventory = ItemUtil.getPlayerSelector(player, item.getItemMeta().getDisplayName());
					if(inventory != null) {
						if(ProMcGames.getPlugin() == Plugins.UHC) {
							inventory = Spectating.getInventory(player, item.getItemMeta().getDisplayName(), inventory);
							if(inventory == null) {
								player.teleport(WorldHandler.getGround(new Location(WorldHandler.getWorld(), 0, 0, 0)));
							}
						}
						if(inventory != null) {
							player.openInventory(inventory);
						}
					}
				} else if(item.getType() == Material.GLOWSTONE_DUST) {
					if(ProMcGames.getMiniGame() == null && ProMcGames.getPlugin() != Plugins.UHC) {
						if(StaffMode.contains(player)) {
							player.chat("/staffMode");
						}
						remove(player);
					} else {
						ProPlugin.sendPlayerToServer(player, "hub");
					}
				} else if(item.getType() == Material.DIAMOND) {
					AutoJoinHandler.send(player);
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			final Player player = (Player) event.getWhoClicked();
			if(contains(player)) {
				ItemStack item = event.getCurrentItem();
				if(item != null && item.getItemMeta() != null) {
					if(event.getInventory().getName().equals(teleporter.getItemMeta().getDisplayName())) {
						Player target = ProPlugin.getPlayer(item.getItemMeta().getDisplayName());
						if(target == null) {
							MessageHandler.sendMessage(player, "&cThat player is no longer playing");
						} else {
							player.teleport(target);
							MessageHandler.sendMessage(player, "&eNote: &aYou can also teleport with &c/spectate <player name>");
						}
					}
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(contains(player)) {
			if(ProMcGames.getMiniGame() != null && !ProMcGames.getMiniGame().getUseSpectatorChatChannel()) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!Ranks.isStaff(player)) {
						event.getRecipients().remove(online);
					}
				}
			}
			event.setFormat(ChatColor.GRAY + "[Spectator] " + AccountHandler.getPrefix(player, false) + ": " + event.getMessage());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		if(contains(event.getPlayer())) {
			event.setAmount(0);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleEnter(VehicleEnterEvent event) {
		if(event.getEntered() instanceof Player) {
			Player player = (Player) event.getEntered();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleDamage(VehicleDamageEvent event) {
		if(event.getAttacker() instanceof Player) {
			Player player = (Player) event.getAttacker();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if(event.getAttacker() instanceof Player) {
			Player player = (Player) event.getAttacker();
			if(contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getPlayersHaveOneLife()) {
			ProPlugin.resetPlayer(event.getPlayer());
			add(event.getPlayer());
			Player killer = event.getPlayer().getKiller();
			if(killer != null) {
				event.setRespawnLocation(killer.getLocation());
			}
			event.setRespawnLocation(ProMcGames.getMiniGame().getLobby().getSpawnLocation());
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(contains(player)) {
				event.setFoodLevel(20);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(player.getPassenger() != null && player.getPassenger() instanceof Player) {
			Player passenger = (Player) player.getPassenger();
			if(contains(passenger)) {
				MessageHandler.sendMessage(passenger, "&cYou have been moved off this player due to them teleporting");
				player.eject();
			}
		}
		if(contains(player) && player.getVehicle() != null && player.getVehicle() instanceof Player) {
			player.leaveVehicle();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onWaterSplash(WaterSplashEvent event) {
		if(contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
}
