package promcgames.gameapi.games.arcade.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.FifteenTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

public class ChickenHunt extends ArcadeGame {
	private Map<String, Integer> kills = null;
	private int chickens = 0;
	private int size = 0;
	
	public ChickenHunt() {
		super("Chicken Hunt");
	}
	
	@Override
	public void enable() {
		super.enable();
		ProMcGames.getMiniGame().setAllowBowShooting(true);
		kills = new HashMap<String, Integer>();
		for(Player player : ProPlugin.getPlayers()) {
			kills.put(Disguise.getName(player), 0);
		}
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		kills.clear();
		kills = null;
		chickens = 0;
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BossBar.display("&eFirst to 20 kills wins!", BossBar.maxHealth);
		ProMcGames.getSidebar().setName(ChatColor.YELLOW + "First to 20 kills wins!");
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().addItem(new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).getItemStack());
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));
		}
		size = ProPlugin.getPlayers().size() * 5;
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles("largesmoke", -13.5f, 10.5f, 3.5f, 0, 0, 0, 0, 3);
		for(Player player : Bukkit.getOnlinePlayers()) {
			CraftPlayer craftPlayer = (CraftPlayer) player;
			craftPlayer.getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		event.getEntity().remove();
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && chickens < size) {
			Location spawn = getWorld().getSpawnLocation().add(0, 20, 0);
			int range = 15;
			Random random = new Random();
			spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
			spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
			getWorld().spawnEntity(spawn, EntityType.CHICKEN);
			++chickens;
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Chicken && event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if(arrow.getShooter() instanceof Player) {
				Player player = (Player) arrow.getShooter();
				EffectUtil.displayParticles(Material.REDSTONE_BLOCK, event.getEntity().getLocation());
				event.getEntity().remove();
				--chickens;
				int killCounter = kills.get(Disguise.getName(player)) + 1;
				kills.put(Disguise.getName(player), killCounter);
				ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), killCounter);
				ProMcGames.getSidebar().update();
				if(killCounter >= 20) {
					FifteenTickTaskEvent.getHandlerList().unregister(this);
					for(Entity entity : getWorld().getEntities()) {
						if(entity instanceof Chicken || entity instanceof Arrow) {
							entity.remove();
						}
					}
					for(Player playing : ProPlugin.getPlayers()) {
						MessageHandler.sendMessage(playing, "You got " + kills.get(playing.getName()) + " kills");
					}
					disable(player);
				}
			}
		}
	}
}
