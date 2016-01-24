package promcgames.gameapi.games.clanbattles;

import org.bukkit.entity.Player;

import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup.ClanSetting;
import promcgames.gameapi.games.survivalgames.SponsorHandler;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
import promcgames.gameapi.kits.KitShop;
import promcgames.player.trophies.SurvivalGamesTrophies;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.ClanHandler;

/*
	Clan Battle Options:
	
	Players per Team - 2, 3, 4, 5
	Team Damage - On, Off
	SG Kits - On, Off
	Grace Period (min) - 0, 1, 2, 3, 4
	Sponsors - On, Off
	Force DM (players) - 0, 2, 3, 4
	Chest Restock - On, Off
*/

public class ClanBattle {
	private static String clanOne = null;
	private static String clanTwo = null;
	private static String clanOneLeader = null;
	private static String clanTwoLeader = null;
	private static int clanOneID = -1;
	private static int clanTwoID = -1;
	
	public ClanBattle() {
		ClanBattleSetup.getInfoEntity().remove();
		ProMcGames.getMiniGame().setRequiredPlayers(2);
		ProMcGames.getMiniGame().setGameState(GameStates.WAITING);
		new ClanBattleEvents();
		if(ProMcGames.getMiniGame() != null) {
			ProMcGames.getMiniGame().setUpdateTitleSidebar(true);
			ProMcGames.getMiniGame().setUpdateBossBar(true);
			ProMcGames.getMiniGame().setStoreStats(ClanBattleSetup.ClanSetting.RANKED.getCurrentOption().equalsIgnoreCase("yes"));
			if(ProMcGames.getMiniGame().getStoreStats()) {
				StatsHandler.setViewOnly(false);
			}
			if(SurvivalGames.getCanUseSponsors()) {
				new SponsorHandler();
				for(Player player : ProPlugin.getPlayers()) {
					player.getInventory().addItem(SponsorHandler.getParticleSelector());
				}
			}
			ProMcGames.getMiniGame().setVotingCounter(60);
		}
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().addItem(SurvivalGamesTrophies.getItem());
		}
		clanOne = ClanHandler.getClanName(ProPlugin.getPlayers().get(0));
		clanTwo = ClanHandler.getClanName(ProPlugin.getPlayers().get(1));
		DB.NETWORK_CLANS_BATTLES.insert("'" + ClanHandler.getClanID(clanOne) + "', '" + ClanHandler.getClanID(clanTwo) + "', '0', '0'");
		if(ClanSetting.SG_KITS.getCurrentOption().equalsIgnoreCase("on")) {
			SurvivalGames.registerKits();
			for(Player player : ProPlugin.getPlayers()) {
				player.getInventory().addItem(KitShop.getMostRecentShop().getItem());
			}
		}
	}
	
	public static void setClanOne(String clanOne) {
		ClanBattle.clanOne = clanOne;
	}
	
	public static void setClanTwo(String clanTwo) {
		ClanBattle.clanTwo = clanTwo;
	}
	
	public static void setClanOneLeader(String clanOneLeader) {
		ClanBattle.clanOneLeader = clanOneLeader;
	}
	
	public static void setClanTwoLeader(String clanTwoLeader) {
		ClanBattle.clanTwoLeader = clanTwoLeader;
	}
	
	public static void setClanOneID(int clanOneID) {
		ClanBattle.clanOneID = clanOneID;
	}
	
	public static void setClanTwoID(int clanTwoID) {
		ClanBattle.clanTwoID = clanTwoID;
	}
	
	public static String getClanOne() {
		return clanOne;
	}
	
	public static String getClanTwo() {
		return clanTwo;
	}
	
	public static String getClanOneLeader() {
		return clanOneLeader;
	}
	
	public static String getClanTwoLeader() {
		return clanTwoLeader;
	}
	
	public static int getClanOneID() {
		return clanOneID;
	}
	
	public static int getClanTwoID() {
		return clanTwoID;
	}
}