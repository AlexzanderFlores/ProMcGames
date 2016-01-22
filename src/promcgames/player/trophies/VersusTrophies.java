package promcgames.player.trophies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
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
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class VersusTrophies implements Listener {
	private static ItemStack item = null;
	
	public VersusTrophies() {
		if(ProMcGames.getPlugin() == Plugins.VERSUS) {
			item = new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies").getItemStack();
		}
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static String getName() {
		return "Versus Trophies";
	}
	
	public static void open(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int unlocked = 0;
				int total = 0;
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				if(KillstreakSeeker1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KillstreakSeeker2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KillstreakSeeker3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BoomHeadshot1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BoomHeadshot2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BoomHeadshot3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(LeatherChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(GoldChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ChainChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(IronChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(DiamondChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(SurvivalGamesChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ArcherChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(UHCChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(SwordsmanChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(PyroChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(EnderChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BaccaChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(GappleChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(TNTChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(OneHitWonderChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KohiChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(NoDebuffChampion.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(QuickshotChampion.getInstance().addToInventory(player, inventory)) {
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
