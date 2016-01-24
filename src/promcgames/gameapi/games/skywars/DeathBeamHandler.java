package promcgames.gameapi.games.skywars;

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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.ChestOpenEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.Loading;

@SuppressWarnings("deprecation")
public class DeathBeamHandler implements Listener {
	private static Map<String, List<Block>> playerBlocks = null;
	private static Map<String, Byte> colors = null;
	private List<String> delayed = null;
	private static List<BeamColor> beamColors = null;
	private String name = null;
	private ItemStack item = null;
	private Random random = null;
	
	public class BeamColor {
		private int data = 0;
		private int slot = 0;
		private String color = null;
		private String name = null;
		
		public BeamColor(int data, int slot, String color, String name) {
			this.data = data;
			this.slot = slot;
			this.color = color;
			this.name = name;
			if(beamColors == null) {
				beamColors = new ArrayList<BeamColor>();
			}
			beamColors.add(this);
		}
		
		public int getData() {
			return this.data;
		}
		
		public int getSlot() {
			return this.slot;
		}
		
		public String getColor() {
			return this.color;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	public DeathBeamHandler() {
		colors = new HashMap<String, Byte>();
		delayed = new ArrayList<String>();
		name = "Death Beam Color Selector";
		item = new ItemCreator(Material.BEACON).setName("&b" + name).getItemStack();
		random = new Random();
		new BeamColor(15, 0, "&0", "Black");
		new BeamColor(11, 1, "&1", "Dark Blue");
		new BeamColor(13, 2, "&2", "Dark Green");
		new BeamColor(3, 3, "&3", "Dark Aqua");
		new BeamColor(14, 4, "&4", "Dark Red");
		new BeamColor(10, 5, "&5", "Dark Purple");
		new BeamColor(1, 6, "&6", "Gold");
		new BeamColor(8, 7, "&7", "Gray");
		new BeamColor(7, 8, "&8", "Dark Gray");
		new BeamColor(9, 10, "&9", "Blue");
		new BeamColor(5, 11, "&a", "Lime Green");
		new BeamColor(3, 12, "&b", "Aqua");
		new BeamColor(6, 13, "&c", "Red");
		new BeamColor(2, 14, "&d", "Purple");
		new BeamColor(4, 15, "&e", "Yellow");
		new BeamColor(0, 16, "&f", "White");
		EventUtil.register(this);
	}
	
	public static void spawn(Player player, byte data) {
		final String name = player.getName();
		if(playerBlocks == null) {
			playerBlocks = new HashMap<String, List<Block>>();
		}
		List<Block> blocks = new ArrayList<Block>();
		Location location = player.getLocation();
		location.setY(0);
		Block block = location.getBlock();
		World world = block.getWorld();
		Block min = block.getRelative(-1, 0, -1);
		Block max = block.getRelative(1, 0, 1);
		for(int x1 = min.getX(); x1 <= max.getX(); ++x1) {
			for(int z1 = min.getZ(); z1 <= max.getZ(); ++z1) {
				Block near = world.getBlockAt(x1, 0, z1);
				near.setType(Material.IRON_BLOCK);
				blocks.add(near);
			}
		}
		block = block.getRelative(0, 1, 0);
		block.setType(Material.BEACON);
		blocks.add(block);
		block = block.getRelative(0, 1, 0);
		block.setType(Material.STAINED_GLASS);
		block.setData(data);
		blocks.add(block);
		playerBlocks.put(name, blocks);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Block> blocks = playerBlocks.get(name);
				for(Block block : blocks) {
					block.setType(Material.AIR);
				}
				blocks.clear();
				blocks = null;
				playerBlocks.remove(name);
			}
		}, 20 * 10);
	}
	
	public static void loadBeams() {
		new Loading("Death Beams");
		for(Player player : ProPlugin.getPlayers()) {
			if(DB.PLAYERS_SKY_WARS_BEAMS_SELECTED.isUUIDSet(player.getUniqueId())) {
				String selected = DB.PLAYERS_SKY_WARS_BEAMS_SELECTED.getString("uuid", player.getUniqueId().toString(), "beam");
				BeamColor color = null;
				for(BeamColor colors : beamColors) {
					if(colors.getName().equals(selected)) {
						color = colors;
						break;
					}
				}
				if(color != null) {
					colors.put(player.getName(), (byte) color.getData());
					MessageHandler.sendMessage(player, "Selecting " + color.getName());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		GameStates state = ProMcGames.getMiniGame().getGameState();
		if(state == GameStates.WAITING || state == GameStates.VOTING) {
			event.getPlayer().getInventory().addItem(item);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), item)) {
			if(!delayed.contains(player.getName())) {
				final String playerName = player.getName();
				delayed.add(playerName);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(playerName);
					}
				}, 20 * 2);
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(playerName);
						if(player != null) {
							Inventory inventory = Bukkit.createInventory(player, 9 * 2, name);
							String [] keys = new String [] {"uuid", "beam"};
							for(BeamColor color : beamColors) {
								String [] values = new String [] {player.getUniqueId().toString(), color.getName()};
								Material material = Material.BEDROCK;
								if(DB.PLAYERS_SKY_WARS_BEAMS_UNLOCKED.isKeySet(keys, values)) {
									material = Material.STAINED_GLASS;
								}
								inventory.setItem(color.getSlot(), new ItemCreator(material, color.getData())
									.setName(color.getColor() + color.getName() + " &fDeath Beam").setLores(new String [] {
										"&aIf you fall into the void a",
										"&abeacon will be spawned with the",
										"&acolor selected for 10 seconds.",
										"",
										"&eYou can find these randomly",
										"&ein chests."
									}).getItemStack());
							}
							player.openInventory(inventory);
						}
					}
				});
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.BEDROCK) {
				MessageHandler.sendMessage(player, "&cYou do not own that death beam!");
				MessageHandler.sendMessage(player, "Find death beams randomly in chests");
			} else {
				colors.put(player.getName(), item.getData().getData());
				final UUID uuid = player.getUniqueId();
				final String beam = ChatColor.stripColor(item.getItemMeta().getDisplayName().split(" " + ChatColor.WHITE)[0]);
				MessageHandler.sendMessage(player, "Selected \"&e" + beam + "&a\"");
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.PLAYERS_SKY_WARS_BEAMS_SELECTED.isUUIDSet(uuid)) {
							DB.PLAYERS_SKY_WARS_BEAMS_SELECTED.updateString("beam", beam, "uuid", uuid.toString());
						} else {
							DB.PLAYERS_SKY_WARS_BEAMS_SELECTED.insert("'" + uuid.toString() + "', '" + beam + "'");
						}
					}
				});
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onChestOpen(ChestOpenEvent event) {
		if(random.nextInt(200) == 1) {
			Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			BeamColor color = beamColors.get(random.nextInt(beamColors.size()));
			final String name = color.getName();
			MessageHandler.alert(AccountHandler.getPrefix(player) + " &6has found the \"&b" + name + "&6\" death beam in a chest");
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "beam"};
					String [] values = new String [] {uuid.toString(), name};
					if(!DB.PLAYERS_SKY_WARS_BEAMS_UNLOCKED.isKeySet(keys, values)) {
						DB.PLAYERS_SKY_WARS_BEAMS_UNLOCKED.insert("'" + uuid.toString() + "', '" + name + "'");
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(colors.containsKey(player.getName())) {
				spawn(player, colors.get(player.getName()));
				colors.remove(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		colors.remove(event.getPlayer().getName());
	}
}
