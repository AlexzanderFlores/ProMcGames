package promcgames.gameapi.games.clanbattles.setup;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import promcgames.gameapi.TeamHandler;
import promcgames.gameapi.games.clanbattles.ClanBattle;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup.SetupPhase;
import promcgames.player.MessageHandler;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.servers.clans.ClanHandler;

@SuppressWarnings("deprecation")
public class RosterHandler implements Listener {
	private static boolean rosterLeader1Done = false;
	private static boolean rosterLeader2Done = false;
	
	public RosterHandler() {
		new TeamHandler(2, new String [] {ClanBattle.getClanOne(), ClanBattle.getClanTwo()});
		TeamHandler.add(Bukkit.getOfflinePlayer(ClanBattle.getClanOneLeader()), ClanBattle.getClanOne());
		TeamHandler.add(Bukkit.getOfflinePlayer(ClanBattle.getClanTwoLeader()), ClanBattle.getClanTwo());
		for(Team team : TeamHandler.getTeams()) {
			team.setAllowFriendlyFire(false);
		}
		new CommandBase("roster", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(!canModifyRoster(player)) {
					MessageHandler.sendMessage(player, "&cYou aren't allowed to modify the roster");
					return true;
				}
				if(ClanBattleSetup.getSetupPhase() != SetupPhase.ROSTER && arguments.length == 1 && !arguments[0].equalsIgnoreCase("list")) {
					MessageHandler.sendMessage(player, "&cYou can't modify the roster at this time");
					return true;
				}
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendLine(player, "&a");
					MessageHandler.sendMessage(player, "&b/roster help &eDisplays roster info");
					MessageHandler.sendMessage(player, "&b/roster add <player> &eAdds a player to your roster");
					MessageHandler.sendMessage(player, "&b/roster remove <player> &eRemoves a player from your roster");
					MessageHandler.sendMessage(player, "&b/roster list &eLists the players in your roster");
					MessageHandler.sendMessage(player, "&b/roster submit &eSubmits your roster for use");
					MessageHandler.sendLine(player, "&a");
				} else if(arguments[0].equalsIgnoreCase("add")) {
					if(arguments.length == 2) {
						addPlayer(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/roster add <player>");
					}
				} else if(arguments[0].equalsIgnoreCase("remove")) {
					if(arguments.length == 2) {
						removePlayer(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/roster remove <player>");
					}
				} else if(arguments[0].equalsIgnoreCase("list")) {
					listPlayers(player);
				} else if(arguments[0].equalsIgnoreCase("submit")) {
					submitRoster(player);
				} else {
					MessageHandler.sendMessage(player, "&cInvalid argument. &b/roster help");
				}
				return true;
			}
		};
	}
	
