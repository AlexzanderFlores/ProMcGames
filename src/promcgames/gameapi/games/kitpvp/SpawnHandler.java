package promcgames.gameapi.games.kitpvp;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerSpectateEndEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.kitpvp.events.JumpDownEvent;
import promcgames.gameapi.games.kitpvp.ffa.KitPVP;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.MessageHandler;
import promcgames.player.trophies.KitPVPTrophies;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class SpawnHandler implements Listener {
	public static int spawnY = 112;//82;
	private static double radius = 40;
	private static ItemStack playClans = null;
	private static ItemStack playVersus = null;
	private static ItemStack trophies = null;
	private static double x = 0;
	private static double y = 0;
	private static double z = 0;
	private static float yaw = 0.0f;
	private static float pitch = 0.0f;
	
	public SpawnHandler(double x, double y, double z, float yaw, float pitch, double radius) {
		//12.5, 112, -157.5, -270.0f, 0.0f
		SpawnHandler.x = x;
		SpawnHandler.y = y;
		SpawnHandler.z = z;
		SpawnHandler.yaw = yaw;
		SpawnHandler.pitch = pitch;
		SpawnHandler.radius = radius;
		playClans = new ItemCreator(Material.IRON_INGOT).setName("&aPlay &bSurvival Games").getItemStack();
		playVersus = new ItemCreator(Material.DIAMOND).setName("&bClick to 1v1").getItemStack();
		trophies = new ItemCreator(Material.GOLD_INGOT).setName("&6Trophies").getItemStack();
		EventUtil.register(this);
	}
	
	public static Location spawn(Player player) {
		Random random = new Random();
		int range = 3;
		Location spawn = new Location(player.getWorld(), x, y, z, yaw, pitch);
		spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setY(spawn.getY() + 2.5d);
		spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		player.teleport(spawn);
		player.getInventory().setHeldItemSlot(8);
		final String name = player.getName();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					player.getInventory().setItem(5, trophies);
					player.getInventory().setItem(6, playClans);
					player.getInventory().setItem(7, playVersus);
					player.getInventory().setItem(8, new ItemCreator(Material.EMERALD).setName("&bKit Shop").getItemStack());
					player.getInventory().setHeldItemSlot(8);
				}
			}
		}, 23);
		return spawn;
	}
	
	public static boolean isAtSpawn(Entity entity) {
		return entity.getLocation().getY() >= y;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		spawn(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(isAtSpawn(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player)) {
			if(player.getItemInHand().getType() == Material.EMERALD) {
				KitPVP.getShop().open(player);
			} else if(player.getItemInHand().getType() == Material.DIAMOND) {
				ProPlugin.sendPlayerToServer(player, "versus");
			} else if(player.getItemInHand().getType() == Material.IRON_INGOT) {
				ProPlugin.sendPlayerToServer(player, "sghub");
			} else if(player.getItemInHand().getType() == Material.GOLD_INGOT) {
				KitPVPTrophies.open(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(event.getTo().getBlockY() == spawnY - 2 && !SpectatorHandler.contains(player)) {
			boolean hasKit = false;
			for(KitBase kit : KitBase.getKits()) {
				if(kit.has(player)) {
					hasKit = true;
					break;
				}
			}
			if(!hasKit) {
				MessageHandler.sendMessage(player, "&cYou must have a kit before falling down");
				spawn(player);
				KitPVP.getShop().open(player);
				return;
			}
			Vector spawn = player.getWorld().getSpawnLocation().toVector();
			Vector location = event.getTo().toVector();
			if(location.isInSphere(spawn, radius)) {
				MessageHandler.sendMessage(player, "&cYou can't jump down this hole, jump down the far edge");
				spawn(player);
				return;
			}
			JumpDownEvent jumpDownEvent = new JumpDownEvent(player);
			Bukkit.getPluginManager().callEvent(jumpDownEvent);
			if(jumpDownEvent.isCancelled()) {
				spawn(player);
			} else {
				player.getInventory().remove(Material.DIAMOND);
				player.getInventory().remove(Material.EMERALD);
				player.getInventory().remove(Material.IRON_INGOT);
				player.getInventory().remove(Material.GOLD_INGOT);
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateEnd(PlayerSpectateEndEvent event) {
		spawn(event.getPlayer());
	}
}
