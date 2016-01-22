package promcgames;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import promcgames.anticheat.AntiGamingChair;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerArmorEquipEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerShootBowEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.games.arcade.Arcade;
import promcgames.gameapi.games.factions.Factions;
import promcgames.gameapi.games.kitpvp.ffa.KitPVP;
import promcgames.gameapi.games.skywars.SkyWars;
import promcgames.gameapi.games.skywars.SkyWarsTeams;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
import promcgames.gameapi.games.uhc.UHC;
import promcgames.gameapi.games.uhc.UHCPrefix;
import promcgames.gameapi.games.uhcbattles.UHCBattles;
import promcgames.gameapi.games.versus.Versus;
import promcgames.player.AliveTracker;
import promcgames.player.ArrowTrails;
import promcgames.player.ChatLogger;
import promcgames.player.CommunityLevelHandler;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.Friends;
import promcgames.player.IgnoreHandler;
import promcgames.player.NameColor;
import promcgames.player.Particles;
import promcgames.player.PartyHandler;
import promcgames.player.PrivateMessaging;
import promcgames.player.ProRankTrial;
import promcgames.player.VotingHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.PlayerTracker;
import promcgames.player.account.PlaytimeTracker;
import promcgames.player.bossbar.Scroller;
import promcgames.player.scoreboard.BelowNameScoreboardUtil;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.AlertHandler;
import promcgames.server.AutoAlerts;
import promcgames.server.AutoBroadcasts;
import promcgames.server.CommandBase;
import promcgames.server.CommandDispatcher;
import promcgames.server.DB;
import promcgames.server.DB.Databases;
import promcgames.server.GeneralEvents;
import promcgames.server.GlobalCommands;
import promcgames.server.PerformanceHandler;
import promcgames.server.RankAdvertiser;
import promcgames.server.RestarterHandler;
import promcgames.server.networking.Client;
import promcgames.server.servers.building.Building;
import promcgames.server.servers.clans.Clans;
import promcgames.server.servers.hub.Hub;
import promcgames.server.servers.hub.StatResetHandler;
import promcgames.server.servers.slave.Slave;
import promcgames.server.servers.testing.Testing;
import promcgames.server.servers.uhc.UHCHub;
import promcgames.server.servers.worker.Worker;
import promcgames.server.util.FileHandler;
import promcgames.server.util.JarUtils;
import promcgames.staff.Punishment;
import promcgames.staff.StaffMode;

public class ProMcGames extends JavaPlugin {
	public enum Plugins {
		BUILDING("Building"),
		HUB("HUB"),
		SURVIVAL_GAMES("SG"),
		SGHUB("sghub"),
		UHCHUB("uhchub"),
		CLAN_BATTLES("CB"),
		UHC("UHC"),
		UHC_BATTLES("UHCB"),
		FACTIONS("Factions"),
		ARCADE("Arcade"),
		SKY_WARS("SW"),
		SKY_WARS_TEAMS("SWT"),
		KIT_PVP("KitPVP"),
		VERSUS("Versus"),
		TESTING("Testing"),
		WORKER("Worker"),
		SLAVE("Slave");
		
		private String server = null;
		
		private Plugins(String server) {
			this.server = server;
		}
		
		public String getServer() {
			return this.server;
		}
	}
	
	private static Client client = null;
	private static ProMcGames instance = null;
	private static Plugins plugin = null;
	private static ProPlugin proPlugin = null;
	private static MiniGame miniGame = null;
	private static String serverName = null;
	private static SidebarScoreboardUtil sidebar = null;
	private static BelowNameScoreboardUtil belowName = null;
	
