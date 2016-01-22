package promcgames.gameapi.games.arcade;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.VotingHandler;
import promcgames.gameapi.games.arcade.games.BlockRain;
import promcgames.gameapi.games.arcade.games.BowWarfare;
import promcgames.gameapi.games.arcade.games.ChickenHunt;
import promcgames.gameapi.games.arcade.games.ColorRun;
import promcgames.gameapi.games.arcade.games.LavaRun;
import promcgames.gameapi.games.arcade.games.ParkourRunner;
import promcgames.gameapi.games.arcade.games.QuickSand;
import promcgames.gameapi.games.arcade.games.TNTBomber;
import promcgames.gameapi.games.arcade.games.TNTWarfare;
import promcgames.gameapi.games.arcade.games.WoolShooter;
import promcgames.server.DB;

public class Arcade extends MiniGame {
	private static List<ArcadeGame> games = null;
	
	public Arcade() {
		super("Arcade");
		setAutoJoin(false);
		games = new ArrayList<ArcadeGame>();
		new StatsHandler(DB.PLAYERS_STATS_ARCADE);
		new Events();
		loadGames();
		startLobby();
	}
	
	@Override
	public void disable() {
		if(games.isEmpty()) {
			setGameState(GameStates.ENDING);
		}
		super.disable();
	}
	
	private static void loadGames() {
		new ChickenHunt();
		new ColorRun();
		new LavaRun();
		new QuickSand();
		new TNTBomber();
		new WoolShooter();
		new TNTWarfare();
		new ParkourRunner();
		new BowWarfare();
		new BlockRain();
	}
	
	public static void startLobby() {
		if(ProPlugin.getPlayers().size() >= ProMcGames.getMiniGame().getRequiredPlayers()) {
			ProMcGames.getMiniGame().setGameState(GameStates.VOTING);
		} else {
			ProMcGames.getMiniGame().setGameState(GameStates.WAITING);
		}
		if(games.isEmpty()) {
			ProMcGames.getMiniGame().disable();
		} else {
			int max = 5;
			List<String> options = new ArrayList<String>();
			do {
				for(int a = 0; a < max && a < games.size(); ++a) {
					String name = games.get(new Random().nextInt(games.size())).getName();
					if(!options.contains(name)) {
						options.add(name);
					}
				}
			} while(options.size() < max && options.size() < games.size());
			new VotingHandler(options);
		}
	}
	
	public static void addGame(ArcadeGame game) {
		games.add(game);
	}
	
	public static void removeGame(ArcadeGame game) {
		games.remove(game);
		startLobby();
	}
	
	public static void executeGame(String name) {
		for(ArcadeGame game : games) {
			if(game.getName().equals(name)) {
				game.enable();
				break;
			}
		}
	}
}
