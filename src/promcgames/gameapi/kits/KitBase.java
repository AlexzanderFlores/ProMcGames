package promcgames.gameapi.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerKitPurchaseEvent;
import promcgames.customevents.player.PlayerKitSelectEvent;
import promcgames.customevents.player.SpecialKitDenyEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.TimeUtil;

public abstract class KitBase {
	private static List<KitBase> kits = null;
	private static int lastSlot = -1;
	private ItemStack icon = null;
	private int slot = 0;
	private int price = -1;
	private int votes = -1;
	private KitShop shop = null;
	private boolean donator = false;
	private KitBase required = null;
	private List<String> users = null;
	
	public KitBase(ItemStack icon) {
		this(icon, -1);
	}
	
	public KitBase(ItemStack icon, int slot) {
		this(icon, slot, null);
	}
	
	public KitBase(ItemStack icon, int slot, KitShop shop) {
		this(icon, slot, false, shop);
	}
	
	public KitBase(ItemStack icon, int slot, boolean donator) {
		this(icon, slot, donator, null);
	}
	
	public KitBase(ItemStack icon, int slot, boolean donator, KitShop shop) {
		if(kits == null) {
			kits = new ArrayList<KitBase>();
		}
		if(slot > -1) {
			lastSlot = slot;
		} else {
			slot = ++lastSlot;
		}
		this.slot = slot;
		try {
			this.price = Integer.valueOf(ChatColor.stripColor(icon.getItemMeta().getLore().get(0).replace(ChatColor.AQUA + "Price: ", "")));
		} catch(NumberFormatException e) {
			
		} catch(NullPointerException e) {
			
		} catch(IndexOutOfBoundsException e) {
			
		}
		try {
			this.votes = Integer.valueOf(ChatColor.stripColor(icon.getItemMeta().getLore().get(1).replace(ChatColor.AQUA + "Monthly Votes: ", "")));
		} catch(NumberFormatException e) {
			
		} catch(NullPointerException e) {
			
		} catch(IndexOutOfBoundsException e) {
			
		}
		ItemMeta meta = icon.getItemMeta();
		icon = new ItemCreator(icon).setName("&a" + meta.getDisplayName()).getItemStack();
		this.icon = icon;
		this.donator = donator;
		if(shop == null) {
			shop = KitShop.getMostRecentShop();
		}
		this.shop = shop;
		kits.add(this);
	}
	
	public boolean owns(Player player) {
		if(ProMcGames.getProPlugin().getStaffGetAllKits() && Ranks.isStaff(player)) {
			return true;
		}
		return DB.PLAYERS_KITS.isKeySet(new String [] {"uuid", "kit"}, new String [] {Disguise.getUUID(player).toString(), getPermission()});
	}
	
	public void use(Player player) {
		use(player, false);
	}
	
	public void use(Player player, boolean defaultKit) {
		boolean hasForFree = Disguise.getRealRank(player) == Ranks.YOUTUBER || (ProMcGames.getProPlugin().getStaffGetAllKits() && Ranks.HELPER.hasRank(player, true));
		if(!hasForFree && required != null && !required.owns(player)) {
			MessageHandler.sendMessage(player, "&cYou must have &e" + required.getName() + " &cbefore purchasing this kit");
			return;
		}
		if(hasForFree || owns(player)) {
			String [] keys = new String [] {"uuid", "date"};
			String [] values = new String [] {player.getUniqueId().toString(), TimeUtil.getTime().substring(0, 7)};
			if(votes > -1 && DB.PLAYERS_VOTES.getInt(keys, values, "votes") < votes) {
				MessageHandler.sendMessage(player, "&cYou do not have enough monthly votes! &e/vote");
				return;
			}
			PlayerKitSelectEvent event = new PlayerKitSelectEvent(player, this);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				for(KitBase kit : kits) {
					if(kit.getShop() == this.getShop()) {
						kit.remove(player);
					}
				}
				MessageHandler.sendMessage(player, "Selected " + getName());
				if(!defaultKit) {
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							String game = ProMcGames.getPlugin().toString();
							String [] keys = new String [] {"uuid", "game_name"};
							String [] values = new String [] {uuid.toString(), game};
							if(DB.PLAYERS_STORED_KITS.isKeySet(keys, values)) {
								DB.PLAYERS_STORED_KITS.updateString("kit", getName(), keys, values);
							} else {
								DB.PLAYERS_STORED_KITS.insert("'" + uuid.toString() + "', '" + game + "', '" + getName() + "'");
							}
						}
					});
				}
				if(users == null) {
					users = new ArrayList<String>();
				}
				users.add(Disguise.getName(player));
				if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
					execute(player);
				}
			}
		} else {
			boolean ignoreEmeralds = false;
			if(getPrice() == -1 && ProMcGames.getMiniGame().getGameState() != GameStates.ENDING) {
				SpecialKitDenyEvent event = new SpecialKitDenyEvent(player, this);
				Bukkit.getPluginManager().callEvent(event);
				if(event.isCancelled()) {
					return;
				}
				ignoreEmeralds = true;
			}
			if(ignoreEmeralds || EmeraldsHandler.getEmeralds(player) >= getPrice()) {
				PlayerKitPurchaseEvent event = new PlayerKitPurchaseEvent(player, this);
				Bukkit.getPluginManager().callEvent(event);
				if(!event.isCancelled()) {
					if(!ignoreEmeralds) {
						EmeraldsHandler.addEmeralds(player, getPrice() * -1, EmeraldReason.KIT_PURCHASE, false);
					}
					giveKit(player);
					for(KitBase kit : kits) {
						kit.remove(player);
					}
					MessageHandler.sendMessage(player, "Selected " + getName());
					if(users == null) {
						users = new ArrayList<String>();
					}
					users.add(Disguise.getName(player));
					if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
						execute(player);
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have enough emeralds for this kit");
			}
		}
	}
	
	public void giveKit(Player player) {
		DB.PLAYERS_KITS.insert("'" + Disguise.getUUID(player).toString() + "', '" + getPermission() + "'");
	}
	
	public String getName() {
		return ChatColor.stripColor(this.getIcon().getItemMeta().getDisplayName());
	}
	
	public ItemStack getIcon() {
		return this.icon;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	public int getPrice() {
		return this.price;
	}
	
	public boolean isDonator() {
		return this.donator;
	}
	
	public void setRequired(KitBase required) {
		this.required = required;
	}
	
	public boolean has(Player player) {
		return users != null && users.contains(Disguise.getName(player)) && !SpectatorHandler.contains(player);
	}
	
	public void remove(Player player) {
		if(has(player)) {
			users.remove(Disguise.getName(player));
		}
	}
	
	public KitShop getShop() {
		return shop;
	}
	
	public void setShop(KitShop shop) {
		this.shop = shop;
	}
	
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(has(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static List<KitBase> getKits() {
		return kits;
	}
	
	public static int getLastSlot() {
		return lastSlot;
	}
	
	public abstract String getPermission();
	public abstract void execute();
	public abstract void execute(Player player);
}
