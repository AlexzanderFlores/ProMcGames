package promcgames.gameapi.games.survivalgames;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameWaitingEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.game.GracePeriodEndingEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.GracePeriod;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpawnPointHandler;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.VotingHandler;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup.SetupPhase;
import promcgames.gameapi.games.survivalgames.deathmatch.DeathmatchHandler;
import promcgames.gameapi.games.survivalgames.events.PlayerBreakLegsEvent;
import promcgames.gameapi.games.survivalgames.mapeffects.MapEffectHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.player.scoreboard.BelowNameHealthScoreboardUtil;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	public static World arena = null;
	public static SpawnPointHandler spawnPointHandler = null;
	private List<Material> allowedToBreak = null;
	private List<Material> cannotSpawn = null;
	private boolean alreadyLoadedMaps = false;
	
	public Events() {
		allowedToBreak = Arrays.asList(
			Material.MELON_BLOCK, Material.WEB, Material.CAKE_BLOCK, Material.LONG_GRASS, Material.POTATO, Material.DEAD_BUSH,
			Material.CROPS, Material.CARROT, Material.LEAVES, Material.LEAVES_2, Material.VINE, Material.FIRE, Material.DOUBLE_PLANT
		);
		cannotSpawn = Arrays.asList(
			Material.SEEDS, Material.SAPLING, Material.INK_SACK, Material.SADDLE, Material.LEATHER, Material.STRING, Material.WOOD,
			Material.LEAVES, Material.LEAVES_2
		);
		EventUtil.register(this);
	}
	
	public static SpawnPointHandler getSpawnPointHandler() {
		return spawnPointHandler;
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		MiniGame miniGame = ProMcGames.getMiniGame();
		GameStates gameState = miniGame.getGameState();
		if(gameState == GameStates.WAITING) {
			ParticleTypes.HAPPY_VILLAGER.display(new Location(ProMcGames.getMiniGame().getLobby(), -6, 27.5, -42));
		} else if(gameState == GameStates.STARTING) {
			if(miniGame.getCounter() == 10) {
				new TieringHandler();
				if(ProMcGames.getMiniGame() != null && SurvivalGames.getCanUseSponsors()) {
					SponsorHandler.loadParticles();
				}
			} else if(miniGame.getCounter() == 15) {
				new MapEffectHandler(arena);
			} else if(miniGame.getCounter() == 5) {
				for(Player player : ProPlugin.getPlayers()) {
					player.getInventory().remove(Material.SNOW_BALL);
				}
			}
		} else if(gameState == GameStates.STARTED) {
			if(miniGame.getCounter() <= 0) {
				OneSecondTaskEvent.getHandlerList().unregister(this);
				new DeathmatchHandler();
			} else if(miniGame.getCounter() > 0) {
				if(!GracePeriod.isRunning()) {
					BossBar.display("&c&lDeathmatch in &e" + miniGame.getCounterAsString());
					if(miniGame.getCounter() <= 5 || (miniGame.getCounter() < 60 && miniGame.getCounter() % 10 == 0)) {
						MessageHandler.alert("Deathmatch in &e" + miniGame.getCounterAsString());
					}
				}
				if(miniGame.canDisplay()) {
					EffectUtil.playSound(Sound.CLICK);
				}
				ProMcGames.getSidebar().update("&aIn Game " + miniGame.getCounterAsString());
			}
		}
	}
	
	@EventHandler
	public void onGameWaiting(GameWaitingEvent event) {
		World lobby = ProMcGames.getMiniGame().getLobby();
		if(ProMcGames.getPlugin() == Plugins.SURVIVAL_GAMES) {
			if(!alreadyLoadedMaps) {
				alreadyLoadedMaps = true;
				VotingHandler.loadMaps();
			}
		} else {
			if(ClanBattleSetup.getSetupPhase() == SetupPhase.DONE) {
				for(Player player : ProPlugin.getPlayers()) {
					player.teleport(lobby.getSpawnLocation());
				}
				if(!alreadyLoadedMaps) {
					alreadyLoadedMaps = true;
					VotingHandler.loadMaps("survivalgames");
				}
			} else {
				new ClanBattleSetup();
			}
		}
		World world = lobby;
		Block block = world.getBlockAt(-6, 28, -43);
		block.setType(Material.CHEST);
		block.setData((byte) 3);
		block = world.getBlockAt(-5, 28, -42);
		block.setType(Material.CHEST);
		block.setData((byte) 4);
		/*new NPCEntity(EntityType.SKELETON, "&c&nCompetitive Clans", new Location(world, -10.5, 26, -36.5)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "");
				ChatClickHandler.sendMessageToRunCommand(player, "&6Click to join &cClans", "Click to join to Clans", "/join clans");
				MessageHandler.sendMessage(player, "");
			}
		};*/
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		if(ProMcGames.getPlugin() == Plugins.SURVIVAL_GAMES) {
			arena = VotingHandler.loadWinningWorld();
		} else {
			arena = VotingHandler.loadWinningWorld("survivalgames");
		}
		spawnPointHandler = new SpawnPointHandler(arena);
		List<Location> spawns = spawnPointHandler.getSpawns();
		SurvivalGames.arenaCenter = SpawnPointHandler.getCenter(arena, spawns);
		new StartingGlassAnimation(spawns);
		if(ProMcGames.getMiniGame() != null && SurvivalGames.getSpawnChangingSnowballsEnabled()) {
			new SpawnPointSelector();
		}
		if(!SurvivalGames.isClanBattle()) {
			spawnPointHandler.teleport(ProPlugin.getPlayers());
		}
		new BelowNameHealthScoreboardUtil();
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		PlayerMoveEvent.getHandlerList().unregister(this);
		MiniGame miniGame = ProMcGames.getMiniGame();
		miniGame.setAllowFoodLevelChange(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setDropItemsOnLeave(true);
		miniGame.setAllowEntityCombusting(true);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowInventoryClicking(true);
		miniGame.setCounter(60 * 20);
		new GracePeriod(SurvivalGames.getGracePeriodSeconds());
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		SurvivalGames.setRounds(SurvivalGames.getRounds() - 1);
		if(SurvivalGames.getRounds() > 0) {
			new SurvivalGames();
			for(Player player : Bukkit.getOnlinePlayers()) {
				ProPlugin.resetPlayer(player);
				Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, null));
				for(Player online : Bukkit.getOnlinePlayers()) {
					online.showPlayer(player);
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGracePeriodEnding(GracePeriodEndingEvent event) {
		if(!SurvivalGames.getCanUseSponsors()) {
			return;
		}
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				MessageHandler.alertLine();
				MessageHandler.alert("");
				MessageHandler.alert("&6Giving out auto sponsors due to voting &c/vote");
				MessageHandler.alert("");
				MessageHandler.alertLine();
				for(Player player : ProPlugin.getPlayers()) {
					if(DB.PLAYERS_SG_AUTO_SPONSORS.isUUIDSet(Disguise.getUUID(player))) {
						int amount = DB.PLAYERS_SG_AUTO_SPONSORS.getInt("uuid", Disguise.getUUID(player).toString(), "amount") - 1;
						if(amount > 0) {
							DB.PLAYERS_SG_AUTO_SPONSORS.updateInt("amount", amount, "uuid", Disguise.getUUID(player).toString());
						} else {
							DB.PLAYERS_SG_AUTO_SPONSORS.deleteUUID(Disguise.getUUID(player));
						}
						SponsorHandler.sponsor(player, null, false);
						MessageHandler.sendMessage(player, "&6You now have &c" + amount + " &6games left with this perk");
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		ProPlugin.dispatchCommandToGroup("sghub", "gameWon " + AccountHandler.getPrefix(event.getPlayer()) + " &ewon &cSurvival Games &eon " + ProMcGames.getServerName().toUpperCase());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(ProMcGames.getMiniGame().getGameState() != GameStates.STARTED && event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			if(block.getType() == Material.CHEST) {
				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();
				if((x == -6 || x == -5) && y == 28 && (z == -43 || z == -42)) {
					TieringHandler.explain(event.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer())) {
			MiniGame miniGame = ProMcGames.getMiniGame();
			Location to = event.getTo();
			if(miniGame.getGameState() == GameStates.STARTING && to.getWorld().getName().equals(arena.getName())) {
				Location from = event.getFrom();
				if(to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ()) {
					event.setTo(from);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(allowedToBreak.contains(event.getBlock().getType())) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(block.getType() == Material.TNT) {
			if(GracePeriod.isRunning()) {
				MessageHandler.sendMessage(player, "&cCannot place " + block.getType().toString() + " during grace period");
			} else {
				event.getBlock().setType(Material.AIR);
				TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().add(0, 1, 0),  EntityType.PRIMED_TNT);
				tnt.setFuseTicks(tnt.getFuseTicks() / 2);
				event.setCancelled(false);
			}
		} else if(allowedToBreak.contains(block.getType())) {
			if(block.getType() == Material.FIRE && GracePeriod.isRunning()) {
				MessageHandler.sendMessage(player, "&cCannot place " + block.getType().toString() + " during grace period");
			} else {
				event.setCancelled(false);
			}
		}
		if(!event.isCancelled()) {
			for(Entity entity : block.getWorld().getEntities()) {
				if(entity instanceof ItemFrame) {
					Location itemFrameLocation = entity.getLocation();
					Location blockLocation = block.getLocation();
					if(itemFrameLocation.getBlockX() == blockLocation.getBlockX()) {
						if(itemFrameLocation.getBlockY() == blockLocation.getBlockY()) {
							if(itemFrameLocation.getBlockZ() == blockLocation.getBlockZ()) {
								entity.remove();
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.FALL && !event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player) && event.getDamage() >= 8.0d && event.getDamage() < player.getHealth() && player.getVehicle() == null) {
				PlayerBreakLegsEvent breakLegsEvent = new PlayerBreakLegsEvent(player);
				Bukkit.getPluginManager().callEvent(breakLegsEvent);
				if(!breakLegsEvent.isCancelled()) {
					int duration = 10;
					MessageHandler.alert(AccountHandler.getPrefix(player, false) + " has fallen and can't get up!");
					MessageHandler.alert("(Broken legs, " + duration + " seconds of slowness)");
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * duration, 0));
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerBreakLegs(PlayerBreakLegsEvent event) {
		if(!SurvivalGames.getBreakingLegs()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH) // Priority is high so the death match cancellation will cancel this
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.ENDER_PEARL && !event.isCancelled()) {
			event.getPlayer().teleport(event.getTo()); // Teleport manually to prevent damaging the player
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(cannotSpawn.contains(event.getEntity().getItemStack().getType())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		if(item != null && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
			if(item.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Ultra Golden Apple (" + ChatColor.GOLD + "+4 Hearts" + ChatColor.LIGHT_PURPLE + ")")) {
				Player player = event.getPlayer();
				double newHealth = event.getPlayer().getHealth() + 4.0d;
				player.setHealth(newHealth > 20 ? 20 : newHealth);
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 0));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(ProMcGames.getMiniGame().getGameState() != GameStates.STARTED) {
			event.setCancelled(true);
			event.getPlayer().closeInventory();
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		event.getPlayer().setLevel(event.getPlayer().getLevel() + 1);
		EffectUtil.playSound(event.getPlayer(), Sound.LEVEL_UP);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		ItemStack skull = new ItemCreator(ItemUtil.getSkull(player.getName())).setName(Disguise.getName(player) + "'s Skull").getItemStack();
		player.getWorld().dropItem(player.getLocation(), skull);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		checkForDeathmatchStart();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		checkForDeathmatchStart();
	}
	
	private void checkForDeathmatchStart() {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				if(ProPlugin.getPlayers().size() <= SurvivalGames.getForceDMPlayers() && ProMcGames.getProPlugin().getCounter() > 61) {
					ProMcGames.getProPlugin().setCounter(61);
					BossBar.setCounter(ProMcGames.getMiniGame().getCounter());
				}
			}
		}, 10);
	}
}
