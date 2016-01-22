package promcgames.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Shop implements Listener {
	private List<String> purchasedGoldenApple = null;
	private String name = null;
	
	public Shop(double x, double y, double z) {
		purchasedGoldenApple = new ArrayList<String>();
		name = "Shop";
		new NPCEntity(EntityType.ZOMBIE, "&6" + name, new Location(Bukkit.getWorlds().get(0), x, y, z)) {
			@Override
			public void onInteract(Player player) {
				open(player);
			}
		};
		EventUtil.register(this);
	}
	
	private void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, name);
		inventory.setItem(2, new ItemCreator(Material.GOLDEN_APPLE).setName("&aGolden Apple").setLores(new String [] {
			"",
			"&bPrice: 20",
			"",
			"&6Max of &b1 &6per life"
		}).getItemStack());
		inventory.setItem(4, new ItemCreator(Material.ARROW).setAmount(16).setName("&aArrows x16").setLores(new String [] {
			"",
			"&bPrice: 15",
			"",
			"&6Only useful with the &bArcher &6kit"
		}).getItemStack());
		inventory.setItem(6, new ItemCreator(Material.EXP_BOTTLE).setName("&a+1 Level").setLores(new String [] {
			"",
			"&bPrice: 10",
			"",
			"&6Useful for the killstreak selector",
			"&7(Click the enchantment table)"
		}).getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			Material type = event.getItem().getType();
			int price = Integer.valueOf(event.getItem().getItemMeta().getLore().get(1).split(": ")[1]);
			if(EmeraldsHandler.getEmeralds(player) >= price) {
				if(type == Material.GOLDEN_APPLE) {
					if(purchasedGoldenApple.contains(player.getName())) {
						MessageHandler.sendMessage(player, "&cYou can only purchase one of those per life");
						EffectUtil.playSound(player, Sound.CAT_HISS);
					} else {
						purchasedGoldenApple.add(player.getName());
						player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
						EffectUtil.playSound(player, Sound.EAT);
						EmeraldsHandler.addEmeralds(player, price * -1, EmeraldReason.KIT_PURCHASE, false);
					}
					player.closeInventory();
				} else if(type == Material.ARROW) {
					player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
					EffectUtil.playSound(player, Sound.ARROW_HIT);
					open(player);
					EmeraldsHandler.addEmeralds(player, price * -1, EmeraldReason.KIT_PURCHASE, false);
				} else if(type == Material.EXP_BOTTLE) {
					player.setLevel(player.getLevel() + 1);
					EffectUtil.playSound(player, Sound.LEVEL_UP);
					open(player);
					EmeraldsHandler.addEmeralds(player, price * -1, EmeraldReason.KIT_PURCHASE, false);
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do now have enough Emeralds for this");
				EffectUtil.playSound(player, Sound.CAT_HISS);
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		purchasedGoldenApple.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		purchasedGoldenApple.remove(event.getPlayer().getName());
	}
}
