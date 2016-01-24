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

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup.SetupPhase;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
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
import promcgames.gameapi.games.survivalgames.trophies.VictoryHunter1;
import promcgames.gameapi.games.survivalgames.trophies.VictoryHunter2;
import promcgames.gameapi.games.survivalgames.trophies.VictoryHunter3;
import promcgames.gameapi.games.survivalgames.trophies.VictoryHunter4;
import promcgames.gameapi.games.survivalgames.trophies.VictoryHunter5;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class SurvivalGamesTrophies implements Listener {
	private static ItemStack item = null;
	
	public SurvivalGamesTrophies() {
		if(ProMcGames.getMiniGame() != null) {
			item = new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies").getItemStack();
		}
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static String getName() {
		return "Survival Games Trophies";
	}
	
	public static void open(final Player player) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int unlocked = 0;
				int total = 0;
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				if(KatnissChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(FastWin.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(SecondariesChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BaccaChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(FirstBlood.getInstance().addToInventory(player, inventory)) {
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
				if(ChestHunter1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ChestHunter2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ChestHunter3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(BrokenLegsChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(NoChestChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(OneChestChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Tier1OnlyChallenge.getInstance().addToInventory(player, inventory)) {
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
				if(DoubleKillChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(NoSponsorChallenge.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(CookieMonsterChallenge.getInstance().addToInventory(player, inventory)) {
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
		if(item != null && ProMcGames.getMiniGame().getJoiningPreGame() && (!SurvivalGames.isClanBattle() || ClanBattleSetup.getSetupPhase() == SetupPhase.DONE)) {
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
