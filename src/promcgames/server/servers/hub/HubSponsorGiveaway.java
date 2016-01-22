package promcgames.server.servers.hub;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import promcgames.ProMcGames;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AlertHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.Tweeter;
import promcgames.server.servers.hub.items.HubSponsor;
import promcgames.server.tasks.DelayedTask;

public class HubSponsorGiveaway {
	private static long id = -1;
	
	public HubSponsorGiveaway() {
		if(ProMcGames.getServerName().equals("HUB1")) {
			//ProMcGames:
			new Tweeter("XwNjW1ZJTQadiRfkJwvSjd5l5", "fo3P1xcrAWtaGItKPQnuChd3CVJJhu0n0B1DhWmeMe0Y90VUJa", "2395173859-N85rJm20JEN0uVTFzdVFHRQGtU94GRjmnXXusUt", "UB8b9VoCfpurco5y6pv5q7CW2uF4eoCeyMq0DsXwXMkbV");
		}
		new CommandBase("hubSponsorGiveaway", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(ProMcGames.getServerName().equals("HUB1")) {
					try {
						int minutes = Integer.valueOf(arguments[0]);
						String text = "Everyone who is on the server in " + minutes + " minute" + (minutes == 1 ? "" : "s") + " get a free Hub Sponsor!";
						id = Tweeter.tweet(text, "sponsor.jpg");
						if(id == -1) {
							MessageHandler.sendMessage(sender, "&cFailed to send tweet! Possible duplicate tweet");
						} else {
							startGiveaway(minutes, text);
						}
					} catch(NumberFormatException e) {
						return false;
					}
				} else {
					MessageHandler.sendMessage(sender, "&cYou can only run this command on HUB1 &e/hub 1");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static void startGiveaway(int minutes, String text) {
		AlertHandler.alert("&6&l" + text + (id == -1 ? "" : " &chttps://twitter.com/ProMcGames/status/" + id));
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				int counter = 0;
				for(String uuid : DB.PLAYERS_LOCATIONS.getAllStrings("uuid")) {
					HubSponsor.add(UUID.fromString(uuid), 1, false);
					++counter;
				}
				for(String uuid : DB.STAFF_ONLINE.getAllStrings("uuid")) {
					HubSponsor.add(UUID.fromString(uuid), 1, false);
					++counter;
				}
				AlertHandler.alert("&6&lGave &c&l" + counter + " &6&lplayers &c&l1 &6&lHub Sponsor! " + (id == -1 ? "" : " &chttps://twitter.com/ProMcGames/status/" + id));
				id = -1;
			}
		}, 60 * 20 * minutes);
	}
}
