package promcgames.server.servers.clans.battle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

public class BattleHistoryHandler {
	private static Map<Clan, List<ClanBattleInfo>> loadedBattleHistory = null;
	
	/*
	 * Filter Settings:
	 * 0 = all
	 * 1 = ranked
	 * 2 = unranked
	 */
	public static void viewBattleHistory(Player player, int filter) {
		Clan clan = ClanHandler.getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			viewBattleHistory(player, clan.getClanName(), filter);
		}
	}
	
	public static void viewBattleHistory(Player player, String clanName, int filter) {
		final Clan clan = ClanHandler.getClan(clanName);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cThis clan does not exist. Note that it is &bcase-sensitive");
		} else {
			if(loadedBattleHistory == null) {
				loadedBattleHistory = new HashMap<Clan, List<ClanBattleInfo>>();
			}
			List<ClanBattleInfo> clanBattles = null;
			if(loadedBattleHistory.containsKey(clan)) {
				clanBattles = loadedBattleHistory.get(clan);
			} else {
				try {
					clanBattles = new ArrayList<ClanBattleInfo>();
					ResultSet rs = DB.NETWORK_ClANS_BATTLE_HISTORY.getConnection().prepareStatement("SELECT enemy_clan_id,result,players_left,ranked FROM clans_battle_history WHERE clan_id = '" + clan.getClanID() + "'").executeQuery();
					while(rs.next()) {
						clanBattles.add(new ClanBattleInfo(rs.getInt("enemy_clan_id"), rs.getInt("result"), rs.getInt("players_left"), rs.getInt("ranked")));
					}
					loadedBattleHistory.put(clan, clanBattles);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							if(loadedBattleHistory.containsKey(clan)) {
								loadedBattleHistory.get(clan).clear();
								loadedBattleHistory.remove(clan);
							}
						}
					}, 20 * 10);
				} catch(SQLException e) {
					e.printStackTrace();
					clanBattles = null;
				}
			}
			if(clanBattles != null) {
				Inventory inv = Bukkit.createInventory(player, 54, clan.getClanName() + "'s Battle History");
				inv.setItem(4, new ItemCreator(Material.ARROW).setName("&bClick to go back").addLore("").addLore("&bGreen Glass represents a Win")
						.addLore("&bRed Glass represents a Loss").getItemStack());
				int index = 9;
				for(int i = clanBattles.size() - 1; i >= 0; i--) {
					if(index >= 54) {
						break;
					}
					ClanBattleInfo info = clanBattles.get(i);
					if(filter == 0 || (filter == 1 && info.getRanked() == 1) || (filter == 2 && info.getRanked() == 0)) {
						int data = info.getResult() == 0 ? 14 : 5;
						String enemyClanName = ClanHandler.getClanName(info.getEnemyClanID());
						if(enemyClanName == null) {
							enemyClanName = "Not Known";
						} else {
							Clan enemyClan = ClanHandler.getClan(enemyClanName);
							if(enemyClan != null) {
								enemyClanName = enemyClan.getColorTheme() + enemyClan.getClanName();
							}
						}
						Bukkit.getLogger().info("stained glass data: " + data);
						inv.setItem(index++, new ItemCreator(Material.STAINED_GLASS, data).setName("&bGame #" + (i + 1)).addLore("&bEnemy Clan: " + enemyClanName)
								.addLore("&bPlayers Left: " + info.getPlayersLeft()).addLore(" ").addLore(info.getRanked() == 1 ? "&6Ranked" : "&6Unranked").getItemStack());
					}
				}
				if(player.getOpenInventory() != null) {
					player.closeInventory();
				}
				player.openInventory(inv);
			} else {
				MessageHandler.sendMessage(player, "&cThe battle history could not load. Try again later");
			}
		}
	}
}
