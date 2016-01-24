package promcgames.gameapi.games.arcade.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.DelayedTask;

public class TNTWarfare extends ArcadeGame {
	private List<String> red = null;
	private List<String> blue = null;
	private List<String> cannotThrow = null;
	private Map<TNTPrimed, String> teamThrown = null;
	private NPCEntity redNPC = null;
	private NPCEntity blueNPC = null;
	
	public TNTWarfare() {
		super("TNT Warfare");
	}
	
	@Override
	public void enable() {
		super.enable();
		red = new ArrayList<String>();
		blue = new ArrayList<String>();
		cannotThrow = new ArrayList<String>();
		teamThrown = new HashMap<TNTPrimed, String>();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				ProMcGames.getMiniGame().setCounter(20);
				BossBar.setCounter(ProMcGames.getMiniGame().getCounter());
				redNPC = new NPCEntity(EntityType.ZOMBIE, "&cJoin Red Team", new Location(getWorld(), 4.5, 5.0, 0.5)) {
					@Override
					public void onInteract(Player player) {
						if(red.contains(Disguise.getName(player))) {
							MessageHandler.sendMessage(player, "&cYou are already on this team");
						} else {
							if(red.size() >= 5) {
								if(Ranks.PRO.hasRank(player)) {
									Player toKick = null;
									for(String name : red) {
										Player inTeam = ProPlugin.getPlayer(name);
										if(inTeam != null && AccountHandler.getRank(inTeam) == Ranks.PLAYER) {
											toKick = inTeam;
											break;
										}
									}
									if(toKick == null) {
										MessageHandler.sendMessage(player, "&cTeam is full of " + Ranks.PRO.getPrefix() + "&cor above, cannot join team");
									} else {
										red.remove(toKick.getName());
										red.add(Disguise.getName(player));
										updateScoreboard();
										MessageHandler.sendMessage(player, "You joined team &cRED");
									}
								} else {
									MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
								}
							} else {
								blue.remove(Disguise.getName(player));
								red.add(Disguise.getName(player));
								updateScoreboard();
								MessageHandler.sendMessage(player, "You joined team &cRED");
							}
						}
					}
				};
				blueNPC = new NPCEntity(EntityType.ZOMBIE, "&bJoin Blue Team", new Location(getWorld(), -3.5, 5.0, 0.5)) {
					@Override
					public void onInteract(Player player) {
						if(blue.contains(Disguise.getName(player))) {
							MessageHandler.sendMessage(player, "&cYou are already on this team");
						} else {
							if(blue.size() >= 5) {
								if(Ranks.PRO.hasRank(player)) {
									Player toKick = null;
									for(String name : blue) {
										Player inTeam = ProPlugin.getPlayer(name);
										if(inTeam != null && AccountHandler.getRank(inTeam) == Ranks.PLAYER) {
											toKick = inTeam;
											break;
										}
									}
									if(toKick == null) {
										MessageHandler.sendMessage(player, "&cTeam is full of " + Ranks.PRO.getPrefix() + "&cor above, cannot join team");
									} else {
										blue.remove(toKick.getName());
										blue.add(Disguise.getName(player));
										updateScoreboard();
										MessageHandler.sendMessage(player, "You joined team &bBLUE");
									}
								} else {
									MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
								}
							} else {
								red.remove(Disguise.getName(player));
								blue.add(Disguise.getName(player));
								updateScoreboard();
								MessageHandler.sendMessage(player, "You joined team &bBLUE");
							}
						}
					}
				};
				updateScoreboard();
			}
		}, 10);
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		redNPC.remove();
		redNPC = null;
		blueNPC.remove();
		blueNPC = null;
		red.clear();
		red = null;
		blue.clear();
		blue = null;
		cannotThrow.clear();
		cannotThrow = null;
		teamThrown.clear();
		teamThrown = null;
	}
	
	private void checkForGameEnd(Player player) {
		red.remove(Disguise.getName(player));
		blue.remove(Disguise.getName(player));
		updateScoreboard();
		if(red.isEmpty()) {
			MessageHandler.alert("The &1BLUE &ateam won");
			for(Player playing : ProPlugin.getPlayers()) {
				if(blue.contains(playing.getName())) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(playing, false));
				}
			}
			disable(null);
		} else if(blue.isEmpty()) {
			MessageHandler.alert("The &cRED &ateam won");
			for(Player playing : ProPlugin.getPlayers()) {
				if(red.contains(playing.getName())) {
					Bukkit.getPluginManager().callEvent(new GameWinEvent(playing, false));
				}
			}
			disable(null);
		}
	}
	
	private void updateScoreboard() {
		int counter = 0;
		int playersDisplayed = 0;
		ProMcGames.getSidebar().setText(ChatColor.RED + "Red Team:", --counter);
		for(String name : red) {
			Player inTeam = ProPlugin.getPlayer(name);
			if(inTeam != null) {
				ProMcGames.getSidebar().setText(AccountHandler.getRank(inTeam).getColor() + inTeam.getName(), --counter);
				++playersDisplayed;
			}
		}
		ProMcGames.getSidebar().setText(ChatColor.WHITE + " ", --counter);
		ProMcGames.getSidebar().setText(ChatColor.BLUE + "Blue Team:", --counter);
		for(String name : blue) {
			Player inTeam = ProPlugin.getPlayer(name);
			if(inTeam != null) {
				ProMcGames.getSidebar().setText(AccountHandler.getRank(inTeam).getColor() + inTeam.getName(), --counter);
				++playersDisplayed;
			}
		}
		if(playersDisplayed < ProPlugin.getPlayers().size()) {
			ProMcGames.getSidebar().setText(ChatColor.WHITE + "  ", --counter);
			ProMcGames.getSidebar().setText(ChatColor.GRAY + "Other:", --counter);
			for(Player player : ProPlugin.getPlayers()) {
				if(!red.contains(Disguise.getName(player)) && !blue.contains(Disguise.getName(player))) {
					ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), --counter);
				}
			}
		} else {
			ProMcGames.getSidebar().removeText(ChatColor.WHITE + "  ");
			ProMcGames.getSidebar().removeText(ChatColor.GRAY + "Other:");
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		redNPC.remove();
		blueNPC.remove();
		while(blue.size() > red.size() + 1) {
			String name = blue.get(blue.size() - 1);
			blue.remove(name);
			red.add(name);
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, "To balance the teams you've been moved");
			}
		}
		while(red.size() > blue.size() + 1) {
			String name = red.get(red.size() - 1);
			red.remove(name);
			blue.add(name);
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, "To balance the teams you've been moved");
			}
		}
		for(Player player : ProPlugin.getPlayers()) {
			player.getInventory().setItem(0, new ItemStack(Material.TNT));
			if(!red.contains(Disguise.getName(player)) && !blue.contains(Disguise.getName(player))) {
				if(red.size() < blue.size()) {
					red.add(Disguise.getName(player));
				} else if(blue.size() < red.size()) {
					blue.add(Disguise.getName(player));
				} else if(new Random().nextBoolean()){
					red.add(Disguise.getName(player));
				} else {
					blue.add(Disguise.getName(player));
				}
			}
			if(red.contains(Disguise.getName(player))) {
				player.teleport(new Location(getWorld(), 36.5, 5.0, 0.5, 90.0f, 0.0f));
			} else if(blue.contains(Disguise.getName(player))) {
				player.teleport(new Location(getWorld(), -35.5, 5.0, 0.5, 270.0f, 0.0f));
			}
		}
		updateScoreboard();
		BossBar.remove();
		for(int x = -7; x <= 7; ++x) {
			for(int y = 2; y <= 12; ++y) {
				for(int z = -9; z <= 9; ++z) {
					getWorld().getBlockAt(x, y, z).setType(Material.AIR);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTING) {
			updateScoreboard();
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(red != null) {
			red.remove(event.getPlayer().getName());
		}
		if(blue != null) {
			blue.remove(event.getPlayer().getName());
		}
		updateScoreboard();
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			checkForGameEnd(event.getPlayer());
			Bukkit.getPluginManager().callEvent(new GameLossEvent(event.getPlayer()));
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		checkForGameEnd(event.getPlayer());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			SpectatorHandler.add(player);
			checkForGameEnd(player);
			Bukkit.getPluginManager().callEvent(new GameLossEvent(player));
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof TNTPrimed && event.getEntity() instanceof Player) {
			if(teamThrown.containsKey(event.getDamager())) {
				String team = teamThrown.get(event.getDamager());
				Player player = (Player) event.getEntity();
				if(team.equals("red")) {
					if(red.contains(Disguise.getName(player))) {
						return;
					}
				} else if(team.equals("blue")) {
					if(blue.contains(Disguise.getName(player))) {
						return;
					}
				} else {
					return;
				}
				teamThrown.remove(event.getDamager());
				event.setDamage(2.0d);
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if(event.getItem() != null && event.getItem().getType() == Material.TNT && !cannotThrow.contains(event.getPlayer().getName())) {
			cannotThrow.add(event.getPlayer().getName());
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					cannotThrow.remove(event.getPlayer().getName());
				}
			}, 20);
			TNTPrimed tnt = (TNTPrimed) event.getPlayer().getWorld().spawnEntity(event.getPlayer().getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
			tnt.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2.5));
			if(red.contains(event.getPlayer().getName())) {
				teamThrown.put(tnt, "red");
			} else if(blue.contains(event.getPlayer().getName())) {
				teamThrown.put(tnt, "blue");
			}
		}
	}
}
