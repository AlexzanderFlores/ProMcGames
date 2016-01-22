package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.ChestOpenEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.survivalgames.TieringHandler;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Tier1OnlyChallenge extends Trophy {
	private static Tier1OnlyChallenge instance = null;
	private List<String> players = null;
	private List<String> opened = null;
	
	public Tier1OnlyChallenge() {
		super(Plugins.SURVIVAL_GAMES, 34);
		instance = this;
		if(canRegister()) {
			players = new ArrayList<String>();
			opened = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	public static Tier1OnlyChallenge getInstance() {
		if(instance == null) {
			new Tier1OnlyChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.CHEST).setName("&aTier 1 Only Challenge").addLore("&eWin a game while only opening tier 1 chests").getItemStack();
	}
	
	@EventHandler
	public void onChestOpen(ChestOpenEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			Player player = event.getPlayer();
			if(!SpectatorHandler.contains(player)) {
				String name = Disguise.getName(player);
				if(!TieringHandler.tierOne.contains(event.getChest().getLocation())) {
					players.add(name);
				}
				if(!opened.contains(name)) {
					opened.add(name);
				}
			}
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		Player player = event.getPlayer();
		String name = Disguise.getName(player);
		if((players == null || !players.contains(name)) && opened.contains(name)) {
			setAchieved(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		String name = Disguise.getName(player);
		players.remove(name);
		opened.remove(name);
	}
}
