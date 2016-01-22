package promcgames.player;

import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

public class NameColor implements Listener {
	private Map<String, String> colorCodes = null;
	private String name = null;
	
	public NameColor() {
		colorCodes = new HashMap<String, String>();
		name = "Name Color Selector";
		new CommandBase("nameColor", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(Disguise.isDisguised(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot use this while Disguise7d");
				} else {
					Inventory inventory = Bukkit.createInventory(player, 9 * 2, name);
					int [] slots = new int [] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16};
					int [] data = new int [] {15, 11, 13, 3, 14, 10, 1, 8, 7, 9, 5, 3, 6, 2, 4, 0};
					for(int a = 0; a < slots.length && a < data.length; ++a) {
						String color = getColor(a);
						inventory.setItem(slots[a], new ItemCreator(Material.STAINED_GLASS, data[a])
											 .setName("&" + color + "Click for this color")
											 .addLore("&aColor Code: " + color)
											 .addLore("&aExample: " + AccountHandler.getRank(player).getPrefix() + StringUtil.color("&" + color + player.getName()))
											 .getItemStack());
					}
					player.openInventory(inventory);
				}
				return true;
			}
		}.setRequiredRank(Ranks.ELITE);
		EventUtil.register(this);
	}
	
	private String getColor(int a) {
		if(a <= 9) {
			return String.valueOf(a);
		} else if(a == 10) {
			return "a";
		} else if(a == 11) {
			return "b";
		} else if(a == 12) {
			return "c";
		} else if(a == 13) {
			return "d";
		} else if(a == 14) {
			return "e";
		} else if(a == 15) {
			return "f";
		} else {
			return "";
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			final Player player = event.getPlayer();
			final String color = ChatColor.stripColor(event.getItem().getItemMeta().getLore().get(0)).replace("Color Code: ", "");
			colorCodes.put(player.getName(), color);
			MessageHandler.sendMessage(player, "You selected color code&" + color + " " + color);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(DB.PLAYERS_NAME_COLORS.isUUIDSet(player.getUniqueId())) {
						DB.PLAYERS_NAME_COLORS.updateString("color", color, "uuid", player.getUniqueId().toString());
					} else {
						DB.PLAYERS_NAME_COLORS.insert("'" + player.getUniqueId().toString() + "', '" + color + "'");
					}
				}
			});
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(Ranks.ELITE.hasRank(player, true) && !Disguise.isDisguised(player)) {
			if(!colorCodes.containsKey(player.getName())) {
				if(DB.PLAYERS_NAME_COLORS.isUUIDSet(player.getUniqueId())) {
					colorCodes.put(player.getName(), DB.PLAYERS_NAME_COLORS.getString("uuid", player.getUniqueId().toString(), "color"));
				} else {
					ChatClickHandler.sendMessageToRunCommand(player, " &c&lCLICK HERE", "Click to set your name color", "/nameColor", "&bYou can set your name color in chat");
					MessageHandler.sendMessage(player, "You can access this any time with &e/nameColor");
					colorCodes.put(player.getName(), "f");
				}
			}
			event.setFormat(event.getFormat().replace(player.getName(), StringUtil.color("&" + colorCodes.get(player.getName()) + player.getName() + ChatColor.WHITE)));
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		colorCodes.remove(event.getPlayer().getName());
	}
}
