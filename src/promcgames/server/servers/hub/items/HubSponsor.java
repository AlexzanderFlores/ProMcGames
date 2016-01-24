package promcgames.server.servers.hub.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.gameapi.games.survivalgames.SponsorHandler;
import promcgames.player.MessageHandler;
import promcgames.player.VotingHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.AtomLocationEffect;
import de.slikey.effectlib.effect.HelixLocationEffect;
import de.slikey.effectlib.util.ParticleEffect;

@SuppressWarnings("deprecation")
public class HubSponsor extends HubItemBase {
	private static HubItemBase instance = null;
	private String title = null;
	private String resultTitle = null;
	private String noSponsorsTitle = null;
	private static String store = null;
	private List<String> delayed = null;
	private int delay = 5;
	private Map<String, Block> flares = null;
	private Map<FallingBlock, String> fallingChests = null;
	private Map<Block, String> chests = null;
	private Map<String, AtomLocationEffect> effects = null;
	private Map<String, Integer> counters = null;
	private List<String> placedPremium = null;
	private List<String> inChest = null;
	private List<String> placed = null;
	private static List<HubSponsorItems> items = null;
	private Random random = null;
	private EffectManager manager = null;
	private static int rarityCounter = 0;
	
	private enum HubSponsorItems {
		ELITE_RANK(true, Material.DIAMOND, Ranks.ELITE.getPrefix() + "Rank", "setRank % ELITE true", "$70"),
		PRO_PLUS_RANK(true, Material.DIAMOND, Ranks.PRO_PLUS.getPrefix() + "Rank", "setRank % PRO_PLUS true", "$40"),
		PRO_RANK(true, Material.DIAMOND, Ranks.PRO.getPrefix() + "Rank", "setRank % PRO true", "$20"),
		UNLIMITED_STAT_RESET(Material.NAME_TAG, "Unlimited Stat Reset", "statReset % true", "$7.50"),
		SINGLE_STAT_RESET(Material.NAME_TAG, "Single Stat Reset", "statReset % false", "$3"),
		RANK_TRASNFER(Material.DIAMOND, "Rank Transfer", "rankTransfer %", "$2.50"),
		PREMIUM_THREE_HUB_SPONSORS(Material.REDSTONE_TORCH_ON, "Premium Hub Sponsor x3", "addHubSponsors % 3 true", "$2"),
		PREMIUM_TWO_HUB_SPONSORS(Material.REDSTONE_TORCH_ON, "Premium Hub Sponsor x2", "addHubSponsors % 2 true", "$1.50"),
		PREMIUM_HUB_SPONSOR(Material.REDSTONE_TORCH_ON, "Premium Hub Sponsor", "addHubSponsors % 1 true", "$1"),
		EMERALDS_5000(Material.EMERALD, "5,000 Emeralds", "addEmeralds % 5000"),
		SPIRAL_MOVING_LIGHT_CAGE(Material.STAINED_GLASS, "Spiral Moving Light Sky Wars Cage", "addKit % sky_wars_effect_spiral_moving_light"),
		RAPIDLY_CHANGING_RANDOM_COLOR_CAGE(Material.STAINED_GLASS, "Rapidly Changing Random Color Sky Wars Cage", "addKit % sky_wars_effect_rapidly_changing_random_color"),
		BOTTOM_UP_CHANGING_COLOR_CAGE(Material.STAINED_GLASS, "Bottom Up Changing Color Sky Wars Cage", "addKit % sky_wars_effect_bottom_up_changing_color"),
		VERTICLE_STROBE_PATTERN_CAGE(Material.STAINED_GLASS, "Verticle Strobe Pattern Sky Wars Cage", "addKit % sky_wars_effect_vertical_strobe_pattern"),
		HORIZONTAL_STROBE_PATTERN_CAGE(Material.STAINED_GLASS, "Horizontal Strobe Pattern Sky Wars Cage", "addKit % sky_wars_effect_horizontal_strobe_pattern"),
		FREE_PARKOUR_CHECKPOINTS_15(Material.NAME_TAG, "15 Free Parkour Checkpoints", "addFreeCheckPoints % 15"),
		KIT_PVP_AUTO_REGEN_50(Material.GOLDEN_APPLE, "Kit PVP Auto Regen Passes x50", "addKitPVPAutoRegenPass % 50"),
		SG_AUTO_SPONSORS_25(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x25", "addSGAutoSponsors % 25"),
		SG_KIT_ENCHANTER(Material.ENCHANTMENT_TABLE, "Enchanter SG Kit", "addKit % survival_games.enchanter", "$7.50"),
		SG_KIT_CRAFTER(Material.WORKBENCH, "Crafter SG Kit", "addKit % survival_games.crafter", "$5"),
		SG_KIT_RESURRECTION(Material.GOLD_NUGGET, "Resurrection SG Kit", "addKit % survival_games.resurrection", "$5"),
		SG_KIT_PAIN_KILLER(Material.POTION, "Pain Killer SG Kit", "addKit % survival_games.pain_killer", "$5"),
		SG_KIT_TRACKER(Material.COMPASS, "Tracker SG Kit", "addKit % survival_games.tracker", "$5"),
		TOP_ROW_MOVING_LIGHT_CAGE(Material.STAINED_GLASS, "Top Row Moving Light Sky Wars Cage", "addKit % sky_wars_effect_top_row_moving_light"),
		FREE_PARKOUR_CHECKPOINTS_10(Material.NAME_TAG, "10 Free Parkour Checkpoints", "addFreeCheckPoints % 10"),
		KIT_PVP_AUTO_REGEN_40(Material.GOLDEN_APPLE, "Kit PVP Auto Regen Passes x40", "addKitPVPAutoRegenPass % 40"),
		SG_AUTO_SPONSORS_20(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x20", "addSGAutoSponsors % 20"),
		SG_KIT_RESTOCK(Material.CHEST, "Restock SG Kit", "addKit % survival_games.restock"),
		SG_KIT_TELEPORTER(Material.ENDER_PEARL, "Teleporter SG Kit", "addKit % survival_games.teleporter"),
		SG_KIT_SWORDSMAN(Material.STONE_SWORD, "Swordsman SG Kit", "addKit % survival_games.swordsman"),
		SG_KIT_ARCHER(Material.BOW, "Archer SG Kit", "addKit % survival_games.archer"),
		EMERALDS_1000(Material.EMERALD, "1,000 Emeralds", "addEmeralds % 1000"),
		FREE_PARKOUR_CHECKPOINTS_5(Material.NAME_TAG, "5 Free Parkour Checkpoints", "addFreeCheckPoints % 5"),
		KIT_PVP_AUTO_REGEN_30(Material.GOLDEN_APPLE, "Kit PVP Auto Regen Passes x30", "addKitPVPAutoRegenPass % 30"),
		SG_AUTO_SPONSORS_15(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x15", "addSGAutoSponsors % 15"),
		KIT_PVP_AUTO_REGEN_15(Material.GOLDEN_APPLE, "Kit PVP Auto Regen Passes x15", "addKitPVPAutoRegenPass % 15"),
		SG_AUTO_SPONSORS_10(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x10", "addSGAutoSponsors % 10"),
		FREE_PARKOUR_CHECKPOINTS_3(Material.NAME_TAG, "3 Free Parkour Checkpoints", "addFreeCheckPoints % 3"),
		EMERALDS_500(Material.EMERALD, "500 Emeralds", "addEmeralds % 500"),
		EMERALDS_250(Material.EMERALD, "250 Emeralds", "addEmeralds % 250"),
		EMERALDS_100(Material.EMERALD, "100 Emeralds", "addEmeralds % 100"),
		KIT_PVP_AUTO_REGEN_5(Material.GOLDEN_APPLE, "Kit PVP Auto Regen Passes x5", "addKitPVPAutoRegenPass % 5"),
		SG_AUTO_SPONSORS_5(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x5", "addSGAutoSponsors % 5"),
		SG_AUTO_SPONSORS_1(Material.REDSTONE_TORCH_ON, "SG Auto Sponsor Passes x1", "addSGAutoSponsors % 1"),
		EMERALDS_50(Material.EMERALD, "50 Emeralds", "addEmeralds % 50"),
		FREE_PARKOUR_CHECKPOINT_1(Material.NAME_TAG, "1 Free Parkour Checkpoint", "addFreeCheckPoints % 1");
		
		private ItemStack itemStack = null;
		private int rarity = 0;
		private String command = null;
		private String value = null;
		private boolean premium = false;
		
		private HubSponsorItems(Material material, String name, String command) {
			this(false, material, name, command, null);
		}
		
		private HubSponsorItems(Material material, String name, String command, String value) {
			this(false, new ItemCreator(material).setName("&b" + name).getItemStack(), command, value);
		}
		
		private HubSponsorItems(ItemStack itemStack, String command, String value) {
			this(false, itemStack, command, value);
		}
		
		private HubSponsorItems(boolean premium, Material material, String name, String command) {
			this(premium, material, name, command, null);
		}
		
		private HubSponsorItems(boolean premium, Material material, String name, String command, String value) {
			this(premium, new ItemCreator(material).setName(name).getItemStack(), command, value);
		}
		
		private HubSponsorItems(boolean premium, ItemStack itemStack, String command, String value) {
			this.premium = premium;
			this.itemStack = itemStack;
			this.command = command;
			this.rarity = ++rarityCounter;
			for(int a = 0; a < rarity; ++a) {
				items.add(this);
			}
			this.value = value;
		}
		
		public ItemStack getItemStack() {
			return this.itemStack;
		}
		
		public String getName() {
			return getItemStack().getItemMeta().getDisplayName();
		}
		
		public String getValue() {
			return this.value;
		}
		
		public boolean isPremiumItem() {
			return this.premium;
		}
		
		public void execute(Player player) {
			MessageHandler.sendLine(player);
			for(int a = 0; a < 3; ++a) {
				MessageHandler.sendMessage(player, "&c&lNote that you cannot exchange, refund, or transfer this item in any way");
			}
			MessageHandler.sendLine(player);
			String valueString = (getValue() == null ? "" : " &eValue: &b" + getValue());
			final UUID uuid = player.getUniqueId();
			final String name = getName();
			if(rarity <= 3) {
				MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened an ultra rare &e" + name + valueString);
			} else if(rarity <= 5) {
				MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened a rare &e" + name + valueString);
			} else {
				MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened &e" + name + valueString);
			}
			MessageHandler.sendMessage(player, "&eChance: &b" + rarity + "&7/&b" + items.size());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%", player.getName()));
			MessageHandler.sendMessage(player, "&6&lYou can get Hub Sponsors for just &b&l$0.99!");
			MessageHandler.sendMessage(player, "&c&l" + store);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					DB.HUB_SPONSOR_LOGS.insert("'" + uuid.toString() + "', '" + name + "'");
				}
			});
		}
	}
	
	public HubSponsor() {
		super(new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&aHub Sponsor &7(Left Click for info &8| &7Place for use)"), 7);
		instance = this;
		title = "Hub Sponsor - Click an Item!";
		resultTitle = "Hub Sponsor - Results!";
		noSponsorsTitle = "You Have 0 Hub Sponsors!";
		store = "store.promcgames.com/category/480788";
		delayed = new ArrayList<String>();
		flares = new HashMap<String, Block>();
		fallingChests = new HashMap<FallingBlock, String>();
		chests = new HashMap<Block, String>();
		effects = new HashMap<String, AtomLocationEffect>();
		counters = new HashMap<String, Integer>();
		placedPremium = new ArrayList<String>();
		inChest = new ArrayList<String>();
		placed = new ArrayList<String>();
		items = new ArrayList<HubSponsorItems>();
		random = new Random();
		manager = new EffectManager(ProMcGames.getInstance());
		new CommandBase("viewRewards", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String name = arguments[0];
						UUID uuid = AccountHandler.getUUID(name);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + name + " has never logged into the server");
						} else if(DB.HUB_SPONSOR_LOGS.isUUIDSet(uuid)) {
							MessageHandler.sendMessage(sender, name + "'s rewards:");
							int counter = 1;
							for(String reward : DB.HUB_SPONSOR_LOGS.getAllStrings("reward", "uuid", uuid.toString())) {
								MessageHandler.sendMessage(sender, "&c" + counter++ + ". &e" + reward);
							}
						} else {
							MessageHandler.sendMessage(sender, name + " has never gotten a Hub Sponsor reward");
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.DEV).enableDelay(2);
		HubSponsorItems.values();
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}
	
	@Override
	public void onMouseClick(MouseClickEvent event) {
		
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(title)) {
			if(event.getSlot() >= 10 && event.getSlot() < 17) {
				HubSponsorItems item = null;
				do {
					item = items.get(new Random().nextInt(items.size()));
				} while(item.isPremiumItem() && !placedPremium.contains(player.getName()));
				item.execute(player);
				EffectUtil.launchFirework(player.getLocation());
				add(player.getUniqueId(), -1, placedPremium.contains(player.getName()));
				openGetSponsorsInventory(player, resultTitle, item.getItemStack());
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals(resultTitle) || event.getTitle().equals(noSponsorsTitle)) {
			ItemStack item = event.getItem();
			if(item.getType().equals(Material.STAINED_GLASS)) {
				if(item.getData().getData() == 4) {
					MessageHandler.sendMessage(player, "&cBuy Sponsors: &e" + store);
					player.closeInventory();
				} else if(item.getData().getData() == 5) {
					VotingHandler.display(player);
					player.closeInventory();
				}
			}
			event.setCancelled(true);
		} else if(event.getTitle().equals("Select Sponsor Type")) {
			player.closeInventory();
			event.setCancelled(true);
			UUID uuid = player.getUniqueId();
			if(event.getItemTitle().contains("Premium")) {
				if(DB.PLAYERS_HUB_PREMIUM_SPONSORS.getInt("uuid", uuid.toString(), "amount") > 0) {
					placedPremium.add(player.getName());
				} else {
					openGetSponsorsInventory(player, noSponsorsTitle);
					return;
				}
			} else {
				if(DB.PLAYERS_HUB_SPONSORS.getInt("uuid", uuid.toString(), "amount") <= 0) {
					openGetSponsorsInventory(player, noSponsorsTitle);
					return;
				}
			}
			final String name = player.getName();
			placed.add(name);
			flares.get(name).setType(Material.REDSTONE_TORCH_ON);
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, delay * 20);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					Block block = flares.get(name);
					if(player != null) {
						FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0, SponsorHandler.requiredClearSpace, 0), Material.CHEST, (byte) 0);
						fallingBlock.setDropItem(false);
						fallingChests.put(fallingBlock, player.getName());
					}
					block.setType(Material.AIR);
					flares.remove(name);
				}
			}, 20 * 5);
		}
	}
	
	public static void add(final UUID uuid, final int value, final boolean premium) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				DB db = premium ? DB.PLAYERS_HUB_PREMIUM_SPONSORS : DB.PLAYERS_HUB_SPONSORS;
				int amount = db.getInt("uuid", uuid.toString(), "amount") + value;
				if(db.isUUIDSet(uuid)) {
					if(amount <= 0) {
						db.delete("uuid", uuid.toString());
					} else {
						db.updateInt("amount", amount, "uuid", uuid.toString());
					}
				} else if(amount > 0) {
					db.insert("'" + uuid.toString() + "', '" + amount + "'");
				}
				Player player = Bukkit.getPlayer(uuid);
				if(player != null) {
					MessageHandler.sendMessage(player, "You now have &e" + amount + " &aHub Sponsors");
				}
			}
		});
	}
	
	private void openGetSponsorsInventory(Player player, String title) {
		openGetSponsorsInventory(player, title, null);
	}
	
	private void openGetSponsorsInventory(Player player, String  title, ItemStack result) {
		Inventory inventory = Bukkit.createInventory(player, 9, title);
		inventory.setItem(1, new ItemCreator(new ItemStack(Material.STAINED_GLASS, 1, (byte) 4)).setName("&aOpen Buycraft").getItemStack());
		if(result != null) {
			inventory.setItem(4, new ItemCreator(result).addEnchantment(Enchantment.DURABILITY).getItemStack());
		}
		inventory.setItem(7, new ItemCreator(new ItemStack(Material.STAINED_GLASS, 1, (byte) 5)).setName("&aVote for a Hub Sponsor").getItemStack());
		player.openInventory(inventory);
	}
	
	private void remove(Player player) {
		remove(player, null);
	}
	
	private void remove(Player player, Block block) {
		if(block == null) {
			for(Block chest : chests.keySet()) {
				if(chests.get(chest).equals(player.getName())) {
					block = chest;
					break;
				}
			}
		}
		if(block != null && block.getType() != Material.AIR) {
			MessageHandler.sendMessage(player, "&cHub Sponsor removed: &eOne minute expiration time reached");
			MessageHandler.sendMessage(player, "&cYou will be given back &e1 &cHub Sponsor");
			block.setType(Material.AIR);
		}
		Iterator<FallingBlock> iterator = fallingChests.keySet().iterator();
		while(iterator.hasNext()) {
			FallingBlock fallingBlock = iterator.next();
			if(fallingChests.get(fallingBlock).equals(player.getName())) {
				fallingBlock.remove();
				iterator.remove();
				break;
			}
		}
		chests.remove(player.getName());
		placed.remove(player.getName());
		inChest.remove(player.getName());
		if(effects.containsKey(player.getName())) {
			effects.get(player.getName()).cancel();
			effects.remove(player.getName());
		}
		counters.remove(player.getName());
		placedPremium.remove(player.getName());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(placed.contains(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou can only place &e1 &cHub Sponsor at a time");
				} else {
					Block block = event.getClickedBlock().getRelative(0, 1, 0);
					int x = block.getX();
					int z = block.getZ();
					int x2 = block.getWorld().getSpawnLocation().getBlockX();
					int z2 = block.getWorld().getSpawnLocation().getBlockZ();
					if(Math.sqrt((x - x2) * (x - x2) + (z - z2) * (z - z2)) <= 20) {
						MessageHandler.sendMessage(player, "&cCannot place here: &eToo close to spawn");
					} else {
						boolean canPlace = true;
						for(int a = 1; a <= SponsorHandler.requiredClearSpace; ++a) {
							if(block.getRelative(0, a, 0).getType() != Material.AIR) {
								MessageHandler.sendMessage(player, "&cYou must have a clear space of &e" + SponsorHandler.requiredClearSpace + " &cblocks above the flare");
								canPlace = false;
								break;
							}
						}
						if(canPlace) {
							for(Entity entity : player.getWorld().getEntities()) {
								if(entity instanceof FallingBlock) {
									FallingBlock fallingBlock = (FallingBlock) entity;
									if(fallingBlock.getMaterial() == Material.CHEST) {
										int blockX = fallingBlock.getLocation().getBlockX();
										int blockZ = fallingBlock.getLocation().getBlockZ();
										if(Math.sqrt((blockX - x) * (blockX - x) + (blockZ - z) * (blockZ - z)) <= 3) {
											canPlace = false;
											MessageHandler.sendMessage(player, "&cCannot place here: &eCrate falling near this location");
											break;
										}
									}
								}
							}
							if(canPlace) {
								if(delayed.contains(player.getName())) {
									MessageHandler.sendMessage(player, "&cYou can only place these once every &e" + delay + " &cseconds");
								} else {
									flares.put(player.getName(), block);
									Inventory inventory = Bukkit.createInventory(player, 9, "Select Sponsor Type");
									inventory.setItem(3, new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&aVoting Sponsor").setLores(new String [] {
										"",
										"&aCan get all normal perks",
										"&aaside from ranks",
										"&aYou CAN get premium sponsors",
										"",
										"&eGet these by voting &b/vote",
										"&eOr with the &bGift Giver NPC&e!"
									}).getItemStack());
									inventory.setItem(5, new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&aPremium Sponsor").setAmount(2).setLores(new String [] {
										"",
										"&aCan get all normal perks",
										"&aINCLUDING LIFETIME RANKS!",
										"",
										Ranks.ELITE.getPrefix() + "&e$70 value!",
										Ranks.PRO_PLUS.getPrefix() + "&e$40 value!",
										Ranks.PRO.getPrefix() + "&e$20 value!",
										"",
										"&eGet these for as low as &b$0.99&e!",
										"&eRun command &b/buy"
									}).getItemStack());
									player.openInventory(inventory);
								}
							}
						}
					}
				}
				player.updateInventory();
			} else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				MessageHandler.sendLine(player, "&c");
				MessageHandler.sendMessage(player, "Hub Sponsors will give you a random cool perk!");
				MessageHandler.sendMessage(player, "You can get \"&eVoting Sponsors&a\" by voting: &b/vote");
				MessageHandler.sendMessage(player, "You can get \"&ePremium Sponsors&a\" through buycraft: &b/buy");
				MessageHandler.sendMessage(player, "There is a chance you can get a cool rank such as:");
				MessageHandler.sendMessage(player, Ranks.ELITE.getPrefix() + "&e: $70 value");
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getPrefix() + "&e: $40 value");
				MessageHandler.sendMessage(player, Ranks.PRO.getPrefix() + "&e: $20 value");
				MessageHandler.sendMessage(player, "&eNote: &aYou can only get ranks in \"&ePremium Sponsors&a\"");
				MessageHandler.sendLine(player, "&c");
			}
			event.setCancelled(true);
		} else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
			Block block = event.getClickedBlock();
			if(chests.containsKey(block)) {
				if(chests.get(block).equals(player.getName())) {
					block.setType(Material.AIR);
					player.openInventory(Bukkit.createInventory(player, 9 * 3, title));
					inChest.add(player.getName());
					placed.remove(player.getName());
					effects.get(player.getName()).cancel();
					effects.remove(player.getName());
				} else {
					openGetSponsorsInventory(player, noSponsorsTitle);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(event.getEntity() instanceof FallingBlock) {
			FallingBlock fallingBlock = (FallingBlock) event.getEntity();
			if(fallingBlock.getMaterial() == Material.CHEST && fallingChests.containsKey(fallingBlock)) {
				Player player = ProPlugin.getPlayer(fallingChests.get(fallingBlock));
				if(player != null) {
					final String name = player.getName();
					final Block block = event.getBlock();
					AtomLocationEffect atomEffect = new AtomLocationEffect(manager, event.getEntity().getLocation());
					atomEffect.visibleRange = 10.0f;
					atomEffect.radiusNucleus = 0.0f;
					atomEffect.particleOrbital = ParticleEffect.HAPPY_VILLAGER;
					atomEffect.particleNucleus = ParticleEffect.HAPPY_VILLAGER;
					atomEffect.radius = 1;
					atomEffect.iterations = 10 * 60;
					atomEffect.start();
					HelixLocationEffect helixEffect = new HelixLocationEffect(manager, event.getEntity().getLocation());
					helixEffect.visibleRange = 10.0f;
					helixEffect.radius = 5.0f;
					helixEffect.iterations = 1;
					helixEffect.start();
					effects.put(player.getName(), atomEffect);
					chests.put(block, name);
					MessageHandler.sendMessage(player, "&cYour Hub Sponsor has landed");
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = ProPlugin.getPlayer(name);
							if(player != null && block.getType() != Material.AIR && chests.get(block).equals(name)) {
								remove(player, block);
							}
						}
					}, 20 * 60);
					event.setCancelled(false);
				}
			}
		}
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		Iterator<String> iterator = inChest.iterator();
		while(iterator.hasNext()) {
			String name = iterator.next();
			Player player = ProPlugin.getPlayer(name);
			if(player == null) {
				iterator.remove();
			} else {
				int counter = 0;
				if(counters.containsKey(player.getName())) {
					counter = counters.get(player.getName());
				}
				counters.put(player.getName(), counter);
				InventoryView inventory = player.getOpenInventory();
				for(int a = 0; a < inventory.getTopInventory().getSize(); ++a) {
					if(a >= 10 && a < 17) {
						inventory.setItem(a, items.get(random.nextInt(items.size())).getItemStack());
					} else {
						inventory.setItem(a, new ItemCreator(Material.STAINED_GLASS_PANE, random.nextInt(15)).setName(" ").getItemStack());
					}
				}
				EffectUtil.playSound(player, Sound.NOTE_PIANO);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(inChest.contains(event.getPlayer().getName())) {
				counters.remove(player.getName());
				inChest.remove(player.getName());
			} else if(placed.contains(player.getName())) {
				placed.remove(player.getName());
				flares.get(player.getName()).setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
