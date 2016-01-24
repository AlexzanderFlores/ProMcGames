package promcgames.gameapi.games.factions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

public class MobSpawnerHandler implements Listener {
	public MobSpawnerHandler() {
		new CommandBase("giveSpawner", 2, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = arguments[0];
				Player player = ProPlugin.getPlayer(target);
				if(player == null) {
					MessageHandler.sendMessage(sender, "&c" + target + " is not online");
				} else {
					String type = StringUtil.getFirstLetterCap(arguments[1]);
					ItemStack spawner = new ItemCreator(Material.MOB_SPAWNER).setName(type + " Spawner").getItemStack();
					boolean gave = false;
					for(int a = 0; a < player.getInventory().getContents().length; ++a) {
						ItemStack item = player.getInventory().getItem(a);
						if(item == null || item.getType() == Material.AIR) {
							player.getInventory().addItem(spawner);
							gave = true;
							break;
						}
					}
					if(gave) {
						MessageHandler.sendMessage(sender, "You were given a &b" + type + " Spawner");
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have enough room in your inventory for this item");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled() && event.getBlock().getType() == Material.MOB_SPAWNER) {
			Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			if(item != null && item.getType() == Material.DIAMOND_PICKAXE && item.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
				if(Ranks.PRO.hasRank(player) || VIPHandler.isVIP(player)) {
					CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
					String type = spawner.getCreatureTypeName();
					player.getWorld().dropItem(event.getBlock().getLocation(), new ItemCreator(Material.MOB_SPAWNER).setName(type + " Spawner").getItemStack());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!event.isCancelled() && event.getBlock().getType() == Material.MOB_SPAWNER) {
			ItemStack item = event.getItemInHand();
			String name = item.getItemMeta().getDisplayName();
			if(name != null) {
				name = ChatColor.stripColor(name.replace(" Spawner", ""));
				CreatureSpawner block = (CreatureSpawner) event.getBlock().getState();
				block.setCreatureTypeByName(name.toUpperCase().replace(" ", "_"));
				block.update(true);
			}
		}
	}
}
