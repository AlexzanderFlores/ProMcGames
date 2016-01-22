package promcgames.staff.ban;

import java.util.UUID;

import org.bukkit.Bukkit;
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
		// Command syntax: /unban <player name> <reason>
		new CommandBase("unban", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Get the UUID of the target player
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
				} else {
					// See the target player is banned
					if(DB.STAFF_BANS.isUUIDSet(uuid)) {
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
							// Get the reason for the unban
							String reason = "";
							for(int a = 1; a < arguments.length; ++a) {
								reason += arguments[a] + " ";
							}
							// Compile the message and proof strings
							String message = getReason(AccountHandler.getRank(sender), arguments, reason, result, true);
							String time = TimeUtil.getTime();
							// Log the unban
							DB.STAFF_UNBANS.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + reason.substring(0, reason.length() - 1) + "', '" + time.substring(0, 7) + "', '" + time + "'");
							// Unban
							DB.STAFF_BANS.deleteUUID(uuid);
							Player player = Bukkit.getPlayer(uuid);
							if(player == null) {
								DB.STAFF_BANS.delete("address", AccountHandler.getAddress(result.getUUID()));
							} else {
								DB.STAFF_BANS.delete("address", player.getAddress().getAddress().getHostAddress());
							}
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
