package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.player.ChestOpenEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.gameapi.GracePeriod;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.survivalgames.deathmatch.DeathmatchHandler;
import promcgames.gameapi.games.survivalgames.events.SponsorOpenEvent;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.Particles;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.Loading;
import promcgames.server.util.StringUtil;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.AtomLocationEffect;
import de.slikey.effectlib.effect.HelixLocationEffect;
import de.slikey.effectlib.util.ParticleEffect;

@SuppressWarnings("deprecation")
public class SponsorHandler implements Listener {
	// Base item
	public static int requiredClearSpace = 30;
	private static int maxNumberOfSponsors = 2;
	private boolean canSponsorAll = true;
	private String inventoryName = null;
	protected static int cost = 250;
	private static Map<String, Integer> numberOfTimesLeftAvailable = null;
	private static List<Location> flares = null;
	private static Map<Location, Boolean> blocks = null;
	private static Map<Location, AtomLocationEffect> effects = null;
	private static Map<FallingBlock, String> crates = null;
	private static EffectManager manager = null;
	
	// Sponsor item
	private static ItemStack sponsorItem = null;
	private static String sponsorItemInventoryName = null;
	private String normalSponsorText = null;
	private String superSponsorText = null;
	private String normalSponsorAllText = null;
	private String superSponsorAllText = null;
	
	// Option displayer
	private String itemPossibilities = null;
	
	// Normal sponsor
	private String crateInventoryName = null;
	private static ItemStack flareItem = null;
	private static List<Material> possibleItems = null;
	
	// Super sponsor
	private String superInventoryName = null;
	private String superCrateInventoryName = null;
	private static ItemStack superFlareItem = null;
	private static List<Material> possibleSuperItems = null;
	
	// Spiral particle selector
	private String name = null;
	private static ItemStack particleSelector = null;
	private static Map<String, ParticleTypes> spiralParticles = null;
	private static List<String> delayed = null;
	private static int delay = 3;
	private static Random random = null;
	
	private static enum Rarity {COMMON, UNCOMMON, RARE}
	
