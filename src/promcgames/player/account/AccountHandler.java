package promcgames.player.account;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.player.NewPlayerJoiningEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerRankChangeEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class AccountHandler implements Listener {
	public enum Ranks {
		PLAYER(ChatColor.GRAY, "", 1),
		PRO(ChatColor.GREEN, "[Pro]", 8),
		PRO_PLUS(ChatColor.AQUA, "[Pro" + ChatColor.GREEN + "+" + ChatColor.AQUA + "]", 9),
		ELITE(ChatColor.DARK_PURPLE, "[Elite]", 10),
		STREAMER(ChatColor.LIGHT_PURPLE, "[Streamer]", 12),
		YOUTUBER(ChatColor.LIGHT_PURPLE, "[YouTuber]", 11),
		HELPER(ChatColor.DARK_AQUA, "[Helper]", 4),
		MODERATOR(ChatColor.BLUE, "[Mod]", 3),
		SENIOR_MODERATOR(ChatColor.DARK_GREEN, "[Sr. Mod]", 5),
		DEV(ChatColor.RED, "[Dev]", 7),
		ADMIN(ChatColor.RED, "[Admin]", 2),
		OWNER(ChatColor.DARK_RED, "[Owner]", 6);
		
		private ChatColor color = null;
		private String prefix = null;
		private int groupID = 0;
		
		private Ranks(ChatColor color, String prefix, int groupID) {
			this.color = color;
			this.prefix = color + prefix + (prefix.equals("") ? "" : " " + ChatColor.WHITE);
			this.groupID = groupID;
		}
		
		public ChatColor getColor() {
			return this.color;
		}
		
		public String getPrefix() {
			return this.prefix;
		}
		
		public String getNoPermission() {
			return "&cTo use this you must have " + getPrefix();
		}
		
		public String getPermission() {
			return "rank." + toString().toLowerCase().replace("_", ".");
		}
		
		public boolean hasRank(CommandSender sender) {
			return hasRank(sender, false);
		}
		
		public boolean hasRank(CommandSender sender, boolean realRank) {
			return realRank ? Disguise.getRealRank(sender).isAboveRank(this) : getRank(sender).isAboveRank(this);
		}
		
		public boolean isAboveRank(Ranks rank) {
			return ordinal() >= rank.ordinal();
		}
		
		public int getGroupID() {
			return this.groupID;
		}
		
		public static boolean isStaff(CommandSender sender) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				return Ranks.HELPER.hasRank(player, true);
			} else {
				return true;
			}
		}
		
		public static int getVotes(Player player) {
			Ranks rank = Disguise.getRealRank(player);
			return rank.ordinal() + 1 >= 5 ? 5 : rank.ordinal() + 1;
		}
	}
	
	private static Map<String, Ranks> ranks = null;
	private static boolean launchFireworks = true;
	
	public AccountHandler() {
		ranks = new HashMap<String, Ranks>();
		new CommandBase("getRank", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
						} else {
							Ranks rank = Ranks.valueOf(DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "rank"));
							MessageHandler.sendMessage(sender, arguments[0] + " has the rank " + rank.getPrefix());
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("setRank", 2, 3, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
						} else {
							try {
								Ranks rank = Ranks.valueOf(arguments[1].toUpperCase());
								Player target = ProPlugin.getPlayer(arguments[0]);
								if(target != null) {
									if(arguments.length == 3) {
										if(rank.hasRank(target)) {
											MessageHandler.sendMessage(sender, "&c" + target.getName() + " already has that rank");
											return;
										}
									}
									updateRank(target, rank);
								}
								setRank(uuid, rank);
								MessageHandler.sendMessage(sender, arguments[0] + " has been set to " + rank.getPrefix());
							} catch(IllegalArgumentException e) {
								MessageHandler.sendMessage(sender, "&cUnknown rank! Please use one of the following:");
								for(Ranks rank : Ranks.values()) {
									MessageHandler.sendMessage(sender, rank.toString() + " &a- " + rank.getPrefix());
								}
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("forceRank", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = UUID.fromString(arguments[0]);
				String name = arguments[1];
				Ranks rank = Ranks.valueOf(arguments[2]);
				if(DB.PLAYERS_ACCOUNTS.isUUIDSet(uuid)) {
					DB.PLAYERS_ACCOUNTS.updateString("name", name, "uuid", uuid.toString());
					DB.PLAYERS_ACCOUNTS.updateString("rank", rank.toString(), "uuid", uuid.toString());
				} else {
					DB.PLAYERS_ACCOUNTS.insert("'" + uuid.toString() + "', '" + name + "', 'null', '" + rank.toString() + "'");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("updateRank", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String[] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						try {
							UUID uuid = getUUID(arguments[0]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
							} else {
								Ranks rank = Ranks.valueOf(arguments[1].toUpperCase());
								Player player = Bukkit.getPlayer(uuid);
								if(player == null) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
								} else {
									updateRank(player, rank);
									MessageHandler.sendMessage(sender, "Set " + arguments[0] + " to " + rank.getPrefix());
								}
							}
						} catch(IllegalArgumentException e) {
							MessageHandler.sendMessage(sender, "&cUnknown rank! Known ranks:");
							for(Ranks rank : Ranks.values()) {
								MessageHandler.sendMessage(sender, rank.toString());
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static String getPrefix(CommandSender sender) {
		return getPrefix(sender, true);
	}
	
	public static String getPrefix(CommandSender sender, boolean realPrefix) {
		return getPrefix(sender, realPrefix, false);
	}
	
	public static String getPrefix(CommandSender sender, boolean realPrefix, boolean ignoreClan) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(!ignoreClan && (ProMcGames.getPlugin() == Plugins.SGHUB || ProMcGames.getPlugin() == Plugins.CLAN_BATTLES)) {
				String prefix = getRank(player).getPrefix() + player.getName();
				return ClanHandler.getClanPrefix(player) + " " + prefix;
			} else {
				return realPrefix ? Disguise.getRealRank(player).getPrefix() + Disguise.getName(player) : getRank(player).getPrefix() + player.getName();
			}
		} else {
			return Ranks.OWNER.getPrefix() + sender.getName();
		}
	}
	
	public static String getPrefix(String name) {
		return getPrefix(name, false);
	}
	
	public static String getPrefix(String name, boolean realPrefix) {
		Player player = ProPlugin.getPlayer(name);
		if(player == null) {
			return null;
		} else if(ProMcGames.getPlugin() == Plugins.SGHUB || ProMcGames.getPlugin() == Plugins.CLAN_BATTLES) {
			Ranks rank = getRank(name);
			String prefix = rank.getPrefix() + name;
			return ClanHandler.getClanPrefix(name) + " " + prefix;
		} else {
			return getPrefix(player, realPrefix);
		}
	}
	
	public static String getPrefix(UUID uuid) {
		return getPrefix(uuid, false);
	}
	
	public static String getPrefix(UUID uuid, boolean realPrefix) {
		Player player = Bukkit.getPlayer(uuid);
		if(player == null) {
			Ranks rank = getRank(uuid);
			return rank.getPrefix() + getName(uuid);
		} else if(ProMcGames.getPlugin() == Plugins.SGHUB || ProMcGames.getPlugin() == Plugins.CLAN_BATTLES) {
			Ranks rank = getRank(uuid);
			String name = getName(uuid);
			String prefix = rank.getPrefix() + name;
			return ClanHandler.getClanPrefix(player) + " " + prefix;
		} else {
			return getPrefix(player, realPrefix);
		}
	}
	
	public static void updateRank(Player player, Ranks rank) {
		if(rank == Ranks.PLAYER) {
			ranks.remove(player.getName());
		} else {
			ranks.put(player.getName(), rank);
		}
		Bukkit.getPluginManager().callEvent(new PlayerRankChangeEvent(player, rank));
	}
	
	public static void setRank(Player player, Ranks rank) {
		setRank(player, rank, false);
	}
	
	public static void setRank(Player player, Ranks rank, boolean updateDatabase) {
		updateRank(player, rank);
		if(updateDatabase) {
			setRank(Disguise.getUUID(player), rank);
		}
	}
	
	public static void setRank(final UUID uuid, final Ranks rank) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				DB.PLAYERS_ACCOUNTS.updateString("rank", rank.toString(), "uuid", uuid.toString());
			}
		});
	}
	
	public static Ranks getRank(CommandSender sender) {
		return sender instanceof Player ? ranks.containsKey(sender.getName()) ? ranks.get(sender.getName()) : Ranks.PLAYER : Ranks.OWNER;
	}
	
	public static Ranks getRank(String name) {
		return ranks.containsKey(name) ? ranks.get(name) : Ranks.PLAYER;
	}
	
	public static Ranks getRank(UUID uuid) {
		try {
			return Ranks.valueOf(DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "rank"));
		} catch(NullPointerException e) {
			return Ranks.PLAYER;
		}
	}
	
	public static String getAddress(Player player) {
		return player.getAddress().getAddress().getHostAddress();
	}
	
	public static String getAddress(UUID uuid) {
		return DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "address");
	}
	
	public static UUID getUUID(String name) {
		try {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && Disguise.isDisguised(player)) {
				return Disguise.getUUID(player);
			}
			return UUID.fromString(DB.PLAYERS_ACCOUNTS.getString("name", name, "uuid"));
		} catch(NullPointerException e) {
			return null;
		}
	}
	
	public static String getName(UUID uuid) {
		try {
			return DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "name");
		} catch(NullPointerException e) {
			return null;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPrePlayerLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		final String address = event.getAddress().getHostAddress();
		if(DB.PLAYERS_ACCOUNTS.isUUIDSet(Disguise.getUUID(player))) {
			setRank(player, Ranks.valueOf(DB.PLAYERS_ACCOUNTS.getString("uuid", Disguise.getUUID(player).toString(), "rank")));
			if(ProMcGames.getPlugin() == Plugins.HUB || ProMcGames.getPlugin() == Plugins.TESTING) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						DB.PLAYERS_ACCOUNTS.updateString("name", Disguise.getName(player), "uuid", Disguise.getUUID(player).toString());
						DB.PLAYERS_ACCOUNTS.updateString("address", address, "uuid", Disguise.getUUID(player).toString());
					}
				});
			}
		} else if(ProMcGames.getPlugin() == Plugins.HUB) {
			setRank(player, Ranks.PLAYER);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					String uuid = Disguise.getUUID(player).toString();
					String name = Disguise.getName(player);
					String rank = Ranks.PLAYER.toString();
					DB.PLAYERS_ACCOUNTS.insert("'" + uuid + "', '" + name + "', '" + address + "', '" + rank + "'");
					MessageHandler.alert("&eWelcome " + player.getName() + " to ProMcGames! &7(&c#" + DB.PLAYERS_ACCOUNTS.getSize() + "&7)");
					if(launchFireworks) {
						launchFireworks = false;
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								launchFireworks = true;
							}
						}, 20 * 3);
						World world = player.getWorld();
						EffectUtil.launchFirework(new Location(world, -105, 128, -173));
						EffectUtil.launchFirework(new Location(world, -101, 128, -173));
						EffectUtil.launchFirework(new Location(world, -91, 128, -163));
						EffectUtil.launchFirework(new Location(world, -115, 128, -163));
						EffectUtil.launchFirework(new Location(world, -91, 128, -159));
						EffectUtil.launchFirework(new Location(world, -115, 128, -159));
						EffectUtil.launchFirework(new Location(world, -105, 128, -149));
						EffectUtil.launchFirework(new Location(world, -101, 128, -149));
					}
					Bukkit.getPluginManager().callEvent(new NewPlayerJoiningEvent(player));
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPostPlayerLogin(PlayerLoginEvent event) {
		if(event.getResult() == Result.KICK_FULL || event.getResult() == Result.KICK_WHITELIST || event.getResult() == Result.KICK_OTHER) {
			ranks.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		final String name = event.getPlayer().getName();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				ranks.remove(name);
			}
		});
	}
}
