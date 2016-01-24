package promcgames.gameapi;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameVotingEvent;
import promcgames.customevents.game.GameWaitingEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.Loading;

public class MiniGameEvents implements Listener {
	public MiniGameEvents() {
		EventUtil.register(this);
	}
	
	private MiniGame getMiniGame() {
		return ProMcGames.getMiniGame();
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProPlugin.getPlayers().size() > 0) {
			MiniGame miniGame = getMiniGame();
			GameStates gameState = miniGame.getGameState();
			//boolean isEvent = ProMcGames.getPlugin() == Plugins.UHC && HostedEvent.isEvent();
			if(gameState == GameStates.WAITING) {
				int waitingFor = miniGame.getRequiredPlayers() - ProPlugin.getPlayers().size();
				if(waitingFor <= 0) {
					miniGame.setGameState(GameStates.VOTING);
				} else {
					if(ProMcGames.getMiniGame().getUpdateBossBar()) {
						BossBar.display("&bWaiting for &e" + waitingFor + (waitingFor == 1 ? " &bplayer" : " &bplayers"));
					}
					if(ProMcGames.getMiniGame().getUpdateTitleSidebar()) {
						ProMcGames.getSidebar().setName("&aWaiting for " + waitingFor + (waitingFor == 1 ? " player" : " players"));
					}
				}
			} else if(gameState == GameStates.VOTING) {
				if(miniGame.getCounter() <= 0) {
					miniGame.setGameState(GameStates.STARTING);
				} else {
					BossBar.display("&cTeleporting in &e" + miniGame.getCounterAsString());
					if(miniGame.getCounter() <= 5) {
						MessageHandler.alert("&aVoting ends in &e" + miniGame.getCounterAsString());
					}
					if(miniGame.canDisplay()) {
						EffectUtil.playSound(Sound.CLICK);
					}
					if(ProMcGames.getMiniGame().getUpdateTitleSidebar()) {
						ProMcGames.getSidebar().update("&aVoting " + miniGame.getCounterAsString());
					}
				}
			} else if(gameState == GameStates.STARTING) {
				if(miniGame.getStoreStats() && miniGame.getCounter() == 10) {
					if(StatsHandler.isEnabled()) {
						new Loading("Player's Stats", "/stats");
						for(Player player : ProPlugin.getPlayers()) {
							try {
								StatsHandler.loadStats(player);
							} catch(NullPointerException e) {
								
							}
						}
					}
				}
				if(miniGame.getCounter() <= 0) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						new TitleDisplayer(player, "&cGame Started!").setFadeIn(20).setStay(20).setFadeOut(20).display();
					}
					miniGame.setGameState(GameStates.STARTED);
				} else {
					if(ProMcGames.getMiniGame().getUpdateBossBar()) {
						BossBar.display("&cStarting in &e" + miniGame.getCounterAsString());
					}
					if(miniGame.getCounter() <= 5) {
						MessageHandler.alert("&aStarting in &e" + miniGame.getCounterAsString());
					}
					if(miniGame.canDisplay()) {
						EffectUtil.playSound(Sound.CLICK);
					}
					if(ProMcGames.getMiniGame().getUpdateTitleSidebar()) {
						ProMcGames.getSidebar().update("&aStarting " + miniGame.getCounterAsString());
					}
					if(miniGame.getCounter() <= 5) {
						for(Player player : Bukkit.getOnlinePlayers()) {
							String color = "&e";
							if(miniGame.getCounter() == 2) {
								color = "&c";
							} else if(miniGame.getCounter() == 1) {
								color = "&4";
							}
							new TitleDisplayer(player, "&bStarting in " + color + miniGame.getCounter()).display();
						}
					}
				}
			}
		}
		ProMcGames.getSidebar().update();
		getMiniGame().decrementCounter();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameWaiting(GameWaitingEvent event) {
		BossBar.setCounter(-1);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameVoting(GameVotingEvent event) {
		BossBar.setCounter(getMiniGame().getCounter());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameStarting(GameStartingEvent event) {
		BossBar.setCounter(getMiniGame().getCounter());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPreGameEnding(GameEndingEvent event) {
		getMiniGame().setCounter(5);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameEnding(GameEndingEvent event) {
		if(event.isCancelled()) {
			getMiniGame().setGameState(GameStates.STARTED, false);
		} else {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					ProPlugin.restartServer();
				}
			}, 20 * getMiniGame().getCounter());
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(ProMcGames.getMiniGame().getJoiningPreGame()) {
			Player player = event.getPlayer();
			if(getMiniGame().getGameState() == GameStates.STARTING && getMiniGame().getCanJoinWhileStarting() && ProPlugin.getPlayers().size() > 0) {
				player.teleport(ProPlugin.getPlayers().get(0));
			} else {
				player.teleport(new Location(getMiniGame().getLobby(), -17.5, 27, -29.5, -135.30f, -7.79f));
			}
			if(ProMcGames.getMiniGame().getGameState() == GameStates.WAITING && ProPlugin.getPlayers().size() >= ProMcGames.getMiniGame().getRequiredPlayers()) {
				ProMcGames.getMiniGame().setGameState(GameStates.VOTING);
			}
		}
		new TitleDisplayer(event.getPlayer(), "&aWelcome to", "&c&l" + getMiniGame().getDisplayName()).setFadeIn(40).setStay(60).setFadeOut(40).display();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		GameStates state = getMiniGame().getGameState();
		if(Ranks.ELITE.hasRank(player) && !Disguise.isDisguised(player) && (state == GameStates.WAITING || state == GameStates.VOTING)) {
			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				GameStates gameState = ProMcGames.getMiniGame().getGameState();
				List<Player> players = ProPlugin.getPlayers();
				int playing = players.size();
				Player leaving = event.getPlayer();
				if(gameState == GameStates.VOTING && playing < ProMcGames.getMiniGame().getRequiredPlayers()) {
					MessageHandler.alert("&cNot enough players");
					ProMcGames.getMiniGame().setGameState(GameStates.WAITING);
				} else if(gameState == GameStates.STARTING && playing == 1 && !ProMcGames.getMiniGame().getTeamBased()) {
					MessageHandler.alert("&cNot enough players");
					ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
				}
				if(gameState == GameStates.STARTING || gameState == GameStates.STARTED) {
					if(ProMcGames.getMiniGame() != null) {
						if(!ProMcGames.getMiniGame().getTeamBased()) {
							if(playing == 1 && ProMcGames.getMiniGame().getRestartWithOnePlayerLeft()) {
								Player winner = players.get(0);
								if(winner.getName().equals(leaving.getName())) {
									winner = players.get(1);
								}
								Bukkit.getPluginManager().callEvent(new GameWinEvent(winner));
							} else if(playing == 0) {
								ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
							}
						}
					}
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(ProMcGames.getMiniGame().getPlayersHaveOneLife()) {
			if(ProMcGames.getMiniGame() != null && !ProMcGames.getMiniGame().getTeamBased()) {
				Bukkit.getPluginManager().callEvent(new GameLossEvent(event.getPlayer()));
			}
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					if(!ProMcGames.getMiniGame().getTeamBased()) {
						List<Player> players = ProPlugin.getPlayers();
						if(players.size() == 1 && ProMcGames.getMiniGame().getRestartWithOnePlayerLeft()) {
							Bukkit.getPluginManager().callEvent(new GameWinEvent(players.get(0)));
						} else if(players.size() == 0) {
							ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getEndServer() && getMiniGame().getGameState() != GameStates.ENDING) {
			getMiniGame().setGameState(GameStates.ENDING);
		}
		getMiniGame().setAllowEntityDamage(false);
		if(event.getPlayer() != null) {
			Player winner = event.getPlayer();
			winner.setAllowFlight(true);
			winner.setFlying(true);
			MessageHandler.alert("Fly mode enabled for " + AccountHandler.getPrefix(winner));
		} else if(event.getTeam() != null) {
			for(OfflinePlayer offlinePlayer : event.getTeam().getPlayers()) {
				Player player = offlinePlayer.getPlayer();
				if(player != null) {
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			}
			Clan clan = ClanHandler.getClan(event.getTeam().getName());
			if(clan != null) {
				MessageHandler.alert("Fly mode enabled for all players in " + clan.getColorTheme() + clan.getClanName());
			}
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() != SpawnReason.CUSTOM && event.getEntity().getWorld().getName().equals("lobby")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		GameStates gameState = getMiniGame().getGameState();
		if(gameState == GameStates.WAITING || gameState == GameStates.VOTING || gameState == GameStates.STARTING) {
			if(!event.getTo().getWorld().getName().equals(event.getFrom().getWorld().getName()) && event.getPlayer().getAllowFlight() && !SpectatorHandler.contains(event.getPlayer())) {
				event.getPlayer().setFlying(false);
				event.getPlayer().setAllowFlight(false);
			}
		}
	}
}
