package promcgames.gameapi;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameVotingEvent;
import promcgames.customevents.game.GameWaitingEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.game.PostGameStartingEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ServerLogger;
import promcgames.server.nms.npcs.NPCRegistrationHandler.NPCs;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.FileHandler;

public abstract class MiniGame extends ProPlugin {
	public enum GameStates {WAITING, VOTING, STARTING, STARTED, ENDING}
	private int requiredPlayers = 4;
	private int votingCounter = 61;
	private int startingCounter = 30;
	private int endingCounter = 5;
	private boolean autoJoin = true;
	private boolean canJoinWhileStarting = true;
	private boolean useSpectatorChatChannel = true;
	private boolean playersHaveOneLife = true;
	private boolean teamBased = false;
	private boolean storeStats = true;
	private boolean restartWithOnePlayerLeft = true;
	private boolean updateTitleSidebar = true;
	private boolean updateBossBar = true;
	private World lobby = null;
	private GameStates gameState = GameStates.WAITING;
	
	public MiniGame(String name) {
		super(name);
		ProMcGames.setMiniGame(this);
		addGroup("mini-game");
		new MiniGameEvents();
		new SpectatorHandler();
		new AutoJoinHandler();
		new PerformanceLogger();
		new PostGameStartEvent(true);
		new PostGameStartingEvent(true);
		new ServerLogger();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				setGameState(GameStates.WAITING);
			}
		});
		setLobby(Bukkit.getWorlds().get(0));
		lobby.setTime(12250);
		new CommandBase("startGame", 0) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				setGameState(GameStates.VOTING);
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		new CommandBase("setTimer", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]));
					if(BossBar.getCounter() > 0) {
						BossBar.setCounter(getCounter());
					}
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}.setRequiredRank(Ranks.DEV);
		new CommandBase("win", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(ProMcGames.getMiniGame() != null && !ProMcGames.getMiniGame().getTeamBased()) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(player));
				} else if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getTeamBased()) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(TeamHandler.getTeam(player)));
				}
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		setUseTop8(true);
		setToDefaultSidebar();
	}
	
	@Override
	public void disable() {
		super.disable();
		String container = Bukkit.getWorldContainer().getPath();
		for(World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, false);
			if(world.getName().equals("lobby") && !new File(container + "/../resources/maps/lobby").exists()) {
				continue;
			}
			FileHandler.delete(new File(container + "/" + world.getName()));
		}
		if(!new File(container + "/lobby").exists()) {
			FileHandler.copyFolder(new File(container + "/../resources/maps/lobby"), new File(container + "/lobby"));
		}
	}
	
	@Override
	public void resetFlags() {
		super.resetFlags();
		setAutoJoin(true);
		setCanJoinWhileStarting(true);
		setUseSpectatorChatChannel(true);
		setPlayersHaveOneLife(true);
		setStoreStats(true);
		setUpdateTitleSidebar(true);
		setUpdateBossBar(true);
	}
	
	public int getVotingCounter() {
		return this.votingCounter;
	}
	
	public boolean getUpdateTitleSidebar() {
		return updateTitleSidebar;
	}
	
	public void setUpdateTitleSidebar(boolean updateTitleSidebar) {
		this.updateTitleSidebar = updateTitleSidebar;
	}
	
	public boolean getUpdateBossBar() {
		return updateBossBar;
	}
	
	public void setUpdateBossBar(boolean updateBossBar) {
		this.updateBossBar = updateBossBar;
	}
	
	public void setVotingCounter(int votingCounter) {
		this.votingCounter = votingCounter;
		if(getGameState() == GameStates.VOTING) {
			setCounter(votingCounter);
		}
	}
	
	public int getStartingCounter() {
		return this.startingCounter;
	}
	
	public void setStartingCounter(int startingCounter) {
		this.startingCounter = startingCounter;
		if(getGameState() == GameStates.STARTING) {
			setCounter(startingCounter);
		}
	}
	
	public int getEndingCounter() {
		return this.endingCounter;
	}
	
	public void setEndingCounter(int endingCounter) {
		this.endingCounter = endingCounter;
	}
	
	public int getRequiredPlayers() {
		return this.requiredPlayers;
	}
	
	public void setRequiredPlayers(int requiredPlayers) {
		this.requiredPlayers = requiredPlayers;
	}
	
	public void setTeamBased(boolean teamBased) {
		this.teamBased = teamBased;
	}
	
	public boolean getTeamBased() {
		return this.teamBased;
	}
	
	public void setStoreStats(boolean storeStats) {
		this.storeStats = storeStats;
	}
	
	public boolean getStoreStats() {
		return storeStats;
	}
	
	public void setAutoJoin(boolean autoJoin) {
		this.autoJoin = autoJoin;
	}
	
	public boolean getAutoJoin() {
		return this.autoJoin;
	}
	
	public boolean getCanJoinWhileStarting() {
		return this.canJoinWhileStarting;
	}
	
	public void setCanJoinWhileStarting(boolean canJoinWhileStarting) {
		this.canJoinWhileStarting = canJoinWhileStarting;
	}
	
	public boolean getJoiningPreGame() {
		MiniGame miniGame = ProMcGames.getMiniGame();
		GameStates gameState = miniGame.getGameState();
		return gameState == GameStates.WAITING || gameState == GameStates.VOTING || (gameState == GameStates.STARTING && getCanJoinWhileStarting());
	}
	
	public boolean getUseSpectatorChatChannel() {
		return this.useSpectatorChatChannel;
	}
	
	public void setUseSpectatorChatChannel(boolean useSpectatorChatChannel) {
		this.useSpectatorChatChannel = useSpectatorChatChannel;
	}
	
	public boolean getPlayersHaveOneLife() {
		return this.playersHaveOneLife;
	}
	
	public void setPlayersHaveOneLife(boolean oneLife) {
		this.playersHaveOneLife = oneLife;
	}
	
	public boolean getRestartWithOnePlayerLeft() {
		return this.restartWithOnePlayerLeft;
	}
	
	public void setRestartWithOnePlayerLeft(boolean restartWithOnePlayerLeft) {
		this.restartWithOnePlayerLeft = restartWithOnePlayerLeft;
	}
	
	public World getLobby() {
		return this.lobby;
	}
	
	public void setLobby(World lobby) {
		this.lobby = lobby;
	}
	
	public GameStates getGameState() {
		return this.gameState;
	}
	
	public void setGameState(GameStates gameState) {
		setGameState(gameState, true);
	}
	
	public void setGameState(GameStates gameState, boolean callEvents) {
		this.gameState = gameState;
		if(callEvents) {
			if(getGameState() == GameStates.WAITING) {
				setCounter(0);
				Bukkit.getPluginManager().callEvent(new GameWaitingEvent());
				World lobby = Bukkit.getWorld("lobby");
				if(lobby != null) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(!player.getWorld().getName().equals(lobby.getName())) {
							player.teleport(lobby.getSpawnLocation());
						}
					}
				}
			} else if(getGameState() == GameStates.VOTING) {
				setCounter(getVotingCounter());
				Bukkit.getPluginManager().callEvent(new GameVotingEvent());
			} else if(getGameState() == GameStates.STARTING) {
				for(NPCs npc : NPCs.values()) {
					npc.unregister();
				}
				setCounter(getStartingCounter());
				Bukkit.getPluginManager().callEvent(new GameStartingEvent());
			} else if(getGameState() == GameStates.STARTED) {
				setCounter(0);
				for(Player player : getPlayers()) {
					player.getInventory().clear();
					player.getInventory().setHeldItemSlot(0);
				}
				EffectUtil.playSound(Sound.EXPLODE);
				MessageHandler.alert("Game started!");
				Bukkit.getPluginManager().callEvent(new GameStartEvent());
			} else if(getGameState() == GameStates.ENDING) {
				setCounter(getEndingCounter());
				Bukkit.getPluginManager().callEvent(new GameEndingEvent());
			}
		}
	}
	
	public void setToDefaultSidebar() {
		ProMcGames.setSidebar(new SidebarScoreboardUtil(ChatColor.AQUA + getDisplayName()) {
			@Override
			public void update() {
				setText("Players", ProPlugin.getPlayers().size());
				setText("Spectators", SpectatorHandler.getNumberOf());
				setText(new String [] {" ", "Server:", ChatColor.RED + ProMcGames.getServerName()});
				super.update();
			}
		});
	}
}
