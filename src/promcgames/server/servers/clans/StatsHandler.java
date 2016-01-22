package promcgames.server.servers.clans;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.servers.clans.ClansHubStatsHandler.ClanStats;
import promcgames.server.util.ItemCreator;

public class StatsHandler {
	public static void viewStats(Player player) {
		Clan clan = ClanHandler.getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		} else {
			viewStats(player, player.getName());
		}
	}
	
	public static void viewStats(Player player, String user) {
		UUID uuid = AccountHandler.getUUID(user);
		if(uuid == null) {
			MessageHandler.sendMessage(player, "&cThis user has never logged on before");
		} else {
			String clanName = ClanHandler.getClanName(user);
			if(clanName == null) {
				MessageHandler.sendMessage(player, "&cThis user is not in a clan");
			} else {
				Clan clan = ClanHandler.getClan(clanName);
				if(clan == null) {
					MessageHandler.sendMessage(player, "&cThis user is not in a clan");
				} else {
					ClanStats clanStats = ClansHubStatsHandler.getClanStats(uuid);
					if(clanStats == null) {
						MessageHandler.sendMessage(player, "&cThis user doesn't have any clan stats");
					} else {
						Inventory inv = Bukkit.createInventory(player, 9, user + "'s Clan Stats");
						inv.setItem(0, new ItemCreator(Material.SKULL_ITEM).setName("&bCurrent Clan").addLore(clan.getColorTheme() + clan.getClanName()).getItemStack());
						inv.setItem(1, new ItemCreator(Material.DIAMOND).setName("&bBattle Wins: " + clanStats.getWins()).getItemStack());
						inv.setItem(2, new ItemCreator(Material.COAL).setName("&bBattle Losses: " + clanStats.getLosses()).getItemStack());
						inv.setItem(3, new ItemCreator(Material.DIAMOND_SWORD).setName("&bBattle Kills: " + clanStats.getKills()).getItemStack());
						inv.setItem(4, new ItemCreator(Material.SKULL_ITEM).setName("&bBattle Deaths: " + clanStats.getDeaths()).getItemStack());
						inv.setItem(8, new ItemCreator(Material.ARROW).setName("&bClick to go back").addLore("")
								.addLore("&6These stats are only").addLore("&6battles which the user has").addLore("&6participated in from their")
								.addLore("current clan").getItemStack());
						if(player.getOpenInventory() != null) {
							player.closeInventory();
						}
						player.openInventory(inv);
					}
				}
			}
		}
	}
	

	
	public static void backupClanStats(String user) {
		String clanName = ClanHandler.getClanName(user);
		if(clanName != null) {
			Clan clan = ClanHandler.getClan(clanName);
			if(clan != null) {
				UUID uuid = AccountHandler.getUUID(user);
				if(uuid != null) {
					ClanStats clanStats = ClansHubStatsHandler.getClanStats(uuid);
					if(clanStats != null) {
						DB.PLAYERS_CLANS_BACKED_UP_STATS.insert("'" + uuid.toString() + "', '" + clan.getClanID() + "', '" + clanStats.getWins() + "', '" + clanStats.getLosses() + "', '" + clanStats.getKills() + "', '" + clanStats.getDeaths() + "'");
					}
				}
			}
		}
	}
}
