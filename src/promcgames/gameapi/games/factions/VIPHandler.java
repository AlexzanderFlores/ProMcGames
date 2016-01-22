package promcgames.gameapi.games.factions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;

public class VIPHandler implements Listener {
	private static List<UUID> vips = null;
	private static List<UUID> vipPluses = null;
	
	public VIPHandler() {
		vips = new ArrayList<UUID>();
		vipPluses = new ArrayList<UUID>();
		new CommandBase("echest", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIPPlus(player)) {
					player.openInventory(player.getEnderChest());
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPlusPrefix() + " &cto use this command &b/buy");
				}
				return true;
			}
		};
		new CommandBase("enchant", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIPPlus(player)) {
					player.openEnchanting(player.getLocation(), true);
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPlusPrefix() + " &cto use this command &b/buy");
				}
				return true;
			}
		};
		new CommandBase("ext", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIP(player)) {
					player.setFireTicks(0);
					MessageHandler.sendMessage(player, "You have been extinguished");
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPrefix() + " &cto use this command &b/buy");
				}
				return true;
			}
		};
		new CommandBase("repairAll", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIPPlus(player)) {
					for(ItemStack item : player.getInventory().getContents()) {
						if(item != null && item.getType() != Material.AIR) {
							item.setDurability((short) 0);
						}
					}
					for(ItemStack item : player.getInventory().getArmorContents()) {
						if(item != null && item.getType() != Material.AIR) {
							item.setDurability((short) 0);
						}
					}
					MessageHandler.sendMessage(player, "Repaired all of your items");
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPlusPrefix() + " &cto use this command &b/buy");
				}
				return true;
			}
		}.enableDelay(60 * 60 * 2);
		new CommandBase("repair", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIP(player) || Ranks.ELITE.hasRank(player)) {
					ItemStack item = player.getItemInHand();
					if(item == null || item.getType() == Material.AIR) {
						MessageHandler.sendMessage(player, "&cYou must be holding an item");
						removeFromDelay(player);
					} else {
						item.setDurability((short) 0);
						MessageHandler.sendMessage(player, "Repaired the item in your hand");
					}
				} else {
					removeFromDelay(player);
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPrefix() + " &cor " + Ranks.ELITE.getPrefix() + "&cto use this command &b/buy");
				}
				return true;
			}
		}.enableDelay(60 * 30);
		new CommandBase("craft", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(VIPHandler.isVIP(player)) {
					player.openWorkbench(player.getLocation(), true);
				} else {
					MessageHandler.sendMessage(player, "&cYou must be a " + getVIPPrefix() + " &cto use this command &b/buy");
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static void remove(Player player) {
		vips.remove(player.getUniqueId());
		vipPluses.remove(player.getUniqueId());
	}
	
	public static boolean isVIP(Player player) {
		return isVIPPlus(player) || (vips != null && vips.contains(player.getUniqueId()));
	}
	
	public static void setVIP(Player player) {
		remove(player);
		vips.add(player.getUniqueId());
	}
	
	public static boolean isVIPPlus(Player player) {
		return vipPluses != null && vipPluses.contains(player.getUniqueId());
	}
	
	public static void setVIPPlus(Player player) {
		remove(player);
		vipPluses.add(player.getUniqueId());
	}
	
	public static boolean hasRank(Player player) {
		return vips.contains(player.getUniqueId()) || vipPluses.contains(player.getUniqueId());
	}
	
	public static String getVIPPrefix() {
		return StringUtil.color("&6[VIP]");
	}
	
	public static String getVIPPlusPrefix() {
		return StringUtil.color("&6[VIP&a+&6]");
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		String rank = DB.PLAYERS_FACTIONS_RANKS.getString("uuid", event.getPlayer().getUniqueId().toString(), "rank");
		if(rank != null) {
			if(rank.equals("VIP")) {
				vips.add(event.getPlayer().getUniqueId());
			} else if(rank.equals("VIP_PLUS")) {
				vipPluses.add(event.getPlayer().getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		if(vips.contains(uuid)) {
			event.setFormat(getVIPPrefix() + " " + event.getFormat());
		} else if(vipPluses.contains(uuid)) {
			event.setFormat(getVIPPlusPrefix() + " " + event.getFormat());
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if(isVIP(event.getPlayer())) {
			for(int a = 0; a < 4; ++a) {
				event.setLine(a, StringUtil.color(event.getLine(a)));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(isVIPPlus(event.getEntity())) {
			event.setKeepLevel(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
