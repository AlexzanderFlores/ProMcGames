package promcgames.gameapi.games.skywars;

import promcgames.gameapi.MiniGame;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.TeamDetector;
import promcgames.gameapi.games.skywars.cages.CageHandler;
import promcgames.gameapi.games.skywars.islands.IslandHandler;
import promcgames.gameapi.games.skywars.trophies.solo.MonthlyKillSeeker1;
import promcgames.gameapi.games.skywars.trophies.solo.MonthlyKillSeeker2;
import promcgames.gameapi.games.skywars.trophies.solo.MonthlyKillSeeker3;
import promcgames.gameapi.games.skywars.trophies.solo.Swordsman;
import promcgames.gameapi.games.skywars.trophies.solo.ToTheVoid1;
import promcgames.gameapi.games.skywars.trophies.solo.ToTheVoid2;
import promcgames.gameapi.games.skywars.trophies.solo.ToTheVoid3;
import promcgames.gameapi.games.skywars.trophies.solo.VictoryHunter1;
import promcgames.gameapi.games.skywars.trophies.solo.VictoryHunter2;
import promcgames.gameapi.games.skywars.trophies.solo.VictoryHunter3;
import promcgames.gameapi.games.skywars.trophies.solo.VictoryHunter4;
import promcgames.gameapi.games.skywars.trophies.solo.VictoryHunter5;
import promcgames.gameapi.scenarios.scenarios.CutClean;
import promcgames.player.scoreboard.BelowNameScoreboardUtil;
import promcgames.player.trophies.SkyWarsTrophies;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;

public class SkyWars extends MiniGame {
	public SkyWars() {
		this("Sky Wars");
	}
	
	public SkyWars(String name) {
		super(name);
		setRequiredPlayers(4);
		setStartingCounter(15);
		setCanJoinWhileStarting(false);
		setKillEmeralds(3);
		setWinEmeralds(25);
		new CutClean().enable(false);
		new ChestHandler();
		new IslandHandler();
		new CageHandler();
		new DeathBeamHandler();
		new StatsHandler(DB.PLAYERS_STATS_SKY_WARS, DB.PLAYERS_STATS_SKY_WARS_MONTHLY);
		new Events();
		new BelowNameScoreboardUtil(ProMcGames.getScoreboard(), "&6Islands Owned", "dummy");
		if(ProMcGames.getPlugin() == Plugins.SKY_WARS) {
			new TeamDetector();
			new SkyWarsTrophies();
			new Swordsman();
			new VictoryHunter1();
			new VictoryHunter2();
			new VictoryHunter3();
			new VictoryHunter4();
			new VictoryHunter5();
			new MonthlyKillSeeker1();
			new MonthlyKillSeeker2();
			new MonthlyKillSeeker3();
			new ToTheVoid1();
			new ToTheVoid2();
			new ToTheVoid3();
		}
	}
}
