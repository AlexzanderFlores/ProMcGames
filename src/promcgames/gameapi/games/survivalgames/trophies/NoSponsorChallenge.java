package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.survivalgames.events.SponsorOpenEvent;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class NoSponsorChallenge extends Trophy {
	private static NoSponsorChallenge instance = null;
	private List<String> players = null;
	
	public NoSponsorChallenge() {
		super(Plugins.SURVIVAL_GAMES, 42);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static NoSponsorChallenge getInstance() {
		if(instance == null) {
			new NoSponsorChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&aNo Sponsor Challenge").addLore("&eWin a game without opening a sponsor crate").getItemStack();
	}
	
	@EventHandler
	public void onSponsorOpen(SponsorOpenEvent event) {
		if(players == null) {
			players = new ArrayList<String>();
		}
		if(!players.contains(Disguise.getName(event.getPlayer()))) {
			players.add(Disguise.getName(event.getPlayer()));
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(players == null || !players.contains(Disguise.getName(event.getPlayer()))) {
			setAchieved(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(players != null) {
			players.remove(Disguise.getName(event.getPlayer()));
		}
	}
}
