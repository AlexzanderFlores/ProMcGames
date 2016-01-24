package promcgames.server.servers.uhc;

import promcgames.player.TeamScoreboardHandler;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ServerLogger;
import promcgames.server.servers.clans.HubItem;
import promcgames.server.servers.hub.items.CosmeticsItem;
import promcgames.server.servers.hub.items.ServerSelectorItem;
import promcgames.server.servers.hub.items.cosmetic.BackArrowItem;
import promcgames.server.servers.hub.items.cosmetic.EliteItem;
import promcgames.server.servers.hub.items.cosmetic.MainMenuItem;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.ProItem;
import promcgames.server.servers.hub.items.cosmetic.ProPlusItem;

public class UHCHub extends ProPlugin {
	public static int hubNumber = 0;
	
	public UHCHub() {
		super("UHCHub");
		addGroup("uhchub");
		addGroup("24/7");
		try {
			hubNumber = Integer.valueOf(ProMcGames.getServerName().toLowerCase().replace("uhchub", ""));
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		setAllowPlayerInteraction(true);
		setAutoVanishStaff(true);
		new MainMenuItem();
		new CosmeticsItem();
		new ProItem();
		new ProPlusItem();
		new EliteItem();
		new BackArrowItem();
		new HubItem();
		new PerkLoader();
		new Events();
		new ServerSelectorItem();
		new WaterBucket();
		new BlockRunning();
		new TeamScoreboardHandler();
		new ServerLogger();
		new UHCTrophiesItem();
	}
}
