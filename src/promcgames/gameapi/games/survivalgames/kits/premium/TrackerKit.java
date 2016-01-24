package promcgames.gameapi.games.survivalgames.kits.premium;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class TrackerKit extends KitBase implements Listener {
	private static KitBase instance = null;
	private static boolean enabled = false;
	
	public TrackerKit() {
		super(new ItemCreator(Material.COMPASS).setName("&aTracker").setLores(new String[] {
			"&bPrice: 0",
			"",
			"&6Use of this kit will give all",
			"&6players a tracking compass at",
			"&6the start of the game. This will",
			"&6point to the nearest player",
			"",
			"&cPurchase with &e/buy"
		}).getItemStack(), 29, true);
		instance = this;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public String getPermission() {
		return "survival_games.tracker";
	}
	
	@Override
	public void use(Player player) {
		if(Ranks.ELITE.hasRank(player, true)) {
			super.use(player);
		} else {
			String [] keys = new String [] {"uuid", "kit"};
			String [] values = new String [] {Disguise.getUUID(player).toString(), getPermission()};
			if(DB.PLAYERS_KITS.isKeySet(keys, values)) {
				super.use(player);
			} else {
				MessageHandler.sendMessage(player, "&cYou do not own this kit! You can purchase it here:");
				MessageHandler.sendMessage(player, "http://store.promcgames.com/category/372707");
			}
		}
	}

	@Override
	public void execute() {
		if(getPlayers().size() > 0) {
			enabled = true;
			for(Player player : ProPlugin.getPlayers()) {
				player.getInventory().addItem(new ItemCreator(Material.COMPASS).setName("&aPlayer Tracker").getItemStack());
			}
			MessageHandler.alert("&l&n" + getName() + "&e is in use due to 1 or more players using it");
			MessageHandler.alert("Everyone starts with a tracking compass");
			MessageHandler.alert("Use this kit any time: &chttp://store.promcgames.com/category/372707");
			EventUtil.register(this);
		}
	}

	@Override
	public void execute(Player player) {
		
	}
	
	public static KitBase getInstance() {
		return instance;
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			Player near = null;
			double closest = 0.0d;
			for(Player online : ProPlugin.getPlayers()) {
				if(!online.getName().equals(Disguise.getName(player)) && !SpectatorHandler.contains(online)) {
					double x1 = player.getLocation().getX();
					double y1 = player.getLocation().getX();
					double z1 = player.getLocation().getZ();
					double x2 = online.getLocation().getX();
					double y2 = online.getLocation().getX();
					double z2 = online.getLocation().getX();
					double distance = Math.sqrt((x1 - x2) * (x1 - x2) + ((y1 - y2) * (y1 - y2)) + (z1 - z2) * (z1 - z2));
					if(near == null || distance < closest) {
						near = online;
						closest = distance;
					}
				}
			}
			if(near != null) {
				player.setCompassTarget(near.getLocation());
			}
		}
	}
}
