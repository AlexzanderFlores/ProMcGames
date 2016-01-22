package promcgames.gameapi.games.survivalgames.kits.premium;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class EnchanterKit extends KitBase implements Listener {
	private static KitBase instance = null;
	private boolean used = false;
	
	public EnchanterKit() {
		super(new ItemCreator(Material.ENCHANTMENT_TABLE).setName("&aEnchanter").setLores(new String[] {
			"&bPrice: 0",
			"",
			"&6Use of this kit will give all",
			"&6players access to /enchant to open",
			"&6an enchanting table anywhere",
			"",
			"&cPurchase with &e/buy"
		}).getItemStack(), 33, true);
		instance = this;
		EventUtil.register(this);
		new CommandBase("enchant", true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(used) {
					Player player = (Player) sender;
					player.openEnchanting(null, true);
				} else {
					MessageHandler.sendMessage(sender, "&e\"" + getName() + "\" is not in use");
					MessageHandler.sendMessage(sender, "Use this kit any time: &ehttp://store.promcgames.com/category/372707");
				}
				return true;
			}
		};
	}
	
	@Override
	public void use(Player player) {
		if(Ranks.ELITE.hasRank(player, true)) {
			super.use(player);
		} else {
			String [] keys = new String [] {"uuid", "kit"};
			String [] values = new String [] {Disguise.getUUID(player).toString(), getPermission()};
			if(DB.PLAYERS_KITS.isKeySet(keys, values)) {
				super.use(player);
			} else {
				MessageHandler.sendMessage(player, "&cYou do not own this kit! You can purchase it here:");
				MessageHandler.sendMessage(player, "http://store.promcgames.com/category/372707");
			}
		}
	}
	
	@Override
	public String getPermission() {
		return "survival_games.enchanter";
	}

	@Override
	public void execute() {
		if(getPlayers().size() > 0) {
			used = true;
			MessageHandler.alert("&l&n" + getName() + "&a is in use due to 1 or more players using it");
			MessageHandler.alert("Everyone has access to /enchant to open an enchanting table");
			MessageHandler.alert("Use this kit any time: &chttp://store.promcgames.com/category/372707");
		}
	}

	@Override
	public void execute(Player player) {
		
	}
	
	public static KitBase getInstance() {
		return instance;
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(used && event.getInventory().getType() == InventoryType.ENCHANTING && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			MessageHandler.sendMessage(player, "You can open an enchanting table with &e/enchant");
			MessageHandler.sendMessage(player, "This is because the \"" + getName() + "\" kit is in use");
			MessageHandler.sendMessage(player, "Use this kit any time: &chttp://store.promcgames.com/category/372707");
		}
	}
}
