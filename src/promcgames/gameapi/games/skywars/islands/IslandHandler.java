package promcgames.gameapi.games.skywars.islands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.player.PlayerKitPurchaseEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.games.skywars.islands.islands.Chikara;
import promcgames.gameapi.games.skywars.islands.islands.Cloud;
import promcgames.gameapi.games.skywars.islands.islands.Desert;
import promcgames.gameapi.games.skywars.islands.islands.Dungeon;
import promcgames.gameapi.games.skywars.islands.islands.Ender;
import promcgames.gameapi.games.skywars.islands.islands.Forest;
import promcgames.gameapi.games.skywars.islands.islands.Mesa;
import promcgames.gameapi.games.skywars.islands.islands.Mineshaft;
import promcgames.gameapi.games.skywars.islands.islands.Mushroom;
import promcgames.gameapi.games.skywars.islands.islands.Nether;
import promcgames.gameapi.games.skywars.islands.islands.Swamp;
import promcgames.gameapi.kits.KitBase;
import promcgames.gameapi.kits.KitShop;
import promcgames.player.MessageHandler;
import promcgames.player.PartyHandler;
import promcgames.player.PartyHandler.Party;
import promcgames.server.CommandBase;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EventUtil;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

@SuppressWarnings("deprecation")
public class IslandHandler implements Listener {
	public static Map<String, Location> spawns = null;
	private World arena = null;
	private KitShop shop = null;
	
