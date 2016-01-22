package promcgames.gameapi.games.skywars;

import promcgames.gameapi.games.skywars.trophies.teams.MonthlyKillSeeker1;
import promcgames.gameapi.games.skywars.trophies.teams.MonthlyKillSeeker2;
import promcgames.gameapi.games.skywars.trophies.teams.MonthlyKillSeeker3;
import promcgames.gameapi.games.skywars.trophies.teams.Swordsman;
import promcgames.gameapi.games.skywars.trophies.teams.ToTheVoid1;
import promcgames.gameapi.games.skywars.trophies.teams.ToTheVoid2;
import promcgames.gameapi.games.skywars.trophies.teams.ToTheVoid3;
import promcgames.gameapi.games.skywars.trophies.teams.VictoryHunter1;
import promcgames.gameapi.games.skywars.trophies.teams.VictoryHunter2;
import promcgames.gameapi.games.skywars.trophies.teams.VictoryHunter3;
import promcgames.gameapi.games.skywars.trophies.teams.VictoryHunter4;
import promcgames.gameapi.games.skywars.trophies.teams.VictoryHunter5;
import promcgames.player.trophies.SkyWarsTeamsTrophies;

public class SkyWarsTeams extends SkyWars {
	public SkyWarsTeams() {
		super("Team Sky Wars");
		setRequiredPlayers(6);
		new TeamEvents();
		new SkyWarsTeamsTrophies();
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
