package promcgames.server.servers.hub.items;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB.Databases;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class ServerSelectorItem extends HubItemBase {
	private static HubItemBase instance = null;
	private static Map<String, Plugins> watching = null;
	private static Map<String, Integer> animationSlots = null;
	private static List<String> viewing = null;
	private static int [] slots = new int [] {0, 10, 2, 12, 4, 14, 6, 16, 8};
	
	public ServerSelectorItem() {
		super(new ItemCreator(Material.COMPASS).setName("&aServer Selector"), 0);
		instance = this;
		watching = new HashMap<String, Plugins>();
		animationSlots = new HashMap<String, Integer>();
		viewing = new ArrayList<String>();
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			World world = Bukkit.getWorlds().get(0);
			/*new NPCEntity(EntityType.SKELETON, "&aFactions", new Location(world, -111.5, 126.0, -183.5), Material.TNT) {
				@Override
				public void onInteract(Player player) {
					ProPlugin.sendPlayerToServer(player, "factions");
				}
			}.getLivingEntity();*/
			new NPCEntity(EntityType.SNOWMAN, "&aVersus (1v1s)", new Location(world, -111.5, 126.0, -183.5), Material.FISHING_ROD) {
				@Override
				public void onInteract(Player player) {
					ProPlugin.sendPlayerToServer(player, "versus");
				}
			};
			new NPCEntity(EntityType.SNOWMAN, "&aFactions", new Location(world, -108.5, 126.0, -184.5), Material.TNT) {
				@Override
				public void onInteract(Player player) {
					ProPlugin.sendPlayerToServer(player, "factions");
				}
			}.getLivingEntity();
			/*new NPCEntity(EntityType.SNOWMAN, "&aSurvival Games Clans", new Location(world, -108.5, 126.0, -184.5), Material.GOLD_SWORD) {
				@Override
				public void onInteract(Player player) {
					ProPlugin.sendPlayerToServer(player, "sghub");
				}
			};*/
			new NPCEntity(EntityType.SNOWMAN, "&aSurvival Games", new Location(world, -105.5, 126.0, -185.5), Material.DIAMOND_SWORD) {
				@Override
				public void onInteract(Player player) {
					open(player, Plugins.SURVIVAL_GAMES);
				}
			};
			new NPCEntity(EntityType.SNOWMAN, "&aSky Wars", new Location(world, -102.5, 126.0, -185.5), Material.GRASS) {
				@Override
				public void onInteract(Player player) {
					openSkyWars(player, false);
				}
			};
			new NPCEntity(EntityType.SNOWMAN, "&aKit PVP", new Location(world, -99.5, 126.0, -185.5), Material.STONE_SWORD) {
				@Override
				public void onInteract(Player player) {
					//ProPlugin.sendPlayerToServer(player, "kitpvp1");
					open(player, Plugins.KIT_PVP);
				}
			};
			new NPCEntity(EntityType.SNOWMAN, "&aHosted UHC", new Location(world, -96.5, 126.0, -184.5), Material.GOLDEN_APPLE) {
				@Override
				public void onInteract(Player player) {
					open(player, Plugins.UHC);
				}
			};
			new NPCEntity(EntityType.SNOWMAN, "&aArcade", new Location(world, -93.5, 126.0, -183.5), Material.SUGAR) {
				@Override
				public void onInteract(Player player) {
					open(player, Plugins.ARCADE);
				}
			};
		}
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	public static void open(Player player, Plugins plugin) {
		if(open(player, plugin, true)) {
			player.getInventory().setHeldItemSlot(ServerSelectorItem.getInstance().getSlot());
		}
	}
	
	private static boolean open(Player player, Plugins plugin, boolean npcUsed) {
		openGameInventory(player, plugin, !npcUsed);
		viewing.add(player.getName());
		watching.put(player.getName(), plugin);
		update(plugin);
		return true;
	}
	
	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}
	
	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(isItem(event.getPlayer())) {
			openInventory(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
		boolean npc = false;
		Plugins plugin = null;
		if(event.getTitle().equals(ChatColor.stripColor(getName()))) {
			if(event.getItem().getType() == Material.DIAMOND_SWORD) {
				ProPlugin.sendPlayerToServer(player, "sghub");
				player.closeInventory();
				final String playerName = player.getName();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(playerName);
						if(player != null) {
							open(player, Plugins.SURVIVAL_GAMES, false);
						}
					}
				}, 10);
			} else if(event.getItem().getType() == Material.FISHING_ROD) {
				ProPlugin.sendPlayerToServer(player, "versus");
				player.closeInventory();
			} else if(event.getItem().getType() == Material.TNT) {
				ProPlugin.sendPlayerToServer(player, "factions");
				player.closeInventory();
			} else if(event.getItem().getType() == Material.NETHER_STAR) {
				plugin = Plugins.HUB;
			} else if(event.getItem().getType() == Material.GRASS) {
				openSkyWars(player, true);
			} else if(event.getItem().getType() == Material.SUGAR) {
				plugin = Plugins.ARCADE;
			} else if(event.getItem().getType() == Material.IRON_SWORD) {
				//ProPlugin.sendPlayerToServer(player, "kitpvp1");
				plugin = Plugins.KIT_PVP;
			} else if(event.getItem().getType() == Material.GOLDEN_APPLE) {
				plugin = Plugins.UHC;
				/*ProPlugin.sendPlayerToServer(player, "uhchub");
				player.closeInventory();
				final String playerName = player.getName();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(playerName);
						if(player != null) {
							open(player, Plugins.UHC, false);
						}
					}
				}, 10);*/
			} else {
				openInventory(player);
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals("UHC")) {
			if(event.getItem().getType() == Material.GOLDEN_APPLE) {
				ProPlugin.sendPlayerToServer(player, "uhc1");
				player.closeInventory();
			} else if(event.getItem().getType() == Material.IRON_INGOT) {
				if(event.getItem().getAmount() == 1) {
					ProPlugin.sendPlayerToServer(player, "uhcb1");
				} else {
					ProPlugin.sendPlayerToServer(player, "uhcb2");
				}
				player.closeInventory();
			} else {
				watching.remove(player.getName());
				viewing.remove(player.getName());
				openInventory(player);
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals("Sky Wars")) {
			if(event.getItem().getType() == Material.SKULL_ITEM) {
				if(event.getItem().getAmount() == 1) {
					plugin = Plugins.SKY_WARS;
				} else {
					plugin = Plugins.SKY_WARS_TEAMS;
				}
				npc = player.getInventory().getHeldItemSlot() != 0;
				if(npc) {
					player.getInventory().setHeldItemSlot(0);
				}
			} else if(event.getItem().getType() == Material.ARROW) {
				watching.remove(player.getName());
				viewing.remove(player.getName());
				openInventory(player);
			} else {
				player.closeInventory();
			}
			event.setCancelled(true);
		} else if(isItem(player)) {
			if(event.getItem().getType() == Material.ARROW) {
				if(watching.get(player.getName()) == Plugins.SKY_WARS || watching.get(player.getName()) == Plugins.SKY_WARS_TEAMS) {
					openSkyWars(player, true);
				} else {
					openInventory(player);
				}
				watching.remove(player.getName());
				viewing.remove(player.getName());
			} else if(event.getItem().getType() == Material.WOOL) {
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&6You can get these perks here:");
				MessageHandler.sendMessage(player, "&bhttp://store.promcgames.com/category/250196");
				MessageHandler.sendMessage(player, "");
				player.closeInventory();
			} else {
				if(event.getItem().getData().getData() == getGlassColor(1) && !Ranks.PRO.hasRank(player)) {
					MessageHandler.sendMessage(player, "&cServer is full! Join full servers with " + Ranks.PRO.getPrefix());
					player.closeInventory();
				} else {
					for(Plugins plugins : Plugins.values()) {
						if(plugins.getServer().equalsIgnoreCase(name.replace(" ", "").replaceAll("[0-9]", ""))) {
							ProPlugin.sendPlayerToServer(player, name.replace(" ", ""));
							break;
						}
					}
				}
				player.closeInventory();
			}
			event.setCancelled(true);
		}
		if(plugin != null) {
			open(player, plugin, npc);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Plugins plugin : Plugins.values()) {
			for(String name : watching.keySet()) {
				if(watching.get(name) == plugin) {
					update(plugin);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		if(viewing.contains(event.getPlayer().getName()) && !Ranks.OWNER.hasRank(event.getPlayer())) {
			event.getPlayer().closeInventory();
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(viewing.contains(event.getPlayer().getName())) {
			watching.remove(event.getPlayer().getName());
			viewing.remove(event.getPlayer().getName());
		}
		animationSlots.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		watching.remove(event.getPlayer().getName());
		viewing.remove(event.getPlayer().getName());
		animationSlots.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		Iterator<String> iterator = animationSlots.keySet().iterator();
		while(iterator.hasNext()) {
			String name = iterator.next();
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				InventoryView inventory = player.getOpenInventory();
				int slot = animationSlots.get(name);
				inventory.setItem(slots[slot], new ItemCreator(Material.STAINED_GLASS_PANE, 11).setName(" ").getItemStack());
				if(++slot >= slots.length) {
					iterator.remove();
				} else {
					animationSlots.put(name, slot);
				}
			}
		}
	}
	
	public static Inventory openInventory(Player player) {
		return openInventory(player, null);
	}
	
	public static Inventory openInventory(Player player, String title) {
		if(ProMcGames.getPlugin() == Plugins.SGHUB) {
			open(player, Plugins.SURVIVAL_GAMES, true);
		} else if(ProMcGames.getPlugin() == Plugins.UHCHUB) {
			open(player, Plugins.UHC, true);
		} else {
			if(title == null) {
				title = ChatColor.stripColor(getInstance().getName());
			}
			Inventory inventory = Bukkit.createInventory(player, 9 * 2, title);
			inventory.setItem(1, new ItemCreator(Material.DIAMOND_SWORD).setName(ChatColor.AQUA + "Survival Games").getItemStack());
			inventory.setItem(3, new ItemCreator(Material.FISHING_ROD).setName(ChatColor.AQUA + "Versus").getItemStack());
			inventory.setItem(5, new ItemCreator(Material.IRON_SWORD).setName(ChatColor.AQUA + "Kit PVP").getItemStack());
			inventory.setItem(7, new ItemCreator(Material.GOLDEN_APPLE).setName(ChatColor.AQUA + "Hosted UHC").getItemStack());
			inventory.setItem(9, new ItemCreator(Material.GRASS).setName(ChatColor.AQUA + "Sky Wars").getItemStack());
			inventory.setItem(11, new ItemCreator(Material.TNT).setName(ChatColor.AQUA + "Factions").getItemStack());
			inventory.setItem(13, new ItemCreator(Material.NETHER_STAR).setName(ChatColor.AQUA + "Hub").getItemStack());
			inventory.setItem(15, new ItemCreator(Material.BEDROCK).setName(ChatColor.GRAY + "Coming Soon").getItemStack());
			inventory.setItem(17, new ItemCreator(Material.SUGAR).setName(ChatColor.AQUA + "Arcade").getItemStack());
			player.openInventory(inventory);
			animationSlots.put(player.getName(), 0);
			return inventory;
		}
		return null;
	}
	
	private static Inventory openSkyWars(Player player, boolean addBackArrow) {
		Inventory inventory = Bukkit.createInventory(player, addBackArrow ? 18 : 9, "Sky Wars");
		inventory.setItem(3, new ItemCreator(Material.SKULL_ITEM, 3).setName("&aSolo").getItemStack());
		inventory.setItem(5, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(2).setName("&aTeams of Two").getItemStack());
		if(addBackArrow) {
			inventory.setItem(9, new ItemCreator(Material.ARROW).setName("&bBack").getItemStack());
		}
		player.openInventory(inventory);
		return inventory;
	}
	
	private static void openGameInventory(Player player, Plugins plugin, boolean addBackArrow) {
		Inventory inventory = Bukkit.createInventory(player, 54, plugin.toString().replace("_", " "));
		if(plugin == Plugins.HUB) {
			inventory.setItem(3, new ItemCreator(Material.STAINED_GLASS, 5).setName("&eAvailable hub").addLore("&bCan join").getItemStack());
			inventory.setItem(5, new ItemCreator(Material.STAINED_GLASS, 4).setName("&eYour current hub").addLore("&bYou are here").getItemStack());
		} else if(plugin == Plugins.UHC) {
			inventory = Bukkit.createInventory(player, addBackArrow ? 18 : 9, "UHC");
			inventory.setItem(2, new ItemCreator(Material.GOLDEN_APPLE).setName("&aHosted UHC").getItemStack());
			inventory.setItem(4, new ItemCreator(Material.IRON_INGOT).setName("&aUHC Battles").getItemStack());
			inventory.setItem(6, new ItemCreator(Material.IRON_INGOT).setAmount(2).setName("&aUHC Battles 2").getItemStack());
		} else {
			inventory.setItem(3, new ItemCreator(Material.STAINED_GLASS, getGlassColor(1)).setName("&eGame is full").setLores(new String [] {"&bOnly " + Ranks.PRO.getPrefix() + "&band above can join"}).getItemStack());
			inventory.setItem(4, new ItemCreator(Material.STAINED_GLASS, getGlassColor(2)).setName("&eGame is available").addLore("&bAnyone can join").getItemStack());
			inventory.setItem(5, new ItemCreator(Material.STAINED_GLASS, getGlassColor(3)).setName("&eGame is spectatable").addLore("&bYou may spectate").getItemStack());
			List<String> proPerks = new ArrayList<String>();
			List<String> proPlusPerks = new ArrayList<String>();
			List<String> elitePerks = new ArrayList<String>();
			proPerks.add(Ranks.PRO.getColor() + "Gain 2x the amount of &2Emerlads");
			proPerks.add(Ranks.PRO.getColor() + "Can join full servers");
			proPerks.add(Ranks.PRO.getColor() + "Access to &e/spectate");
			proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "Gain 3x the amount of &2Emerlads");
			proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "Can join full servers");
			proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "Access to &e/spectate");
			elitePerks.add(Ranks.ELITE.getColor() + "Gain 4x the amount of &2Emerlads");
			elitePerks.add(Ranks.ELITE.getColor() + "Can join full servers");
			elitePerks.add(Ranks.ELITE.getColor() + "Access to &e/spectate");
			if(plugin == Plugins.SURVIVAL_GAMES) {
				proPerks.add(Ranks.PRO.getColor() + "2x the amount of votes");
				proPerks.add(Ranks.PRO.getColor() + "2 player swapping snowballs");
				proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "3x the amount of votes");
				proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "3 player swapping snowballs");
				proPlusPerks.add(Ranks.PRO_PLUS.getColor() + "Ability to sponsor all players");
				elitePerks.add(Ranks.ELITE.getColor() + "4x the amount of votes");
				elitePerks.add(Ranks.ELITE.getColor() + "4 player swapping snowballs");
				elitePerks.add(Ranks.ELITE.getColor() + "Ability to sponsor all players");
				elitePerks.add(Ranks.ELITE.getColor() + "Access to all paid kits");
				elitePerks.add(Ranks.ELITE.getColor() + "Locate 1 tier 1 chest per game");
			}
			byte proData = DyeColor.LIME.getData();
			byte proPlusData = DyeColor.LIGHT_BLUE.getData();
			byte eliteData = DyeColor.PURPLE.getData();
			String proName = Ranks.PRO.getPrefix() + "&ePerks";
			String proPlusName = Ranks.PRO_PLUS.getPrefix() + "&ePerks";
			String eliteName = Ranks.ELITE.getPrefix() + "&ePerks";
			int slot = addBackArrow ? 46 : 45;
			inventory.setItem(slot++, new ItemCreator(Material.WOOL, proData).setLores(proPerks).setName(proName).getItemStack());
			inventory.setItem(slot++, new ItemCreator(Material.WOOL, proPlusData).setLores(proPlusPerks).setName(proPlusName).getItemStack());
			inventory.setItem(slot++, new ItemCreator(Material.WOOL, eliteData).setLores(elitePerks).setName(eliteName).getItemStack());
			proPerks.clear();
			proPerks = null;
			proPlusPerks.clear();
			proPlusPerks = null;
			elitePerks.clear();
			elitePerks = null;
		}
		if(addBackArrow) {
			inventory.setItem(plugin == Plugins.UHC ? 9 : 45, new ItemCreator(Material.ARROW).setName("&bBack").getItemStack());
		}
		player.openInventory(inventory);
	}
	
	private static byte getGlassColor(int priority) {
		return (byte) (priority == 1 ? 4 : priority == 2 ? 5 : priority == 3 ? 9 : -1);
	}
	
	private static void update(final Plugins plugin) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ResultSet resultSet = null;
				try {
					List<Integer> priorities = new ArrayList<Integer>();
					List<Integer> serverNumbers = new ArrayList<Integer>();
					List<String> lores = new ArrayList<String>();
					List<Integer> playerCounts = new ArrayList<Integer>();
					List<Integer> maxPlayers = new ArrayList<Integer>();
					resultSet = Databases.NETWORK.getConnection().prepareStatement("SELECT * FROM server_status WHERE game_name = '" + plugin.toString() + "' ORDER BY listed_priority, players DESC, server_number LIMIT 36").executeQuery();
					while(resultSet.next()) {
						priorities.add(resultSet.getInt("listed_priority"));
						serverNumbers.add(resultSet.getInt("server_number"));
						lores.add(resultSet.getString("lore"));
						playerCounts.add(resultSet.getInt("players"));
						maxPlayers.add(resultSet.getInt("max_players"));
					}
					int players = 0;
					for(int playing : playerCounts) {
						players += playing;
					}
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(watching.containsKey(player.getName()) && watching.get(player.getName()) == plugin) {
							for(int a = 0; a < serverNumbers.size(); ++a) {
								int serverNumber = serverNumbers.get(a);
								String server = ChatColor.GREEN + plugin.getServer() + " " + serverNumber;
								byte data = getGlassColor(priorities.get(a));
								String name = null;
								if(plugin == Plugins.HUB) {
									name = "HUB" + serverNumber;
								}
								if(name == null) {
									String [] lore = null;
									if(lores.get(0).equalsIgnoreCase("null")) {
										lore = new String [] {
											ChatColor.GREEN + "" + playerCounts.get(a) + "/" + maxPlayers.get(a)
										};
									} else {
										lore = new String [] {
											ChatColor.YELLOW + StringUtil.getFirstLetterCap(lores.get(a)),
											ChatColor.GREEN + "" + playerCounts.get(a) + "/" + maxPlayers.get(a)
										};
									}
									if(plugin == Plugins.UHC) {
										int slot = a == 0 ? 2 : a == 1 ? 4 : 6;
										ItemCreator item = new ItemCreator(player.getOpenInventory().getItem(slot));
										item.setLores(lore);
										player.getOpenInventory().setItem(slot, item.getItemStack());
									} else {
										player.getOpenInventory().setItem(a + 9, new ItemCreator(Material.STAINED_GLASS, data).setAmount(serverNumber).setName(server).setLores(lore).getItemStack());
									}
								} else {
									if(ProMcGames.getServerName().equals(name.toUpperCase().replace(" ", "_"))) {
										data = 4;
									} else {
										data = 5;
									}
									int count = playerCounts.get(a);
									player.getOpenInventory().setItem(a + 9, new ItemCreator(Material.STAINED_GLASS, data).setAmount(serverNumber).setName(server).setLores(new String [] {ChatColor.GREEN + "" + count + (count == 1 ? " player" : " players")}).getItemStack());
								}
							}
							for(int a = serverNumbers.size() + 9; a <= 44; ++a) {
								if(player.getOpenInventory().getItem(a).getType() == Material.STAINED_GLASS) {
									player.getOpenInventory().setItem(a, new ItemStack(Material.AIR));
								}
							}
							if(plugin != Plugins.UHC) {
								try {
									player.getOpenInventory().setItem(53, new ItemCreator(Material.DIAMOND).setName("&6Server Stats").setLores(new String [] {ChatColor.GREEN + "Total Players: " + ChatColor.YELLOW + players}).getItemStack());
								} catch(IndexOutOfBoundsException e) {
									
								}
							}
						}
					}
					priorities.clear();
					serverNumbers.clear();
					lores.clear();
					playerCounts.clear();
					maxPlayers.clear();
					priorities = null;
					serverNumbers = null;
					lores = null;
					playerCounts = null;
					maxPlayers = null;
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						if(resultSet != null) {
							resultSet.close();
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
