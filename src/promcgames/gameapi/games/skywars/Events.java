package promcgames.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameWaitingEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.VotingHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.player.scoreboard.BelowNameHealthScoreboardUtil;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	private World arena = null;
	private List<String> delayed = null;
	private int delay = 5;
	private boolean endGame = false;
	private boolean red = true;
	private boolean alreadyLoadedMaps = false;
	
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		MiniGame miniGame = ProMcGames.getMiniGame();
		GameStates gameState = miniGame.getGameState();
		if(gameState == GameStates.STARTING) {
			if(miniGame.getCounter() == 10) {
				DeathBeamHandler.loadBeams();
			}
		} else if(gameState == GameStates.STARTED) {
			if(miniGame.getCounter() <= 0) {
				OneSecondTaskEvent.getHandlerList().unregister(this);
				endGame = true;
				MessageHandler.alertLine("&6");
				MessageHandler.alert("");
				MessageHandler.alert("&4Fireballs incoming!");
				MessageHandler.alert("");
				MessageHandler.alertLine("&6");
			} else if(miniGame.getCounter() > 0) {
				if(miniGame.canDisplay()) {
					EffectUtil.playSound(Sound.CLICK);
				}
				BossBar.display("&c&lGame ending in &e" + miniGame.getCounterAsString());
				BossBar.setCounter(ProMcGames.getMiniGame().getCounter());
				if(miniGame.getCounter() <= 5 || (miniGame.getCounter() < 60 && miniGame.getCounter() % 10 == 0)) {
					MessageHandler.alert("Game Ending in &e" + miniGame.getCounterAsString());
				}
				ProMcGames.getSidebar().update("&aIn Game " + miniGame.getCounterAsString());
			}
		}
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		if(endGame) {
			if(red) {
				ProMcGames.getSidebar().setName("&4Fireballs");
			} else {
				ProMcGames.getSidebar().setName("&6Fireballs");
			}
			red = !red;
			BossBar.display("&4&lFireballs Incoming!");
			for(Player player : ProPlugin.getPlayers()) {
				Fireball fireball = (Fireball) player.getWorld().spawnEntity(player.getLocation().add(0, 25, 0), EntityType.FIREBALL);
				fireball.setVelocity(new Vector(0, -3, 0));
			}
		}
	}
	
	@EventHandler
	public void onGameWaiting(GameWaitingEvent event) {
		if(!alreadyLoadedMaps) {
			alreadyLoadedMaps = true;
			VotingHandler.loadMaps();
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		arena = VotingHandler.loadWinningWorld();
		new BelowNameHealthScoreboardUtil();
		if(ProMcGames.getPlugin() == Plugins.SKY_WARS) {
			MessageHandler.alertLine();
			MessageHandler.alert("&4&lTeaming is not allowed in solo mode");
			MessageHandler.alertLine();
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		MiniGame miniGame = ProMcGames.getMiniGame();
		miniGame.setAllowFoodLevelChange(true);
		miniGame.setAllowDroppingItems(true);
		miniGame.setAllowPickingUpItems(true);
		miniGame.setDropItemsOnLeave(true);
		miniGame.setAllowBuilding(true);
		miniGame.setAllowEntityCombusting(true);
		miniGame.setAllowPlayerInteraction(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowInventoryClicking(true);
		miniGame.setAllowItemSpawning(true);
		miniGame.setFlintAndSteelUses(4);
		miniGame.setCounter(60 * 8);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MiniGame miniGame = ProMcGames.getMiniGame();
				miniGame.setAllowEntityDamageByEntities(true);
				miniGame.setAllowEntityDamage(true);
			}
		}, 20 * 5);
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		Player player = event.getPlayer();
		player.setLevel(player.getLevel() + 1);
		EffectUtil.playSound(player, Sound.LEVEL_UP);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if(SpectatorHandler.contains(player) && !Ranks.isStaff(player) && to.getY() < 62 && to.getWorld().getName().equals(arena.getName())) {
			to.setY(62);
			player.setFlying(true);
			player.teleport(to);
			if(delayed == null) {
				delayed = new ArrayList<String>();
			}
			final String name = player.getName();
			if(!delayed.contains(name)) {
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, 20 * delay);
				MessageHandler.sendMessage(player, "&cYou cannot go below &eY 62 &cas a spectator");
				MessageHandler.sendMessage(player, "This is to prevent blocking building & killing players");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		GameStates state = ProMcGames.getMiniGame().getGameState();
		if((state == GameStates.STARTED || state == GameStates.ENDING) && !Ranks.PRO.hasRank(event.getPlayer())) {
			event.setResult(Result.KICK_OTHER);
			event.setKickMessage("You must have " + Ranks.PRO.getPrefix() + ChatColor.RED + "to spectate Sky Wars" + ChatColor.AQUA + " /buy");
		}
		if(Ranks.isStaff(event.getPlayer())) {
			event.setResult(Result.ALLOWED);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player) && ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(event.getBlock().getType() == Material.GLASS) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		String type = event.getBlock().getType().toString();
		if(type.contains("LAVA") || type.contains("WATER")) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		List<Block> blocks = event.blockList();
		if(blocks != null && !blocks.isEmpty()) {
			for(Block block : blocks) {
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
			blocks.clear();
			blocks = null;
		}
	}
}
