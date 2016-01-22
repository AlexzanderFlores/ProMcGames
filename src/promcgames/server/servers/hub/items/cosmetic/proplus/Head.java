package promcgames.server.servers.hub.items.cosmetic.proplus;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class Head extends HubItemBase {
	private static HubItemBase instance = null;
	
	public Head() {
		super(new ItemCreator(Material.SKULL_ITEM, 3).setName(Ranks.PRO_PLUS.getColor() + "Head Selector"), 5);
		instance = this;
		new CommandBase("head", 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = arguments[0];
				Player player = (Player) sender;
				if(target.equalsIgnoreCase("off")) {
					player.getInventory().setHelmet(new ItemStack(Material.AIR));
				} else {
					CraftPlayer craftPlayer = (CraftPlayer) player;
					if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() != 47) {
						MessageHandler.sendMessage(player, "&cNote: &eThis feature works better on 1.8 clients!");
					}
					ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).getItemStack();
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setOwner(target);
					item.setItemMeta(meta);
					player.getInventory().setHelmet(item);
					MessageHandler.sendMessage(player, "&cTo remove your head do &f/head off");
				}
				return true;
			}
		}.setRequiredRank(Ranks.PRO_PLUS).enableDelay(2);
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(Ranks.PRO_PLUS.hasRank(player)) {
				MessageHandler.sendMessage(player, "Run command &f/head <player name>");
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
}
