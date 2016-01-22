package promcgames.server.servers.hub.items.cosmetic.pro.pets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerStaffModeEvent;
import promcgames.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.Particles;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.Parkour;
import promcgames.server.servers.hub.SnowballFight;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.ChickenPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SheepPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SquidPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.WolfPet;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class PetEvents implements Listener {
	private List<Entity> eggs = null;
	
	public PetEvents() {
		EventUtil.register(this);
	}
	
	// Makes squids fly and eggs fall from riding a chicken
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		if(SquidPet.flyingSquids != null) {
			for(Squid squid : SquidPet.flyingSquids) {
				if(squid.getPassenger() != null && squid.getPassenger() instanceof Player) {
					Player player = (Player) squid.getPassenger();
					squid.setVelocity(player.getLocation().getDirection());
					EffectUtil.playEffect(Effect.MOBSPAWNER_FLAMES, squid.getLocation());
				}
			}
		}
		if(ChickenPet.flyingChickens != null) {
			Iterator<Chicken> iterator = ChickenPet.flyingChickens.iterator();
			while(iterator.hasNext()) {
				Chicken chicken = iterator.next();
				if(chicken.getPassenger() != null && chicken.getPassenger() instanceof Player) {
					Player player = (Player) chicken.getPassenger();
					chicken.setVelocity(player.getLocation().getDirection());
					player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.EGG));
				} else {
					iterator.remove();
				}
			}
		}
		if(eggs != null) {
			for(Entity egg : eggs) {
				if(egg.getTicksLived() >= 20) {
					egg.remove();
				}
			}
		}
	}
	
	// Rainbow effects for sheep and wolfs
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(SheepPet.rainbowSheep != null) {
			for(Sheep sheep : SheepPet.rainbowSheep) {
				sheep.setColor(DyeColor.values()[new Random().nextInt(DyeColor.values().length)]);
			}
		}
		if(WolfPet.rainbowWolfs != null) {
			for(Wolf wolf : WolfPet.rainbowWolfs) {
				wolf.setCollarColor(DyeColor.values()[new Random().nextInt(DyeColor.values().length)]);
			}
		}
	}
	
	// Displays fire particles for skeleton horses
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		for(Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
			if(entity instanceof Horse) {
				Horse horse = (Horse) entity;
				if(horse.getVariant() == Variant.SKELETON_HORSE) {
					Particles.ParticleTypes.FLAME.display(entity.getLocation());
				}
			}
		}
	}
	
	// Makes pets follow their owners
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		if(Pet.playersPets != null) {
			for(String name : Pet.playersPets.keySet()) {
				Pet pet = Pet.playersPets.get(name);
				pet.walkTo(ProPlugin.getPlayer(name), 0.0f);
			}
		}
	}
	
	// Allow eggs to spawn for chickens
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(event.getEntity().getItemStack().getType() == Material.EGG) {
			if(eggs == null) {
				eggs = new ArrayList<Entity>();
			}
			eggs.add(event.getEntity());
			event.setCancelled(false);
		}
	}
	
	// Remove eggs from memory upon despawn
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if(event.getEntity().getItemStack().getType() == Material.EGG) {
			Egg egg = (Egg) event.getEntity();
			if(eggs != null && eggs.contains(egg)) {
				eggs.remove(egg);
			}
		}
	}
	
	// Disable air damage for squids
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Squid) {
			event.setCancelled(true);
		}
	}
	
	// Open options inventory
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			if(event.getDamager() instanceof Player && Pet.playersPets != null && !NPCEntity.isNPC(livingEntity)) {
				Player player = (Player) event.getDamager();
				if(Pet.playersPets.containsKey(player.getName())) {
					Pet pet = Pet.playersPets.get(player.getName());
					if(pet.isEntity(event.getEntity())) {
						if(Parkour.isParkouring(player)) {
							MessageHandler.sendMessage(player, "&cCannot open pet options while parkouring!");
						} else if(SnowballFight.isPlaying(player)) {
							MessageHandler.sendMessage(player, "&cYou cannot interact with pets while in the snowball fight");
						} else {
							Inventory inventory = pet.getOptionsInventory(player, null);
							if(inventory != null) {
								player.openInventory(inventory);
							}
							event.setCancelled(true);
						}
					}
				} else if(Ranks.PRO.hasRank(player)) {
					MessageHandler.sendMessage(player, "&cCannot interact with a pet that you do not own");
				} else {
					MessageHandler.sendMessage(player, "&cTo use pets you must be " + Ranks.PRO.getPrefix() + "&b/buy");
				}
			}
		}
	}
	
	// Open options inventory
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if(Pet.playersPets != null && event.getRightClicked() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getRightClicked();
			if(!(event.getRightClicked() instanceof Player) && !NPCEntity.isNPC(livingEntity)) {
				if(Pet.playersPets.containsKey(player.getName())) {
					Pet pet = Pet.playersPets.get(player.getName());
					if(pet.isEntity(event.getRightClicked())) {
						if(Parkour.isParkouring(player)) {
							MessageHandler.sendMessage(player, "&cCannot open pet options while parkouring!");
						} else if(SnowballFight.isPlaying(player)) {
							MessageHandler.sendMessage(player, "&cYou cannot interact with pets while in the snowball fight");
						} else {
							LivingEntity entity = (LivingEntity) pet.getLivingEntity();
							if(player.isSneaking() && event.getRightClicked() == entity && Pet.playersPets.get(player.getName()).getLivingEntity() instanceof Slime) {
								if(Ranks.ELITE.hasRank(player)) {
									if(event.getRightClicked() instanceof Slime) {
										Slime slime = (Slime) entity;
										int newSize = slime.getSize() + 1;
										if (newSize > 5) {
											slime.setSize(1);
										} else {
											slime.setSize(slime.getSize() + 1);
										}
									}
								} else {
									MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
								}
							} else {
								Inventory inventory = pet.getOptionsInventory(player, null);
								if(inventory != null) {
									player.openInventory(inventory);
								}
								event.setCancelled(true);
							}
						}
					}
				} else if(Ranks.PRO.hasRank(player)) {
					MessageHandler.sendMessage(player, "&cCannot interact with a pet that you do not own");
				} else {
					MessageHandler.sendMessage(player, "&cTo use pets you must be " + Ranks.PRO.getPrefix() + "&b/buy");
				}
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	// Worn by owner, toggle pet staying, toggle baby/adult, toggle pet sounds, remove pet, and custom options
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals(ChatColor.GOLD + player.getName() + "'s Pet") && Pet.playersPets != null && Pet.playersPets.containsKey(player.getName())) {
			Pet pet = Pet.playersPets.get(player.getName());
			String clicked = event.getItem().getItemMeta().getDisplayName();
			if(clicked.equals(ChatColor.YELLOW + "Wear your pet")) {
				pet.wornBy(player);
			} else if(clicked.equals(ChatColor.YELLOW + "Toggle pet staying")) {
				pet.togglePetStaying(player);
			} else if(clicked.equals(ChatColor.YELLOW + "Toggle pet sounds")) {
				pet.togglePetSounds(player);
			} else if(clicked.equals(ChatColor.YELLOW + "Toggle to Adult")) {
				if(Ranks.ELITE.hasRank(player)) {
					Ageable ageable = (Ageable) pet.getLivingEntity();
					ageable.setAdult();
				} else {
					MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
				}
			} else if(clicked.equals(ChatColor.YELLOW + "Toggle to Baby")) {
				if(Ranks.ELITE.hasRank(player)) {
					Ageable ageable = (Ageable) pet.getLivingEntity();
					ageable.setBaby();
				} else {
					MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
				}
			} else if(clicked.equals(ChatColor.YELLOW + "Ride pet")) {
				if(Ranks.ELITE.hasRank(player)) {
					pet.getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
					pet.getLivingEntity().setPassenger(player);
				} else {
					MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
				}
			} else if(clicked.equals(ChatColor.RED + "Remove your pet")) {
				pet.remove(player);
				if(DB.HUB_PETS.isUUIDSet(player.getUniqueId())) {
					DB.HUB_PETS.deleteUUID(player.getUniqueId());
				}
			} else {
				pet.clickedOnCustomOption(player, event.getItem());
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	// Toss pet
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Entity passenger = player.getPassenger();
		if(Pet.playersPets != null && Pet.playersPets.containsKey(player.getName()) && passenger != null && passenger instanceof LivingEntity && event.getAction() == Action.LEFT_CLICK_AIR) {
			Pet pet = Pet.playersPets.get(player.getName());
			if(pet.isEntity(passenger)) {
				pet.tossedBy(player);
			}
		}
	}
	
	// Remove pet
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		if(event.getAFK() && Pet.playersPets != null && Pet.playersPets.containsKey(event.getPlayer().getName())) {
			Pet.playersPets.get(event.getPlayer().getName()).remove(event.getPlayer());
		}
	}
	
	// Remove pet
	@EventHandler
	public void onPlayerStaffMode(PlayerStaffModeEvent event) {
		if(event.getType() == StaffModeEventType.ENABLE && Pet.playersPets != null && Pet.playersPets.containsKey(event.getPlayer().getName())) {
			Pet.playersPets.get(event.getPlayer().getName()).remove(event.getPlayer());
		}
	}
	
	// Remove pet
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(Pet.playersPets != null && Pet.playersPets.containsKey(player.getName())) {
			Pet.playersPets.get(player.getName()).remove(player);
		}
	}
}
