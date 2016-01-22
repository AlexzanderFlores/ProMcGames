package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import promcgames.customevents.PurchaseEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.Scroller;
import promcgames.server.CommandBase;
import promcgames.server.DB;

public class RecentPurchaseDisplayer {
	private List<String> text = null;
	private static List<String> mostRecent = null;
	
	public RecentPurchaseDisplayer() {
		mostRecent = new ArrayList<String>();
		startScroll();
		new CommandBase("purchase", false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				startScroll();
				Bukkit.getPluginManager().callEvent(new PurchaseEvent());
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	public static List<String> getRecentCustomers() {
		return mostRecent;
	}
	
	public void startScroll() {
		if(text != null) {
			text.clear();
		}
		text = DB.NETWORK_RECENT_PURCHASES.getOrdered("id", "text", 3, true);
		mostRecent.clear();
		String scrollText = "";
		for(String scroll : text) {
			mostRecent.add(scroll.split(":")[0]);
			scrollText += scroll + " - ";
		}
		Scroller.setText("Recent purchases: " + scrollText + "Thank you for the support!");
	}
}
