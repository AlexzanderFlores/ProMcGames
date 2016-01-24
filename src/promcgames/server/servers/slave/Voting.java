package promcgames.server.servers.slave;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.player.account.AccountHandler;
import promcgames.server.CommandDispatcher;
import promcgames.server.DB;
import promcgames.server.Tweeter;
import promcgames.server.servers.hub.HubSponsorGiveaway;
import promcgames.server.servers.hub.ScoreboardHandler;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;

import com.vexsoftware.votifier.model.VotifierEvent;

public class Voting implements Listener {
	public static int emeraldsForVoting = 350;
	private int dayToIgnore = -1;
	
	public Voting() {
		new Tweeter("XwNjW1ZJTQadiRfkJwvSjd5l5", "fo3P1xcrAWtaGItKPQnuChd3CVJJhu0n0B1DhWmeMe0Y90VUJa", "2395173859-N85rJm20JEN0uVTFzdVFHRQGtU94GRjmnXXusUt", "UB8b9VoCfpurco5y6pv5q7CW2uF4eoCeyMq0DsXwXMkbV");
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onVotifier(final VotifierEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String name = event.getVote().getUsername();
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid != null) {
					String date = TimeUtil.getTime().substring(0, 7);
					String [] keys = new String [] {"uuid", "date"};
					String [] values = new String [] {uuid.toString(), date};
					if(DB.PLAYERS_VOTES.isKeySet(keys, values)) {
						int votes = DB.PLAYERS_VOTES.getInt(keys, values, "votes") + 1;
						DB.PLAYERS_VOTES.updateInt("votes", votes, keys, values);
					} else {
						DB.PLAYERS_VOTES.insert("'" + uuid.toString() + "', '" + date + "', '1'");
					}
					String day = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
					if(DB.PLAYERS_DAILY_VOTES.isKeySet("day", day)) {
						int amount = DB.PLAYERS_DAILY_VOTES.getInt("day", day, "votes") + 1;
						DB.PLAYERS_DAILY_VOTES.updateInt("votes", amount, "day", day);
					} else {
						DB.PLAYERS_DAILY_VOTES.insert("'" + day + "', '1'");
					}
					String voteGoal = "&bOur Daily Vote Goal is now " + ScoreboardHandler.getDailyVotes(true) + " &bmore info: &c/voteGoal";
					if(ScoreboardHandler.voteGoalReached()) {
						int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
						if(dayToIgnore != today) {
							dayToIgnore = today;
							String time = DateFormat.getDateInstance().format(Calendar.getInstance().getTime()).split(",")[0];
							String text = time + " Vote Goal was reached! Everyone on the server in 5 minutes gets a Hub Sponsor\n\nVote: http://minecraftservers.org/server/141907";
							long id = Tweeter.tweet(text, "sponsor.jpg");
							if(id != -1) {
								HubSponsorGiveaway.startGiveaway(5, "&6&lEveryone who is on the server in &c&l5 &6&lminutes get a free &b&lHub Sponsor&6&l! &ehttps://twitter.com/ProMcGames/status/" + id);
							}
						}
					}
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addHubSponsors " + name + " 1 false");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addEmeralds " + name + " " + emeraldsForVoting);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addSGAutoSponsors " + name + " 3");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addFreeCheckPoints " + name + " 20");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addMapVotePasses " + name + " 5");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addExclusiveSponsorPasses " + name + " 5");
					CommandDispatcher.dispatchToHubs("updateCheckpoints " + name);
					CommandDispatcher.dispatchToHubs("say &d[Vote] &e" + name + " voted with &c/vote &efor many cool perks! " + voteGoal);
					CommandDispatcher.dispatch("versus", "say &d[Vote] &e" + name + " voted with &c/vote &efor many cool perks! " + voteGoal);
					CommandDispatcher.dispatch("kitpvp1", "say &d[Vote] &e" + name + " voted with &c/vote &efor &c25 &eregen passes! " + voteGoal);
					CommandDispatcher.dispatch("kitpvp1", "addKitPVPAutoRegenPass " + name + " 25");
					CommandDispatcher.dispatch("factions", "pokeball " + name + " 5");
					CommandDispatcher.dispatch("factions", "giveKey " + name + " VOTING");
					CommandDispatcher.dispatch("factions", "addCoins " + name + " 50");
					CommandDispatcher.dispatch("factions", "say &d[Vote] &e" + name + " voted with &c/vote &efor &a1 Vote Crate Key&e, &a5 Pokeballs &eand &a50 Coins&e! " + voteGoal);
					ProPlugin.dispatchCommandToGroup("sghub", "say &d[Vote] &e" + name + " voted with &c/vote &efor many cool perks! " + voteGoal);
					ProPlugin.dispatchCommandToGroup("uhchub", "say &d[Vote] &e" + name + " voted with &c/vote &efor many cool perks! " + voteGoal);
					ProPlugin.dispatchCommandToGroup("sg", "say &d[Vote] &e" + name + " voted with &c/vote &efor &cAuto Sponsors &eand &cVote Passes! " + voteGoal);
				}
			}
		});
	}
}
