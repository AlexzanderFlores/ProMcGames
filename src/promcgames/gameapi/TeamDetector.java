package promcgames.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.anticheat.AntiGamingChair;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class TeamDetector implements Listener {
	public static class PossibleTeam extends AntiGamingChair {
		private String playerOne = null;
		private String playerTwo = null;
		private TeamType type = null;
		private int counter = 0;
		
		public PossibleTeam(String playerOne, String playerTwo, TeamType type) {
			super("Teaming");
			this.playerOne = playerOne;
			this.playerTwo = playerTwo;
			this.type = type;
			teams.add(this);
		}
		
		public String getPlayerOneName() {
			return this.playerOne;
		}
		
		public String getPlayerTwoName() {
			return this.playerTwo;
		}
		
		public Player getPlayerOne() {
			return ProPlugin.getPlayer(getPlayerOneName());
		}
		
		public Player getPlayerTwo() {
			return ProPlugin.getPlayer(getPlayerTwoName());
		}
		
		public boolean arePlayers(String playerOne, String playerTwo) {
			if(playerOne.equals(getPlayerOneName()) || playerOne.equals(getPlayerTwoName())) {
				if(playerTwo.equals(getPlayerOneName()) || playerTwo.equalsIgnoreCase(getPlayerTwoName())) {
					return true;
				}
			}
			return false;
		}
		
		public TeamType getType() {
			return this.type;
		}
		
		public int getCounter() {
			return this.counter;
		}
		
		public void setCounter(int counter) {
			this.counter = counter;
		}
		
		public int incrementCounter() {
			return ++this.counter;
		}
		
		public int decrementCounter() {
			return --this.counter;
		}
		
		public void delete() {
			playerOne = null;
			playerTwo = null;
			type = null;
			counter = 0;
			teams.remove(this);
		}
		
		public void warn() {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Player playerOne = getPlayerOne();
					if(playerOne == null) {
						teams.remove(this);
					} else {
						Player playerTwo = getPlayerTwo();
						if(playerTwo == null) {
							delete();
						} else {
							MessageHandler.sendMessage(playerOne, "&4&lWARNING: &e&lTeaming is not allowed");
							MessageHandler.sendMessage(playerTwo, "&4&lWARNING: &e&lTeaming is not allowed");
							String uuid1 = playerOne.getUniqueId().toString();
							String uuid2 = playerTwo.getUniqueId().toString();
							String reason = type.toString();
							String insert = "'" + uuid1 + "', '" + uuid2 + "', '" + reason + "'";
							DB.PLAYERS_TEAMING_WARNINGS.insert(insert);
							String [] keys = new String [] {"uuid_one", "uuid_two", "reason"};
							String [] values = new String [] {uuid1, uuid2, reason};
							if(DB.PLAYERS_TEAMING_WARNINGS.getSize(keys, values) % 5 == 0) {
								DB.PLAYERS_TEAMING_KICKS.insert(insert);
								kick(playerOne);
								kick(playerTwo);
								/*if(DB.PLAYERS_TEAMING_KICKS.getSize(keys, values) % 2 == 0) {
									DB.PLAYERS_TEAM_BANS.insert(insert);
									//kick(playerOne, "BANNED");
									//kick(playerTwo, "BANNED");
								} else {
									kick(playerOne);
									kick(playerTwo);
								}*/
							}
						}
					}
				}
			});
		}
	}
	
	private static List<PossibleTeam> teams = null;
	private Map<Item, String> items = null;
	
	public enum TeamType {
		NEAR, ITEMS, DAMAGE
	}
	
	public TeamDetector() {
		teams = new ArrayList<PossibleTeam>();
		items = new HashMap<Item, String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player playerOne : ProPlugin.getPlayers()) {
						for(Player playerTwo : ProPlugin.getPlayers()) {
							if(!playerOne.getName().equals(playerTwo.getName())) {
								Vector locationOne = playerOne.getLocation().toVector();
								Vector locationTwo = playerTwo.getLocation().toVector();
								PossibleTeam team = null;
								for(PossibleTeam teams : TeamDetector.teams) {
									if(teams.arePlayers(playerOne.getName(), playerTwo.getName())) {
										team = teams;
										break;
									}
								}
								if(locationOne.isInSphere(locationTwo, 5)) {
									if(team == null) {
										team = new PossibleTeam(playerOne.getName(), playerTwo.getName(), TeamType.NEAR);
									}
									if(team.incrementCounter() % 30 == 0) {
										team.warn();
									}
								} else if(team != null && team.decrementCounter() <= 0) {
									team.delete();
								}
							}
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			if(event.getEntity() instanceof Player) {
				Player damager = null;
				if(event.getDamager() instanceof Player) {
					damager = (Player) event.getDamager();
				} else if(event.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) event.getDamager();
					if(projectile.getShooter() instanceof Player) {
						damager = (Player) projectile.getShooter();
					}
				}
				if(damager != null) {
					ItemStack item = damager.getItemInHand();
					String type = item.getType().toString();
					if(type.contains("SWORD") || type.contains("AXE")) {
						Player player = (Player) event.getEntity();
						PossibleTeam team = null;
						for(PossibleTeam teams : TeamDetector.teams) {
							if(teams.arePlayers(player.getName(), damager.getName())) {
								team = teams;
								break;
							}
						}
						if(team != null) {
							team.setCounter(0);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			Player player = event.getPlayer();
			Player killed = event.getKilled();
			for(PossibleTeam teams : TeamDetector.teams) {
				if(teams.arePlayers(player.getName(), killed.getName())) {
					teams.delete();
					break;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerItemDrop(PlayerDropItemEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			if(!event.isCancelled()) {
				items.put(event.getItemDrop(), event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			Item item = event.getItem();
			if(!event.isCancelled() && items.containsKey(item)) {
				if(item.getTicksLived() <= (20 * 10)) {
					String playerOne = items.get(item);
					if(!playerOne.equals(event.getPlayer().getName())) {
						Player player = ProPlugin.getPlayer(playerOne);
						if(!SpectatorHandler.contains(player)) {
							String playerTwo = event.getPlayer().getName();
							PossibleTeam team = null;
							for(PossibleTeam teams : TeamDetector.teams) {
								if(teams.arePlayers(playerOne, playerTwo)) {
									team = teams;
									break;
								}
							}
							if(team == null) {
								team = new PossibleTeam(playerOne, playerTwo, TeamType.ITEMS);
							}
							if(team.incrementCounter() % 5 == 0) {
								team.warn();
							}
						}
					}
				} else {
					items.remove(item);
				}
			}
		}
	}
}
