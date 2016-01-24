package promcgames.gameapi.games.arcade;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameVotingEvent;
import promcgames.gameapi.VotingHandler;
import promcgames.gameapi.VotingHandler.VoteData;
import promcgames.player.MessageHandler;
import promcgames.server.ProMcGames;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameVoting(GameVotingEvent event) {
		ProMcGames.getMiniGame().setCounter(20);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		VoteData voteData = VotingHandler.getWinner();
		MessageHandler.alert(voteData.getName() + " has won with " + voteData.getVotes() + " votes");
		Arcade.executeGame(ChatColor.stripColor(voteData.getName()));
		ProMcGames.getMiniGame().setCounter(10);
	}
}
