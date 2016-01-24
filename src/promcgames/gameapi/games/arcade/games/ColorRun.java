package promcgames.gameapi.games.arcade.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.PlayerExpFillEvent_;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.PlayerExpGainer;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.MessageHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;

@SuppressWarnings("deprecation")
public class ColorRun extends ArcadeGame {
	private class Region {
		public int x1 = 0;
		public int z1 = 0;
		public int x2 = 0;
		public int z2 = 0;
		
		public Region(int x1, int z1, int x2, int z2) {
			this.x1 = x1;
			this.z1 = z1;
			this.x2 = x2;
			this.z2 = z2;
		}
		
		public void setColor() {
			DyeColor color = null;
			do {
				color = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
			} while(color.getData() == new Location(getWorld(), x1, 4, z1).getBlock().getData());
			placed.add(color);
			for(int x = x1; x <= x2; ++x) {
				for(int z = z1; z <= z2; ++z) {
					Block block = new Location(getWorld(), x, 4, z).getBlock();
					block.setType(Material.WOOL);
					block.setData(color.getData());
				}
			}
		}
		
		public void remove() {
			if(dyeColor.getData() != new Location(getWorld(), x1, 4, z1).getBlock().getData()) {
				for(int x = x1; x <= x2; ++x) {
					for(int z = z1; z <= z2; ++z) {
						Block block = new Location(getWorld(), x, 4, z).getBlock();
						block.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	private List<Region> regions = null;
	private List<DyeColor> placed = null;
	private int delay = 8;
	private int counter = delay;
	private boolean selectingWool = true;
	private DyeColor dyeColor = null;
	
	public ColorRun() {
		super("Color Run");
	}
	
	@Override
	public void enable() {
		super.enable();
		regions = new ArrayList<Region>();
		placed = new ArrayList<DyeColor>();
		for(int x = -22; x <= 22; x += 5) {
			for(int z = -22; z <= 22; z += 5) {
				regions.add(new Region(x, z, x + 4, z + 4));
			}
		}
		setWool();
		ProMcGames.getMiniGame().setPlayersHaveOneLife(true);
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		regions.clear();
		regions = null;
		placed.clear();
		placed = null;
	}
	
	private void setWool() {
		placed.clear();
		for(Region region : regions) {
			region.setColor();
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BossBar.setCounter(-1);
		for(Player player : ProPlugin.getPlayers()) {
			PlayerExpGainer.start(player);
		}
	}
	
	@EventHandler
	public void onPlayerExpFill(PlayerExpFillEvent_ event) {
		MessageHandler.sendMessage(event.getPlayer(), "Your double jump is ready to use!");
		event.getPlayer().setAllowFlight(true);
	}
	
	@EventHandler
	public void onPlayerToggleFlight(final PlayerToggleFlightEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer())) {
			event.setCancelled(true);
			event.getPlayer().setAllowFlight(false);
			event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(new Vector(0, 1.5, 0)));
			PlayerExpGainer.start(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && selectingWool) {
			do {
				dyeColor = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
			} while(!placed.contains(dyeColor));
			for(Player player : ProPlugin.getPlayers()) {
				player.getInventory().setItem(0, new ItemStack(Material.WOOL, 1, dyeColor.getData()));
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			SpectatorHandler.add(player);
			int left = ProPlugin.getPlayers().size();
			if(left <= 0) {
				disable(null);
			} else if(left == 1) {
				Player winner = ProPlugin.getPlayers().get(0);
				disable(winner);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			if(selectingWool) {
				if(counter <= 0) {
					selectingWool = false;
					if(delay > 2) {
						--delay;
					}
					counter = delay;
				} else {
					ProMcGames.getSidebar().setName("Selecting in " + "0:0" + counter);
					BossBar.display("&eWool selecting in &c" + counter + (counter == 1 ? " &esecond" : " &eseconds"));
					--counter;
				}
			} else {
				if(counter == 0) {
					for(Region region : regions) {
						region.remove();
					}
				} else if(counter > 0) {
					ProMcGames.getSidebar().setName("Falling in " + "0:0" + counter);
					BossBar.display("&eFloor falling in &c" + counter + (counter == 1 ? " &esecond" : " &eseconds"));
				} else if(counter == -3) {
					selectingWool = true;
					setWool();
					counter = delay;
				}
				--counter;
			}
		}
	}
}
