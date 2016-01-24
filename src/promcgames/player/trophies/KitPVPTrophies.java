package promcgames.player.trophies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.gameapi.games.kitpvp.trophies.BlazeOfGlory;
import promcgames.gameapi.games.kitpvp.trophies.ChickenHunt;
import promcgames.gameapi.games.kitpvp.trophies.Enchanted;
import promcgames.gameapi.games.kitpvp.trophies.ExplosiveBow;
import promcgames.gameapi.games.kitpvp.trophies.ExtraHealth;
import promcgames.gameapi.games.kitpvp.trophies.Juggernaut;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker1;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker2;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker3;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp1;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp2;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp3;
import promcgames.gameapi.games.kitpvp.trophies.MonthlyKillSeeker1;
import promcgames.gameapi.games.kitpvp.trophies.MonthlyKillSeeker2;
import promcgames.gameapi.games.kitpvp.trophies.MonthlyKillSeeker3;
import promcgames.gameapi.games.kitpvp.trophies.PoisonBow;
import promcgames.gameapi.games.kitpvp.trophies.Revenge;
import promcgames.gameapi.games.kitpvp.trophies.SlimeTime;
import promcgames.gameapi.games.kitpvp.trophies.SnowballFight;
import promcgames.gameapi.games.kitpvp.trophies.Speed;
import promcgames.gameapi.games.kitpvp.trophies.Strength;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class KitPVPTrophies implements Listener {
	private static ItemStack item = null;
	
	public KitPVPTrophies() {
		if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
			item = new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies").getItemStack();
		}
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static String getName() {
		return "Kit PVP Trophies";
	}
	
	public static void open(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int unlocked = 0;
				int total = 0;
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				if(ChickenHunt.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Enchanted.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(LevelUp1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(LevelUp2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(LevelUp3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BlazeOfGlory.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Revenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KillSeeker1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KillSeeker2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(KillSeeker3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(PoisonBow.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ExtraHealth.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Strength.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Speed.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Juggernaut.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ExplosiveBow.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(SnowballFight.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(SlimeTime.getInstance().addToInventory(player, inventory)) {
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
