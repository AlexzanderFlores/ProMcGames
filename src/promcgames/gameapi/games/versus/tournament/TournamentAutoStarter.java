package promcgames.gameapi.games.versus.tournament;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.server.DB;
import promcgames.server.PerformanceHandler;
import promcgames.server.util.EventUtil;

public class TournamentAutoStarter implements Listener {
	
	public TournamentAutoStarter() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		if(getHourOfDay() >= 15) {
			String date = getDate();
			if(!DB.NETWORK_VERSUS_TOURNAMENT_WINS.isKeySet("date", date)) {
				if(PerformanceHandler.getMemory() >= 60) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restartserver 3");
				} else {
					DB.NETWORK_VERSUS_TOURNAMENT_WINS.insert("'NULL', '" + date + "'");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "queue start");
				}
			}
		}
	}
	
	private static int getHourOfDay() {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}
	
	private static String getDate() {
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return month + "/" + day + "/" + year;
	}
	
}