	/*
	 * This method is ran when the /roster add command is executed.
	 */
	public static void addPlayer(Player player, String user) {
		String clanName = ClanHandler.getClanName(user);
		if(clanName == null) {
			MessageHandler.sendMessage(player, "&b" + user + " &cisn't in a clan");
		} else {
			if((player.getName().equalsIgnoreCase(ClanBattle.getClanOneLeader()) && rosterLeader1Done) || (player.getName().equalsIgnoreCase(ClanBattle.getClanTwoLeader()) && rosterLeader2Done)) {
				MessageHandler.sendLine(player, "&b");
				MessageHandler.sendMessage(player, "&cYou have already submitted your roster");
				MessageHandler.sendMessage(player, "Un-submit your roster to make changes");
				ChatClickHandler.sendMessageToRunCommand(player, "&6Click here to un-submit your roster", "Click here to un-submit your roster", "/roster submit");
				MessageHandler.sendLine(player, "&b");
			} else {
				if((player.getName().equalsIgnoreCase(ClanBattle.getClanOneLeader()) && clanName.equalsIgnoreCase(ClanBattle.getClanOne())) || (player.getName().equalsIgnoreCase(ClanBattle.getClanTwoLeader()) && clanName.equalsIgnoreCase(ClanBattle.getClanTwo()))) {
					Team team = getTeam(player);
					if(team.getPlayers().size() < ClanBattleSetup.getMaxPerTeam()) {
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user);
						if(team.hasPlayer(offlinePlayer)) {
							MessageHandler.sendMessage(player, "&cThis user is already in the roster");
						} else {
							team.addPlayer(Bukkit.getOfflinePlayer(user));
							if(team.getSize() == ClanBattleSetup.getMaxPerTeam()) {
								MessageHandler.sendLine(player, "&b");
								MessageHandler.sendMessage(player, "&b" + user + " &ahas been added to the roster");
								ChatClickHandler.sendMessageToRunCommand(player, "&6Click here to submit your roster", "Click here to submit your roster.", "/roster submit");
								MessageHandler.sendLine(player, "&b");
							} else {
								MessageHandler.sendMessage(player, "&b" + user + " &ahas been added to the roster");
							}
						}
					} else {
						MessageHandler.sendMessage(player, "Your roster is full. Remove a player to add another. &b/roster remove");
					}
				} else {
					MessageHandler.sendMessage(player, "&b" + user + " &bis not in your clan");
				}
			}
		}
	}
	
	/*
	 * This method is ran when the /roster remove command is executed.
	 */
	public static void removePlayer(Player player, String user) {
		Team team = getTeam(player);
		if((player.getName().equalsIgnoreCase(ClanBattle.getClanOneLeader()) && rosterLeader1Done) || (player.getName().equalsIgnoreCase(ClanBattle.getClanTwoLeader()) && rosterLeader2Done)) {
			MessageHandler.sendLine(player, "&b");
			MessageHandler.sendMessage(player, "&cYou have already submitted your roster");
			MessageHandler.sendMessage(player, "Un-submit your roster to make changes");
			ChatClickHandler.sendMessageToRunCommand(player, "&6Click here to un-submit your roster", "Click here to un-submit your roster", "/roster submit");
			MessageHandler.sendLine(player, "&b");
		} else {
			if(team.hasPlayer(Bukkit.getOfflinePlayer(user))) {
				team.removePlayer(Bukkit.getOfflinePlayer(user));
				MessageHandler.sendMessage(player, "&b" + user + " &ahas been removed from your current roster");
			} else {
				MessageHandler.sendMessage(player, "&b" + user + " &cis not in your current roster");
			}
		}
	}
	
	/*
	 * This method is ran when the /roster list command is executed.
	 */
	public static void listPlayers(Player player) {
		Team team = getTeam(player);
		for(OfflinePlayer offlinePlayer : team.getPlayers()) {
			MessageHandler.sendMessage(player, offlinePlayer.getName());
		}
	}
	
	/*
	 * This method is ran when the /roster submit command is executed.
	 */
	public static void submitRoster(Player player) {
		if(player.getName().equalsIgnoreCase(ClanBattle.getClanOneLeader())) {
			if(rosterLeader1Done) {
				rosterLeader1Done = false;
				MessageHandler.sendMessage(player, "You have un-submitted your roster");
				return;
			} else {
				//int teamSize = TeamHandler.getTeam(ClanBattle.getClanOne()).getPlayers().size();
				String clanOne = ClanBattle.getClanOne();
				Team team = TeamHandler.getTeam(clanOne);
				int teamSize = team.getSize();
				if(teamSize == ClanBattleSetup.getMaxPerTeam()) {
					rosterLeader1Done = true;
					MessageHandler.sendMessage(player, "You have submitted your roster");
					MessageHandler.sendMessage(player, "If you ever need to un-submit your roster, do &b/roster submit");
				} else {
					MessageHandler.sendMessage(player, "&cYou have &b" + teamSize + " &cplayer(s) in your roster when you need &b" + ClanBattleSetup.getMaxPerTeam());
					MessageHandler.sendMessage(player, "Do &b/roster add &ato add users to the roster");
				}
			}
		} else if(player.getName().equalsIgnoreCase(ClanBattle.getClanTwoLeader())) {
			if(rosterLeader2Done) {
				rosterLeader2Done = false;
				MessageHandler.sendMessage(player, "You have un-submitted your roster");
				MessageHandler.sendMessage(player, "To re-submit your roster, do &b/roster submit");
				return;
			} else {
				String clanTwo = ClanBattle.getClanTwo();
				Team team = TeamHandler.getTeam(clanTwo);
				int teamSize = team.getSize();
				if(teamSize == ClanBattleSetup.getMaxPerTeam()) {
					rosterLeader2Done = true;
					MessageHandler.sendMessage(player, "You have submitted your roster");
					MessageHandler.sendMessage(player, "If you ever need to un-submit your roster, do &b/roster submit");
				} else {
					MessageHandler.sendMessage(player, "&cYou have &b" + teamSize + " &cplayer(s) in your roster when you need &b" + ClanBattleSetup.getMaxPerTeam());
					MessageHandler.sendMessage(player, "Do &b/roster add &ato add users to the roster");
				}
			}
		}
		if(rosterLeader1Done && rosterLeader2Done) {
			for(OfflinePlayer offlinePlayer : TeamHandler.getTeam(0).getPlayers()) {
				DB.PLAYERS_CLANS_TOJOIN.insert("'" + offlinePlayer.getUniqueId().toString() + "', '" + ProMcGames.getServerName().toLowerCase() + "'");
			}
			for(OfflinePlayer offlinePlayer : TeamHandler.getTeam(1).getPlayers()) {
				DB.PLAYERS_CLANS_TOJOIN.insert("'" + offlinePlayer.getUniqueId().toString() + "', '" + ProMcGames.getServerName().toLowerCase() + "'");
			}
			MessageHandler.alertLine("&b");
			MessageHandler.alert("Any user on the roster can now join");
			MessageHandler.alert("Instruct any roster users to join the clans hub");
			MessageHandler.alert("They will receive a diamond sword to right click");
			MessageHandler.alert("This will teleport them to this server");
			MessageHandler.alert("All users on both rosters must join or the server restarts");
			MessageHandler.alertLine("&b");
			ClanBattleSetup.setSetupPhase(SetupPhase.DONE);
			Events.unregister();
			new ClanBattle();
		}
	}
	
	public static Team getTeam(Player player) {
		return TeamHandler.getTeam(0).hasPlayer(player) ? TeamHandler.getTeam(0) : TeamHandler.getTeam(1);
	}
	
	public static boolean getRosterLeader1Done() {
		return rosterLeader1Done;
	}
	
	public static boolean getRosterLeader2Done() {
		return rosterLeader2Done;
	}
	
	public static boolean canModifyRoster(Player player) {
		return ClanBattle.getClanOneLeader().equalsIgnoreCase(player.getName()) || ClanBattle.getClanTwoLeader().equalsIgnoreCase(player.getName());
	}
}