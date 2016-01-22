package promcgames.gameapi.games.factions.spawn.crates;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;

public class CrateHandler {
	public CrateHandler() {
		new CommandBase("giveKey", 2) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String name = arguments[0];
						UUID uuid = AccountHandler.getUUID(name);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else {
							String type = arguments[1];
							if(type.equalsIgnoreCase("voting")) {
								DB db = DB.PLAYERS_FACTIONS_VOTING_CRATES;
								if(db.isUUIDSet(uuid)) {
									db.updateInt("amount", db.getInt("uuid", uuid.toString(), "amount") + 1, "uuid", uuid.toString());
								} else {
									db.insert("'" + uuid.toString() + "', '1'");
								}
							} else if(type.equalsIgnoreCase("premium")) {
								DB db = DB.PLAYERS_FACTIONS_PREMIUM_CRATES;
								if(db.isUUIDSet(uuid)) {
									db.updateInt("amount", db.getInt("uuid", uuid.toString(), "amount") + 1, "uuid", uuid.toString());
								} else {
									db.insert("'" + uuid.toString() + "', '1'");
								}
							} else if(type.equalsIgnoreCase("elite")) {
								DB db = DB.PLAYERS_FACTIONS_ELITE_CRATES;
								if(db.isUUIDSet(uuid)) {
									db.updateInt("amount", db.getInt("uuid", uuid.toString(), "amount") + 1, "uuid", uuid.toString());
								} else {
									db.insert("'" + uuid.toString() + "', '1'");
								}
							} else if(type.equalsIgnoreCase("spawner")) {
								DB db = DB.PLAYERS_FACTIONS_SPAWNER_CRATES;
								if(db.isUUIDSet(uuid)) {
									db.updateInt("amount", db.getInt("uuid", uuid.toString(), "amount") + 1, "uuid", uuid.toString());
								} else {
									db.insert("'" + uuid.toString() + "', '1'");
								}
							} else {
								MessageHandler.sendMessage(sender, "&cUnknown crate type");
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new SpawnerCrate();
		new VotingCrate();
		new PremiumCrate();
		new EliteCrate();
	}
}
