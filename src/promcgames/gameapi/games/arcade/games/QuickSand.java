package promcgames.gameapi.games.arcade.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.PlayerExpFillEvent_;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.PlayerExpGainer;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.tasks.DelayedTask;

public class QuickSand extends ArcadeGame {
	private List<Block> blocks = null;
	private boolean superTNT = false;
	
	public QuickSand() {
		super("Quick Sand");
	}
	
	@Override
	public void enable() {
		super.enable();
		blocks = new ArrayList<Block>();
		for(int x = -30; x <= 30; ++x) {
			for(int z = -30; z <= 30; ++z) {
				Block block = getWorld().getBlockAt(x, 4, z);
				if(block.getType() != Material.AIR) {
					blocks.add(block);
				}
			}
		}
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		blocks.clear();
		blocks = null;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BossBar.remove();
		for(Player player : ProPlugin.getPlayers()) {
			PlayerExpGainer.start(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostGameStart(PostGameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), 1);
		}
		ProMcGames.getSidebar().setName("&bQuick Sand");
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			final Block block = blocks.get(new Random().nextInt(blocks.size()));
			if(block.getType() == Material.SANDSTONE) {
				block.setType(Material.SAND);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						block.setType(Material.AIR);
						if(blocks != null) {
							blocks.remove(block);
						}
					}
				}, 20 * 2);
			}
		}
	}
	
	@EventHandler
	public void onPlayerExpFill(PlayerExpFillEvent_ event) {
		event.getPlayer().getInventory().addItem(new ItemStack(Material.TNT));
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getPlayer().getItemInHand().getType() == Material.TNT) {
			if(!superTNT) {
				event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
			}
			TNTPrimed tnt = (TNTPrimed) getWorld().spawnEntity(event.getPlayer().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
			tnt.setFuseTicks(tnt.getFuseTicks() / 2);
			if(superTNT) {
				tnt.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.0));
			} else {
				tnt.setVelocity(event.getPlayer().getLocation().getDirection());
				PlayerExpGainer.start(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for(Block block : event.blockList()) {
			if(block.getLocation().getBlockY() == 4) {
				block.setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		event.getPlayer().getInventory().remove(Material.TNT);
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
			} else if(!superTNT && left <= 3) {
				superTNT = true;
				MessageHandler.alert("Super TNT is now enabled!");
				for(Player playing : ProPlugin.getPlayers()) {
					for(int a = 0; a < 9; ++a) {
						playing.getInventory().setItem(a, new ItemStack(Material.TNT, 64));
					}
				}
			}
			event.setCancelled(true);
		}
	}
}
