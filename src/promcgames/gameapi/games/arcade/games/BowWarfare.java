package promcgames.gameapi.games.arcade.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

public class BowWarfare extends ArcadeGame {
	private Map<String, Integer> ticksAlive = null;
	private List<Location> spawns = null;
	
	public BowWarfare() {
		super("Bow Warfare");
	}
	
	@Override
	public void enable() {
		super.enable();
		ProMcGames.getMiniGame().setAllowBowShooting(true);
		ticksAlive = new HashMap<String, Integer>();
		spawns = new ArrayList<Location>();
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		ticksAlive.clear();
		ticksAlive = null;
		spawns.clear();
		spawns = null;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostGameStart(PostGameStartEvent event) {
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + getName().replace(" ", "_") + "/spawns.yml");
		for(String key : config.getConfig().getKeys(false)) {
			double x = config.getConfig().getDouble(key + ".x");
			double y = config.getConfig().getDouble(key + ".y");
			double z = config.getConfig().getDouble(key + ".z");
			float yaw = (float) config.getConfig().getDouble(key + ".yaw");
			spawns.add(new Location(getWorld(), x, y, z, yaw, 0.0f));
		}
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + player.getName(), 1);
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + player.getName(), 0);
		}
		ProMcGames.getSidebar().setName("&bBow Warfare");
		List<Location> used = new ArrayList<Location>();
		Random random = new Random();
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().addItem(new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).addEnchantment(Enchantment.DURABILITY, 100).getItemStack());
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));
			Location spawn = null;
			do {
				spawn = spawns.get(random.nextInt(spawns.size()));
			} while(used.contains(spawn));
			used.add(spawn);
			player.teleport(spawn);
			player.setScoreboard(ProMcGames.getScoreboard());
		}
		used.clear();
		used = null;
		BossBar.remove();
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			int ticks = 0;
			if(ticksAlive.containsKey(player.getName())) {
				ticks = ticksAlive.get(player.getName());
			}
			ticksAlive.put(player.getName(), ++ticks);
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(5));
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Player player = (Player) event.getEntity();
			Arrow arrow = (Arrow) event.getDamager();
			if(arrow.getShooter() instanceof Player) {
				Player shooter = (Player) arrow.getShooter();
				if(ticksAlive.get(player.getName()) <= 30) {
					MessageHandler.sendMessage(shooter, "&cThat player is under spawn protection! (&e1.5s&c)");
				} else {
					ticksAlive.put(player.getName(), 0);
					player.teleport(spawns.get(new Random().nextInt(spawns.size())));
					EffectUtil.playSound(shooter, Sound.LEVEL_UP);
					shooter.setLevel(shooter.getLevel() + 1);
					ProMcGames.getSidebar().setText(AccountHandler.getRank(shooter).getColor() + shooter.getName(), shooter.getLevel());
					MessageHandler.sendMessage(player, "You were killed by " + AccountHandler.getPrefix(shooter));
					MessageHandler.sendMessage(shooter, "You killed " + AccountHandler.getPrefix(player));
					if(shooter.getLevel() >= 30) {
						ProMcGames.getMiniGame().setAllowBowShooting(false);
						for(Player playing : ProPlugin.getPlayers()) {
							int points = playing.getLevel();
							MessageHandler.sendMessage(playing, "You got &e" + points + " &akill" + (points == 1 ? "" : "s"));
						}
						disable(shooter);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		event.getEntity().remove();
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		ticksAlive.remove(event.getPlayer().getName());
	}
}