	public SponsorHandler() {
		// Base item
		inventoryName = ChatColor.YELLOW + "Sponsor Player";
		numberOfTimesLeftAvailable = new HashMap<String, Integer>();
		flares = new ArrayList<Location>();
		blocks = new HashMap<Location, Boolean>();
		effects = new HashMap<Location, AtomLocationEffect>();
		crates = new HashMap<FallingBlock, String>();
		manager = new EffectManager(ProMcGames.getInstance());
		
		// Sponsor item
		sponsorItemInventoryName = "Sponsor Options";
		sponsorItem = new ItemCreator(Material.EMERALD).setName("&a" + sponsorItemInventoryName).getItemStack();
		normalSponsorText = ChatColor.GREEN + "Normal Sponsor";
		superSponsorText = ChatColor.GREEN + "Super Sponsor";
		normalSponsorAllText = ChatColor.GREEN + "Normal Sponsor (All Players)";
		superSponsorAllText = ChatColor.GREEN + "Super Sponsor (All Players)";
		
		// Option displayer
		itemPossibilities = "Item Possibilities";
		
		// Normal sponsor
		crateInventoryName = ChatColor.AQUA + "Sponsor Crate";
		flareItem = new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&bSponsor Flare").getItemStack();
		possibleItems = new ArrayList<Material>();
		
		// Super sponsor
		superInventoryName = ChatColor.YELLOW + "Super Sponsor Player";
		superCrateInventoryName = ChatColor.AQUA + "Super Sponsor Crate";
		superFlareItem = new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&bSuper Sponsor Flare").getItemStack();
		possibleSuperItems = new ArrayList<Material>();
		
		// Spiral particle selector
		name = "Sponsor Spiral Particle Selector";
		particleSelector = new ItemCreator(Material.BLAZE_ROD).setName("&b" + name).getItemStack();
		spiralParticles = new HashMap<String, ParticleTypes>();
		delayed = new ArrayList<String>();
		random = new Random();
		
		new CommandBase("sponsor", 2, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				if(!SurvivalGames.getCanUseSponsors()) {
					MessageHandler.sendMessage(player, "&4Sponsors are currently disabled!");
					return true;
				}
				if(SpectatorHandler.contains(player)) {
					String targetName = arguments[0];
					Player targetPlayer = ProPlugin.getPlayer(targetName);
					if(targetPlayer == null || SpectatorHandler.contains(targetPlayer)) {
						MessageHandler.sendMessage(player, "&c" + targetName + " is not playing");
					} else {
						if(arguments[1].equalsIgnoreCase("normal")) {
							sponsor(targetPlayer, player);
						} else if(arguments[1].equalsIgnoreCase("super")){
							sponsor(targetPlayer, player, true);
						} else {
							return false;
						}
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou can only sponsor as a spectator");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PRO);
		
		new CommandBase("sponsorParticles", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.openInventory(Particles.getParticlesMenu(player, name));
				return true;
			}
		}.setRequiredRank(Ranks.ELITE);
		
		addItem(Material.GOLDEN_APPLE, Rarity.COMMON);
		addItem(Material.IRON_INGOT, Rarity.COMMON);
		addItem(Material.FLINT_AND_STEEL, Rarity.COMMON);
		addItem(Material.DIAMOND, Rarity.UNCOMMON);
		addItem(Material.ENDER_PEARL, Rarity.UNCOMMON);
		addItem(Material.IRON_SWORD, Rarity.UNCOMMON);
		addItem(Material.GOLD_INGOT, Rarity.UNCOMMON);
		addItem(Material.SKULL_ITEM, Rarity.RARE);
		addItem(Material.DIAMOND_SWORD, Rarity.UNCOMMON, true);
		addItem(Material.SKULL_ITEM, Rarity.UNCOMMON, true);
		addItem(Material.IRON_HELMET, Rarity.RARE, true);
		addItem(Material.IRON_BOOTS, Rarity.RARE, true);
		EventUtil.register(this);
	}
	
	private void addItem(Material material, Rarity rarity) {
		addItem(material, rarity, false);
	}
	
	private void addItem(Material material, Rarity rarity, boolean superItem) {
		for(int a = 0; a < (rarity == Rarity.COMMON ? 3 : rarity == Rarity.UNCOMMON ? 2 : 1); ++a) {
			possibleSuperItems.add(material);
			if(!superItem) {
				possibleItems.add(material);
			}
		}
	}
	
	public static boolean sponsor(Player player, Player sponsoring) {
		return sponsor(player, sponsoring, false);
	}
	
	public static boolean sponsor(Player player, Player sponsoring, boolean superSponsor) {
		return sponsor(player, sponsoring, superSponsor, false);
	}
	
	public static boolean sponsor(Player player, Player sponsoring, boolean superSponsor, boolean sponsoringAll) {
		if(DeathmatchHandler.isRunning() || ProMcGames.getMiniGame().getGameState() != GameStates.STARTED || GracePeriod.isRunning()) {
			if(sponsoring != null) {
				MessageHandler.sendMessage(sponsoring, "&cCannot sponsor at this time");
			}
		} else if(numberOfTimesLeftAvailable.containsKey(Disguise.getName(player)) || sponsoringAll) {
			if(sponsoring == null || EmeraldsHandler.getEmeralds(sponsoring) >= cost || sponsoringAll) {
				if(!sponsoringAll) {
					if(sponsoring != null) {
						int price = cost * -1;
						if(superSponsor) {
							price *= 2;
						}
						EmeraldsHandler.addEmeralds(sponsoring, price, EmeraldReason.SG_SPONSOR, false);
					}
					int uses = superSponsor ? 2 : 1;
					numberOfTimesLeftAvailable.put(Disguise.getName(player), numberOfTimesLeftAvailable.get(Disguise.getName(player)) - uses);
					if(numberOfTimesLeftAvailable.get(Disguise.getName(player)) <= 0) {
						numberOfTimesLeftAvailable.remove(Disguise.getName(player));
					}
				}
				if(sponsoring != null) {
					MessageHandler.sendLine(player);
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "");
					if(sponsoringAll) {
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(sponsoring) + " &ehas sponsored everyone!");
						MessageHandler.sendMessage(player, "Sponsoring everyone is a " + Ranks.PRO_PLUS.getPrefix() + "&aperk! &b/buy");
					} else {
						MessageHandler.sendMessage(player, "You've been sponsored by " + AccountHandler.getPrefix(sponsoring));
					}
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendLine(player);
				}
				if(!sponsoringAll && sponsoring != null) {
					MessageHandler.sendMessage(sponsoring, "You sponsored " + AccountHandler.getPrefix(player, false));
				}
				ItemStack flare = superSponsor ? superFlareItem : flareItem;
				Inventory inventory = player.getInventory();
				for(int a = 0; a < inventory.getSize(); ++a) {
					ItemStack item = inventory.getItem(a);
					if(item == null || item.getType() == Material.AIR || ItemUtil.isItem(item, flareItem)) {
						inventory.addItem(flare);
						return true;
					}
				}
				player.getWorld().dropItem(player.getLocation(), flare);
				return true;
			} else if(sponsoring != null) {
				MessageHandler.sendMessage(sponsoring, "&cYou don't have enough emeralds to sponsor");
			}
		} else if(sponsoring != null) {
			MessageHandler.sendMessage(sponsoring, AccountHandler.getPrefix(player, false) + "&c has already been sponsored " + maxNumberOfSponsors + " times");
		}
		return false;
	}
	
