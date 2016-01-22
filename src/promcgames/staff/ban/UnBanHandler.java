package promcgames.staff.ban;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.TimeUtil;
import promcgames.staff.Punishment;

public class UnBanHandler extends Punishment {
	public UnBanHandler() {
		super("UnBanned");
		// Command syntax: /unban <player name>
		new CommandBase("unban", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Get the UUID of the target player
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
				} else {
					// See the target player is banned
					String [] keys = new String [] {"uuid", "active"};
					String [] values = new String [] {uuid.toString(), "1"};
					if(DB.STAFF_BAN.isKeySet(keys, values)) {
						// Detect if the command should be activated
						PunishmentExecuteReuslts result = executePunishment(sender, arguments, true);
						if(result.isValid()) {
							// Get the staff uuid for the unban
							String staff = "CONSOLE";
							String staffUUID = staff;
							if(sender instanceof Player) {
								Player player = (Player) sender;
								staff = Disguise.getName(player);
								staffUUID = Disguise.getUUID(player).toString();
							}
							// Compile the message and proof strings
							String message = getReason(AccountHandler.getRank(sender), arguments, "", result, true);
							// Unban
							String time = TimeUtil.getTime();
							String date = time.substring(0, 7);
							DB.STAFF_BAN.updateString("who_unbanned", staffUUID, keys, values);
							DB.STAFF_BAN.updateString("unban_date", date, keys, values);
							DB.STAFF_BAN.updateString("unban_time", time, keys, values);
							DB.STAFF_BAN.updateInt("active", 0, keys, values);
							// Perform any final execution instructions
							MessageHandler.alert(message);
						}
					} else {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not banned");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.MODERATOR);
	}
}
