package promcgames.gameapi.games.factions.spawn.crates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.factions.CoinHandler;
import promcgames.gameapi.games.factions.spawn.SpawnHandler;
import promcgames.gameapi.games.factions.spawn.SpawnHandler.WorldLocation;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler;
import promcgames.server.CustomEntityFirework;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public abstract class CrateBase implements Listener {
	protected static boolean anyBeingUsed = false;
	private static final int delay = 5;
	private List<String> delayed = null;
	private List<ItemStack> items = null;
	protected DB table = null;
	protected Location location = null;
	protected Hologram hologram = null;
	protected String user = null;
	private String message = null;
	protected boolean delayUse = false;
	private boolean voting = false;
	private int rarity = 0;
	private double counter = 0;
	private double maxTime = 7.5;
	protected NPCEntity npc = null;
	
	public CrateBase(DB table, Location location, String message) {
		this(table, location, message, false);
	}
	
	public CrateBase(DB table, Location location, String message, boolean voting) {
		delayed = new ArrayList<String>();
		this.table = table;
		this.location = location;
		hologram = HologramsAPI.createHologram(ProMcGames.getInstance(), location.clone().add(0.5, 2, 0.5));
		this.message = message;
		this.voting = voting;
		EventUtil.register(this);
	}
	
	protected void addItem(ItemStack item) {
		if(items == null) {
			items = new ArrayList<ItemStack>();
		}
		++rarity;
		for(int a = 0; a < rarity; ++a) {
			items.add(item);
		}
	}
	
	protected void moveHologramUp() {
		hologram.teleport(hologram.getLocation().add(0, 2, 0));
	}
	
	protected void moveHologramDown() {
		hologram.teleport(hologram.getLocation().add(0, -2, 0));
	}
	
	protected void selectItem() {
		Player player = ProPlugin.getPlayer(user);
		if(player != null) {
			if(npc != null && npc.getLivingEntity().getPassenger() != null) {
				boolean given = false;
				ItemStack item = items.get(new Random().nextInt(items.size()));
				String type = null;
				for(ItemStack content : player.getInventory().getContents()) {
					if(content == null || content.getType() == Material.AIR) {
						if(item.getType() == Material.GOLD_INGOT) {
							CoinHandler.addCoins(player, item.getAmount());
							type = item.getAmount() + " Coins";
						} else if(item.getType() == Material.EXP_BOTTLE && item.getAmount() == 450) {
							player.setLevel(player.getLevel() + 45);
							type = "45 Levels";
						} else if(item.getType() == Material.EXP_BOTTLE && item.getAmount() == 300) {
							player.setLevel(player.getLevel() + 30);
							type = "30 Levels";
						} else if(item.getType() == Material.EXP_BOTTLE && item.getAmount() == 150) {
							player.setLevel(player.getLevel() + 15);
							type = "15 Levels";
						} else {
							player.getInventory().addItem(item);
							if(item.getType() == Material.MONSTER_EGG) {
								type = "CREEPER EGG" + " x" + item.getAmount();
							} else if(item.getType() == Material.SULPHUR) {
								type = "GUN POWDER" + " x" + item.getAmount();
							} else {
								type = item.getType().toString().replace("_", " ") + " x" + item.getAmount();
							}
						}
						given = true;
						break;
					}
				}
				if(given) {
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							int amount = table.getInt("uuid", uuid.toString(), "amount") - 1;
							table.updateInt("amount", amount, "uuid", uuid.toString());
						}
					});
					alert(player, type);
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have enough room in your inventory for this item");
					MessageHandler.sendMessage(player, "You did &cNOT &alose your key. Please make room and try again");
				}
			}
			ParticleTypes.FLAME.displaySpiral(npc.getLivingEntity().getLocation());
			EffectUtil.playSound(player, Sound.ENDERDRAGON_WINGS);
		}
		moveHologramDown();
		user = null;
		counter = 0;
		if(npc != null) {
			if(npc.getLivingEntity().getPassenger() != null) {
				npc.getLivingEntity().getPassenger().remove();
			}
			npc.remove();
			npc = null;
		}
		items.clear();
		items = null;
		anyBeingUsed = false;
	}
	
	protected void interact(Player player) {
		user = player.getName();
		if(npc != null) {
			npc.remove();
		}
		npc = new NPCEntity(EntityType.SILVERFISH, null, location.clone().add(0.5, 1, 0.5)) {
			@Override
			public void onInteract(Player player) {
				
			}
		};
		npc.setSpawnZombie(false);
		LivingEntity livingEntity = (LivingEntity) npc.getLivingEntity();
		livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
		moveHologramUp();
		anyBeingUsed = true;
	}
	
	protected void alert(Player player, String item) {
		if(voting) {
			MessageHandler.sendMessage(player, "&6&lOpened &b&l" + item + "&6&l!");
		} else {
			MessageHandler.alert(AccountHandler.getPrefix(player) + " &6&lopened &b&l" + item + "&6&l! Want a key? &b&l/buy");
		}
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(npc != null && counter <= maxTime) {
			if(items != null) {
				ItemStack itemStack = items.get(new Random().nextInt(items.size()));
				LivingEntity livingEntity = (LivingEntity) npc.getLivingEntity();
				if(livingEntity.getPassenger() == null) {
					Item item = livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), itemStack);
					livingEntity.setPassenger(item);
				} else {
					Item current = (Item) livingEntity.getPassenger();
					current.setItemStack(itemStack);
				}
				counter += .25;
			}
		} else if(counter > maxTime) {
			selectItem();
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(user != null && npc != null) {
			List<Player> players = new ArrayList<Player>();
			for(Entity near : npc.getLivingEntity().getNearbyEntities(5, 5, 5)) {
				if(near instanceof Player) {
					Player player = (Player) near;
					players.add(player);
				}
			}
			CustomEntityFirework.spawn(location.clone().add(0.5, 0.5, 0.5), FireworkEffect.builder().with(Type.BALL).withColor(Color.RED).withColor(Color.BLUE).withColor(Color.WHITE).build(), players);
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		if(item.getVehicle() != null && item.getVehicle() instanceof Silverfish && SpawnHandler.getSpawnLevel(item.getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if(block != null) {
			Location location = block.getLocation();
			if(location.equals(this.location)) {
				Player player = event.getPlayer();
				if(anyBeingUsed) {
					MessageHandler.sendMessage(player, "&cA crate is being used currently");
					MessageHandler.sendMessage(player, "&cTo prevent lag only one can be used at a time");
				} else if(user == null) {
					if(delayed.contains(player.getName())) {
						MessageHandler.sendMessage(player, "&cYou can only use this once every &e" + delay + " &cseconds");
					} else {
						final String name = player.getName();
						delayed.add(name);
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								delayed.remove(name);
							}
						}, 20 * delay);
						int amount = table.getInt("uuid", player.getUniqueId().toString(), "amount");
						if(amount > 0) {
							user = name;
							interact(player);
						} else {
							MessageHandler.sendMessage(player, message);
						}
					}
				} else {
					MessageHandler.sendMessage(player, "&cThis crate is already being used by " + user);
				}
				event.setCancelled(true);
			}
		}
	}
}