	public IslandHandler() {
		if(ProMcGames.getPlugin() == Plugins.BUILDING) {
			new CommandBase("saveSkyWarsIsland", 1, true) {
				@Override
				public boolean execute(final CommandSender sender, final String [] arguments) {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = (Player) sender;
							Plugin plugin = ProMcGames.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
							if(plugin == null || !(plugin instanceof WorldEditPlugin)) {
								MessageHandler.sendMessage(player, "&cWorld Edit is not installed");
							} else {
								WorldEditPlugin worldEdit = (WorldEditPlugin) plugin;
								Selection selection = worldEdit.getSelection(player);
								if(selection == null) {
									MessageHandler.sendMessage(player, "&cYou do not have a WorldEdit selection made");
								} else {
									Vector min = selection.getNativeMinimumPoint();
									Vector max = selection.getNativeMaximumPoint();
									World world = selection.getWorld();
									String name = arguments[0];
									String path = Bukkit.getWorldContainer().getPath() + "/plugins/ProMcGamesCore/" + name + ".swm";
									ConfigurationUtil config = new ConfigurationUtil(path);
									Location center = player.getLocation();
									int centerX = center.getBlockX();
									int centerY = center.getBlockY();
									int centerZ = center.getBlockZ();
									int blocks = 0;
									for(int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
										for(int y = 0; y <= 255; ++y) {
											for(int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
												if(x == min.getBlockX() && y == min.getBlockY() && z == min.getBlockZ()) {
													continue;
												}
												if(x == max.getBlockX() && y == max.getBlockY() && z == max.getBlockZ()) {
													continue;
												}
												Block block = world.getBlockAt(x, y, z);
												if(block.getType() != Material.AIR) {
													config.getConfig().set(++blocks + ".id", block.getTypeId());
													config.getConfig().set(blocks + ".data", block.getData());
													int offsetX = x - centerX;
													int offsetY = y - centerY;
													int offsetZ = z - centerZ;
													config.getConfig().set(blocks + ".offset", offsetX + "," + offsetY + "," + offsetZ);
												}
											}
										}
									}
									config.save();
									MessageHandler.sendMessage(player, "Saved map \"&6" + name + "&a\" &7(&e" + blocks + " &bblocks&7)");
								}
							}
						}
					});
					return true;
				}
			};
		} else {
			spawns = new HashMap<String, Location>();
			shop = new KitShop("Island Shop");
			new Forest();
			new Desert();
			new Swamp();
			new Nether();
			new Ender();
			new Mushroom();
			new Chikara();
			new Cloud();
			new Mesa();
			new Dungeon();
			new Mineshaft();
			EventUtil.register(this);
		}
	}
	
	public static Location getLocation(World world, int counter) {
		if(counter == 1) {
			return new Location(world, 0, 51, 48);
		} else if(counter == 2) {
			return new Location(world, -24, 51, 42);
		} else if(counter == 3) {
			return new Location(world, -42, 51, 24);
		} else if(counter == 4) {
			return new Location(world, -48, 51, 0);
		} else if(counter == 5) {
			return new Location(world, -42, 51, -24);
		} else if(counter == 6) {
			return new Location(world, -24, 51, -42);
		} else if(counter == 7) {
			return new Location(world, 0, 51, -48);
		} else if(counter == 8) {
			return new Location(world, 24, 51, -42);
		} else if(counter == 9) {
			return new Location(world, 42, 51, -24);
		} else if(counter == 10) {
			return new Location(world, 48, 51, 0);
		} else if(counter == 11) {
			return new Location(world, 42, 51, 24);
		} else if(counter == 12) {
			return new Location(world, 24, 51, 42);
		} else {
			Bukkit.getLogger().info("NULL COUNTER: " + counter);
			Bukkit.getLogger().info("NULL COUNTER: " + counter);
			Bukkit.getLogger().info("NULL COUNTER: " + counter);
			return null;
		}
	}
	
	private Location pasteIsland(World world, int counter, String name) {
		Location center = getLocation(world, counter);
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/../resources/maps/skywars/islands/" + name + ".swm");
		if(config.getFile().exists()) {
			List<BlockPastingData> blocks = new ArrayList<BlockPastingData>();
			for(String key : config.getConfig().getKeys(false)) {
				int id = config.getConfig().getInt(key + ".id");
				int data = config.getConfig().getInt(key + ".data");
				String offset = config.getConfig().getString(key + ".offset");
				String [] offsetSplit = offset.split(",");
				int x = Integer.valueOf(offsetSplit[0]);
				int y = Integer.valueOf(offsetSplit[1]);
				int z = Integer.valueOf(offsetSplit[2]);
				Block block = center.getBlock().getRelative(x, y, z);
				if(id == Material.LADDER.getId() || id == Material.VINE.getId() || id == Material.RAILS.getId()) {
					blocks.add(new BlockPastingData(center.getBlock(), id, data, x, y, z));
				} else {
					block.setTypeId(id);
					block.setData((byte) data);
				}
			}
			for(BlockPastingData block : blocks) {
				block.set();
			}
		}
		return center;
	}
	
	private void setIslandsOwned(final Player player) {
		GameStates state = ProMcGames.getMiniGame().getGameState();
		if(state == GameStates.WAITING || state == GameStates.VOTING) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					int amount = 0;
					int total = 0;
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getShop().equals(shop)) {
							if(kit.owns(player)) {
								++amount;
							}
							++total;
						}
					}
					MessageHandler.sendMessage(player, "You own &e" + amount + "&7/&e" + total + " &aIslands");
					ProMcGames.getBelowName().setScore(player.getName(), amount);
				}
			});
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		setIslandsOwned(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerKitPurchase(PlayerKitPurchaseEvent event) {
		if(event.getKit().getShop().equals(shop)) {
			setIslandsOwned(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		arena = event.getWorld();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameStarting(GameStartingEvent event) {
		List<Player> players = ProPlugin.getPlayers();
		for(int a = 0; a < players.size(); ++a) {
			Player player = players.get(a);
			if(ProMcGames.getPlugin() == Plugins.SKY_WARS_TEAMS) {
				Party party = PartyHandler.getParty(player);
				if(party == null) {
					MessageHandler.sendMessage(player, "&cYou are not in a team");
					ProPlugin.sendPlayerToServer(player, "hub");
					continue;
				} else if(!party.isLeader(player)) {
					continue;
				}
			}
			spawn(player, a);
		}
		if(ProMcGames.getPlugin() == Plugins.SKY_WARS_TEAMS) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					List<Player> players = ProPlugin.getPlayers();
					String name = ProMcGames.getMiniGame().getLobby().getName();
					for(Player player : players) {
						if(player.getWorld().getName().equals(name)) {
							Party party = PartyHandler.getParty(player);
							if(party != null) {
								spawn(player, players.indexOf(party.getLeader()), party.getLeader().getLocation().add(0, 10, 0));
							}
						}
					}
				}
			}, 20 * 2);
		}
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		spawns.clear();
	}
	
	private void spawn(Player player, int a) {
		spawn(player, a, null);
	}
	
	private void spawn(Player player, int a, Location location) {
		player.getInventory().clear();
		KitBase selectedKit = null;
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getShop().equals(shop) && kit.has(player)) {
				selectedKit = kit;
				break;
			}
		}
		String name = "Forest";
		if(selectedKit != null) {
			name = selectedKit.getName();
		}
		if(location == null) {
			location = pasteIsland(arena, a + 1, name);
			if(location == null) {
				MessageHandler.sendMessage(player, "&cFailed to load your island, please report this!");
				ProPlugin.sendPlayerToServer(player, "hub");
				return;
			}
		}
		location.setY(62);
		location.setPitch(90.0f);
		player.teleport(location.add(0.5, 0, 0.5));
		spawns.put(player.getName(), location);
	}
}
