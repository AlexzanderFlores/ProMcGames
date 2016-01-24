package promcgames.gameapi.games.clanbattles.setup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import promcgames.gameapi.TeamHandler;
import promcgames.gameapi.games.clanbattles.ClanBattle;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.tasks.DelayedTask;

public class ClanBattleSetup {
	public static enum SetupPhase {WAITING, SETTINGS, ROSTER, DONE}
	
	private static SetupPhase setupPhase = null;
	private static int settingIndex = 0;
	private static int maxPerTeam = 0;
	private static boolean doneWithSettings = false;
	private static boolean onLastSetting = false;
	private static NPCEntity settingsEntity = null;
	private static NPCEntity nextEntity = null;
	private static NPCEntity infoEntity = null;
	private static ClanSetting currentClanSetting = null;
	private static List<String> playersClickedOnNext = null;
	
	public static enum ClanSetting {
		PLAYERS_PER_TEAM(new String [] {"2", "3", "4", "5"}),
		GRACE_PERIOD_MINUTES(new String [] {"0", "1", "2", "3", "4"}),
		FORCE_DEATHMATCH_PLAYERS(new String [] {"0", "2", "4"}),
		//TEAM_DAMAGE(new String [] {"ON", "OFF"}),
		SG_KITS(new String [] {"ON", "OFF"}),
		SPONSORS(new String [] {"ON", "OFF"}),
		CHEST_RESTOCK(new String [] {"ON", "OFF"}),
		BROKEN_LEGS(new String [] {"ON", "OFF"}),
		//ROUNDS(new String [] {"3", "5"}),
		RANKED(new String [] {"YES", "NO"});
		
		private String [] options = null;
		private String currentOption = null;
		private int index = 0;
		
		ClanSetting(String [] options) {
			this.options = options;
			this.currentOption = options[0];
		}
		
		public void setNextOption() {
			index++;
			if(index >= options.length) {
				index = 0;
			}
			currentOption = options[index];
		}
		
		public boolean isOption(String option) {
			for(String options : this.options) {
				if(option == options) {
					return true;
				}
			}
			return false;
		}
		
		/*
		 * If this method returns false, the option was not set because it is not a valid option for that specific clan setting.
		 */
		public boolean setCurrentOption(String currentOption) {
			if(isOption(currentOption)) {
				this.currentOption = currentOption;
				return true;
			}
			return false;
		}
		
		public String getCurrentOption() {
			return currentOption;
		}
	}
	
	public ClanBattleSetup() {
		setSetupPhase(SetupPhase.WAITING);
		ProMcGames.getSidebar().setName("Clan Battle Setup");
		ClanBattle.setClanOneLeader("");
		ClanBattle.setClanTwoLeader("");
		DB.PLAYERS_CLANS_TOJOIN.delete("server_name", ProMcGames.getServerName().toLowerCase());
		new Events();
	}
	
