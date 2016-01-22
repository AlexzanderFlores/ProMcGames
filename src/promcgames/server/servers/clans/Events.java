package promcgames.server.servers.clans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.ProPlugin;
import promcgames.customevents.player.PlayerViewStatsEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.gameapi.EloHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Events implements Listener {
	private static Map<UUID, String> toJoin = null;
	
	public Events() {
		toJoin = new HashMap<UUID, String>();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = (Player) event.getPlayer();
		Random random = new Random();
		Location spawn = new Location(player.getWorld(), -23.5, 8.0, -23.5);
		int range = 5;
		spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setY(spawn.getY() + 2.5d);
		spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setYaw(-224.55f);
		spawn.setPitch(-1.80f);
		player.teleport(spawn);
		if(Ranks.PRO.hasRank(player)) {
			player.setAllowFlight(true);
		}
		if(ClanHandler.isInClan(player)) {
			Clan clan = ClanHandler.getClan(player);
			if(clan != null) {
				clan.addPlayer(player);
			} else {
				ClanHandler.loadClan(player, ClanHandler.getClanName(player));
			}
		}
		if(toJoin.containsKey(player.getUniqueId())) {
			player.getInventory().setItem(6, new ItemCreator(Material.DIAMOND_SWORD).setName("&a&lClick To Join Clan Battle")
					.addLore("").addLore("&6If right clicking the sword doesn't").addLore("&6work, try:").addLore("&b/join " + toJoin.get(player.getUniqueId()))
					.addLore("").addLore("&6If you wait too long").addLore("&6you may not be able to join").getItemStack());
			MessageHandler.sendMessage(player, "Right-click the diamond sword to join the clan battle");
			EffectUtil.playSound(player, Sound.ANVIL_LAND);
		}
		player.setLevel(Clans.hubNumber);
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				if(DB.PLAYERS_CLANS_TOJOIN.getSize() != 0) {
					List<String> uuids = DB.PLAYERS_CLANS_TOJOIN.getAllStrings("uuid");
					List<String> serverNames = DB.PLAYERS_CLANS_TOJOIN.getAllStrings("server_name");
					int index = 0;
					for(String uuid : uuids) {
						Player player = Bukkit.getPlayer(UUID.fromString(uuid));
						String serverName = serverNames.get(index++);
						if(player != null) {
							player.getInventory().setItem(6, new ItemCreator(Material.DIAMOND_SWORD).setName("&a&lClick To Join Clan Battle")
									.addLore("").addLore("&6If right clicking the sword doesn't").addLore("&6work, try:").addLore("&b/join " + serverName)
									.addLore("").addLore("&6If you wait too long").addLore("&6you may not be able to join").getItemStack());
							MessageHandler.sendMessage(player, "Right-click the diamond sword to join the clan battle");
							EffectUtil.playSound(player, Sound.ANVIL_LAND);
						} else {
							final UUID uuidO = UUID.fromString(uuid);
							toJoin.put(uuidO, serverName);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									toJoin.remove(uuidO);
								}
							}, 60 * 20);
						}
					}
					DB.PLAYERS_CLANS_TOJOIN.execute("delete from " + DB.PLAYERS_CLANS_TOJOIN.getName().toLowerCase());
				}
			}
		});
	}
	
	@EventHandler
	public void onPlayerViewStats(PlayerViewStatsEvent event) {
		Player player = event.getPlayer();
		Player target = ProPlugin.getPlayer(event.getTargetName());
		if(target == null) {
			MessageHandler.sendMessage(player, "&c" + event.getTargetName() + " is not online");
		} else {
			MessageHandler.sendMessage(player, "&eElo: &c" + EloHandler.getElo(target));
		}
	}
}