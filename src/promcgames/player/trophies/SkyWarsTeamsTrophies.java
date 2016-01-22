package promcgames.player.trophies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
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
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class SkyWarsTeamsTrophies implements Listener {
	private static ItemStack item = null;
	
	public SkyWarsTeamsTrophies() {
		if(ProMcGames.getPlugin() == Plugins.SKY_WARS_TEAMS) {
			item = new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies").getItemStack();
		}
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static String getName() {
		return "Sky Wars Teams Trophies";
	}
	
	public static void open(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int unlocked = 0;
				int total = 0;
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				if(Swordsman.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(VictoryHunter1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(VictoryHunter2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(VictoryHunter3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(VictoryHunter4.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(VictoryHunter5.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(MonthlyKillSeeker1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(MonthlyKillSeeker2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(MonthlyKillSeeker3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ToTheVoid1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ToTheVoid2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ToTheVoid3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					inventory.setItem(inventory.getSize() - 9, new ItemCreator(Material.ARROW).setName("&bBack").getItemStack());
				}
				inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.EMERALD).setName("&e" + unlocked + "&7/&e" + total + " &aTrophies Unlocked").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(item != null && ProMcGames.getMiniGame().getJoiningPreGame()) {
			event.getPlayer().getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(ItemUtil.isItem(event.getPlayer().getItemInHand(), item)) {
			open(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			Player player = event.getPlayer();
			if(item == null) {
				TrophiesItem.open(player);
			} else {
				player.closeInventory();
			}
			event.setCancelled(true);
		}
	}
}
