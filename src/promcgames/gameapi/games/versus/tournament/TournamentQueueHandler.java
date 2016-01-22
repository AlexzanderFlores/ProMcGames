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

public class TournamentQueueHandler implements Listener {
	
	private static List<String> queue = null;
	private static List<String> delayed = null;
	private static boolean queueEnabled = false;
	private static boolean construction = true;
	private static boolean countdownRunning = false;
	private static int countdown = 0;
	
	public TournamentQueueHandler() {
		queue = new ArrayList<String>();
		delayed = new ArrayList<String>();
		//EventUtil.register(this);
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
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(sender, "&b/queue start &eAllow players to start queueing up");
					MessageHandler.sendMessage(sender, "&b/queue stop &eClear queue and stop players from queueing up");
					MessageHandler.sendMessage(sender, "&b/queue list &eList the queue");
				} else if(arguments[0].equalsIgnoreCase("start")) {
					setQueueEnabled(true);
					MessageHandler.alert("A versus tournament is now available to join");
					MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
				} else if(arguments[0].equalsIgnoreCase("stop")) {
					setQueueEnabled(false);
					queue.clear();
					countdownRunning = true;
					MessageHandler.sendMessage(sender, "Versus tournament queue has been cleared and stopped");
					MessageHandler.alert("The versus tournament has been cancelled");
				} else if(arguments[0].equalsIgnoreCase("list")) {
					MessageHandler.sendMessage(sender, "Queue size: " + queue.size());
					String message = "Players: ";
					for(String playerName : queue) {
						message += playerName + (playerName.equals(queue.get(queue.size() - 1)) ? "" : ", ");
					}
					MessageHandler.sendMessage(sender, message);
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
					} else if(queue.size() >= 8) {
						MessageHandler.sendMessage(sender, "&cThere must be at least &b8 &cpeople in the queue before starting the tournament");
					} else {
						new VersusTournament();
						MessageHandler.sendMessage(sender, "A new versus tournament has begun");
					}
				} else {
					MessageHandler.sendMessage(sender, "&cInvalid arguments &e/tournament help");
				}
				return true;
			}
		};
		new NPCEntity(EntityType.SKELETON, "&bTournament", new Location(Bukkit.getWorlds().get(0), -3.5, 5, 11.5, -180.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				if(construction) {
					MessageHandler.sendMessage(player, "&cComing soon");
				} else if(queueEnabled) {
					final String playerName = player.getName();
					if(!delayed.contains(playerName)) {
						delayed.add(playerName);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								delayed.remove(playerName);
							}
						}, 20L);
						if(queue.contains(playerName)) {
							queue.remove(playerName);
							MessageHandler.sendMessage(player, "You have been removed from the queue");
							if(queue.size() <= 7 && countdownRunning) {
								countdownRunning = false;
								MessageHandler.alert("Versus tournament countdown cancelled due to too little players");
							}
						} else if(SpectatorHandler.contains(player)) {
							MessageHandler.sendMessage(player, "&cYou can't queue as a spectator");
						} else {
							queue.add(playerName);
							MessageHandler.sendMessage(player, "You have been added to the queue");
							MessageHandler.sendMessage(player, "The tournament will begin soon");
							MessageHandler.sendMessage(player, "To leave the queue, click the NPC again");
							if(queue.size() >= 8 && !countdownRunning) {
								countdown = 600;
								countdownRunning = true;
								MessageHandler.alert("The versus tournament will start in &b10 &aminutes");
								MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
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
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		queue.remove(event.getPlayer().getName());
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
			if(countdown == 450) {
				MessageHandler.alert("Versus tournament will start in &b7 minutes 30 seconds");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 300) {
				MessageHandler.alert("Versus tournament will start in &b5 minutes");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 60) {
				MessageHandler.alert("Versus tournament will start in &b1 minute");
				MessageHandler.alert("Click the &bTournament Skeleton &ain the versus lobby to join");
			} else if(countdown == 0) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tournament start");
				countdownRunning = false;
			}
			--countdown;
		}
	}
	
	public static void setQueueEnabled(boolean queueEnabled) {
		TournamentQueueHandler.queueEnabled = queueEnabled;
	}
	
	public static List<String> getQueue() {
		return queue;
	}
	
	public static boolean getQueueEnabled() {
		return queueEnabled;
	}
	
}