package promcgames.server.servers.hub;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.player.MessageHandler;
import promcgames.player.TeamScoreboardHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ServerLogger;
import promcgames.server.events.GameNights;
import promcgames.server.events.OMN;
import promcgames.server.servers.hub.items.CosmeticsItem;
import promcgames.server.servers.hub.items.HubGamesItem;
import promcgames.server.servers.hub.items.HubSponsor;
import promcgames.server.servers.hub.items.ProfileItem;
import promcgames.server.servers.hub.items.ServerSelectorItem;
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.servers.hub.items.cosmetic.BackArrowItem;
import promcgames.server.servers.hub.items.cosmetic.EliteItem;
import promcgames.server.servers.hub.items.cosmetic.MainMenuItem;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.ProItem;
import promcgames.server.servers.hub.items.cosmetic.ProPlusItem;
import promcgames.server.util.FileHandler;
import promcgames.server.world.AngryBob;

public class Hub extends ProPlugin {
	public static int hubNumber = 0;
	
	public Hub() {
		super("Hub");
		addGroup("24/7");
		addGroup("hub");
		for(World world : Bukkit.getWorlds()) {
			for(Entity entity : world.getEntities()) {
				if(entity instanceof Item || (entity instanceof LivingEntity && !(entity instanceof Player))) {
					entity.remove();
				}
			}
		}
		try {
			hubNumber = Integer.valueOf(ProMcGames.getServerName().toLowerCase().replace("hub", ""));
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		new ServerLogger();
		//new HubServerLogger();
		new Events();
		new ScoreboardHandler();
		new ServerSelectorItem();
		new CosmeticsItem();
		new HubSponsor();
		new TrophiesItem();
		new ProfileItem();
		new HubGamesItem();
		new MainMenuItem();
		new ProItem();
		new ProPlusItem();
		new EliteItem();
		new BackArrowItem();
		new RankTransferHandler();
		new WelcomeHandler();
		new Parkour();
		new ColorEgg();
		new RecentPurchaseDisplayer();
		new TeamScoreboardHandler();
		new KingOfTheLadder();
		new Spleef();
		new SupporterHeads();
		new CommunityLevelTop8();
		new OMN();
		new GameNights();
		new VotingWall();
		new HubSponsorGiveaway();
		new GiftGiver();
		new SnowballFight();
		new PerkLoader();
		new Vanisher();
		World world = Bukkit.getWorlds().get(0);
		world.setAutoSave(false);
		world.setTime(6000);
		new AngryBob(new Location(world, -102.5, 126.0, -206.5));
		/*final int price = 1500;
		new NPCEntity(EntityType.ZOMBIE, "&bBuy Hub Sponsors &7(&e" + price + " &aEmeralds&7)", new Location(world, -119.5, 126, -160.5)) {
			@Override
			public void onInteract(Player player) {
				if(EmeraldsHandler.getEmeralds(player) >= price) {
					EmeraldsHandler.addEmeralds(player, price * -1, EmeraldReason.HUB_SPONSOR_PURCHASE, false);
					HubSponsor.add(player.getUniqueId(), 1);
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have enough Emeralds for this");
				}
				MessageHandler.sendMessage(player, "Get a free Hub Sponsor each day by voting: &b/vote");
			}
		};*/
		new CommandBase("spawn", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.teleport(player.getWorld().getSpawnLocation());
				if(player.getAllowFlight()) {
					player.setFlying(false);
				}
				return true;
			}
		};
		new CommandBase("restartHub") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("vpn", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else if(DB.PLAYERS_VPN.isUUIDSet(uuid)) {
					DB.PLAYERS_VPN.deleteUUID(uuid);
					MessageHandler.sendMessage(sender, "&cDisallowed " + name + " from using VPNs");
				} else {
					DB.PLAYERS_VPN.insert("'" + uuid.toString() + "'");
					MessageHandler.sendMessage(sender, "Allowed " + name + " to use VPNs");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	@Override
	public void disable() {
		super.disable();
		String container = Bukkit.getWorldContainer().getPath();
		Bukkit.unloadWorld(Bukkit.getWorlds().get(0), false);
		FileHandler.delete(new File(container + "/world"));
		FileHandler.copyFolder(new File(container + "/../resources/maps/hub"), new File(container + "/world"));
	}
}