	public static void startSetup() {
		setSetupPhase(SetupPhase.SETTINGS);
		String clanOneLeader = ProPlugin.getPlayers().get(0).getName();
		String clanTwoLeader = ProPlugin.getPlayers().get(1).getName();
		ClanBattle.setClanOne(ClanHandler.getClanName(clanOneLeader));
		ClanBattle.setClanTwo(ClanHandler.getClanName(clanTwoLeader));
		ClanBattle.setClanOneID(ClanHandler.getClanID(ClanBattle.getClanOne()));
		ClanBattle.setClanTwoID(ClanHandler.getClanID(ClanBattle.getClanTwo()));
		ClanBattle.setClanOneLeader(clanOneLeader);
		ClanBattle.setClanTwoLeader(clanTwoLeader);
		World world = ProMcGames.getMiniGame().getLobby();
		ProPlugin.getPlayers().get(0).teleport(new Location(world, -17.5, 28, -29.5, -135f, 0f));
		ProPlugin.getPlayers().get(1).teleport(new Location(world, -17.5, 28, -29.5, -135f, 0f));
		ProPlugin.getPlayers().get(0).setFlying(false);
		ProPlugin.getPlayers().get(1).setFlying(false);
		currentClanSetting = ClanSetting.values()[0];
		playersClickedOnNext = new ArrayList<String>();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MessageHandler.alertLine("&b");
				MessageHandler.alert("Clan Battle setup has begun");
				MessageHandler.alert("You have 5 minutes to setup before the server restarts");
				MessageHandler.alertLine("&b");
			}
		}, 20 * 3);
		
		nextEntity = new NPCEntity(EntityType.CREEPER, ChatColor.GREEN + "Next Setting " + ChatColor.AQUA + "(0/2)", new Location(world, -0.5, 29, -42.5)) {
			@Override
			public void onInteract(Player player) {
				if(!ClanBattleSetup.getPlayersClickedOnNext().contains(player.getName())) {
					ClanBattleSetup.getPlayersClickedOnNext().add(player.getName());
					this.setName(ChatColor.GREEN + (ClanBattleSetup.getOnLastSetting() ? "Finish Setup " : "Next Setting ") + ChatColor.AQUA + "(" + ClanBattleSetup.getPlayersClickedOnNext().size() + "/2)");
				} else {
					ClanBattleSetup.getPlayersClickedOnNext().remove(player.getName());
					this.setName(ChatColor.GREEN + (ClanBattleSetup.getOnLastSetting() ? "Finish Setup " : "Next Setting ") + ChatColor.AQUA + "(" + ClanBattleSetup.getPlayersClickedOnNext().size() + "/2)");
				}
				if(ClanBattleSetup.getPlayersClickedOnNext().size() >= 2) {
					ClanBattleSetup.getPlayersClickedOnNext().clear();
					if(this.getName().startsWith(ChatColor.GREEN + "Finish Setup")) {
						doneWithSettings();
					} else {
						settingIndex++;
						if(settingIndex == (ClanSetting.values().length - 1)) {
							ClanBattleSetup.setOnLastSetting(true);
							this.setName(ChatColor.GREEN + "Finish Setup " + ChatColor.AQUA + "(0/2)");
						} else {
							this.setName(ChatColor.GREEN + "Next Setting " + ChatColor.AQUA + "(0/2)");
						}
						currentClanSetting = ClanSetting.values()[settingIndex];
						ClanBattleSetup.getSettingsEntity().setName(ClanBattleSetup.getSettingsEntityName());
					}
				}
			}
		};
		settingsEntity = new NPCEntity(EntityType.CREEPER, getSettingsEntityName(), new Location(world, -3.5, 29, -45.5)) {
			@Override
			public void onInteract(Player player) {
				currentClanSetting.setNextOption();
				this.setName(getSettingsEntityName());
			}
		};
		infoEntity = new NPCEntity(EntityType.CREEPER, ChatColor.YELLOW + "Clan Battle How-To " + ChatColor.GRAY + "CLICK ME", new Location(world, -0.5, 29, -36.5)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendLine(player, "&b");
				if(doneWithSettings) {
					MessageHandler.sendMessage(player, "Use &b/roster &ato modify your roster");
				} else {
					MessageHandler.sendMessage(player, "The creeper to the left is the \"&eSettings&a\" creeper");
					MessageHandler.sendMessage(player, "Click to scroll through options for settings");
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "The creeper to the right is the \"&eNext Setting&a\" creeper");
					MessageHandler.sendMessage(player, "Both founders must click it to get to the next setting");
				}
				MessageHandler.sendLine(player, "&b");
			}
		};
	}
	
	public static void doneWithSettings() {
		setSetupPhase(SetupPhase.ROSTER);
		playersClickedOnNext.clear();
		playersClickedOnNext = null;
		for(ClanSetting clanSetting : ClanSetting.values()) {
			clanSetting.options = null;
		}
		nextEntity.remove();
		settingsEntity.remove();
		doneWithSettings = true;
		maxPerTeam = Integer.valueOf(ClanSetting.PLAYERS_PER_TEAM.getCurrentOption());
		SurvivalGames.setGracePeriodSeconds(Integer.valueOf(ClanSetting.GRACE_PERIOD_MINUTES.getCurrentOption()) * 60);
		SurvivalGames.setForceDMPlayers(Integer.valueOf(ClanSetting.FORCE_DEATHMATCH_PLAYERS.getCurrentOption()));
		//SurvivalGames.setRounds(Integer.valueOf(ClanSetting.ROUNDS.getCurrentOption()));
		SurvivalGames.setCanUseSponsors(ClanSetting.SPONSORS.getCurrentOption().equalsIgnoreCase("on"));
		SurvivalGames.setChestRestockEnabled(ClanSetting.CHEST_RESTOCK.getCurrentOption().equalsIgnoreCase("on"));
		SurvivalGames.setBreakingLegs(ClanSetting.BROKEN_LEGS.getCurrentOption().equalsIgnoreCase("on"));
		ProMcGames.getMiniGame().setStoreStats(ClanSetting.RANKED.getCurrentOption().equalsIgnoreCase("yes"));
		new RosterHandler();
		MessageHandler.alertLine("&b");
		MessageHandler.alert("Settings are now complete");
		MessageHandler.alert("Now to modify your roster");
		MessageHandler.alert("The roster is who will play in the battle on your team");
		MessageHandler.alert("Any player you put on the roster must be in your clan");
		MessageHandler.alert("Use &b/roster &ato get started");
		MessageHandler.alertLine("&b");
		for(Player player : ProPlugin.getPlayers()) {
			Team team = TeamHandler.getTeam(player);
			if(team != null) {
				String tabName = team.getPrefix() + player.getName();
				if(tabName.length() > 16) {
					tabName = tabName.substring(0, 16);
				}
				player.setPlayerListName(tabName);
			}
		}
	}
	
	public static void setOnLastSetting(boolean onLastSetting) {
		ClanBattleSetup.onLastSetting = onLastSetting;
	}
	
	public static void setSetupPhase(SetupPhase setupPhase) {
		ClanBattleSetup.setupPhase = setupPhase;
		if(DB.NETWORK_CLANS_SETUP.isKeySet("server_name", ProMcGames.getServerName())) {
			DB.NETWORK_CLANS_SETUP.updateString("setup_phase", setupPhase.toString(), "server_name", ProMcGames.getServerName());
			if(setupPhase == SetupPhase.WAITING) {
				DB.NETWORK_CLANS_SETUP.updateString("player1", "", "server_name", ProMcGames.getServerName());
				DB.NETWORK_CLANS_SETUP.updateString("player2", "", "server_name", ProMcGames.getServerName());
			}
		} else {
			DB.NETWORK_CLANS_SETUP.insert("'" + ProMcGames.getServerName() + "', '" + setupPhase.toString() + "', '', ''");
		}
	}
	
	public static String getSettingsEntityName() {
		return ChatColor.GREEN + currentClanSetting.toString().replace("_", " ") + ": " + ChatColor.AQUA + currentClanSetting.currentOption;
	}
	
	public static boolean getOnLastSetting() {
		return onLastSetting;
	}
	
	public static boolean getDoneWithSettings() {
		return doneWithSettings;
	}
	
	public static boolean isSetupPlayer(Player player) {
		return ClanBattle.getClanOneLeader().equalsIgnoreCase(player.getName()) || ClanBattle.getClanTwoLeader().equalsIgnoreCase(player.getName());
	}
	
	public static NPCEntity getSettingsEntity() {
		return settingsEntity;
	}
	
	public static NPCEntity getNextEntity() {
		return nextEntity;
	}
	
	public static NPCEntity getInfoEntity() {
		return infoEntity;
	}
	
	public static List<String> getPlayersClickedOnNext() {
		return playersClickedOnNext;
	}
	
	public static int getMaxPerTeam() {
		return maxPerTeam;
	}
	
	public static SetupPhase getSetupPhase() {
		return setupPhase;
	}
}