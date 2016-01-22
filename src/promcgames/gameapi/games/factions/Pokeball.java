package promcgames.gameapi.games.factions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

@SuppressWarnings("deprecation")
public class Pokeball implements Listener {
	private Item item = null;
	private String name = null;
	private List<Snowball> pokeballs = null;
	
	public Pokeball() {
		new CommandBase("pokeBall", 0, 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 2) {
					if(Ranks.OWNER.hasRank(sender)) {
						try {
							String name = arguments[0];
							int amount = Integer.valueOf(arguments[1]);
							UUID uuid = AccountHandler.getUUID(name);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
							} else if(DB.PLAYERS_FACTIONS_POKEBALLS.isUUIDSet(uuid)) {
								MessageHandler.sendMessage(sender, "Dispatched to give " + name + " " + amount + " Pokeballs");
								amount += DB.PLAYERS_FACTIONS_POKEBALLS.getInt("uuid", uuid.toString(), "amount");
								DB.PLAYERS_FACTIONS_POKEBALLS.updateInt("amount", amount, "uuid", uuid.toString());
							} else {
								MessageHandler.sendMessage(sender, "Dispatched to give " + name + " " + amount + " Pokeballs");
								DB.PLAYERS_FACTIONS_POKEBALLS.insert("'" + uuid.toString() + "', '" + amount + "'");
							}
						} catch(NumberFormatException e) {
							return false;
						}
						return true;
					}
				}
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "Pokeballs are blank spawn eggs that you can get from &b/vote");
				MessageHandler.sendMessage(sender, "You can use these to capture and spawn in friendly mobs");
				MessageHandler.sendMessage(sender, "Useful for moving mobs around!");
				MessageHandler.sendMessage(sender, "");
				return true;
			}
		};
		LivingEntity livingEntity = new NPCEntity(EntityType.COW, null, new Location(Bukkit.getWorlds().get(0), -263.5, 69, 292.5)) {
			@Override
			public void onInteract(Player player) {
				
			}
		}.getLivingEntity();
		livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
		item = livingEntity.getWorld().dropItem(livingEntity.getLocation(), new ItemStack(Material.MONSTER_EGG));
		livingEntity.setPassenger(item);
		Hologram hologram = HologramsAPI.createHologram(ProMcGames.getInstance(), livingEntity.getLocation().add(0, 2.5, 0));
		hologram.appendTextLine(StringUtil.color("&eCapture friendly mobs with &aPokeballs!"));
		hologram.appendTextLine(StringUtil.color("&eMore information: &a/pokeball"));
		name = ChatColor.WHITE + "Pokeball";
		pokeballs = new ArrayList<Snowball>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(item != null && item.equals(event.getItem())) {
			event.setCancelled(true);
		}
		/*ItemStack itemStack = event.getItem().getItemStack();
		if(itemStack.getType() == Material.MONSTER_EGG && itemStack.getData().getData() == 0) {
			event.setCancelled(true);
		}*/
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if(item != null && item.equals(event.getEntity()) && item.getFireTicks() < 9000) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		if(item != null) {
			item.setFireTicks(9001);
			item.remove();
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getClickType() == ClickType.RIGHT_CLICK) {
			Player player = event.getPlayer();
			ItemStack itemStack = player.getItemInHand();
			if(itemStack.getType() == Material.MONSTER_EGG && itemStack.getData().getData() == 0) {
				String name = itemStack.getItemMeta().getDisplayName();
				if(name != null) {
					if(name.equals(this.name)) {
						pokeballs.add(player.launchProjectile(Snowball.class));
						itemStack.setAmount(itemStack.getAmount() - 1);
						if(itemStack.getAmount() <= 0) {
							player.setItemInHand(new ItemStack(Material.AIR));
						}
					} else if(name.contains(this.name + " - ")) {
						EntityType type = EntityType.fromName(name.replace(this.name + " - ", ""));
						if(type != null) {
							player.getWorld().spawnEntity(player.getLocation(), type);
							itemStack.setAmount(itemStack.getAmount() - 1);
							if(itemStack.getAmount() <= 0) {
								player.setItemInHand(new ItemStack(Material.AIR));
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(!event.isCancelled()) {
			Entity entity = event.getEntity();
			if(entity instanceof LivingEntity && !(entity instanceof Monster) && !(entity instanceof Player) && event.getDamager() instanceof Snowball) {
				Snowball snowball = (Snowball) event.getDamager();
				if(pokeballs.contains(snowball) && snowball.getShooter() instanceof Player) {
					Player player = (Player) snowball.getShooter();
					pokeballs.remove(snowball);
					LivingEntity livingEntity = (LivingEntity) entity;
					if(livingEntity instanceof Tameable) {
						Tameable tameable = (Tameable) livingEntity;
						if(tameable.isTamed()) {
							MessageHandler.sendMessage(player, "&cCannot capture tamed animals");
							return;
						}
					}
					if(!NPCEntity.isNPC(livingEntity)) {
						if(new Random().nextInt(100) <= 20) {
							ItemStack itemStack = new ItemCreator(Material.MONSTER_EGG).setName(name + " - " + livingEntity.getType().toString()).getItemStack();
							livingEntity.getWorld().dropItem(livingEntity.getLocation(), itemStack);
							livingEntity.remove();
							MessageHandler.sendMessage(player, "Pokeball Captured " + livingEntity.getType().toString());
						} else {
							MessageHandler.sendMessage(player, "&cPokeball Failed!");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Snowball) {
			Snowball snowball = (Snowball) event.getEntity();
			pokeballs.remove(snowball);
		}
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(String uuidString : DB.PLAYERS_FACTIONS_POKEBALLS.getAllStrings("uuid")) {
					UUID uuid = UUID.fromString(uuidString);
					Player player = Bukkit.getPlayer(uuid);
					if(player != null) {
						int amount = DB.PLAYERS_FACTIONS_POKEBALLS.getInt("uuid", uuidString, "amount");
						boolean given = false;
						for(ItemStack item : player.getInventory().getContents()) {
							if(item == null || item.getType() == Material.AIR) {
								player.getInventory().addItem(new ItemCreator(Material.MONSTER_EGG).setAmount(amount).setName(name).getItemStack());
								given = true;
								break;
							}
						}
						if(given) {
							MessageHandler.sendMessage(player, "&e&lYou have been given &b&l" + amount + " &e&lPokeBalls!");
							DB.PLAYERS_FACTIONS_POKEBALLS.deleteUUID(uuid);
						} else {
							MessageHandler.sendMessage(player, "&c&lYou have Pokeballs waiting for you but you have no room in your inventory!");
						}
					}
				}
			}
		});
	}
}
