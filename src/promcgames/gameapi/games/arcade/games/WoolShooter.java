package promcgames.gameapi.games.arcade.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class WoolShooter extends ArcadeGame {
	private Block block = null;
	private Map<String, Integer> scores = null;
	private boolean up = false;
	private boolean left = false;
	
	public WoolShooter() {
		super("Wool Shooter");
	}
	
	@Override
	public void enable() {
		super.enable();
		ProMcGames.getMiniGame().setAllowBowShooting(true);
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		block = null;
		scores.clear();
		scores = null;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		spawnWool();
		scores = new HashMap<String, Integer>();
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().addItem(new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).getItemStack());
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));
			scores.put(Disguise.getName(player), 0);
		}
		BossBar.display("&eFirst to 10 points wins!", BossBar.maxHealth);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		BlockIterator blockIterator = new BlockIterator(getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0, 4);
		Block blockHit = null;
		while(blockIterator.hasNext()) {
			blockHit = blockIterator.next();
			if(blockHit.getType() != Material.AIR) {
				break;
			}
		}
		if(blockHit != null && blockHit.getType() == Material.WOOL && blockHit.getData() == DyeColor.WHITE.getData() && event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if(!SpectatorHandler.contains(player)) {
				int score = scores.get(Disguise.getName(player)) + 1;
				scores.put(Disguise.getName(player), score);
				ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), score);
				ProMcGames.getSidebar().update();
				EffectUtil.playSound(player, Sound.LEVEL_UP);
				if(score >= 10) {
					disable(player);
				}
			}
		}
		event.getEntity().remove();
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && block != null) {
			int [] xy = getXY();
			if(getWorld().getBlockAt(xy[0], xy[1], 19).getType() != Material.AIR) {
				if(xy[0] == 15 && (xy[1] >= 7 && xy[1] <= 25)) {
					left = false;
				} else if(xy[0] == -15 && (xy[1] >= 7 && xy[1] <= 25)) {
					left = true;
				} else if(xy[1] == 26 && (xy[0] >= -14 && xy[0] <= 14)) {
					up = false;
				} else if(xy[1] == 6 && (xy[0] >= -14 && xy[0] <= 14)) {
					up = true;
				}
			}
			xy = getXY();
			block.setType(Material.AIR);
			block = getWorld().getBlockAt(xy[0], xy[1], 19);
			updateWool();
		}
	}
	
	private int [] getXY() {
		int y = block.getLocation().getBlockY();
		int x = block.getLocation().getBlockX();
		if(up && left) {
			++y;
			++x;
		} else if(up && !left) {
			++y;
			--x;
		} else if(!up && left) {
			--y;
			++x;
		} else if(!up && !left) {
			--y;
			--x;
		}
		return new int [] {x, y};
	}
	
	private void spawnWool() {
		if(block != null) {
			block.setType(Material.AIR);
		}
		block = getWorld().getBlockAt(0, 16, 19);
		up = new Random().nextBoolean();
		left = new Random().nextBoolean();
		updateWool();
	}
	
	private void updateWool() {
		block.setType(Material.WOOL);
		block.setData(DyeColor.WHITE.getData());
	}
}
