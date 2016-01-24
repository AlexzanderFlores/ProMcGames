package promcgames.server;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.factions.CoinHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.trophies.KitPVPTrophies;
import promcgames.player.trophies.SkyWarsTeamsTrophies;
import promcgames.player.trophies.SkyWarsTrophies;
import promcgames.player.trophies.SurvivalGamesTrophies;
import promcgames.player.trophies.UHCTrophies;
import promcgames.player.trophies.VersusTrophies;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.servers.hub.items.HubSponsor;
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.StringUtil;
import promcgames.server.util.TimeUtil;
import promcgames.staff.Punishment;
import promcgames.staff.StaffMode;

public class GlobalCommands {
	public GlobalCommands() {
		new CommandBase("restart247") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("restartIfEmpty") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					ProPlugin.dispatchCommandToAll("restartIfEmpty");
					MessageHandler.sendMessage(sender, "All empty servers are now restarting");
				} else if(ProMcGames.getPlugin() != Plugins.SLAVE) {
					if(Bukkit.getOnlinePlayers().isEmpty()) {
						ProPlugin.restartServer();
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("voteGoal", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "Each time our &bVote Goal &ais reached there will be a giveaway!");
				MessageHandler.sendMessage(player, "Every player on the server will get &e1 &bHub Sponsor");
				MessageHandler.sendMessage(player, "This giveaway will happen automatically from &e@ProMcGames&a!");
				MessageHandler.sendMessage(player, "Vote at &ehttp://minecraftservers.org/server/141907");
				MessageHandler.sendMessage(player, "");
				return true;
			}
		};
		
