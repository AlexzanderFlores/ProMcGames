package promcgames.gameapi.games.survivalgames.kits.premium;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.DeathmatchStartEvent;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.gameapi.games.survivalgames.Events;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ResurrectionKit extends KitBase implements Listener {
	private static KitBase instance = null;
	public static boolean used = false;
	private List<String> died = null;
	private List<String> respawned = null;
	
	public ResurrectionKit() {
		super(new ItemCreator(Material.GOLD_NUGGET).setName("&aResurrection").setLores(new String[] {
			"&bPrice: 0",
			"",
			"&6Use of this kit will give all",
			"&6players one extra life",
			"&6Does not work in deathmatch",
			"&6When you die you get a stone sword",
			"",
			"&cPurchase with &e/buy"
		}).getItemStack(), 31, true);
		instance = this;
		EventUtil.register(this);
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
	public String getPermission() {
		return "survival_games.resurrection";
	}

	@Override
	public void execute() {
		if(getPlayers().size() > 0) {
			used = true;
			died = new ArrayList<String>();
			respawned = new ArrayList<String>();
			MessageHandler.alert("&l&n" + getName() + "&e is in use due to 1 or more players using it");
			MessageHandler.alert("Everyone gets one extra life");
			MessageHandler.alert("Use this kit any time: &chttp://store.promcgames.com/category/372707");
		}
	}

	@Override
	public void execute(Player player) {
		
	}
	
	public static KitBase getInstance() {
		return instance;
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(used) {
			died.add(Disguise.getName(event.getPlayer()));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		if(used) {
			final Player player = event.getPlayer();
			if(!respawned.contains(Disguise.getName(player)) && died.contains(Disguise.getName(player))) {
				died.remove(Disguise.getName(player));
				respawned.add(Disguise.getName(player));
				MessageHandler.sendMessage(player, "Due to one or more players using \"" + getName() + "\" you've respawned");
				MessageHandler.sendMessage(player, "Use this kit any time: &chttp://store.promcgames.com/category/372707");
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						List<Location> spawns = Events.spawnPointHandler.getSpawns();
						player.teleport(spawns.get(new Random().nextInt(spawns.size())));
						while(player.getLocation().getBlock().getType() != Material.AIR) {
							player.teleport(player.getLocation().add(0, 1, 0));
						}
						player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
					}
				});
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onDeathmatchStart(DeathmatchStartEvent event) {
		if(used) {
			HandlerList.unregisterAll(this);
			MessageHandler.alert("&eThe \"" + getName() + "\" kit has now been disabled");
		}
	}
}
