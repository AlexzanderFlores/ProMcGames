package promcgames.server.servers.hub.items.cosmetic.pro.pets;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader.Perk;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.StringUtil;

@SuppressWarnings("deprecation")
public class PetSelectorItem extends HubItemBase {
	private static HubItemBase instance;
	
	public enum PetTypes {
		COW(Ranks.PRO), PIG(Ranks.PRO), CHICKEN(Ranks.PRO), WOLF(Ranks.PRO), MUSHROOM_COW(Ranks.PRO), OCELOT(Ranks.PRO), SHEEP(Ranks.PRO), HORSE(Ranks.PRO),
		SLIME(Ranks.ELITE), MAGMA_CUBE(Ranks.ELITE), SQUID(Ranks.ELITE), SNOWMAN(Ranks.ELITE);
		
		private ItemStack itemStack = null;
		private Ranks requiredRank = Ranks.PRO;
		
		private PetTypes(Ranks rank) {
			this.requiredRank = rank;
			itemStack = new ItemStack(Material.MONSTER_EGG, 1, (byte) EntityType.valueOf(toString()).getTypeId());
		}
		
		public ItemStack getItemStack() {
			return new ItemCreator(itemStack.clone()).setName("&a" + StringUtil.getFirstLetterCap(toString())).getItemStack();
		}
		
		public boolean hasPet(Player player) {
			return requiredRank.hasRank(player);
		}
		
		public Ranks getRequiredRank() {
			return this.requiredRank;
		}
		
		public EntityType getEntityType() {
			return EntityType.valueOf(toString());
		}
	}
	
	public PetSelectorItem() {
		super(new ItemCreator(Material.BONE).setName(Ranks.PRO.getColor() + "Pet Selector"), 1);
		instance = this;
		for(Pets pet : Pets.values()) {
			pet.register();
		}
		new PetSpawningHandler();
		new PetEvents();
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.PRO.hasRank(player)) {
			PerkLoader.addPerkToQueue(player, Perk.PET);
		}
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO.hasRank(player)) {
				boolean openInventory = false;
				if(event.getClickType() == ClickType.RIGHT_CLICK) {
					openInventory = true;
				} else if(event.getClickType() == ClickType.LEFT_CLICK) {
					if(Pet.playersPets != null && Pet.playersPets.containsKey(Disguise.getName(player))) {
						Pet.playersPets.get(Disguise.getName(player)).getLivingEntity().teleport(player);
					} else {
						openInventory = true;
					}
				}
				if(openInventory) {
					int size = ItemUtil.getInventorySize(PetTypes.values().length + 1);
					Inventory inventory = Bukkit.createInventory(player, size, ChatColor.stripColor(getName()));
					for(PetTypes petType : PetTypes.values()) {
						inventory.addItem(petType.getItemStack());
					}
					player.openInventory(inventory);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getInventory().getTitle().equals(ChatColor.stripColor(getName())) && isItem(player)) {
			ItemStack item = event.getItem();
			for(PetTypes petType : PetTypes.values()) {
				if(petType.getItemStack().equals(item)) {
					if(petType.hasPet(player)) {
						PetSpawningHandler.spawn(player, petType.getEntityType());
						if(petType == PetTypes.SLIME || petType == PetTypes.MAGMA_CUBE) {
							MessageHandler.sendMessage(player, "&cShift &e+ &cRight click &eyour pet to change its size");
						}
						final String uuid = player.getUniqueId().toString();
						final String type = petType.getEntityType().toString();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								if(DB.HUB_PETS.isUUIDSet(UUID.fromString(uuid))) {
									DB.HUB_PETS.updateString("pet_type", type, "uuid", uuid);
								} else {
									DB.HUB_PETS.insert("'" + uuid + "', '" + type + "'");
								}
							}
						});
					} else {
						MessageHandler.sendMessage(player, "&cYou do not own this pet. You need " + petType.getRequiredRank().getPrefix() + "&b/buy");
					}
					break;
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
