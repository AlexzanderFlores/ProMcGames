package promcgames.gameapi.games.arcade.games.dragonraces;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProPlugin;
import promcgames.customevents.game.PlayerExpFillEvent_;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.gameapi.PlayerExpGainer;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class PerkHandler implements Listener {
	public enum Perks {
		FIRE_BALL("Fire Ball", new ItemStack(Material.FIREBALL, 3)) {
			@Override
			public void action(Player player) {
				ItemStack itemStack = player.getItemInHand();
				itemStack.setAmount(itemStack.getAmount() - 1);
				if(itemStack.getAmount() <= 0) {
					player.getInventory().clear();
					PlayerExpGainer.start(player);
				} else {
					player.setItemInHand(itemStack);
				}
				player.launchProjectile(Fireball.class, player.getLocation().getDirection().multiply(2.5d));
			}
		},
		
		SPEED_BOOST("Speed Boost", Material.FEATHER) {
			@Override
			public void action(Player player) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 0));
				player.getInventory().clear();
				PlayerExpGainer.start(player);
			}
		},
		
		TNT_BOMB("TNT Bomb", Material.TNT) {
			@Override
			public void action(Player player) {
				final Location playerLocation = player.getLocation();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Location location = playerLocation.clone();
						for(int a = 0; a < 3; ++a) {
							location = location.add(0, -2, 0);
							if(location.getBlock().getType() == Material.AIR) {
								location.getBlock().setType(Material.TNT);
							}
						}
					}
				}, 20);
				player.getInventory().clear();
				PlayerExpGainer.start(player);
			}
		};
		
		private String name = null;
		private ItemStack itemStack = null;
		
		private Perks(String name, Material material) {
			this(name, new ItemStack(material));
		}
		
		private Perks(String name, ItemStack itemStack) {
			this.name = ChatColor.GREEN + name;
			this.itemStack = new ItemCreator(itemStack).setName(this.name).getItemStack();
		}
		
		public String getName() {
			return this.name;
		}
		
		public ItemStack getItemStack() {
			return this.itemStack;
		}
		
		public abstract void action(Player player);
	}
	
	public PerkHandler() {
		EventUtil.register(this);
		for(Player player : ProPlugin.getPlayers()) {
			PlayerExpGainer.start(player);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getPlayer().getItemInHand().getType() != Material.AIR) {
			for(Perks perk: Perks.values()) {
				if(ItemUtil.isItem(event.getPlayer().getItemInHand(), perk.getItemStack())) {
					perk.action(event.getPlayer());
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerExpFill(PlayerExpFillEvent_ event) {
		event.getPlayer().getInventory().setItem(0, Perks.values()[new Random().nextInt(Perks.values().length)].getItemStack());
	}
}
