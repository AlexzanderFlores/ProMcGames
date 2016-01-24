package promcgames.gameapi.games.survivalgames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;

import promcgames.gameapi.MiniGame;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.games.clanbattles.ClanBattleStatsHandler;
import promcgames.gameapi.games.survivalgames.kits.ArcherKit;
import promcgames.gameapi.games.survivalgames.kits.RestockKit;
import promcgames.gameapi.games.survivalgames.kits.SwordsmanKit;
import promcgames.gameapi.games.survivalgames.kits.TeleporterKit;
import promcgames.gameapi.games.survivalgames.kits.premium.CrafterKit;
import promcgames.gameapi.games.survivalgames.kits.premium.EnchanterKit;
import promcgames.gameapi.games.survivalgames.kits.premium.PainKillerKit;
import promcgames.gameapi.games.survivalgames.kits.premium.ResurrectionKit;
import promcgames.gameapi.games.survivalgames.kits.premium.TrackerKit;
import promcgames.gameapi.games.survivalgames.trophies.BaccaChallenge;
import promcgames.gameapi.games.survivalgames.trophies.BoomHeadshot1;
import promcgames.gameapi.games.survivalgames.trophies.BoomHeadshot2;
import promcgames.gameapi.games.survivalgames.trophies.BoomHeadshot3;
import promcgames.gameapi.games.survivalgames.trophies.BrokenLegsChallenge;
import promcgames.gameapi.games.survivalgames.trophies.ChestHunter1;
import promcgames.gameapi.games.survivalgames.trophies.ChestHunter2;
import promcgames.gameapi.games.survivalgames.trophies.ChestHunter3;
import promcgames.gameapi.games.survivalgames.trophies.CookieMonsterChallenge;
import promcgames.gameapi.games.survivalgames.trophies.DoubleKillChallenge;
import promcgames.gameapi.games.survivalgames.trophies.FastWin;
import promcgames.gameapi.games.survivalgames.trophies.FirstBlood;
import promcgames.gameapi.games.survivalgames.trophies.KatnissChallenge;
import promcgames.gameapi.games.survivalgames.trophies.NoChestChallenge;
import promcgames.gameapi.games.survivalgames.trophies.NoSponsorChallenge;
import promcgames.gameapi.games.survivalgames.trophies.OneChestChallenge;
import promcgames.gameapi.games.survivalgames.trophies.SecondariesChallenge;
import promcgames.gameapi.games.survivalgames.trophies.Tier1OnlyChallenge;
import promcgames.gameapi.kits.KitShop;
import promcgames.player.trophies.SurvivalGamesTrophies;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class SurvivalGames extends MiniGame {
	public static Location arenaCenter = null;
	private static int gracePerodSeconds = 30;
	private static int forceDMPlayers = 4;
	private static int rounds = 1;
	private static boolean eliteCanChooseStartingSpawn = false;
	private static boolean canUseSponsors = true;
	private static boolean chestRestockEnabled = true;
	private static boolean spawnChangingSnowballsEnabled = true;
	private static boolean snowballsAlwaysWork = false;
	private static boolean breakingLegs = true;
	
	public SurvivalGames() {
		super(isClanBattle() ? "Clan Battle" : "Survival Games");
		addGroup("sg");
		setSpawnChangingSnowballsEnabled(!isClanBattle());
		setTeamBased(isClanBattle());
		setAutoJoin(!isClanBattle());
		setRequiredPlayers(4);
		setUseTop8(true);
		setCanJoinWhileStarting(false);
		setAllowItemSpawning(true);
		setWinEmeralds(25);
		setKillEmeralds(5);
		setVotingCounter(isClanBattle() ? 60 * 5 + 1 : 61);
		setStartingCounter(31);
		new ChestHandler();
		new Events();
		new ChestLogger();
		if(isClanBattle()) {
			new ClanBattleStatsHandler();
			StatsHandler.setViewOnly(true);
			this.setUpdateBossBar(false);
			this.setUpdateTitleSidebar(false);
		} else {
			new SponsorHandler();
			new StatsHandler(DB.PLAYERS_STATS_SURVIVAL_GAMES, DB.PLAYERS_STATS_SURVIVAL_GAMES_MONTHLY);
			registerKits();
		}
		String ultraGoldenApple = ChatColor.LIGHT_PURPLE + "Ultra Golden Apple (" + ChatColor.GOLD + "+4 Hearts" + ChatColor.LIGHT_PURPLE + ")";
		ShapedRecipe ultraApple = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(ultraGoldenApple).getItemStack());
		ultraApple.shape("012", "345", "678");
		ultraApple.setIngredient('0', Material.GOLD_INGOT);
		ultraApple.setIngredient('1', Material.GOLD_INGOT);
		ultraApple.setIngredient('2', Material.GOLD_INGOT);
		ultraApple.setIngredient('3', Material.GOLD_INGOT);
		ultraApple.setIngredient('4', Material.SKULL_ITEM, 3);
		ultraApple.setIngredient('5', Material.GOLD_INGOT);
		ultraApple.setIngredient('6', Material.GOLD_INGOT);
		ultraApple.setIngredient('7', Material.GOLD_INGOT);
		ultraApple.setIngredient('8', Material.GOLD_INGOT);
		Bukkit.getServer().addRecipe(ultraApple);
		// Trophies
		new SurvivalGamesTrophies();
		new KatnissChallenge();
		new FastWin();
		new SecondariesChallenge();
		new BaccaChallenge();
		new FirstBlood();
		new ChestHunter1();
		new ChestHunter2();
		new ChestHunter3();
		new BrokenLegsChallenge();
		new NoChestChallenge();
		new OneChestChallenge();
		new Tier1OnlyChallenge();
		new BoomHeadshot1();
		new BoomHeadshot2();
		new BoomHeadshot3();
		new DoubleKillChallenge();
		new NoSponsorChallenge();
		new CookieMonsterChallenge();
	}
	
	@Override
	public void resetFlags() {
		super.resetFlags();
		setGracePeriodSeconds(30);
		setEliteCanChooseStartingSpawn(false);
		setCanUseSponsors(true);
		setSnowballsAlwaysWork(false);
		setSpawnChangingSnowballsEnabled(true);
	}
	
	public static void registerKits() {
		new KitShop();
		new ArcherKit();
		new SwordsmanKit();
		new TeleporterKit();
		new RestockKit();
		new TrackerKit();
		new PainKillerKit();
		new ResurrectionKit();
		new CrafterKit();
		new EnchanterKit();
	}
	
	public static boolean isClanBattle() {
		return ProMcGames.getPlugin() == Plugins.CLAN_BATTLES;
	}
	
	public static int getGracePeriodSeconds() {
		return gracePerodSeconds;
	}
	
	public static void setGracePeriodSeconds(int gracePeriodSeconds) {
		gracePerodSeconds = gracePeriodSeconds;
	}
	
	public static int getForceDMPlayers() {
		return forceDMPlayers;
	}
	
	public static void setForceDMPlayers(int forceDMPlayers) {
		SurvivalGames.forceDMPlayers = forceDMPlayers;
	}
	
	public static int getRounds() {
		return rounds;
	}
	
	public static void setRounds(int rounds) {
		SurvivalGames.rounds = rounds;
	}
	
	public static void setSpawnChangingSnowballsEnabled(boolean spawnChangingSnowballsEnabled) {
		SurvivalGames.spawnChangingSnowballsEnabled = spawnChangingSnowballsEnabled;
	}
	
	public static boolean getSpawnChangingSnowballsEnabled() {
		return spawnChangingSnowballsEnabled;
	}
	
	public static void setChestRestockEnabled(boolean chestRestockEnabled) {
		SurvivalGames.chestRestockEnabled = chestRestockEnabled;
	}
	
	public static boolean getChestRestockEnabled() {
		return chestRestockEnabled;
	}
	
	public static void setSnowballsAlwaysWork(boolean snowballsAlwaysWork) {
		SurvivalGames.snowballsAlwaysWork = snowballsAlwaysWork;
	}
	
	public static boolean getSnowballsAlwaysWork() {
		return snowballsAlwaysWork;
	}
	
	public static void setBreakingLegs(boolean breakingLegs) {
		SurvivalGames.breakingLegs = breakingLegs;
	}
	
	public static boolean getBreakingLegs() {
		return breakingLegs;
	}
	
	public static void setCanUseSponsors(boolean canUseSponsors) {
		SurvivalGames.canUseSponsors = canUseSponsors;
	}
	
	public static boolean getCanUseSponsors() {
		return canUseSponsors;
	}
	
	public static void setEliteCanChooseStartingSpawn(boolean eliteCanChooseStartingSpawn) {
		SurvivalGames.eliteCanChooseStartingSpawn = eliteCanChooseStartingSpawn;
	}
	
	public static boolean getEliteCanChooseStartingSpawn() {
		return eliteCanChooseStartingSpawn;
	}
}
