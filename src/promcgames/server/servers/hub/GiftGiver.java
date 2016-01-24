package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.timed.PlayerFiveSecondConnectedOnceEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.CommunityLevelHandler;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.items.HubSponsor;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class GiftGiver implements Listener {
	private String title = null;
	private Map<Ranks, Byte> bytes = null;
	private List<String> queue = null;
	
	public GiftGiver() {
		title = "Gift Giver";
		bytes = new HashMap<Ranks, Byte>();
		bytes.put(Ranks.ELITE, (byte) 2);
		bytes.put(Ranks.PRO_PLUS, (byte) 3);
		bytes.put(Ranks.PRO, (byte) 5);
		queue = new ArrayList<String>();
		new NPCEntity(EntityType.IRON_GOLEM, "&b" + title, new Location(Bukkit.getWorlds().get(0), -95.5, 126, -175.5)) {
			@Override
			public void onInteract(Player player) {
				EffectUtil.playSound(player, Sound.IRONGOLEM_HIT);
				open(player);
			}
		};
		new CommandBase("giftGiver", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.teleport(new Location(player.getWorld(), -97.5, 126, -173.5, -146.85f, -6.59f));
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 5, title);
		Material material = Material.BEDROCK;
		if(Ranks.ELITE.hasRank(player) && canGetGift(player)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(11, new ItemCreator(material, bytes.get(Ranks.ELITE)).setName(Ranks.ELITE.getPrefix() + "&eGift").setLores(new String [] {
			"",
			"&e+3 &aHub Sponsors",
			"&e+15 &aSG Auto Sponsor Passes",
			"&e+60 &aKit PVP Auto Regen Passes"
		}).getItemStack());
		material = Material.BEDROCK;
		if(AccountHandler.getRank(player) == Ranks.PRO_PLUS && canGetGift(player)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(13, new ItemCreator(material, bytes.get(Ranks.PRO_PLUS)).setName(Ranks.PRO_PLUS.getPrefix() + "&eGift").setLores(new String [] {
			"",
			"&e+2 &aHub Sponsors",
			"&e+10 &aSG Auto Sponsor Passes",
			"&e+40 &aKit PVP Auto Regen Passes"
		}).getItemStack());
		material = Material.BEDROCK;
		if(AccountHandler.getRank(player) == Ranks.PRO && canGetGift(player)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(15, new ItemCreator(material, bytes.get(Ranks.PRO)).setName(Ranks.PRO.getPrefix() + "&eGift").setLores(new String [] {
			"",
			"&e+1 &aHub Sponsors",
			"&e+5 &aSG Auto Sponsor Passes",
			"&e+20 &aKit PVP Auto Regen Passes"
		}).getItemStack());
		material = Material.BEDROCK;
		if(canGetDailyGift(player)) {
			material = Material.STAINED_GLASS;
		}
		inventory.setItem(31, new ItemCreator(material, 8).setName("&eDaily Gift").setLores(new String [] {
			"",
			"&fNote: You can get this gift with any rank",
			"",
			"&e+5 &aCommunity Level",
			"&e+50 &aEmeralds",
			"&e+1 &aSG Auto Sponsor Pass",
			"&e+4 &aKit PVP Auto Regen Passes"
		}).getItemStack());
		player.openInventory(inventory);
	}
	
	private boolean canGetGift(Player player) {
		int week = -1;
		if(DB.HUB_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
			week = DB.HUB_GIFT_LOGS.getInt("uuid", player.getUniqueId().toString(), "week");
		}
		int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
		if(week == currentWeek) {
			return false;
		}
		return true;
	}
	
	private boolean canGetDailyGift(Player player) {
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		int day = -1;
		if(DB.HUB_DAILY_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
			day = DB.HUB_DAILY_GIFT_LOGS.getInt("uuid", player.getUniqueId().toString(), "day");
		}
		if(day == currentDay) {
			return false;
		}
		return true;
	}
	
	private void alert(Player player) {
		//MessageHandler.alert(AccountHandler.getPrefix(player) + " &ehas got a gift from the &bGift Giver &eNPC");
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(title)) {
			Player player = event.getPlayer();
			player.closeInventory();
			String item = ChatColor.stripColor(event.getItemTitle());
			if(item.equals("Daily Gift")) {
				if(canGetDailyGift(player)) {
					int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
					if(DB.HUB_DAILY_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
						DB.HUB_DAILY_GIFT_LOGS.updateInt("day", currentDay, "uuid", player.getUniqueId().toString());
					} else {
						DB.HUB_DAILY_GIFT_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + currentDay + "'");
					}
					CommunityLevelHandler.addCommunityLevel(player, 5);
					EmeraldsHandler.addEmeralds(player, 50, EmeraldReason.DAILY_GIFT, false);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addSGAutoSponsors " + player.getName() + " 1");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addKitPVPAutoRegenPass " + player.getName() + " 4");
					open(player);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
					alert(player);
				} else {
					MessageHandler.sendMessage(player, "&cYou have already claimed your daily gift today");
				}
			} else if(Ranks.PRO.hasRank(player)) {
				int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
				if(item.equals("[Elite] Gift")) {
					if(Ranks.ELITE.hasRank(player)) {
						if(canGetGift(player)) {
							if(DB.HUB_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
								DB.HUB_GIFT_LOGS.updateInt("week", currentWeek, "uuid", player.getUniqueId().toString());
							} else {
								DB.HUB_GIFT_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + currentWeek + "'");
							}
							HubSponsor.add(player.getUniqueId(), 3, false);
							MessageHandler.sendMessage(player, "&e+3 &aHub Sponsor");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addSGAutoSponsors " + player.getName() + " 15");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addKitPVPAutoRegenPass " + player.getName() + " 60");
							open(player);
							EffectUtil.playSound(player, Sound.LEVEL_UP);
							alert(player);
						} else {
							MessageHandler.sendMessage(player, "&cYou have already claimed your gift for this week");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have " + Ranks.ELITE.getPrefix() + "&b/buy");
					}
				} else if(item.equals("[Pro+] Gift")) {
					if(AccountHandler.getRank(player) == Ranks.PRO_PLUS) {
						if(canGetGift(player)) {
							if(DB.HUB_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
								DB.HUB_GIFT_LOGS.updateInt("week", currentWeek, "uuid", player.getUniqueId().toString());
							} else {
								DB.HUB_GIFT_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + currentWeek + "'");
							}
							HubSponsor.add(player.getUniqueId(), 2, false);
							MessageHandler.sendMessage(player, "&e+2 &aHub Sponsor");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addSGAutoSponsors " + player.getName() + " 10");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addKitPVPAutoRegenPass " + player.getName() + " 40");
							open(player);
							EffectUtil.playSound(player, Sound.LEVEL_UP);
							alert(player);
						} else {
							MessageHandler.sendMessage(player, "&cYou have already claimed your gift for this week");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have " + Ranks.PRO_PLUS.getPrefix() + "&b/buy");
					}
				} else if(item.equals("[Pro] Gift")) {
					if(AccountHandler.getRank(player) == Ranks.PRO) {
						if(canGetGift(player)) {
							if(DB.HUB_GIFT_LOGS.isUUIDSet(player.getUniqueId())) {
								DB.HUB_GIFT_LOGS.updateInt("week", currentWeek, "uuid", player.getUniqueId().toString());
							} else {
								DB.HUB_GIFT_LOGS.insert("'" + player.getUniqueId().toString() + "', '" + currentWeek + "'");
							}
							HubSponsor.add(player.getUniqueId(), 1, false);
							MessageHandler.sendMessage(player, "&e+1 &aHub Sponsor");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addSGAutoSponsors " + player.getName() + " 5");
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addKitPVPAutoRegenPass " + player.getName() + " 20");
							open(player);
							EffectUtil.playSound(player, Sound.LEVEL_UP);
							alert(player);
						} else {
							MessageHandler.sendMessage(player, "&cYou have already claimed your gift for this week");
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have " + Ranks.PRO.getPrefix() + "&b/buy");
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a " + Ranks.PRO.getPrefix() + "&cor above to use this &b/buy");
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerFiveSecondConnected(PlayerFiveSecondConnectedOnceEvent event) {
		queue.add(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(!queue.isEmpty()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String name = queue.get(0);
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						if(canGetGift(player) || canGetDailyGift(player)) {
							MessageHandler.sendMessage(player, "");
							ChatClickHandler.sendMessageToRunCommand(player, " &bClick here to teleport", "Click to teleport to the NPC", "/giftGiver", "&6You have gifts to claim!");
							MessageHandler.sendMessage(player, "");
							EffectUtil.playSound(player, Sound.LEVEL_UP);
						}
					}
					queue.remove(0);
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		queue.remove(event.getPlayer().getName());
	}
}
