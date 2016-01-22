package promcgames.server.servers.clans;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.gameapi.StatsHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ServerLogger;
import promcgames.server.Tweeter;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.clans.battle.BattleHandler;
import promcgames.server.servers.clans.invites.InviteHandler;
import promcgames.server.servers.hub.items.CosmeticsItem;
import promcgames.server.servers.hub.items.ServerSelectorItem;
import promcgames.server.servers.hub.items.cosmetic.BackArrowItem;
import promcgames.server.servers.hub.items.cosmetic.EliteItem;
import promcgames.server.servers.hub.items.cosmetic.MainMenuItem;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.ProItem;
import promcgames.server.servers.hub.items.cosmetic.ProPlusItem;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.world.CPSDetector;

public class Clans extends ProPlugin {
	public static int hubNumber = 0;
	
	public Clans() {
		super("Clans");
		addGroup("sghub");
		addGroup("24/7");
		try {
			hubNumber = Integer.valueOf(ProMcGames.getServerName().toLowerCase().replace("sghub", ""));
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		setUseTop8(true);
		setAutoVanishStaff(true);
		new ClanHandler();
		new BattleHandler();
		new promcgames.server.servers.clans.StatsHandler();
		new InviteHandler();
		new Events();
		new CPSDetector(new Location(Bukkit.getWorlds().get(0), -33.5, 7, -30.5));
		new StatsHandler(DB.PLAYERS_STATS_SURVIVAL_GAMES);
		new ClansHubStatsHandler();
		StatsHandler.setSaveOnQuit(false);
		StatsHandler.setViewOnly(true);
		new ServerSelectorItem();
		//new ScoreboardHandler();
		new InventoryHandler();
		new MainMenuItem();
		new CosmeticsItem();
		new ProItem();
		new ProPlusItem();
		new EliteItem();
		new BackArrowItem();
		new HubItem();
		new PerkLoader();
		new Tiering();
		new ScoreboardHandler();
		new LogoHandler();
		new ServerLogger();
		new SGTrophiesItems();
		// Clans twitter:
		new Tweeter("EdLmNouc8p7MVffUQjZjKSatk", "fkOVJMj0BrfVqUmna6j3G2rhoqUK9c6IAaCFle56pFDQRL88LC", "3002778008-cPDPvwfAfWkXdIUdidNxaGDJAVVPVxg8FcpdVvo", "z3y8cO2vtkWjJsaUWAO15uYhPCFDSDnuzEeiBt9X37tsF");
		// Rant twitter:
		//new Tweeter("A4hXKqZpO8l5AYfzljdQ9iBAL", "x3PSDOdrxghoeM7t1KTzRj08CACVB2YHlqDi9r9xJSXuzzJdKG", "3314576964-W12yOTlLWP7nyUyXheRF625P95YCodEYzVG12mT", "Kn9FHhBvoZHkyiCUPSfquRqQ4pyoVTcSIxQmqtDhtF1Qn");
		new NPCEntity(EntityType.ZOMBIE, "&b&lClans Help &c&l(CLICK)", new Location(Bukkit.getWorlds().get(0), -30.5, 8, -33.5)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendLine(player, "&b");
				MessageHandler.sendMessage(player, "Right click on the nether star to open the clans menu");
				MessageHandler.sendMessage(player, "For a list of all commands, do &b/clans help [page number]");
				MessageHandler.sendLine(player, "&b");
			}
		};
		new CommandBase("gameWon", 1, -1, false) {
			@Override
			public boolean execute(CommandSender sender, final String [] arguments) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						String alert = "";
						for(String argument : arguments) {
							alert += argument + " ";
						}
						if(ChatColor.stripColor(alert).contains("won a clan battle against")) {
							String colorLess = ChatColor.stripColor(alert);
							String [] split = colorLess.split(" ");
							String clanOneName = split[0];
							Clan clan = ClanHandler.getClan(clanOneName);
							if(clan == null) {
								MessageHandler.alert(" ");
								MessageHandler.alert(alert);
								MessageHandler.alert(" ");
							} else {
								String logo = null;
								File logoFile = new File(Bukkit.getWorldContainer().getPath() + "/../resources/clans/" + clan.getClanName() + ".png");
								if(logoFile.exists()) {
									logo = clan.getClanName();
									Tweeter.tweet(alert, "clans/" + logo + ".png");
								} else {
									Tweeter.tweet(alert, "clans/no.png");
								}
								MessageHandler.alert(" ");
								MessageHandler.alert(alert);
								MessageHandler.alert(" ");
							}
						} else {
							MessageHandler.alert(" ");
							MessageHandler.alert(alert);
							MessageHandler.alert(" ");
						}
					}
				}, 20 * 5);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
}
