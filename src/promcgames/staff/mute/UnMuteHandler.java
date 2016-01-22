package promcgames.staff.mute;

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

public class UnMuteHandler extends Punishment {
	public UnMuteHandler() {
		super("Unmuted");
		// Command syntax: /unmute <player name> <reason>
		new CommandBase("unmute", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Get the UUID of the target player
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
				} else {
					// See if the target player is muted
					if(DB.STAFF_MUTES.isUUIDSet(uuid)) {
						// Detect if the comment should be activated
						PunishmentExecuteReuslts result = executePunishment(sender, arguments, true);
						if(result.isValid()) {
							// Get the staff uuid for the unmute
							String staff = "CONSOLE";
							String staffUUID = staff;
							if(sender instanceof Player) {
								Player player = (Player) sender;
								staff = Disguise.getName(player);
								staffUUID = Disguise.getUUID(player).toString();
							}
							// Get the reason for the unmute
							String reason = "";
							for(int a = 1; a < arguments.length; ++a) {
								reason += arguments[a] + " ";
							}
							// Compile the message string
							String message = getReason(AccountHandler.getRank(sender), arguments, reason, result, true);
							String time = TimeUtil.getTime();
							// Log the unban
							DB.STAFF_UNMUTES.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + reason + "', '" + time.substring(0, 7) + "', '" + time + "'");
							// Unmute
							DB.STAFF_MUTES.deleteUUID(uuid);
							Player player = Bukkit.getPlayer(uuid);
							if(player != null) {
								MuteHandler.unMute(player, false);
							}
							// Perform any final execution instructions
							MessageHandler.alert(message);
						}
					} else {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not currently muted");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.MODERATOR);
	}
}
