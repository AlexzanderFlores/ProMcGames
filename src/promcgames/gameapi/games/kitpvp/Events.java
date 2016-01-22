package promcgames.gameapi.games.kitpvp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import promcgames.ProPlugin;
import promcgames.customevents.RestartAnnounceEvent;
import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.scoreboard.BelowNameHealthScoreboardUtil;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	private Scoreboard scoreboard = null;
	private Map<UUID, Integer> previousLevels = null;
	
	public Events() {
		previousLevels = new HashMap<UUID, Integer>();
		for(String uuidString : DB.PLAYERS_KIT_PVP_LEVELS.getAllStrings("uuid")) {
			UUID uuid = UUID.fromString(uuidString);
			previousLevels.put(uuid, DB.PLAYERS_KIT_PVP_LEVELS.getInt("uuid", uuidString, "level"));
			DB.PLAYERS_KIT_PVP_LEVELS.deleteUUID(uuid);
		}
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		new BelowNameHealthScoreboardUtil(scoreboard);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(scoreboard);
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(previousLevels.containsKey(player.getUniqueId())) {
			MessageHandler.sendMessage(player, "Loading levels from before restart...");
			player.setLevel(previousLevels.get(player.getUniqueId()));
			previousLevels.remove(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(ProPlugin.isServerFull()) {
			if(Ranks.PRO.hasRank(event.getPlayer())) {
				event.setResult(Result.ALLOWED);
			} else {
				event.setResult(Result.KICK_FULL);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		} else if(event.getCause() == DamageCause.SUFFOCATION) {
			event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
		} else if(event.getCause() == DamageCause.BLOCK_EXPLOSION && event.getEntity() instanceof Player && !event.isCancelled()) {
			event.setDamage(event.getDamage() * 2);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		event.getPlayer().setExp(0.0f);
		event.setRespawnLocation(SpawnHandler.spawn(event.getPlayer()));
		for(KitBase kit : KitBase.getKits()) {
			if(kit.has(event.getPlayer())) {
				kit.execute(player);
				break;
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			event.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		Player player = event.getPlayer();
		player.setLevel(player.getLevel() + 1);
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1));
		MessageHandler.sendMessage(player, "&e+1 &alevel due to killing");
		Player killed = event.getKilled();
		if(killed != null) {
			int level = killed.getLevel();
			if(level > 0) {
				if(level % 5 == 0) {
					MessageHandler.sendMessage(killed, "&cYour level is divisible by &e5&c: No loss of level");
				} else {
					MessageHandler.sendMessage(killed, "&c-1 &alevel due to dying");
					killed.setLevel(level - 1);
				}
			}
			MessageHandler.sendMessage(player, "Killed " + AccountHandler.getPrefix(killed));
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(event.getKiller() != null) {
			MessageHandler.sendMessage(event.getPlayer(), "Killed by " + AccountHandler.getPrefix(event.getKiller()));
		}
	}
	
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
			ProPlugin.sendPlayerToServer(event.getPlayer(), "hub");
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(event.getMessage().toLowerCase().contains("team")) {
			MessageHandler.sendMessage(event.getPlayer(), "");
			MessageHandler.sendMessage(event.getPlayer(), "&c&lIf you dislike teaming try our &b&lVERSUS &c&lgame! Do &a&l/join versus");
			MessageHandler.sendMessage(event.getPlayer(), "");
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(event.getEntity().getItemStack().getItemMeta().getDisplayName() != null) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.setYield(0);
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(SpawnHandler.isAtSpawn(player)) {
				InventoryType type = event.getInventory().getType();
				if(type == InventoryType.HOPPER) {
					event.setCancelled(true);
				}
			} else if(event.getInventory().getType() == InventoryType.ENCHANTING){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onRestartAnnounce(RestartAnnounceEvent event) {
		if(event.getCounter() > 5) {
			MessageHandler.alert("&6Note: &aLevels are saved through restarts!");
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			if(player.getLevel() > 0) {
				UUID uuid = player.getUniqueId();
				if(DB.PLAYERS_KIT_PVP_LEVELS.isUUIDSet(uuid)) {
					DB.PLAYERS_KIT_PVP_LEVELS.updateInt("level", player.getLevel(), "uuid", uuid.toString());
				} else {
					DB.PLAYERS_KIT_PVP_LEVELS.insert("'" + uuid.toString() + "', '" + player.getLevel() + "'");
				}
			}
		}
	}
}
