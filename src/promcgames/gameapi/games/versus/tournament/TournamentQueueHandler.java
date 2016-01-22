package promcgames.gameapi.games.versus.tournament;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class TournamentQueueHandler implements Listener {
	
	private static TournamentQueueHandler instance = null;
	private List<String> queue = null;
	private List<String> delayed = null;
	private boolean queueEnabled = false;
	private boolean countdownRunning = false;
	private static boolean commandsRegistered = false;
	private static boolean npcsRegistered = false;
	private static final boolean construction = true;
	private int countdown = 0;
	private final int stopPlayers = 1;
	private final int startPlayers = 2;
	
	public TournamentQueueHandler() {
		if(instance == null) {
			instance = this;
			queue = new ArrayList<>();
			delayed = new ArrayList<>();
			if(!commandsRegistered) {
				commandsRegistered = true;
				registerCommands();
			}
			if(!npcsRegistered) {
				npcsRegistered = true;
				registerNPCs();
			}
			if(!construction) {
				EventUtil.register(this);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		queue.remove(event.getPlayer().getName());
		if(queue.size() <= stopPlayers && countdownRunning) {
			countdownRunning = false;
			MessageHandler.alert("Versus tournament countdown cancelled due to too little players");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		Player player = event.getPlayer();
		if(queue.contains(player.getName())) {
			event.setCancelled(true);
			MessageHandler.sendMessage(player, "&cYou can't spectate whilst in a 1v1 tournament queue");
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(countdownRunning) {
			if(queue.size() <= stopPlayers && countdownRunning) {
				countdownRunning = false;
				MessageHandler.alert("Versus tournament countdown cancelled due to too little players");
			}
			if(countdown == 600) {
				MessageHandler.alert("A versus tournament will start in &b10 minutes");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 450) {
				MessageHandler.alert("A versus tournament will start in &b7 minutes 30 seconds");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 300) {
				MessageHandler.alert("A versus tournament will start in &b5 minutes");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 60) {
				MessageHandler.alert("A versus tournament will start in &b1 minute");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 0) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tournament start");
				countdownRunning = false;
			}
			--countdown;
		}
	}
	
	private static void registerNPCs() {
		new NPCEntity(EntityType.SKELETON, "&bTournament", new Location(Bukkit.getWorlds().get(0), -3.5, 5, 11.5, -180.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				final TournamentQueueHandler instance = getInstance();
				if(construction) {
					MessageHandler.sendMessage(player, "&cComing soon");
				} else if(VersusTournament.getEnabled()) {
					MessageHandler.sendMessage(player, "&cA versus tournament is already in progress");
				} else if(instance.queueEnabled) {
					final String playerName = player.getName();
					if(!instance.delayed.contains(playerName)) {
						instance.delayed.add(playerName);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								instance.delayed.remove(playerName);
							}
						}, 20L);
						if(instance.queue.contains(playerName)) {
							instance.queue.remove(playerName);
							MessageHandler.sendMessage(player, "You have been removed from the queue");
						} else if(SpectatorHandler.contains(player)) {
							MessageHandler.sendMessage(player, "&cYou can't queue as a spectator");
						} else {
							instance.queue.add(playerName);
							MessageHandler.sendMessage(player, "You have been added to the queue");
							MessageHandler.sendMessage(player, "The tournament will begin soon");
							MessageHandler.sendMessage(player, "To leave the queue, click the NPC again");
							if(instance.queue.size() >= instance.startPlayers && !instance.countdownRunning) {
								instance.countdown = 600;
								instance.countdownRunning = true;
							}
						}
					}
				} else {
					MessageHandler.sendMessage(player, "&cVersus tournament is currently not active");
					MessageHandler.sendMessage(player, "&cStay updated on Twitter &e@ProMcGames");
				}
			}
		};
	}
	
	private static void registerCommands() {
		new CommandBase("queue", -1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!Ranks.DEV.hasRank(player)) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				final TournamentQueueHandler instance = getInstance();
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(sender, "&b/queue start &eAllow players to start queueing up");
					MessageHandler.sendMessage(sender, "&b/queue stop &eClear queue and stop players from queueing up");
					MessageHandler.sendMessage(sender, "&b/queue list &eList the queue");
					MessageHandler.sendMessage(sender, "&b/queue startTimer [seconds] &eStarts the timer");
					MessageHandler.sendMessage(sender, "&b/queue stopTimer &eStops the timer");
				} else if(arguments[0].equalsIgnoreCase("start")) {
					instance.setQueueEnabled(true);
					MessageHandler.alert("A versus tournament is now available to join");
					MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
				} else if(arguments[0].equalsIgnoreCase("stop")) {
					instance.setQueueEnabled(false);
					instance.queue.clear();
					instance.countdownRunning = true;
					MessageHandler.sendMessage(sender, "Versus tournament queue has been cleared and stopped");
					MessageHandler.alert("The versus tournament has been cancelled");
				} else if(arguments[0].equalsIgnoreCase("list")) {
					MessageHandler.sendMessage(sender, "Queue size: " + instance.queue.size());
					String message = "Players: ";
					for(String playerName : instance.queue) {
						message += playerName + (playerName.equals(instance.queue.get(instance.queue.size() - 1)) ? "" : ", ");
					}
					MessageHandler.sendMessage(sender, message);
				} else if(arguments[0].equalsIgnoreCase("startTimer")) {
					if(arguments.length == 2) {
						int seconds = 0;
						try {
							seconds = Integer.valueOf(arguments[1]);
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&cIncorrect usage &e/queue startTimer [seconds]");
							return true;
						}
						instance.countdown = seconds;
						instance.countdownRunning = true;
						MessageHandler.sendMessage(sender, "Counter started at &b" + seconds + " seconds");
					} else if(arguments.length == 1) {
						instance.countdownRunning = true;
						MessageHandler.sendMessage(sender, "The countdown has been resumed");
					} else {
						MessageHandler.sendMessage(sender, "&cIncorrect usage &e/queue startTimer [seconds]");
					}
				} else if(arguments[0].equalsIgnoreCase("stopTimer")) {
					instance.countdownRunning = false;
					MessageHandler.sendMessage(sender, "The countdown has been halted");
				} else {
					MessageHandler.sendMessage(sender, "&cInvalid arguments &e/queue help");
				}
				return true;
			}
		};
		new CommandBase("tournament", -1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!Ranks.DEV.hasRank(player)) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(sender, "&b/tournament start &eStart the tournament");
				} else if(arguments[0].equalsIgnoreCase("start")) {
					if(VersusTournament.getEnabled()) {
						MessageHandler.sendMessage(sender, "&cA versus tournament is already in progress");
					} else if(instance.queue.size() >= 8) {
						MessageHandler.sendMessage(sender, "&cThere must be at least &b8 &cpeople in the queue before starting the tournament");
					} else {
						new VersusTournament();
						instance.queue.clear();
						instance.delayed.clear();
						instance.queueEnabled = false;
						instance.countdownRunning = false;
						instance.countdown = 0;
						MessageHandler.sendMessage(sender, "A new versus tournament has begun");
					}
				} else {
					MessageHandler.sendMessage(sender, "&cInvalid arguments &e/tournament help");
				}
				return true;
			}
		};
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
		queue.clear();
		queue = null;
		delayed.clear();
		delayed = null;
		instance = null;
	}
	
	public void setQueueEnabled(boolean queueEnabled) {
		this.queueEnabled = queueEnabled;
	}
	
	public List<String> getQueue() {
		return queue;
	}
	
	public boolean getQueueEnabled() {
		return queueEnabled;
	}
	
	public static TournamentQueueHandler getInstance() {
		return instance;
	}
	
}