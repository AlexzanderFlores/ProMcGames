package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import promcgames.ProPlugin;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.ParkourCompleteEvent;
import promcgames.customevents.player.PartyDeleteEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.PartyHandler;
import promcgames.player.PartyHandler.Party;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class Parkour implements Listener {
	private class Race {
		public Party party = null;
		public int counter = 4;
		
		public Race(Party party) {
			this.party = party;
			party.sendMessage("Parkour race starting soon!");
			if(races == null) {
				races = new HashMap<Party, Race>();
			}
			races.put(party, this);
		}
		
		public int decrementCounter() {
			--counter;
			if(counter == 3) {
				party.sendMessage("&eRace starting in &c" + counter + " &e...");
			} else if(counter == 2) {
				party.sendMessage("&eRace starting in &c" + counter + " &e...");
			} else if(counter == 1) {
				party.sendMessage("&eRace starting in &c" + counter + " &e...");
			} else if(counter == 0) {
				party.sendMessage("&c&lRACE STARTED! &a&lGO");
				for(Player player : party.getPlayers()) {
					EffectUtil.playSound(player, Sound.EXPLODE);
					start(player);
				}
			}
			return counter;
		}
		
		public void win(Player player) {
			party.sendMessage(AccountHandler.getPrefix(player) + " &ehas won the race!");
			remove();
		}
		
		public void remove() {
			races.remove(party);
			party = null;
		}
	}
	
	private static Map<String, Location> checkpoints = null;
	private static Map<String, Integer> freeCheckpoints = null;
	private static Map<String, CountDownUtil> counters = null;
	private static Map<Party, Race> races = null;
	private List<String> delayed = null;
	private ItemStack returnTocheckpoint = null;
	private ItemStack setCheckpoint = null;
	private ItemStack setFreeCheckpoint = null;
	private ItemStack timer = null;
	private ItemStack exitParkour = null;
	private int prize = 1000;
	private int delay = 2;
	
	public Parkour() {
		new CommandBase("resetParkourTime", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else if(DB.HUB_PARKOUR_TIMES.isUUIDSet(uuid)) {
					DB.HUB_PARKOUR_TIMES.deleteUUID(uuid);
				} else {
					MessageHandler.sendMessage(sender, "&c" + name + " has never completed the parkour before");
				}
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
		new CommandBase("updateCheckpoints", 1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = ProPlugin.getPlayer(arguments[0]);
				if(player != null && isParkouring(player)) {
					int amount = DB.HUB_PARKOUR_FREE_CHECKPOINTS.getInt("uuid", player.getUniqueId().toString(), "amount");
					freeCheckpoints.put(player.getName(), amount);
					displayFreeCheckpoints(player);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		checkpoints = new HashMap<String, Location>();
		freeCheckpoints = new HashMap<String, Integer>();
		counters = new HashMap<String, CountDownUtil>();
		delayed = new ArrayList<String>();
		returnTocheckpoint = new ItemCreator(Material.FEATHER).setName("&aReturn to checkpoint").getItemStack();
		setCheckpoint = new ItemCreator(Material.EMERALD).setName("&2Set checkpoint").getItemStack();
		setFreeCheckpoint = new ItemCreator(Material.NAME_TAG).setName("&2Set Free Checkpoint").getItemStack();
		timer = new ItemCreator(Material.WATCH).setName("&6Time: &e0:00").getItemStack();
		exitParkour = new ItemCreator(Material.GLOWSTONE_DUST).setName("&6Exit Parkour").getItemStack();
		World world = Bukkit.getWorlds().get(0);
		new NPCEntity(EntityType.SKELETON, "&bParkour Start &7(&e" + prize + " Emerald Prize&7)", new Location(world, -32.5, 127, -167.5, -242.84f, -2.55f)) {
			@Override
			public void onInteract(Player player) {
				start(player);
			}
		};
		new NPCEntity(EntityType.ZOMBIE, "&bRace with Friends", new Location(world, -32.5, 127, -171.5)) {
			@Override
			public void onInteract(Player player) {
				Party party = PartyHandler.getParty(player);
				if(party == null) {
					MessageHandler.sendMessage(player, "&cYou are not in a party &f/party");
				} else if(party.isLeader(player)) {
					if(party.getSize() >= 2) {
						for(Player member : party.getPlayers()) {
							if(isParkouring(member)) {
								MessageHandler.sendMessage(player, "&cCannot start race: " + AccountHandler.getPrefix(member) + " &cis currently playing parkour");
								return;
							} else {
								member.teleport(player);
							}
						}
						new Race(party);
					} else {
						MessageHandler.sendMessage(player, "&cYou must have at least &e2 &cmembers in your party");
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou are not the leader of your party");
				}
			}
		};
		int counter = 0;
		Location [] locations = new Location [] {
			new Location(world, -26.5, 155, -141.5, -170.0f, 0.0f),
			new Location(world, -38.5, 141, -223.5, -316.20f, -13.20f),
			new Location(world, -102.5, 174, -268.5, -180.0f, 0.0f)
		};
		for(Location location : locations) {
			new NPCEntity(EntityType.ZOMBIE, "&aCheckpoint &7(&e" + ++counter + "&7/&e" + locations.length + "&7)", location) {
				@Override
				public void onInteract(Player player) {
					setCheckpoint(player);
				}
			};
		}
		new NPCEntity(EntityType.SKELETON, "&bParkour End", new Location(world, -102.5, 178.5, -262.5, -180.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				if(isParkouring(player)) {
					CountDownUtil counter = counters.get(player.getName());
					ParkourCompleteEvent event = new ParkourCompleteEvent(player, counter.getCounter());
					Bukkit.getPluginManager().callEvent(event);
					if(!event.isCancelled()) {
						MessageHandler.alert(AccountHandler.getPrefix(player) + " &6completed the parkour in " + counter.getCounterAsString() + "&6!");
						Party party = PartyHandler.getParty(player);
						if(party != null && races != null && races.containsKey(party)) {
							races.get(party).win(player);
						}
						boolean hasPreviousTime = DB.HUB_PARKOUR_TIMES.isUUIDSet(player.getUniqueId());
						int current = 0;
						if(hasPreviousTime) {
							current = DB.HUB_PARKOUR_TIMES.getInt("uuid", player.getUniqueId().toString(), "seconds");
						}
						if(current == 0 || counter.getCounter() < current) {
							current = counter.getCounter();
							MessageHandler.sendMessage(player, "&6NEW Best time!");
							EffectUtil.launchFirework(player);
							if(hasPreviousTime) {
								DB.HUB_PARKOUR_TIMES.updateInt("seconds", counter.getCounter(), "uuid", player.getUniqueId().toString());
							} else {
								DB.HUB_PARKOUR_TIMES.insert("'" + player.getUniqueId().toString() + "', '" + counter.getCounter() + "'");
							}
						}
						updateTop8();
					}
					end(player);
					EmeraldsHandler.addEmeralds(player, prize, EmeraldReason.PARKOUR_FINISH, false);
				} else {
					MessageHandler.sendMessage(player, "&cYou are not playing Parkour");
				}
			}
		};
		updateTop8();
		EventUtil.register(this);
	}
	
	public static boolean isParkouring(Player player) {
		return checkpoints != null && checkpoints.containsKey(player.getName());
	}
	
	private void start(Player player) {
		if(isParkouring(player)) {
			MessageHandler.sendMessage(player, "&cYou are already in the parkour");
		} else if(SnowballFight.isPlaying(player)) {
			MessageHandler.sendMessage(player, "&cYou cannot do the parkour while in the snowball fight");
		} else if(player.getVehicle() == null) {
			checkpoints.put(player.getName(), player.getLocation());
			for(PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
			if(player.getAllowFlight()) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
			player.getInventory().clear();
			final String name = player.getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						int amount = DB.HUB_PARKOUR_FREE_CHECKPOINTS.getInt("uuid", player.getUniqueId().toString(), "amount");
						if(amount > 0) {
							freeCheckpoints.put(player.getName(), amount);
						}
						displayFreeCheckpoints(player);
						player.getInventory().setItem(0, returnTocheckpoint);
						player.getInventory().setItem(1, setCheckpoint);
						player.getInventory().setItem(2, setFreeCheckpoint);
						player.getInventory().setItem(4, timer);
						player.getInventory().setItem(8, exitParkour);
						player.updateInventory();
					}
				}
			});
			counters.put(player.getName(), new CountDownUtil());
			MessageHandler.sendMessage(player, "You have started Parkour");
		} else {
			MessageHandler.sendMessage(player, "&cYou cannot do parkour while riding an entity");
		}
	}
	
	private void end(Player player) {
		if(isParkouring(player)) {
			checkpoints.remove(player.getName());
			if(freeCheckpoints.containsKey(player.getName())) {
				int amount = freeCheckpoints.get(player.getName());
				if(DB.HUB_PARKOUR_FREE_CHECKPOINTS.isUUIDSet(player.getUniqueId())) {
					DB.HUB_PARKOUR_FREE_CHECKPOINTS.updateInt("amount", amount, "uuid", player.getUniqueId().toString());
				} else {
					DB.HUB_PARKOUR_FREE_CHECKPOINTS.insert("'" + player.getUniqueId().toString() + "', '" + amount + "'");
				}
				freeCheckpoints.remove(player.getName());
			}
			HubItemBase.giveOriginalHotBar(player);
			if(Ranks.PRO.hasRank(player)) {
				player.setAllowFlight(true);
			}
			counters.remove(player.getName());
			MessageHandler.sendMessage(player, "You have been removed from Parkour");
			Party party = PartyHandler.getParty(player);
			if(party != null && races.containsKey(party)) {
				races.get(party).remove();
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not playing Parkour");
		}
	}
	
	private boolean setCheckpoint(Player player) {
		if(isParkouring(player)) {
			Block block = player.getLocation().getBlock();
			if(block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType() == Material.AIR && Ranks.PRO.hasRank(player)) {
				MessageHandler.sendMessage(player, "&cYou cannot set your checkpoint in the air");
			} else {
				checkpoints.put(player.getName(), player.getLocation());
				MessageHandler.sendMessage(player, "Check point set!");
				return true;
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not playing Parkour");
		}
		return false;
	}
	
	private void updateTop8() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<Integer> times = new ArrayList<Integer>();
				for(String uuid : DB.HUB_PARKOUR_TIMES.getOrdered("seconds", "uuid", 8)) {
					uuids.add(UUID.fromString(uuid));
					times.add(DB.HUB_PARKOUR_TIMES.getInt("uuid", uuid, "seconds"));
				}
				if(uuids.isEmpty()) {
					return;
				}
				World world = Bukkit.getWorlds().get(0);
				Sign sign = (Sign) world.getBlockAt(-103, 180, -240).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-103, 179, -240).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-104, 180, -240).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, ChatColor.stripColor(new CountDownUtil(times.get(a)).getCounterAsString()));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-104, 179, -240).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, ChatColor.stripColor(new CountDownUtil(times.get(a)).getCounterAsString()));
				}
				sign.update();
				uuids.clear();
				uuids = null;
				times.clear();
				times = null;
			}
		});
	}
	
	private static void displayFreeCheckpoints(Player player) {
		int amount = freeCheckpoints == null || !freeCheckpoints.containsKey(player.getName()) ? 0 : freeCheckpoints.get(player.getName());
		MessageHandler.sendMessage(player, "You have &e" + amount + " &afree checkpoint" + (amount == 1 ? "" : "s"));
		MessageHandler.sendMessage(player, "Get &e20 &afree checkpoints by voting: &d/vote");
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(!counters.isEmpty()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(counters.containsKey(player.getName())) {
					CountDownUtil counter = counters.get(player.getName());
					counter.incrementCounter();
					ItemStack item = player.getInventory().getItem(4);
					if(item != null && item.getType() != Material.AIR) {
						item = new ItemCreator(item).setName("&6Timer: " + counter.getCounterAsString()).getItemStack();
						player.getInventory().setItem(4, item);
					}
				}
			}
		}
		if(races != null && !races.isEmpty()) {
			for(Race race : races.values()) {
				race.decrementCounter();
			}
		}
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		updateTop8();
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.PHYSICAL && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.WALL_SIGN) {
			Location location = event.getClickedBlock().getLocation();
			int x = location.getBlockX();
			int y = location.getBlockY();
			int z = location.getBlockZ();
			if((x == -101 || x == -105) && y == 179 && z == -240) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if(ChatColor.stripColor(sign.getLine(0)).equals("[Click]")) {
					Player player = event.getPlayer();
					final String name = player.getName();
					if(!delayed.contains(name)) {
						delayed.add(name);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								delayed.remove(name);
							}
						}, 20 * delay);
						if(DB.HUB_PARKOUR_TIMES.isUUIDSet(player.getUniqueId())) {
							int seconds = DB.HUB_PARKOUR_TIMES.getInt("uuid", player.getUniqueId().toString(), "seconds");
							MessageHandler.sendMessage(player, "Your best time is " + new CountDownUtil(seconds).getCounterAsString());
						} else {
							MessageHandler.sendMessage(player, "&cYou have no best parkour time logged");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPartyDelete(PartyDeleteEvent event) {
		Party party = event.getParty();
		if(races != null && party != null && races.containsKey(party)) {
			races.get(party).remove();
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		ItemStack item = player.getItemInHand();
		if(ItemUtil.isItem(item, returnTocheckpoint)) {
			player.teleport(checkpoints.get(name));
		} else if(ItemUtil.isItem(item, setCheckpoint)) {
			if(Ranks.PRO.hasRank(player)) {
				if(!Ranks.PRO_PLUS.hasRank(player)) {
					if(EmeraldsHandler.getEmeralds(player) < 20) {
						MessageHandler.sendMessage(player, "&cYou do not have enough emeralds");
						return;
					}
					EmeraldsHandler.addEmeralds(player, -20, EmeraldReason.HUB_CHECKPOINT_SET, false);
				}
				setCheckpoint(player);
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
				MessageHandler.sendMessage(player, "You can use free checkpoints! Click the name tag item");
			}
		} else if(ItemUtil.isItem(item, setFreeCheckpoint)) {
			int checkPoints = freeCheckpoints == null || !freeCheckpoints.containsKey(name) ? 0 : freeCheckpoints.get(name);
			if(checkPoints > 0) {
				if(setCheckpoint(player)) {
					freeCheckpoints.put(name, --checkPoints);
					displayFreeCheckpoints(player);
				}
			} else {
				displayFreeCheckpoints(player);
			}
		} else if(ItemUtil.isItem(item, exitParkour)) {
			end(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		end(event.getPlayer());
	}
}
