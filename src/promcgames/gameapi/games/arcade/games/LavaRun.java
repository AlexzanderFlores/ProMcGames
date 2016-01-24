package promcgames.gameapi.games.arcade.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.PlayerExpFillEvent_;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.PlayerExpGainer;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;

@SuppressWarnings("deprecation")
public class LavaRun extends ArcadeGame {
	private List<Location> lava = null;
	private List<Location> toFill = null;
	private List<Material> blocks = null;
	
	public LavaRun() {
		super("Lava Run");
	}
	
	@Override
	public void enable() {
		super.enable();
		lava = new ArrayList<Location>();
		toFill = new ArrayList<Location>();
		for(int x = 2; x <= 102; ++x) {
			for(int z = -15; z <= 15; ++z) {
				toFill.add(new Location(getWorld(), x, 4, z));
			}
		}
		blocks = Arrays.asList(Material.STONE, Material.COBBLESTONE, Material.LAVA, Material.LAVA, Material.LAVA, Material.LAVA, Material.LAVA);
		ProMcGames.getMiniGame().setAllowPlayerInteraction(true);
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		if(lava != null) {
			lava.clear();
			lava = null;
		}
		if(toFill != null) {
			toFill.clear();
			toFill = null;
		}
		if(blocks != null) {
			blocks.clear();
			blocks = null;
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BossBar.remove();
		for(Player player : ProPlugin.getPlayers()) {
			PlayerExpGainer.start(player);
		}
	}
	
	@EventHandler
	public void onPlayerExpFill(PlayerExpFillEvent_ event) {
		event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 3));
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity() instanceof Snowball && event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if(player.getInventory().getItem(0).getAmount() == 0) {
				PlayerExpGainer.start(player);
			}
		}
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		if(toFill != null) {
			for(int a = 0; a < 15; ++a) {
				if(toFill.isEmpty()) {
					FiveTickTaskEvent.getHandlerList().unregister(this);
					blocks = null;
				} else {
					Material material = blocks.get(new Random().nextInt(blocks.size()));
					if(material == Material.LAVA) {
						lava.add(toFill.get(0));
					}
					toFill.get(0).getBlock().setType(material);
					toFill.remove(0);
				}
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		Random random = new Random();
		for(int a = 0; a < 10; ++a) {
			Location location = lava.get(random.nextInt(lava.size()));
			FallingBlock fallingBlock = (FallingBlock) location.getWorld().spawnFallingBlock(location, Material.LAVA, (byte) 0);
			fallingBlock.setDropItem(false);
			Vector vector = fallingBlock.getVelocity();
			vector.setY(new Random().nextDouble());
			fallingBlock.setVelocity(vector);
		}
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + player.getName(), 103 - player.getLocation().getBlockX());
		}
		MiniGame miniGame = ProMcGames.getMiniGame();
		if(miniGame.getGameState() == GameStates.STARTING && miniGame.getCounter() == 8) {
			MessageHandler.alertLine();
			MessageHandler.alert("");
			MessageHandler.alert("&c&lThis game is best played on 1.7 clients!");
			MessageHandler.alert("&c&l1.8 clients may not see the flying lava blocks");
			MessageHandler.alert("");
			MessageHandler.alertLine();
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.LAVA && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player)) {
				player.teleport(getWorld().getSpawnLocation());
				player.setFireTicks(0);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball) {
			Player player = (Player) event.getEntity();
			Snowball snowball = (Snowball) event.getDamager();
			if(snowball.getShooter() instanceof Player) {
				Player shooter = (Player) snowball.getShooter();
				MessageHandler.sendMessage(player, "You were hit by " + AccountHandler.getPrefix(shooter) + "&a's snowball");
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && !SpectatorHandler.contains(event.getPlayer()) && event.getTo().getWorld().getName().equals(getWorld().getName())) {
			if(event.getTo().getX() >= 103) {
				disable(event.getPlayer());
			} else {
				for(Entity entity : event.getPlayer().getNearbyEntities(0.10, 0.10, 0.10)) {
					if(entity instanceof FallingBlock) {
						event.getPlayer().teleport(getWorld().getSpawnLocation());
						MessageHandler.sendMessage(event.getPlayer(), "You have been hit by a flying lava block!");
					}
				}
			}
		} else if(event.getTo().getX() >= 2) {
			event.setTo(event.getFrom());
		}
	}
}
