package promcgames.staff.kick;

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

public class UndoKick {
	public UndoKick() {
		new CommandBase("undoKick", 3, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				UUID uuid = AccountHandler.getUUID(arguments[0]);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
				} else {
					try {
						int id = Integer.valueOf(arguments[1]);
						String [] keys = new String [] {"uuid", "id"};
						String [] values = new String [] {uuid.toString(), String.valueOf(id)};
						if(DB.STAFF_KICKS.isKeySet(keys, values)) {
							String reason = "";
							for(int a = 2; a < arguments.length; ++a) {
								reason += arguments[a] + " ";
							}
							String proof = DB.STAFF_KICKS.getString("id", String.valueOf(id), "proof");
							DB.STAFF_KICKS.delete(keys, values);
							MessageHandler.sendMessage(sender, "You have deleted kick ID #" + id + " from " + arguments[0]);
							String staffUUID = "CONSOLE";
							if(sender instanceof Player) {
								Player player = (Player) sender;
								staffUUID = Disguise.getUUID(player).toString();
							}
							String time = TimeUtil.getTime();
							String date = time.substring(0, 7);
							DB.STAFF_DELETED_KICKS.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + reason + "', '" + proof + "', '" + date + "', '" + time + "'");
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never been kicked for ID #" + id);
						}
					} catch(NumberFormatException e) {
						return false;
					}
				}
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.MODERATOR);
	}
}