		new CommandBase("getVersion", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 0) {
					int version7 = 0;
					int version8 = 0;
					for(Player player : Bukkit.getOnlinePlayers()) {
						CraftPlayer craftPlayer = (CraftPlayer) player;
						if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
							++version8;
						} else {
							++version7;
						}
					}
					MessageHandler.sendMessage(sender, "1.7 Clients: &e" + version7);
					MessageHandler.sendMessage(sender, "1.8 Clients: &e" + version8);
					MessageHandler.sendMessage(sender, "Total players: &e" + Bukkit.getOnlinePlayers().size());
				} else if(arguments.length == 1) {
					String name = arguments[0];
					Player player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						CraftPlayer craftPlayer = (CraftPlayer) player;
						if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
							MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player) + " &eis on 1.8");
						} else {
							MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player) + " &eis on 1.7");
						}
					}
				}
				return true;
			}
		};
		
		new CommandBase("addExclusiveSponsorPasses") {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String name = arguments[0];
						UUID uuid = AccountHandler.getUUID(name);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
						} else {
							try {
								int amount = Integer.valueOf(arguments[1]);
								if(DB.PLAYERS_SG_EXCLUSIVE_SPONSOR_PASSES.isUUIDSet(uuid)) {
									amount += DB.PLAYERS_SG_EXCLUSIVE_SPONSOR_PASSES.getInt("uuid", uuid.toString(), "amount");
									DB.PLAYERS_SG_EXCLUSIVE_SPONSOR_PASSES.updateInt("amount", amount, "uuid", uuid.toString());
								} else {
									DB.PLAYERS_SG_EXCLUSIVE_SPONSOR_PASSES.insert("'" + uuid.toString() + "', '" + amount + "'");
								}
								MessageHandler.sendMessage(sender, "You now have &e" + amount + " &a");
							} catch(NumberFormatException e) {
								MessageHandler.sendMessage(sender, "&cThe amount must be a number");
								return;
							}
						}
					}
				});
				return true;
			}
		};
		
		new CommandBase("factionsCommand", 1, -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String command = "";
				for(String argument : arguments) {
					command += argument + " ";
				}
				CommandDispatcher.dispatch("factions", command.substring(0, command.length() - 1));
				MessageHandler.sendMessage(sender, "Added command to faction command queue");
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("toggleDebug") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(ProMcGames.getProPlugin().getDebug()) {
					ProMcGames.getProPlugin().setDebug(false);
					MessageHandler.sendMessage(sender, "Debug mode is now &cOFF");
				} else {
					ProMcGames.getProPlugin().setDebug(true);
					MessageHandler.sendMessage(sender, "Debug mode is now &eON");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("addCoins", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = arguments[0];
						int amount = 0;
						try {
							amount = Integer.valueOf(arguments[1]);
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&f/addCoins <name> <amount>");
							return;
						}
						Player player = ProPlugin.getPlayer(target);
						if(player == null) {
							UUID uuid = AccountHandler.getUUID(target);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
							} else {
								if(DB.PLAYERS_FACTIONS_COINS.isUUIDSet(uuid)) {
									MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " Coins");
									amount += DB.PLAYERS_FACTIONS_COINS.getInt("uuid", uuid.toString(), "coins");
									MessageHandler.sendMessage(sender, target + " now has &e" + amount + " Coins");
									DB.PLAYERS_FACTIONS_COINS.updateInt("coins", amount, "uuid", uuid.toString());
								} else {
									DB.PLAYERS_FACTIONS_COINS.insert("'" + uuid.toString() + "', '" + amount + "'");
									MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " Coins");
									MessageHandler.sendMessage(sender, target + " now has &e" + amount + " Coins");
								}
							}
						} else {
							CoinHandler.addCoins(player, amount);
							MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " Coins");
							MessageHandler.sendMessage(sender, target + " now has &e" + CoinHandler.getCoins(player) + " Coins");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("systemTime") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, TimeUtil.getTime());
				return true;
			}
		};
		
		new CommandBase("fix", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					for(Player online : Bukkit.getOnlinePlayers()) {
						if(online.canSee(player)) {
							online.hidePlayer(player);
							online.showPlayer(player);
						}
					}
					MessageHandler.sendMessage(player, "Fixed all players");
				} else if(arguments.length == 1) {
					String name = arguments[0];
					Player target = ProPlugin.getPlayer(name);
					if(target == null || !player.canSee(target)) {
						MessageHandler.sendMessage(player, "&c" + name + " is not online");
					} else {
						player.hidePlayer(target);
						player.showPlayer(target);
						MessageHandler.sendMessage(player, "Fixed " + AccountHandler.getPrefix(target, false));
					}
				}
				return true;
			}
		}.enableDelay(2);
		
		new CommandBase("addMapVotePasses", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = arguments[0];
						UUID uuid = AccountHandler.getUUID(target);
						try {
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
							} else if(DB.PLAYERS_MAP_VOTE_PASSES.isUUIDSet(uuid)) {
								amount = DB.PLAYERS_MAP_VOTE_PASSES.getInt("uuid", uuid.toString(), "amount") + amount;
								DB.PLAYERS_MAP_VOTE_PASSES.updateInt("amount", amount, "uuid", uuid.toString());
								MessageHandler.sendMessage(sender, target + " now has &e" + amount + " &amap vote passes");
							} else {
								DB.PLAYERS_MAP_VOTE_PASSES.insert("'" + uuid.toString() + "', '" + amount + "'");
								MessageHandler.sendMessage(sender, target + " now has &e" + amount + " &amap vote passes");
							}
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&f/addMapVotePasses <player name> <amount>");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("prizes") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendLine(sender, "&c&k");
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "Top 3 players at the end of each month get free prizes:");
				MessageHandler.sendMessage(sender, "&c1st: &e15 Hub Sponsors &afor you or a friend");
				MessageHandler.sendMessage(sender, "&c2nd: &e10 Hub Sponsors &afor you or a friend");
				MessageHandler.sendMessage(sender, "&c3rd: &e5 Hub Sponsors &afor you or a friend");
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendLine(sender, "&c&k");
				if(sender instanceof Player) {
					Player player = (Player) sender;
					EffectUtil.playSound(player, Sound.LEVEL_UP);
				}
				return true;
			}
		};
		
		new CommandBase("ptime", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					Player player = (Player) sender;
					long ticks = Long.valueOf(arguments[0]);
					if(ticks < 0 || ticks > 24000) {
						MessageHandler.sendMessage(player, "&cYour number must be between 0 and 24000");
					} else {
						player.setPlayerTime(ticks, false);
					}
				} catch(NumberFormatException e) {
					MessageHandler.sendMessage(sender, "&cYou must enter a valid number for the time");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PRO);
		
		new CommandBase("trophies", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					TrophiesItem.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.SURVIVAL_GAMES) {
					SurvivalGamesTrophies.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.VERSUS) {
					VersusTrophies.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
					KitPVPTrophies.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.UHC) {
					UHCTrophies.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.SKY_WARS) {
					SkyWarsTrophies.open(player);
				} else if(ProMcGames.getPlugin() == Plugins.SKY_WARS_TEAMS) {
					SkyWarsTeamsTrophies.open(player);
				} else {
					MessageHandler.sendMessage(player, "&cThere are no Trophies for this server yet");
				}
				return true;
			}
		};
		
		new CommandBase("addHubSponsors", 3, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						try {
							UUID uuid = AccountHandler.getUUID(arguments[0]);
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
							} else {
								HubSponsor.add(uuid, amount, Boolean.valueOf(arguments[2]));
								MessageHandler.sendMessage(sender, "Gave " + arguments[0] + " &e" + amount + " &aHub Sponsors");
							}
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&f/addHubSponsors <player name> <amount>");
							return;
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("addSGAutoSponsors", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						try {
							UUID uuid = AccountHandler.getUUID(arguments[0]);
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
							} else if(DB.PLAYERS_SG_AUTO_SPONSORS.isUUIDSet(uuid)){
								amount += DB.PLAYERS_SG_AUTO_SPONSORS.getInt("uuid", uuid.toString(), "amount");;
								DB.PLAYERS_SG_AUTO_SPONSORS.updateInt("amount", amount, "uuid", uuid.toString());
							} else {
								DB.PLAYERS_SG_AUTO_SPONSORS.insert("'" + uuid.toString() + "', '" + amount + "'");
							}
							if(uuid != null) {
								MessageHandler.sendMessage(sender, arguments[0] + " has gained " + amount + " SG Auto Sponsors");
								Player player = Bukkit.getPlayer(uuid);
								if(player != null) {
									MessageHandler.sendMessage(player, "+&c" + arguments[1] + " &aSG Auto Sponsors! You now have &c" + amount);
								}
							}
						} catch(NumberFormatException e) {
							return;
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("addKit", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else if(DB.PLAYERS_KITS.isKeySet(new String [] {"uuid", "kit"}, new String [] {uuid.toString(), arguments[1]})){
							MessageHandler.sendMessage(sender, "&cThat player already has that kit!");
						} else {
							DB.PLAYERS_KITS.insert("'" + uuid.toString() + "', '" + arguments[1] + "'");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("addKitPVPAutoRegenPass", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						try {
							UUID uuid = AccountHandler.getUUID(arguments[0]);
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
							} else if(DB.PLAYERS_KIT_PVP_AUTO_REGEN.isUUIDSet(uuid)){
								amount += DB.PLAYERS_KIT_PVP_AUTO_REGEN.getInt("uuid", uuid.toString(), "amount");;
								DB.PLAYERS_KIT_PVP_AUTO_REGEN.updateInt("amount", amount, "uuid", uuid.toString());
							} else {
								DB.PLAYERS_KIT_PVP_AUTO_REGEN.insert("'" + uuid.toString() + "', '" + amount + "'");
							}
							if(uuid != null) {
								MessageHandler.sendMessage(sender, arguments[0] + " has gained " + amount + " Kit PVP Auto Regen Passes");
								Player player = Bukkit.getPlayer(uuid);
								if(player != null) {
									MessageHandler.sendMessage(player, "+&c" + arguments[1] + " &aKit PVP Auto Regen Passes! You now have &c" + amount);
								}
							}
						} catch(NumberFormatException e) {
							return;
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("addFreeCheckPoints", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						try {
							UUID uuid = AccountHandler.getUUID(arguments[0]);
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
							} else if(DB.HUB_PARKOUR_FREE_CHECKPOINTS.isUUIDSet(uuid)){
								amount += DB.HUB_PARKOUR_FREE_CHECKPOINTS.getInt("uuid", uuid.toString(), "amount");;
								DB.HUB_PARKOUR_FREE_CHECKPOINTS.updateInt("amount", amount, "uuid", uuid.toString());
							} else {
								DB.HUB_PARKOUR_FREE_CHECKPOINTS.insert("'" + uuid.toString() + "', '" + amount + "'");
							}
							if(uuid != null) {
								MessageHandler.sendMessage(sender, arguments[0] + " has gained " + amount + " free Hub parkour checkpoints");
								Player player = Bukkit.getPlayer(uuid);
								if(player != null) {
									MessageHandler.sendMessage(player, "+&c" + arguments[1] + " &afree Hub parkour checkpoints! You now have &c" + amount);
								}
							}
						} catch(NumberFormatException e) {
							return;
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("tpPos", 3, 4, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					double x = Integer.valueOf(arguments[0]);
					double y = Integer.valueOf(arguments[1]);
					double z = Integer.valueOf(arguments[2]);
					Player player = (Player) sender;
					player.setAllowFlight(true);
					player.setFlying(true);
					if(arguments.length == 3) {
						player.teleport(new Location(player.getWorld(), x, y, z));
					} else {
						player.teleport(new Location(Bukkit.getWorld(arguments[3]), x, y, z));
					}
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		
		new CommandBase("apply") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "https://promcgames.com/apply");
				return true;
			}
		};
		
		new CommandBase("appeal") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, Punishment.appeal);
				return true;
			}
		};
		
		new CommandBase("buy") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "http://store.ProMcGames.com");
				return true;
			}
		};
		
		new CommandBase("coloredChat") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "To talk in colored chat do \"&xMessage\"");
				MessageHandler.sendMessage(sender, "Replace the 'x' with any of the following:");
				MessageHandler.sendMessage(sender, Ranks.PRO.getPrefix() + ": " + StringUtil.color("&00 &11 &22 &33 &44 &55 &66 &77 &88 &99 &aa &bb &cc &dd &ee &ff"));
				MessageHandler.sendMessage(sender, Ranks.PRO_PLUS.getPrefix() + ": The above as well as:");
				MessageHandler.sendMessage(sender, "&mStrikethrough" + ChatColor.RESET + " (& m) &a" + "&nUnderline" + ChatColor.RESET + " (& n) &a" + "&oItalic" + ChatColor.RESET + " (& o)");
				MessageHandler.sendMessage(sender, Ranks.ELITE.getPrefix() + ": The above as well as:");
				MessageHandler.sendMessage(sender, "&lBold " + ChatColor.RESET + " (& l)");
				return true;
			}
		};
		
		new CommandBase("forums") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "http://ProMcGames.com");
				return true;
			}
		};
		
		new CommandBase("getLocation", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Location location = player.getLocation();
				MessageHandler.sendMessage(sender, "World: " + location.getWorld().getName());
				MessageHandler.sendMessage(sender, "X: " + location.getX());
				MessageHandler.sendMessage(sender, "Y: " + location.getY());
				MessageHandler.sendMessage(sender, "Z: " + location.getZ());
				MessageHandler.sendMessage(sender, "Yaw: " + location.getYaw());
				MessageHandler.sendMessage(sender, "Pitch: " + location.getPitch());
				return true;
			}
		};
		
		new CommandBase("gmc", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.CREATIVE);
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		
		new CommandBase("gms", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setGameMode(GameMode.SURVIVAL);
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		
		new CommandBase("hub", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					ProPlugin.sendPlayerToServer(player, "hub");
				} else if(arguments.length == 1) {
					ProPlugin.sendPlayerToServer(player, "hub" + arguments[0]);
				}
				return true;
			}
		};
		
		new CommandBase("join", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments[0].equalsIgnoreCase("slave")) {
					ProPlugin.sendPlayerToServer(player, "lolno");
				} else {
					ProPlugin.sendPlayerToServer(player, arguments[0]);
				}
				return true;
			}
		};
		
		new CommandBase("list") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(Bukkit.getOnlinePlayers().isEmpty()) {
					MessageHandler.sendMessage(sender, "&cThere are no players online");
				} else {
					String players = "";
					int online = 0;
					for(Player player : ProPlugin.getPlayers()) {
						if(!StaffMode.contains(player)) {
							players += AccountHandler.getPrefix(player, false) + ", ";
							++online;
						}
					}
					MessageHandler.sendMessage(sender, "Players: (&e" + online + "&a) " + players.substring(0, players.length() - 2));
					if(ProMcGames.getMiniGame() != null && SpectatorHandler.isEnabled() && SpectatorHandler.getNumberOf() > 0) {
						String spectators = "";
						online = 0;
						for(Player player : SpectatorHandler.getPlayers()) {
							if(!Ranks.isStaff(player)) {
								spectators += AccountHandler.getPrefix(player, false) + ", ";
								++online;
							}
						}
						MessageHandler.sendMessage(sender, "Spectators (&e" + online + "&a): " + spectators.substring(0, spectators.length() - 2));
					}
				}
				return true;
			}
		};
		
		new CommandBase("ranks") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "https://promcgames.com/forum/view_topic/?tid=3");
				return true;
			}
		};
		
		new CommandBase("rules") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "https://promcgames.com/forum/view_topic/?tid=1");
				MessageHandler.sendMessage(sender, "https://promcgames.com/forum/view_topic/?tid=2");
				return true;
			}
		};
		
		new CommandBase("ts") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "ts.ProMcGames.com");
				return true;
			}
		};
		
		new CommandBase("twitter") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "https://twitter.com/ProMcGames &e(SERVER's TWITTER)");
				MessageHandler.sendMessage(sender, "https://twitter.com/ProMcUHC &e(UHC TWITTER)");
				MessageHandler.sendMessage(sender, "https://twitter.com/XxLeet_GamerxX &e(OWNER's TWITTER)");
				return true;
			}
		};
		
		new CommandBase("say", -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String message = "";
				for(String argument : arguments) {
					message += argument + " ";
				}
				message = ChatColor.GREEN + StringUtil.color(message.substring(0, message.length() - 1));
				Bukkit.getLogger().info(message);
				for(Player player : Bukkit.getOnlinePlayers()) {
					player.sendMessage(message);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		
		new CommandBase("help") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendLine(sender);
				MessageHandler.sendMessage(sender, "Find a staff: &b/staff");
				MessageHandler.sendMessage(sender, "Join a server: &b/join <server name>");
				MessageHandler.sendMessage(sender, "Find a friend: &b/seen <name>");
				MessageHandler.sendMessage(sender, "Join our forums: &b/forums");
				MessageHandler.sendMessage(sender, "Talk with the community: &b/ts");
				MessageHandler.sendMessage(sender, "View our rules: &b/rules");
				MessageHandler.sendMessage(sender, "Connect with us through twitter: &b/twitter");
				MessageHandler.sendMessage(sender, "Lagging? Find out why: &b/lag &aand &b/bungeeInfo");
				MessageHandler.sendMessage(sender, "View your ping: &b/ping");
				MessageHandler.sendLine(sender);
				return true;
			}
		};
	}
}