	public static void giveItems(Player player) {
		player.getInventory().setItem(1, sponsorItem);
	}
	
	public static void loadParticles() {
		new Loading("Sponsor Spiral Particles", "/sponsorParticles");
		for(Player player : ProPlugin.getPlayers()) {
			if(DB.PLAYERS_SG_PARTICLES_SELECTED.isUUIDSet(player.getUniqueId())) {
				ParticleTypes type = ParticleTypes.valueOf(DB.PLAYERS_SG_PARTICLES_SELECTED.getString("uuid", player.getUniqueId().toString(), "particle"));
				spiralParticles.put(player.getName(), type);
			}
		}
	}
	
	public static ItemStack getParticleSelector() {
		return particleSelector;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		GameStates state = ProMcGames.getMiniGame().getGameState();
		if(state == GameStates.WAITING || state == GameStates.VOTING) {
			event.getPlayer().getInventory().addItem(particleSelector);
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		if(!event.isCancelled()) {
			final String name = event.getPlayer().getName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					giveItems(player);
				}
			});
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			numberOfTimesLeftAvailable.put(Disguise.getName(player), maxNumberOfSponsors);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		numberOfTimesLeftAvailable.remove(event.getPlayer().getName());
		spiralParticles.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), sponsorItem)) {
			if(!SurvivalGames.getCanUseSponsors()) {
				MessageHandler.sendMessage(player, "&4Sponsors are currently disabled!");
				event.setCancelled(true);
				return;
			}
			Inventory inventory = Bukkit.createInventory(player, 9, sponsorItemInventoryName);
			inventory.setItem(1, new ItemCreator(Material.EMERALD).setName(normalSponsorText).setLores(new String [] {
				"",
				"&6Give a player &c1 &6sponsor flare",
				"",
				"&6Items inside of sponsor: &c2",
				"",
				"&6Emerald Cost: &2" + cost,
				"",
				"&6Middle click to view possible items",
				""
			}).getItemStack());
			inventory.setItem(3, new ItemCreator(Material.EMERALD).setName(superSponsorText).setLores(new String [] {
				"",
				"&6Give a player &c1 &6super sponsor flare",
				"",
				"&6Items inside of sponsor: &c3",
				"",
				"&6Emerald Cost: &2" + cost * 2,
				"",
				"&6Middle click to view possible items",
				""
			}).getItemStack());
			inventory.setItem(5, new ItemCreator(Material.EMERALD).setName(normalSponsorAllText).setLores(new String [] {
				"",
				"&6Give all players &c1 &6sponsor flare",
				"",
				"&6Items inside of sponsors: &c2",
				"",
				"&6Emerald Cost: &20",
				"",
				"&6Rank required: " + Ranks.PRO_PLUS.getPrefix(),
				"",
				"&6Middle click to view possible items",
				""
			}).getItemStack());
			inventory.setItem(7, new ItemCreator(Material.EMERALD).setName(superSponsorAllText).setLores(new String [] {
				"",
				"&6Give all players &c1 &6super sponsor flare",
				"",
				"&6Items inside of sponsors: &c3",
				"",
				"&6Emerald Cost: &20",
				"",
				"&6Rank required: " + Ranks.ELITE.getPrefix(),
				"",
				"&6Middle click to view possible items",
				""
			}).getItemStack());
			player.openInventory(inventory);
			event.setCancelled(true);
		} else if(ItemUtil.isItem(player.getItemInHand(), particleSelector) && !delayed.contains(player.getName())) {
			final String playerName = player.getName();
			delayed.add(playerName);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(playerName);
				}
			}, 20 * delay);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(playerName);
					Inventory inventory = Particles.getParticlesMenu(player, name);
					for(int slot = 1; slot < inventory.getSize(); ++slot) {
						ItemStack item = inventory.getItem(slot);
						if(item == null) {
							continue;
						}
						ItemMeta meta = item.getItemMeta();
						if(meta == null || meta.getDisplayName() == null) {
							continue;
						}
						String name = item.getItemMeta().getDisplayName();
						String [] keys = new String [] {"uuid", "particle"};
						String [] values = new String [] {player.getUniqueId().toString(), ChatColor.stripColor(name)};
						if(!DB.PLAYERS_SG_PARTICLES_UNLOCKED.isKeySet(keys, values)) {
							inventory.setItem(slot, new ItemCreator(item).setType(Material.BEDROCK).addLore("&aFind these in chests!").getItemStack());
						}
					}
					player.openInventory(inventory);
				}
			});
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChestOpen(ChestOpenEvent event) {
		if(random.nextInt(200) == 1) {
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			ParticleTypes type = ParticleTypes.values()[random.nextInt(ParticleTypes.values().length)];
			final String particle = StringUtil.getFirstLetterCap(type.toString().replace("_", " "));
			MessageHandler.alert(AccountHandler.getPrefix(player, false) + " &6has found \"&b" + particle + "&6\" sponsor particles in a chest");
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "particle"};
					String [] values = new String [] {uuid.toString(), particle};
					if(!DB.PLAYERS_SG_PARTICLES_UNLOCKED.isKeySet(keys, values)) {
						DB.PLAYERS_SG_PARTICLES_UNLOCKED.insert("'" + uuid.toString() + "', '" + particle + "'");
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(itemPossibilities)) {
			event.setCancelled(true);
		} else if(event.getTitle().equals(sponsorItemInventoryName)) {
			String text = event.getItemTitle();
			if(event.getClickType() == ClickType.MIDDLE) {
				Inventory inventory = null;
				List<Material> items = null;
				if(text.equals(normalSponsorText) || text.equals(normalSponsorAllText)) {
					inventory = Bukkit.createInventory(player, ItemUtil.getInventorySize(possibleItems.size()), itemPossibilities);
					items = possibleItems;
				} else if(text.equals(superSponsorText) || text.equals(superSponsorAllText)) {
					inventory = Bukkit.createInventory(player, ItemUtil.getInventorySize(possibleSuperItems.size()), itemPossibilities);
					items = possibleSuperItems;
				}
				if(inventory != null) {
					for(Material material : items) {
						if(!inventory.contains(material)) {
							if(material == Material.GOLD_INGOT) {
								inventory.addItem(new ItemStack(material, 8));
							} else {
								inventory.addItem(new ItemStack(material));
							}
						}
					}
					player.openInventory(inventory);
				}
			} else {
				if(text.equals(normalSponsorText)) {
					player.openInventory(ItemUtil.getPlayerSelector(player, inventoryName));
				} else if(text.equals(superSponsorText)) {
					player.openInventory(ItemUtil.getPlayerSelector(player, superInventoryName));
				} else if(text.equals(normalSponsorAllText)) {
					if(Ranks.PRO_PLUS.hasRank(player, true)) {
						if(canSponsorAll) {
							canSponsorAll = false;
							for(Player online : ProPlugin.getPlayers()) {
								if(!sponsor(online, player, false, true)) {
									canSponsorAll = true;
									break;
								}
							}
							if(!canSponsorAll) {
								MessageHandler.sendMessage(player, "You sponsored every player!");
							}
						} else {
							MessageHandler.sendMessage(player, "&cThis feature can only be used once per game");
						}
						player.closeInventory();
					} else {
						MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
					}
				} else if(text.equals(superSponsorAllText)) {
					if(Ranks.ELITE.hasRank(player, true)) {
						if(canSponsorAll) {
							canSponsorAll = false;
							for(Player online : ProPlugin.getPlayers()) {
								if(!sponsor(online, player, true, true)) {
									canSponsorAll = true;
									break;
								}
							}
							if(!canSponsorAll) {
								MessageHandler.sendMessage(player, "You sponsored every player!");
							}
						} else {
							MessageHandler.sendMessage(player, "&cThis feature can only be used once per game");
						}
						player.closeInventory();
					} else {
						MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
					}
				}
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals(inventoryName) || event.getTitle().equals(superInventoryName)) {
			String targetName = event.getItemTitle();
			Player targetPlayer = ProPlugin.getPlayer(targetName);
			if(targetPlayer == null || SpectatorHandler.contains(targetPlayer)) {
				MessageHandler.sendMessage(player, "&c" + targetName + " is no longer playing");
			} else if(sponsor(targetPlayer, player, event.getTitle().equals(superInventoryName))){
				MessageHandler.sendMessage(player, "&eNote: &aYou can also sponsor with &c/sponsor <player name>");
			}
			player.closeInventory();
			event.setCancelled(true);
		} else if(event.getTitle().equals(name)) {
			if(event.getItem().getType() == Material.WATER_BUCKET) {
				spiralParticles.remove(player.getName());
				DB.PLAYERS_SG_PARTICLES_SELECTED.deleteUUID(player.getUniqueId());
				MessageHandler.sendMessage(player, "&cYou no longer have any sponsor spiral particles");
			} else if(event.getItem().getType() == Material.BEDROCK) {
				MessageHandler.sendMessage(player, "&cYou do not own this particle type! Find them in chests!");
			} else {
				String particle = event.getItemTitle();
				ParticleTypes type = ParticleTypes.valueOf(ChatColor.stripColor(particle).toUpperCase().replace(" ", "_"));
				spiralParticles.put(player.getName(), type);
				if(DB.PLAYERS_SG_PARTICLES_SELECTED.isUUIDSet(player.getUniqueId())) {
					DB.PLAYERS_SG_PARTICLES_SELECTED.updateString("particle", type.toString(), "uuid", player.getUniqueId().toString());
				} else {
					DB.PLAYERS_SG_PARTICLES_SELECTED.insert("'" + player.getUniqueId().toString() + "', '" + type.toString() + "'");
				}
				MessageHandler.sendMessage(player, "You selected " + particle);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	// Flare placing
	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(event.getItemInHand(), flareItem) || ItemUtil.isItem(event.getItemInHand(), superFlareItem)) {
			final Block block = event.getBlock();
			for(int a = 1; a <= requiredClearSpace; ++a) {
				if(block.getRelative(0, a, 0).getType() != Material.AIR) {
					MessageHandler.sendMessage(player, "&cYou must have a clear space of &e" + requiredClearSpace + " &cblocks above the flare");
					return;
				}
			}
			flares.add(block.getLocation());
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					flares.remove(block.getLocation());
					block.setType(Material.AIR);
					FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0, requiredClearSpace, 0), Material.PISTON_BASE, (byte) 6);
					if(ItemUtil.isItem(event.getItemInHand(), superFlareItem)) {
						fallingBlock.setTicksLived(5000);
					}
					fallingBlock.setDropItem(false);
					crates.put(fallingBlock, event.getPlayer().getName());
				}
			}, 20 * 5);
			event.setCancelled(false);
		}
	}

	// Crate landing
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(crates.containsKey(event.getEntity())) {
			AtomLocationEffect atomEffect = new AtomLocationEffect(manager, event.getEntity().getLocation());
			atomEffect.visibleRange = 10.0f;
			atomEffect.radiusNucleus = 0.0f;
			atomEffect.particleOrbital = ParticleEffect.FLAME;
			atomEffect.particleNucleus = ParticleEffect.FLAME;
			atomEffect.radius = 1;
			atomEffect.start();
			HelixLocationEffect helixEffect = new HelixLocationEffect(manager, event.getEntity().getLocation());
			helixEffect.visibleRange = 10.0f;
			helixEffect.radius = 5.0f;
			helixEffect.iterations = 1;
			helixEffect.start();
			effects.put(event.getBlock().getLocation(), atomEffect);
			blocks.put(event.getBlock().getLocation(), event.getEntity().getTicksLived() >= 5000);
			String name = crates.get(event.getEntity());
			if(spiralParticles.containsKey(name)) {
				spiralParticles.get(name).displaySpiral(event.getEntity().getLocation());
			}
			crates.remove(event.getEntity());
			event.setCancelled(false);
		}
	}
	
	// Opening the crate
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && blocks.containsKey(event.getClickedBlock().getLocation()) && !SpectatorHandler.contains(player)) {
			Bukkit.getPluginManager().callEvent(new SponsorOpenEvent(player));
			boolean superSponsor = blocks.get(event.getClickedBlock().getLocation());
			blocks.remove(event.getClickedBlock().getLocation());
			event.getClickedBlock().setType(Material.AIR);
			Random random = new Random();
			Inventory inventory = null;
			if(superSponsor) {
				inventory = Bukkit.createInventory(player, 9 * 3, superCrateInventoryName);
				for(int a = 0; a < 3; ++a) {
					Material material = possibleItems.get(random.nextInt(possibleItems.size()));
					if(material == Material.GOLD_INGOT) {
						inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(material, 8));
					} else if(material == Material.SKULL_ITEM) {
						inventory.setItem(random.nextInt(inventory.getSize()), ItemUtil.getSkull(player.getName()));
					} else {
						inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(material));
					}
				}
			} else {
				inventory = Bukkit.createInventory(player, 9 * 3, crateInventoryName);
				for(int a = 0; a < 2; ++a) {
					Material material = possibleItems.get(random.nextInt(possibleItems.size()));
					if(material == Material.GOLD_INGOT) {
						inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(material, 8));
					} else if(material == Material.SKULL_ITEM) {
						inventory.setItem(random.nextInt(inventory.getSize()), ItemUtil.getSkull(player.getName()));
					} else {
						inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(material));
					}
				}
			}
			effects.get(event.getClickedBlock().getLocation()).cancel();
			effects.remove(event.getClickedBlock().getLocation());
			player.openInventory(inventory);
			event.setCancelled(true);
		}
	}
	
	// Closing the crate
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		String title = event.getInventory().getTitle();
		if((title.equals(crateInventoryName) || title.equals(superCrateInventoryName)) && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			for(ItemStack item : event.getInventory().getContents()) {
				if(item != null && item.getType() != Material.AIR) {
					player.getWorld().dropItem(player.getLocation(), item);
				}
			}
		}
	}
}
