package promcgames.gameapi.modes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.ThirtySecondTaskEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.Loading;
import promcgames.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class CTF extends ModeBase {
	private int captureLimit = 5;
	private int redCaptures = 0;
	private int blueCaptures = 0;
	private List<BlockState> redFlagLocationRemoved = null;
	private List<BlockState> blueFlagLocationRemoved = null;
	private Location startingRed = null;
	private Location startingBlue = null;
	private Location redFlag = null;
	private Location blueFlag = null;
	private boolean redFlagPickedUp = false;
	private boolean blueFlagPickedUp = false;
	private int standStillCounter = 0; // Used to count how many seconds both flags have been held
	private ItemStack compass = null;
	
	public CTF(int captureLimit) {
		super("Capture the Flag", "CTF");
		this.captureLimit = captureLimit;
		redFlagLocationRemoved = new ArrayList<BlockState>();
		blueFlagLocationRemoved = new ArrayList<BlockState>();
		//compass = ItemHandler.getItem(Material.COMPASS, "");
		compass = new ItemCreator(Material.COMPASS).setName("").getItemStack();
	}

	@Override
	public Teams getWinning() {
		int red = getCaptures(Teams.RED);
		int blue = getCaptures(Teams.BLUE);
		return red > blue ? Teams.RED : blue > red ? Teams.BLUE : null;
	}
	
	public void spawnFlag(Teams team) {
		Location location = null;
		DyeColor color = null;
		if(team == Teams.RED) {
			location = redFlag;
			color = DyeColor.RED;
		} else if(team == Teams.BLUE) {
			location = blueFlag;
			color = DyeColor.BLUE;
		}
		if(location != null && color != null) {
			while(location.getBlock().getRelative(0, -1, 0).getType() == Material.AIR && location.getBlockY() > 0) {
				location = location.add(0, -1, 0);
			}
			for(int a = 0; a < 3; ++a) {
				Block block = location.getBlock().getRelative(0, a, 0);
				if(team == Teams.RED) {
					redFlagLocationRemoved.add(block.getState());
				} else if(team == Teams.BLUE) {
					blueFlagLocationRemoved.add(block.getState());
				}
				block.setType(Material.FENCE);
			}
			for(int a = 1; a < 3; ++a) {
				Block block = location.getBlock().getRelative(a, 2, 0);
				if(team == Teams.RED) {
					redFlagLocationRemoved.add(block.getState());
				} else if(team == Teams.BLUE) {
					blueFlagLocationRemoved.add(block.getState());
				}
				block.setType(Material.WOOL);
				block.setData(color.getData());
			}
		}
	}
	
	private void removeFlag(Teams team) {
		removeFlag(team, true);
	}
	
	private void removeFlag(Teams team, boolean rollBack) {
		Location location = null;
		if(team == Teams.RED) {
			location = redFlag;
		} else if(team == Teams.BLUE) {
			location = blueFlag;
		}
		if(location != null) {
			for(int a = 0; a < 3; ++a) {
				Block block = location.getBlock().getRelative(0, a, 0);
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
			for(int a = 1; a < 3; ++a) {
				Block block = location.getBlock().getRelative(a, 2, 0);
				block.setType(Material.AIR);
				block.setData((byte) 0);
			}
		}
		if(rollBack && team == Teams.RED && !redFlagLocationRemoved.isEmpty()) {
			for(BlockState block : redFlagLocationRemoved) {
				block.getLocation().getBlock().setType(block.getType());
				block.getLocation().getBlock().setData(block.getData().getData());
			}
			redFlagLocationRemoved.clear();
		} else if(rollBack && team == Teams.BLUE && !blueFlagLocationRemoved.isEmpty()) {
			for(BlockState block : blueFlagLocationRemoved) {
				block.getLocation().getBlock().setType(block.getType());
				block.getLocation().getBlock().setData(block.getData().getData());
			}
			blueFlagLocationRemoved.clear();
		}
	}
	
	private void giveFlag(Player player, byte data) {
		player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, data));
	}
	
	public int getCaptureLimit() {
		return this.captureLimit;
	}
	
	public int getCaptures(Teams team) {
		return team == Teams.RED ? redCaptures : team == Teams.BLUE ? blueCaptures : 0;
	}
	
	public void addCapture(Teams team) {
		if(team == Teams.RED) {
			++redCaptures;
			if(comebackEffect == null && ((int) Math.round(redCaptures * 100.0 / captureLimit)) >= comeBackPercentage) {
				executeComebackEffects(team);
			} else if(redCaptures >= captureLimit){
				ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
			}
		} else if(team == Teams.BLUE) {
			++blueCaptures;
			if(comebackEffect == null && ((int) Math.round(blueCaptures * 100.0 / captureLimit)) >= comeBackPercentage) {
				executeComebackEffects(team);
			} else if(blueCaptures >= captureLimit){
				ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		MiniGame miniGame = ProMcGames.getMiniGame();
		GameStates gameState = miniGame.getGameState();
		if(gameState == GameStates.STARTING && miniGame.getCounter() == 10) {
			World world = Bukkit.getWorlds().get(1); //TODO: Set this to something
			ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/ctf_flags.yml");
			new Loading("Flag Locations");
			if(config.getFile().exists()) {
				for(Teams team : Teams.values()) {
					double x = config.getConfig().getDouble(team.toString().toLowerCase() + ".x");
					double y = config.getConfig().getDouble(team.toString().toLowerCase() + ".y");
					double z = config.getConfig().getDouble(team.toString().toLowerCase() + ".z");
					Location location = new Location(world, x, y, z);
					if(team == Teams.RED) {
						startingRed = location;
						redFlag = startingRed;
						spawnFlag(team);
					} else if(team == Teams.BLUE) {
						startingBlue = location;
						blueFlag = startingBlue;
						spawnFlag(team);
					}
				}
			} else {
				MessageHandler.alert("&4ERROR: &cNo flags found for this map... closing game");
				miniGame.setGameState(GameStates.ENDING);
			}
		} else if(gameState == GameStates.STARTED) {
			for(Player player : ProPlugin.getPlayers()) {
				if(player.getLevel() == 9) {
					Teams team = getTeam(player);
					String text = " Flag Tracker " + ChatColor.GRAY + "(Must be holding)";
					String title = team == Teams.RED ? ChatColor.BLUE + "Blue" + text : team == Teams.BLUE ? ChatColor.RED + "Red" + text : "";
					//player.getInventory().addItem(ItemHandler.getItem(compass, title));
					player.getInventory().addItem(new ItemCreator(compass).setName(title).getItemStack());
				}
				if(player.getItemInHand().getType() == Material.COMPASS) {
					String title = ChatColor.stripColor(player.getItemInHand().getItemMeta().getDisplayName());
					if(title != null) {
						if(title.startsWith("Red")) {
							player.setCompassTarget(redFlag);
						} else if(title.startsWith("Blue")) {
							player.setCompassTarget(blueFlag);
						}
					}
				}
			}
			if(redFlagPickedUp && blueFlagPickedUp) {
				if(standStillCounter == 10) {
					Bukkit.broadcastMessage(StringUtil.color("&e&lBoth teams are holding flags... Stand still started"));
				}
				int max = 3;
				if(++standStillCounter >= (60 * max)) {
					giveArmor();
					redFlagPickedUp = false;
					blueFlagPickedUp = false;
					redFlag = startingRed;
					blueFlag = startingBlue;
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							for(Teams team : Teams.values()) {
								spawnFlag(team);
							}
						}
					});
					Bukkit.broadcastMessage(StringUtil.color("&c&l" + standStillCounter / 60 + "&e&l/&c&l" + max + " &e&lminutes of Stand Still have passed"));
					Bukkit.broadcastMessage(StringUtil.color("&c&lFlags returned to their original location"));
				} else if(standStillCounter % 60 == 0) {
					Bukkit.broadcastMessage(StringUtil.color("&c&l" + standStillCounter / 60 + "&e&l/&c&l" + max + " &e&lminutes of Stand Still have passed"));
					Bukkit.broadcastMessage(StringUtil.color("&e&lOnce all " + max + " minutes have passed flags respawn"));
				}
			} else {
				standStillCounter = 0;
			}
		}
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			ParticleTypes.DRIP_LAVA.displaySpiral(redFlag, 7, 3.5);
			ParticleTypes.DRIP_WATER.displaySpiral(blueFlag, 7, 3.5);
		}
	}
	
	@EventHandler
	public void onThirtySecondTask(ThirtySecondTaskEvent event) {
		MessageHandler.alert("");
		MessageHandler.alert("");
		MessageHandler.alert("&c&lTO PICK UP THE FLAG WALK INTO THE FENCE");
		MessageHandler.alert("");
		MessageHandler.alert("");
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && !(SpectatorHandler.isEnabled() && SpectatorHandler.contains(event.getPlayer()))) {
			if(event.getTo().getBlock().getType() == Material.FENCE) {
				Location to = event.getTo();
				int x = to.getBlockX();
				int y = to.getBlockY();
				int z = to.getBlockZ();
				int xRed = redFlag.getBlockX();
				int yRed = redFlag.getBlockY();
				int zRed = redFlag.getBlockZ();
				int xBlue = blueFlag.getBlockX();
				int yBlue = blueFlag.getBlockY();
				int zBlue = blueFlag.getBlockZ();
				Teams team = getTeam(player);
				if(x == xRed && y == yRed && z == zRed) {
					if(team == Teams.RED) {
						int xRedStart = startingRed.getBlockX();
						int yRedStart = startingRed.getBlockY();
						int zRedStart = startingRed.getBlockZ();
						if(x == xRedStart && y == yRedStart && z == zRedStart) {
							if(player.getInventory().getHelmet().getType() == Material.WOOL) {
								giveArmor(player);
								// Score for the red team
								EmeraldsHandler.addEmeralds(player, 50, EmeraldReason.CTF_CAPTURE, true);
								MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has CAPTURED the enemy flag");
								addCapture(team);
								// Return blue flag
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										blueFlag = startingBlue;
										spawnFlag(Teams.BLUE);
									}
								});
							}
						} else {
							// Return red flag
							EmeraldsHandler.addEmeralds(player, 2, EmeraldReason.CTF_RETURN, true);
							MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has RETURNED their flag");
							removeFlag(Teams.RED, false);
							redFlag = startingRed;
							spawnFlag(Teams.RED);
						}
					} else if(team == Teams.BLUE) {
						// Remove the blue flag
						player.setHealth(player.getMaxHealth());
						EmeraldsHandler.addEmeralds(player, 2, EmeraldReason.CTF_PICK_UP, true);
						MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has PICKED UP the enemy flag");
						redFlagPickedUp = true;
						removeFlag(Teams.RED);
						giveFlag(player, DyeColor.RED.getData());
					}
				} else if(x == xBlue && y == yBlue && z == zBlue) {
					if(team == Teams.BLUE) {
						int xBlueStart = startingBlue.getBlockX();
						int yBlueStart = startingBlue.getBlockY();
						int zBlueStart = startingBlue.getBlockZ();
						if(x == xBlueStart && y == yBlueStart && z == zBlueStart) {
							if(player.getInventory().getHelmet().getType() == Material.WOOL) {
								giveArmor(player);
								// Score for the blue team
								EmeraldsHandler.addEmeralds(player, 50, EmeraldReason.CTF_CAPTURE, true);
								MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has CAPTURED the enemy flag");
								addCapture(team);
								// Return red flag
								new DelayedTask(new Runnable() {
									@Override
									public void run() {
										redFlag = startingRed;
										spawnFlag(Teams.RED);
									}
								});
							}
						} else {
							// Return blue flag
							EmeraldsHandler.addEmeralds(player, 2, EmeraldReason.CTF_RETURN, true);
							MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has RETURNED their flag");
							removeFlag(Teams.BLUE, false);
							blueFlag = startingBlue;
							spawnFlag(Teams.BLUE);
						}
					} else if(team == Teams.RED) {
						// Remove the red flag
						player.setHealth(player.getMaxHealth());
						EmeraldsHandler.addEmeralds(player, 2, EmeraldReason.CTF_PICK_UP, true);
						MessageHandler.alert(getPrefix(team) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has PICKED UP the enemy flag");
						blueFlagPickedUp = true;
						removeFlag(Teams.BLUE);
						giveFlag(player, DyeColor.BLUE.getData());
					}
				}
			}
			ItemStack helmet = player.getInventory().getHelmet();
			if(helmet != null && helmet.getType() == Material.WOOL) {
				byte data = player.getInventory().getHelmet().getData().getData();
				if(data == DyeColor.RED.getData()) {
					redFlag = event.getTo();
				} else if(data == DyeColor.BLUE.getData()) {
					blueFlag = event.getTo();
				}
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getPlayer().getItemInHand().getType() == Material.COMPASS && !(SpectatorHandler.isEnabled() && SpectatorHandler.contains(event.getPlayer()))) {
			String title = ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName());
			String text = " Flag Tracker " + ChatColor.GRAY + "(Must be holding)";
			if(title.startsWith("Blue")) {
				title = ChatColor.RED + "Red" + text;
			} else if(title.startsWith("Red")) {
				title = ChatColor.BLUE + "Blue" + text;
			}
			//event.getPlayer().setItemInHand(ItemHandler.getItem(Material.COMPASS, title));
			event.getPlayer().setItemInHand(new ItemCreator(compass).setName(title).getItemStack());
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		dropFlag(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		dropFlag(event.getPlayer());
	}
	
	public void dropFlag(Player player) {
		ItemStack helmet = player.getInventory().getHelmet();
		if(helmet != null && helmet.getType() == Material.WOOL) {
			byte data = player.getInventory().getHelmet().getData().getData();
			final Teams team = data == DyeColor.RED.getData() ? Teams.RED : data == DyeColor.BLUE.getData() ? Teams.BLUE : null;
			Teams enemyTeam = team == Teams.RED ? Teams.BLUE : team == Teams.BLUE ? Teams.RED : null;
			if(team != null && enemyTeam != null) {
				MessageHandler.alert(getPrefix(enemyTeam) + " " + Disguise.getName(player) + ChatColor.YELLOW + " has DROPPED the enemy flag");
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						if(team == Teams.RED) {
							redFlagPickedUp = false;
						} else if(team == Teams.BLUE){
							blueFlagPickedUp = false;
						}
						spawnFlag(team);
					}
				});
			}
		}
	}
}