	@Override
	public void onEnable() {
		instance = this;
		Bukkit.getMessenger().registerOutgoingPluginChannel(getInstance(), "BungeeCord");
		sidebar = new SidebarScoreboardUtil("");
		try {
        	File [] libs = new File [] {
        		new File(Bukkit.getWorldContainer().getPath() + "/../resources/", "Twitter4j.jar")
        	};
            for(File lib : libs) {
                if(lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
                }
            }
            for(final File lib : libs) {
                if(lib.exists()) {
                	addClassPath(JarUtils.getJarUrl(lib));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
		plugin = Plugins.valueOf(getConfig().getString("plugin").toUpperCase());
		if(plugin == Plugins.BUILDING) {
			proPlugin = new Building();
		} else if(plugin == Plugins.HUB) {
			proPlugin = new Hub();
		} else if(plugin == Plugins.SURVIVAL_GAMES || plugin == Plugins.CLAN_BATTLES) {
			proPlugin = new SurvivalGames();
		} else if(plugin == Plugins.SGHUB) {
			proPlugin = new Clans();
		} else if(plugin == Plugins.UHCHUB) {
			proPlugin = new UHCHub();
		} else if(plugin == Plugins.SKY_WARS) {
			proPlugin = new SkyWars();
		} else if(plugin == Plugins.SKY_WARS_TEAMS) {
			proPlugin = new SkyWarsTeams();
		} else if(plugin == Plugins.UHC) {
			proPlugin = new UHC();
		} else if(plugin == Plugins.UHC_BATTLES) {
			proPlugin = new UHCBattles();
		} else if(plugin == Plugins.FACTIONS) {
			proPlugin = new Factions();
		} else if(plugin == Plugins.ARCADE) {
			proPlugin = new Arcade();
		} else if(plugin == Plugins.KIT_PVP) {
			proPlugin = new KitPVP();
		} else if(plugin == Plugins.VERSUS) {
			proPlugin = new Versus();
		} else if(plugin == Plugins.TESTING) {
			proPlugin = new Testing();
		} else if(plugin == Plugins.WORKER) {
			proPlugin = new Worker();
		} else if(plugin == Plugins.SLAVE) {
			proPlugin = new Slave();
		}
		DB.values(); // Call the enumeration constructors for each item to initialize them
		new Disguise();
		new AccountHandler();
		new AntiGamingChair();
		new GlobalCommands();
		new PerformanceHandler();
		new GeneralEvents();
		new NameColor();
		new EmeraldsHandler();
		new PlayerLeaveEvent();
		new PlayerShootBowEvent();
		new StaffMode();
		new InventoryItemClickEvent();
		new CommandDispatcher();
		new PrivateMessaging();
		new PlayerAFKEvent();
		new PlaytimeTracker();
		new RestarterHandler();
		new CommunityLevelHandler();
		new Punishment();
		new PlayerTracker();
		new IgnoreHandler();
		new VotingHandler();
		new AutoBroadcasts();
		new PostPlayerJoinEvent();
		new StatResetHandler();
		new MouseClickEvent();
		new AlertHandler();
		new ChatLogger();
		new Particles();
		new ArrowTrails();
		new Scroller();
		new AliveTracker();
		if(plugin != Plugins.UHC) {
			new PartyHandler();
		}
		new PlayerArmorEquipEvent();
		new Friends();
		new ProRankTrial();
		new AutoAlerts();
		new RankAdvertiser();
		new UHCPrefix();
		client = new Client("198.24.166.226", 4500, 5000);
		client.start();
		
		new CommandBase("convert", -1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(!(sender instanceof Player)) {
					new Thread() {
						@Override
						public void run() {
							try {
								PreparedStatement ps = DB.STAFF_BAN.getConnection().prepareStatement("SELECT * FROM bans");
								ResultSet rs = ps.executeQuery();
								while(rs.next()) {
									String uuid, staff_uuid, reason, proof, date, time;
									uuid = rs.getString("uuid");
									staff_uuid = rs.getString("staff_uuid");
									reason = rs.getString("reason");
									proof = rs.getString("");
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
				return true;
			}
		};
	}
	
	@Override
	public void onDisable() {
		proPlugin.disable();
		for(World world : Bukkit.getWorlds()) {
			Bukkit.unloadWorld(world, false);
		}
		for(Databases database : Databases.values()) {
			database.disconnect();
		}
		FileHandler.checkForUpdates();
		if(client != null) {
			client.shutdown(true);
		} else if(plugin == Plugins.SLAVE && Slave.getServer() != null) {
			Slave.getServer().shutdown();
		}
	}
	
	private void addClassPath(URL url) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch(Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url + " to system classloader");
        }
    }
	
	public static ProMcGames getInstance() {
		return instance;
	}
	
	public static Plugins getPlugin() {
		return plugin;
	}
	
	public static ProPlugin getProPlugin() {
		return proPlugin;
	}
	
	public static void setProPlugin(ProPlugin proPlugin) {
		ProMcGames.proPlugin = proPlugin;
	}
	
	public static void setMiniGame(MiniGame newMiniGame) {
		miniGame = newMiniGame;
	}
	
	public static MiniGame getMiniGame() {
		return miniGame;
	}
	
	public static String getServerName() {
		if(serverName == null) {
			serverName = new File(Bukkit.getWorldContainer().getPath() + "/..").getAbsolutePath().replace("/home/", "");
			serverName = serverName.split("/")[0].toUpperCase();
		}
		return serverName;
	}
	
	public static SidebarScoreboardUtil getSidebar() {
		return sidebar;
	}
	
	public static void setSidebar(SidebarScoreboardUtil sidebar) {
		ProMcGames.sidebar = sidebar;
	}
	
	public static BelowNameScoreboardUtil getBelowName() {
		return belowName;
	}
	
	public static void setBelowName(BelowNameScoreboardUtil belowName) {
		ProMcGames.belowName = belowName;
	}
	
	public static Scoreboard getScoreboard() {
		return getSidebar().getScoreboard();
	}
	
	public static Client getClient() {
		return client;
	}
}