package promcgames.gameapi.games.versus;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.games.versus.kits.Archer;
import promcgames.gameapi.games.versus.kits.Bacca;
import promcgames.gameapi.games.versus.kits.Chain;
import promcgames.gameapi.games.versus.kits.Diamond;
import promcgames.gameapi.games.versus.kits.Ender;
import promcgames.gameapi.games.versus.kits.Gapple;
import promcgames.gameapi.games.versus.kits.Gold;
import promcgames.gameapi.games.versus.kits.Iron;
import promcgames.gameapi.games.versus.kits.Kohi;
import promcgames.gameapi.games.versus.kits.Leather;
import promcgames.gameapi.games.versus.kits.NoDebuff;
import promcgames.gameapi.games.versus.kits.OneHitWonder;
import promcgames.gameapi.games.versus.kits.Pyro;
import promcgames.gameapi.games.versus.kits.Quickshot;
import promcgames.gameapi.games.versus.kits.SurvivalGames;
import promcgames.gameapi.games.versus.kits.Swordsman;
import promcgames.gameapi.games.versus.kits.TNT;
import promcgames.gameapi.games.versus.kits.UHC;
import promcgames.gameapi.games.versus.tournament.TournamentQueueHandler;
import promcgames.gameapi.games.versus.trophies.ArcherChampion;
import promcgames.gameapi.games.versus.trophies.BaccaChampion;
import promcgames.gameapi.games.versus.trophies.BoomHeadshot1;
import promcgames.gameapi.games.versus.trophies.BoomHeadshot2;
import promcgames.gameapi.games.versus.trophies.BoomHeadshot3;
import promcgames.gameapi.games.versus.trophies.ChainChampion;
import promcgames.gameapi.games.versus.trophies.DiamondChampion;
import promcgames.gameapi.games.versus.trophies.EnderChampion;
import promcgames.gameapi.games.versus.trophies.GappleChampion;
import promcgames.gameapi.games.versus.trophies.GoldChampion;
import promcgames.gameapi.games.versus.trophies.IronChampion;
import promcgames.gameapi.games.versus.trophies.KillstreakSeeker1;
import promcgames.gameapi.games.versus.trophies.KillstreakSeeker2;
import promcgames.gameapi.games.versus.trophies.KillstreakSeeker3;
import promcgames.gameapi.games.versus.trophies.KohiChampion;
import promcgames.gameapi.games.versus.trophies.LeatherChampion;
import promcgames.gameapi.games.versus.trophies.NoDebuffChampion;
import promcgames.gameapi.games.versus.trophies.OneHitWonderChampion;
import promcgames.gameapi.games.versus.trophies.PyroChampion;
import promcgames.gameapi.games.versus.trophies.QuickshotChampion;
import promcgames.gameapi.games.versus.trophies.SurvivalGamesChampion;
import promcgames.gameapi.games.versus.trophies.SwordsmanChampion;
import promcgames.gameapi.games.versus.trophies.TNTChampion;
import promcgames.gameapi.games.versus.trophies.UHCChampion;
import promcgames.player.TeamScoreboardHandler;
import promcgames.player.scoreboard.BelowNameHealthScoreboardUtil;
import promcgames.player.trophies.VersusTrophies;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.util.FileHandler;
import promcgames.server.world.CPSDetector;

public class Versus extends ProPlugin {
	private World world = null;
	
	public Versus() {
		super("Versus");
		addGroup("24/7");
		world = Bukkit.getWorlds().get(0);
		for(Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
			entity.remove();
		}
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowPlayerInteraction(true);
		setAllowBowShooting(true);
		setAllowInventoryClicking(true);
		setFlintAndSteelUses(2);
		setAllowEntityCombusting(true);
		setAllowInventoryClicking(true);
		setUseTop8(true);
		setAutoVanishStaff(true);
		setKickDefaultsForPros(false);
		setAutoVanishStaff(false);
		new TournamentQueueHandler();
		new LobbyHandler();
		new QueueHandler();
		new BattleHandler();
		new MapProvider(world);
		new SpectatorHandler();
		new ScoreboardHandler();
		new TeamScoreboardHandler();
		new BelowNameHealthScoreboardUtil();
		new PrivateBattleHandler();
		new VersusStats();
		new HotbarEditor();
		new StatsHandler(DB.PLAYERS_STATS_VERSUS, DB.PLAYERS_STATS_VERSUS_MONTHLY);
		//new VersusElo();
		new CPSDetector(new Location(world, 18.5, 7, -17.5));
		// Kits
		new Leather();
		new Gold();
		new Chain();
		new Iron();
		new Diamond();
		new SurvivalGames();
		new Archer();
		new UHC();
		new Swordsman();
		new Pyro();
		new Ender();
		new Bacca();
		new Gapple();
		new TNT();
		new OneHitWonder();
		new Kohi();
		new NoDebuff();
		new Quickshot();
		// Trophies
		new VersusTrophies();
		new KillstreakSeeker1();
		new KillstreakSeeker2();
		new KillstreakSeeker3();
		new BoomHeadshot1();
		new BoomHeadshot2();
		new BoomHeadshot3();
		new LeatherChampion();
		new GoldChampion();
		new ChainChampion();
		new IronChampion();
		new DiamondChampion();
		new SurvivalGamesChampion();
		new ArcherChampion();
		new UHCChampion();
		new SwordsmanChampion();
		new PyroChampion();
		new EnderChampion();
		new BaccaChampion();
		new GappleChampion();
		new TNTChampion();
		new OneHitWonderChampion();
		new KohiChampion();
		new NoDebuffChampion();
		new QuickshotChampion();
	}
	
	@Override
	public void disable() {
		super.disable();
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		File newWorld = new File(container + "/../resources/maps/versus");
		if(newWorld.exists() && newWorld.isDirectory()) {
			FileHandler.delete(new File(container + "/lobby"));
			FileHandler.copyFolder(newWorld, new File(container + "/lobby"));
		}
	}
}
