package promcgames.gameapi.games.arcade;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.PlayerExpGainer;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;
import promcgames.server.util.FileHandler;

public class ArcadeGame implements Listener {
	private String name = null;
	private World world = null;
	
	public ArcadeGame(String name) {
		this.name = name;
		Arcade.addGame(this);
	}
	
	public void enable() {
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), 0);
		}
		ProMcGames.getSidebar().update();
		String worldName = name.replace(" ", "_");
		File target = new File(Bukkit.getWorldContainer().getPath() + "/" + worldName);
		if(target.exists()) {
			FileHandler.delete(target);
		}
		FileHandler.copyFolder(new File(Bukkit.getWorldContainer().getPath() + "/../resources/maps/arcade/" + worldName), target);
		world = Bukkit.createWorld(new WorldCreator(worldName));
		world.setSpawnLocation(0, 5, 0);
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.teleport(world.getSpawnLocation());
		}
		EventUtil.register(this);
		ProMcGames.getSidebar().toggleEnabled();
		ProMcGames.setSidebar(new SidebarScoreboardUtil(ChatColor.AQUA + getName()));
		BossBar.remove();
	}
	
	public void disable(Player winner) {
		if(winner != null) {
			String text = AccountHandler.getPrefix(winner) +  " &ehas won " + ProMcGames.getMiniGame().getDisplayName() + "!";
			MessageHandler.alert(text);
			BossBar.display(text);
			for(Player online : Bukkit.getOnlinePlayers()) {
				new TitleDisplayer(online, AccountHandler.getRank(winner).getColor() + "&l" + winner.getName() + " has won", "&eThe " + ProMcGames.getMiniGame().getDisplayName());
			}
			Bukkit.getPluginManager().callEvent(new GameWinEvent(winner, false));
		}
		for(Player player : ProPlugin.getPlayers()) {
			if(winner != null && Disguise.getName(player).equals(winner.getName())) {
				continue;
			}
			Bukkit.getPluginManager().callEvent(new GameLossEvent(player));
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getPassenger() != null) {
				player.eject();
			}
			if(player.getVehicle() != null) {
				player.leaveVehicle();
			}
			SpectatorHandler.remove(player);
			player.teleport(new Location(ProMcGames.getMiniGame().getLobby(), -17.5, 27, -29.5, -135.30f, -7.79f));
			ProPlugin.resetPlayer(player);
			PlayerExpGainer.stop(player);
		}
		ProMcGames.getMiniGame().resetFlags();
		HandlerList.unregisterAll(this);
		Arcade.removeGame(this);
		Bukkit.unloadWorld(world, false);
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world.getName()));
		ProMcGames.getSidebar().toggleEnabled();
		name = null;
		world = null;
		ProMcGames.setSidebar(new SidebarScoreboardUtil(ChatColor.AQUA + ProMcGames.getMiniGame().getDisplayName()) {
			@Override
			public void update() {
				setText("Players", ProPlugin.getPlayers().size());
				setText("Spectators", SpectatorHandler.getNumberOf());
				setText(new String [] {" ", "Server:", ChatColor.RED + ProMcGames.getServerName()});
				super.update();
			}
		});
		for(Player player : ProPlugin.getPlayers()) {
			if(Ranks.ELITE.hasRank(player)) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		ProMcGames.setSidebar(new SidebarScoreboardUtil(ChatColor.AQUA + getName()));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTING) {
			event.getPlayer().teleport(new Location(getWorld(), 0, 5, 0));
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		ProMcGames.getSidebar().removeText(AccountHandler.getRank(event.getPlayer()).getColor() + event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		ProMcGames.getSidebar().removeText(AccountHandler.getRank(event.getPlayer()).getColor() + event.getPlayer().getName());
	}
}
