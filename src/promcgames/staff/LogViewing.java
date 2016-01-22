package promcgames.staff;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.player.CommunityLevelHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;

public class LogViewing {
	public LogViewing() {
		new CommandBase("getIPFromName", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_ACCOUNTS.isKeySet("name", arguments[0])) {
							MessageHandler.sendMessage(sender, "IP for " + arguments[0] + " is " + DB.PLAYERS_ACCOUNTS.getString("name", arguments[0], "address"));
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		new CommandBase("getNameFromIP", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_ACCOUNTS.isKeySet("address", arguments[0])) {
							MessageHandler.sendMessage(sender, arguments[0] + " belongs to " + DB.PLAYERS_ACCOUNTS.getString("address", arguments[0], "name"));
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not a logged IP address");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		new CommandBase("getUUIDFromName", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_ACCOUNTS.isKeySet("name", arguments[0])) {
							MessageHandler.sendMessage(sender, "The UUID for " + arguments[0] + " is " + AccountHandler.getUUID(arguments[0]));
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("getNameFromUUID", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_ACCOUNTS.isKeySet("uuid", arguments[0])) {
							MessageHandler.sendMessage(sender, arguments[0] + " is the UUID of " + AccountHandler.getName(UUID.fromString(arguments[0])));
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not a logged UUID");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("getRankTransfers", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						Ranks rank = AccountHandler.getRank(uuid);
						if(DB.PLAYERS_RANK_TRANSFERS.isUUIDSet(uuid)) {
							UUID targetUUID = UUID.fromString(DB.PLAYERS_RANK_TRANSFERS.getString("uuid", uuid.toString(), "target"));
							Ranks targetRank = AccountHandler.getRank(targetUUID);
							String targetName = AccountHandler.getName(targetUUID);
							MessageHandler.sendMessage(player, rank.getPrefix() + arguments[0] + " &amoved their rank to " + targetRank.getPrefix() + targetName);
						} else {
							MessageHandler.sendMessage(player, "&c" + arguments[0] + " has never used a rank transfer");
						}
					}
				});
				return true;
			}
		}.enableDelay(1).setRequiredRank(Ranks.HELPER);
		new CommandBase("canApply", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
						} else {
							boolean canApply = true;
							if(DB.PLAYERS_ACCOUNTS.getString("uuid", uuid.toString(), "rank").equals("PLAYER")) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they are not " + Ranks.PRO.getPrefix());
								canApply = false;
							}
							if(DB.STAFF_KICKS.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they have kicks logged");
								MessageHandler.sendMessage(sender, "View these with &b/viewKicks " + arguments[0]);
								sendThreeMonthMessage(sender);
								canApply = false;
							}
							if(DB.STAFF_DELETED_KICKS.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they have deleted kicks logged");
								MessageHandler.sendMessage(sender, "View these with &b/viewDeletedKicks " + arguments[0]);
								sendThreeMonthMessage(sender);
								canApply = false;
							}
							if(DB.STAFF_MUTES.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they are muted");
								MessageHandler.sendMessage(sender, "View this with &b/isMuted " + arguments[0]);
								sendThreeMonthMessage(sender);
								canApply = false;
							}
							if(DB.STAFF_UNMUTES.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they have been muted in the past");
								sendThreeMonthMessage(sender);
								canApply = false;
							}
							if(DB.STAFF_BAN.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they are banned");
								MessageHandler.sendMessage(sender, "View this with &b/isBanned " + arguments[0]);
								canApply = false;
							}
							if(DB.PLAYERS_COMMUNITY_LEVELS.getInt("uuid", uuid.toString(), "level") < CommunityLevelHandler.requiredForStaff) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they do not have enough community points");
								MessageHandler.sendMessage(sender, "View this with &b/communityLevel " + arguments[0]);
								canApply = false;
							}
							String playtime = DB.PLAYERS_PLAY_TIME.getString("uuid", uuid.toString(), "play_time").split("-")[0];
							int [] times = new int[2];
							for(int a = 0; a < 2; ++a) {
								times[a] = Integer.valueOf(playtime.split("/")[a]);
							}
							if(times[0] == 0 && times[1] < 2) {
								MessageHandler.sendMessage(sender, "&4" + arguments[0] + " cannot apply because they do not have two days of lifetime playtime");
								canApply = false;
							}
							if(canApply) {
								MessageHandler.sendMessage(sender, "&e" + arguments[0] + " can apply!");
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		new CommandBase("getAccounts", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else {
							String address = AccountHandler.getAddress(uuid);
							MessageHandler.sendMessage(sender, "Accounts shared with " + arguments[0] + ":");
							for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", address)) {
								MessageHandler.sendMessage(sender, AccountHandler.getName(UUID.fromString(uuidString)));
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.HELPER);
	}
	
	private void sendThreeMonthMessage(CommandSender sender) {
		MessageHandler.sendMessage(sender, "&4NOTE: &bChat punishments 3+ months old do NOT discount them from applying. Check with the following commands:");
		MessageHandler.sendMessage(sender, "/viewKicks [player name]");
		MessageHandler.sendMessage(sender, "/viewMutes [player name]");
	}
}
