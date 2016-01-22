package promcgames.gameapi.games.versus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateEndEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.versus.kits.VersusKit;
import promcgames.gameapi.games.versus.tournament.TournamentQueueHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.trophies.VersusTrophies;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class LobbyHandler implements Listener {
	private Zombie kit = null;
	private static ItemStack kitSelector = null;
	private static ItemStack hotbarEditor = null;
	private static ItemStack battlesEnabled = null;
	private static ItemStack battlesDisabled = null;
	private static ItemStack playClans = null;
	private static ItemStack playKitPVP = null;
	private static List<String> disabledRequests = null;
	private static List<String> watching = null;
	
	public LobbyHandler() {
		World world = Bukkit.getWorlds().get(0);
		kitSelector = new ItemCreator(Material.ARROW).setName("&aKit Selector").getItemStack();
		hotbarEditor = new ItemCreator(Material.NAME_TAG).setName("&aHotbar Editor").getItemStack();
		battlesEnabled = new ItemCreator(Material.SLIME_BALL).setName("&aBattle Requests Enabled &7(Click to Toggle)").getItemStack();
		battlesDisabled = new ItemCreator(Material.MAGMA_CREAM).setName("&cBattle Requests Disabled &7(Click to Toggle)").getItemStack();
		playClans = new ItemCreator(Material.IRON_SWORD).setName("&aPlay &bSG/PVP Clans").getItemStack();
		playKitPVP = new ItemCreator(Material.STONE_SWORD).setName("&aPlay &bKit/Arena PVP").getItemStack();
		disabledRequests = new ArrayList<String>();
		watching = new ArrayList<String>();
		kit = (Zombie) new NPCEntity(EntityType.ZOMBIE, "&cRandom Kit Battle", new Location(world, 0.5, 5, 11.5, -180.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				if(!TournamentQueueHandler.getInstance().getQueue().contains(player.getName())) {
					openKitSelection(player);
				} else {
					MessageHandler.sendMessage(player, "&cYou can't 1v1 whilst in a tournament queue");
				}
			}
		}.getLivingEntity();
		new NPCEntity(EntityType.ZOMBIE, "&aSuggest a Kit", new Location(world, -7.5, 5, -7.5, -45.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				suggestAKit(player);
			}
		};
		EventUtil.register(this);
	}
	
	private void suggestAKit(Player player) {
		MessageHandler.sendLine(player);
		MessageHandler.sendMessage(player, "Tweet us a kit idea/request: &bhttps://twitter.com/ProMcGames");
		MessageHandler.sendMessage(player, "If we use your idea/request you'll get &e1 &afree Hub Sponsor");
		MessageHandler.sendLine(player);
	}
	
	public static Location spawn(Player player) {
		return spawn(player, true);
	}
	
	public static Location spawn(Player player, boolean giveItems) {
		final Location location = new Location(player.getWorld(), 0.5, 5, 0.5, -360.0f, 0.0f);
		Random random = new Random();
		int range = 5;
		location.setX(location.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		location.setY(location.getY() + 2.5d);
		location.setZ(location.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		player.teleport(location);
		if(!SpectatorHandler.contains(player)) {
			if(Ranks.PRO.hasRank(player)) {
				player.setAllowFlight(true);
			}
			if(giveItems) {
				giveItems(player);
			}
			player.setLevel(ScoreboardHandler.getWins(player));
		}
		return location;
	}
	
	public static void giveItems(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.updateInventory();
		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setItem(0, kitSelector);
		player.getInventory().setItem(1, hotbarEditor);
		if(disabledRequests.contains(player.getName())) {
			player.getInventory().setItem(2, battlesDisabled);
		} else {
			player.getInventory().setItem(2, battlesEnabled);
		}
		player.getInventory().setItem(6, VersusTrophies.getItem());
		player.getInventory().setItem(7, playClans);
		player.getInventory().setItem(8, playKitPVP);
	}
	
	public static boolean isInLobby(Player player) {
		return !BattleHandler.isInBattle(player);
	}
	
	public static Inventory getKitSelectorInventory(Player player, String name, boolean showUsers) {
		Inventory inventory = Bukkit.createInventory(player, 18, name);
		List<VersusKit> kits = VersusKit.getKits();
		for(int a = 0; a < kits.size(); ++a) {
			VersusKit kit = kits.get(a);
			if(showUsers) {
				inventory.setItem(a, new ItemCreator(kit.getIcon().clone()).setAmount(kit.getUsers()).getItemStack());
			} else {
				inventory.setItem(a, new ItemCreator(kit.getIcon().clone()).setAmount(1).getItemStack());
			}
		}
		return inventory;
	}
	
	public static void openKitSelection(Player player) {
		player.openInventory(getKitSelectorInventory(player, "Kit Selection", true));
		watching.add(player.getName());
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(isInLobby(player)) {
				if(event.getCause() == DamageCause.VOID) {
					spawn(player, false);
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		ItemStack chestplate = kit.getEquipment().getChestplate();
		if(chestplate.getType() == Material.AIR || chestplate.getType() == Material.DIAMOND_CHESTPLATE) {
			kit.getEquipment().setArmorContents(new ItemStack [] {
					new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
					new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)});
			kit.getEquipment().setItemInHand(new ItemStack(Material.WOOD_SWORD));
		} else if(chestplate.getType() == Material.LEATHER_CHESTPLATE) {
			kit.getEquipment().setArmorContents(new ItemStack [] {
					new ItemStack(Material.GOLD_BOOTS), new ItemStack(Material.GOLD_LEGGINGS),
			  		new ItemStack(Material.GOLD_CHESTPLATE), new ItemStack(Material.GOLD_HELMET)});
			kit.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
		} else if(chestplate.getType() == Material.GOLD_CHESTPLATE) {
			kit.getEquipment().setArmorContents(new ItemStack [] {
					new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS),
			  		new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)});
			kit.getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
		} else if(chestplate.getType() == Material.CHAINMAIL_CHESTPLATE) {
			kit.getEquipment().setArmorContents(new ItemStack [] {
					new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS),
			  		new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)});
			kit.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
		} else if(chestplate.getType() == Material.IRON_CHESTPLATE) {
			kit.getEquipment().setArmorContents(new ItemStack [] {
					new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS),
					new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_HELMET)});
			kit.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
		}
		for(String name : watching) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				InventoryView view = player.getOpenInventory();
				List<VersusKit> kits = VersusKit.getKits();
				for(int a = 0; a < kits.size(); ++a) {
					VersusKit kit = kits.get(a);
					ItemCreator creator = new ItemCreator(kit.getIcon().clone());
					creator.setAmount(kit.getUsers());
					if(creator.getAmount() % 2 != 0) {
						creator.addEnchantment(Enchantment.DURABILITY);
						creator.addLore("&bPlayer(s) waiting in queue");
						creator.addLore("&bClick to play");
						if(kit.getName().equals("UHC")) {
							creator.setData(3);
						}
					}
					view.setItem(a, creator.getItemStack());
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals("Kit Selection") && !PrivateBattleHandler.choosingMapType(event.getPlayer())) {
			Player player = event.getPlayer();
			player.getInventory().clear();
			VersusKit kit = VersusKit.getKit(event.getItem());
			if(kit == null) {
				MessageHandler.sendMessage(player, "&cAn error occured when selecting kit, please try again");
			} else {
				kit.give(player);
				QueueHandler.add(player, kit);
				EffectUtil.playSound(player, Sound.NOTE_PLING);
			}
			player.closeInventory();
			event.setCancelled(true);
		} else if(event.getTitle().equals("Request a Battle")) {
			Player player = event.getPlayer();
			final String name = event.getItem().getItemMeta().getDisplayName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						player.chat("/battle " + name);
					}
				}
			});
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(!Ranks.DEV.hasRank(player)) {
			Location to = event.getTo();
			if(player.isFlying() && to.getY() >= 23) {
				event.setTo(event.getFrom());
			}
			if(isInLobby(player) && !SpectatorHandler.contains(player)) {
				int x = to.getBlockX();
				int z = to.getBlockZ();
				if(x >= 35 || x <= -35 || z >= 35 || z <= -35) {
					event.setTo(event.getFrom());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(isInLobby(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isInLobby(player)) {
			ItemStack item = player.getItemInHand();
			if(item.equals(kitSelector)) {
				if(TournamentQueueHandler.getInstance().getQueue().contains(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou can't 1v1 people whilst in a tournament queue");
				} else {
					openKitSelection(player);
				}
			} else if(item.equals(hotbarEditor)) {
				HotbarEditor.open(player);
			} else if(item.equals(battlesEnabled)) {
				player.setItemInHand(battlesDisabled);
				if(!disabledRequests.contains(player.getName())) {
					disabledRequests.add(player.getName());
				}
			} else if(item.equals(battlesDisabled)) {
				player.setItemInHand(battlesEnabled);
				disabledRequests.remove(player.getName());
			} else if(item.equals(playClans)) {
				ProPlugin.sendPlayerToServer(player, "sghub");
			} else if(item.equals(playKitPVP)) {
				ProPlugin.sendPlayerToServer(player, "kitpvp1");
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		spawn(player);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(spawn(event.getPlayer()));
	}
	
	@EventHandler
	public void onPlayerSpectateEnd(PlayerSpectateEndEvent event) {
		final Player player = event.getPlayer();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				spawn(player);
			}
		});
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			EffectUtil.playSound(player, Sound.CHEST_OPEN);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		watching.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		Projectile projectile = (Projectile) event.getEntity();
		if(projectile.getShooter() instanceof Player) {
			Player player = (Player) projectile.getShooter();
			if(isInLobby(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
			ProPlugin.sendPlayerToServer(event.getPlayer(), "hub");
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		disabledRequests.remove(player.getName());
		watching.remove(player.getName());
		VersusKit.removePlayerKit(player);
	}
}
