package promcgames.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class SpawnPointHandler implements Listener {
	private World world = null;
	private ConfigurationUtil config = null;
	private Map<String, Integer> locatedAt = null;
	private List<Location> spawns = null;
	private List<String> delayed = null;
	private ItemStack up = null;
	private ItemStack down = null;
	private int delay = 1;
	
	public SpawnPointHandler(World world) {
		this(world, "spawns");
	}
	
	public SpawnPointHandler(World world, String file) {
		this.world = world;
		this.config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/" + file + ".yml");
		spawns = new ArrayList<Location>();
		locatedAt = new HashMap<String, Integer>();
		if(SurvivalGames.getEliteCanChooseStartingSpawn()) {
			delayed = new ArrayList<String>();
			up = new ItemCreator(Material.STAINED_GLASS, 5).setName("&aMove Right").getItemStack();
			down = new ItemCreator(Material.STAINED_GLASS, 14).setName("&cMove Left").getItemStack();
			for(Player player : ProPlugin.getPlayers()) {
				if(SurvivalGames.getEliteCanChooseStartingSpawn()) {
					player.getInventory().setItem(3, down);
					player.getInventory().setItem(5, up);
				}
			}
		}
		EventUtil.register(this);
	}
	
	public static Location getCenter(World world, List<Location> spawns) {
		double x1 = spawns.get(0).getX();
		double z1 = spawns.get(0).getZ();
		double x2 = spawns.get(12).getX();
		double z2 = spawns.get(12).getZ();
		double x = (spawns.get(0).getX() - x2) / 2;
		double z = (z1 - z2) / 2;
		double centerX = (x1 > x2 ? x1 - x : x2 + x);
		double centerZ = (z1 > z2 ? z1 - z : z2 + z);
		return new Location(world, centerX, 0, centerZ);
	}
	
	public Location getSpawnCenter() {
		return getCenter(world, getSpawns());
	}
	
	public void teleport(List<Player> players) {
		locatedAt.clear();
		int counter = 0;
		int numberOfSpawns = getSpawns().size();
		for(Player player : players) {
			if(counter >= numberOfSpawns) {
				counter = 0;
			}
			Location location = getSpawns().get(counter);
			player.teleport(location);
			locatedAt.put(player.getName(), counter++);
		}
	}
	
	public void swapLocations(Player one, Player two) {
		String oneName = Disguise.getName(one);
		String twoName = Disguise.getName(two);
		if(locatedAt.containsKey(oneName) && locatedAt.containsKey(twoName)) {
			int spawnOne = locatedAt.get(oneName);
			int spawnTwo = locatedAt.get(twoName);
			locatedAt.put(oneName, spawnTwo);
			locatedAt.put(twoName, spawnOne);
		}
	}
	
	public List<Location> getSpawns() {
		if(spawns == null || spawns.isEmpty()) {
			spawns = new ArrayList<Location>();
			for(String key : config.getConfig().getKeys(false)) {
				double x = config.getConfig().getDouble(key + ".x");
				double y = config.getConfig().getDouble(key + ".y");
				double z = config.getConfig().getDouble(key + ".z");
				float yaw = (float) config.getConfig().getDouble(key + ".yaw");
				float pitch = (float) config.getConfig().getDouble(key + ".pitch");
				spawns.add(new Location(world, x, y, z, yaw, pitch));
			}
			return spawns;
		}
		return spawns;
	}
	
	private int getSpawnPlayerIsAt(Player player) {
		if(locatedAt == null || locatedAt.isEmpty()) {
			return -1;
		} else {
			return locatedAt.get(player.getName());
		}
	}
	
	private Player getPlayerAtSpawn(int spawn) {
		for(String name : locatedAt.keySet()) {
			if(locatedAt.get(name) == spawn) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					return player;
				}
			}
		}
		return null;
	}
	
	private void teleportPlayerToSpawn(Player player, int spawn) {
		if(spawn >= 0 && spawn < getSpawns().size()) {
			int currentSpawn = getSpawnPlayerIsAt(player);
			Player otherPlayer = getPlayerAtSpawn(spawn);
			if(otherPlayer != null) {
				locatedAt.put(otherPlayer.getName(), currentSpawn);
				otherPlayer.teleport(getSpawns().get(currentSpawn).add(0, 1, 0));
			}
			locatedAt.put(player.getName(), spawn);
			player.teleport(getSpawns().get(spawn).add(0, 1, 0));
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		final Player player = event.getPlayer();
		int isUp = ItemUtil.isItem(player.getItemInHand(), up) ? 1 : (ItemUtil.isItem(player.getItemInHand(), down) ? 2 : 3);
		if(isUp != 3) {
			if(Ranks.ELITE.hasRank(player, true)) {
				if(delayed.contains(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou can only use this once every &e" + delay + " &csecond");
				} else {
					delayed.add(player.getName());
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(player.getName());
						}
					}, 20 * delay);
					int spawn = getSpawnPlayerIsAt(player);
					int newSpawn = isUp == 1 ? spawn + 1 : spawn - 1;
					if(newSpawn < 0) {
						newSpawn = getSpawns().size() - 1;
					} else if(newSpawn >= getSpawns().size()) {
						newSpawn = 0;
					}
					teleportPlayerToSpawn(player, newSpawn);
					EffectUtil.playSound(player, Sound.ENDERMAN_TELEPORT);
					event.setCancelled(true);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		locatedAt.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		locatedAt.clear();
		locatedAt = null;
		HandlerList.unregisterAll(this);
	}
}