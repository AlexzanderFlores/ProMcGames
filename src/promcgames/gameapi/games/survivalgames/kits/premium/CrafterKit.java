package promcgames.gameapi.games.survivalgames.kits.premium;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import promcgames.customevents.game.GameStartEvent;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class CrafterKit extends KitBase implements Listener {
	private static KitBase instance = null;
	private boolean used = false;
	
	public CrafterKit() {
		super(new ItemCreator(Material.WORKBENCH).setName("Crafter").setLores(new String[] {
			"&bPrice: 0",
			"",
			"&6Use of this kit will give all",
			"&6players access to /craft to open",
			"&6a crafting table anywhere",
			"",
			"&cPurchase with &e/buy"
		}).getItemStack(), 32, true);
		instance = this;
		EventUtil.register(this);
		new CommandBase("craft", true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(used) {
					Player player = (Player) sender;
					player.openWorkbench(null, true);
				} else {
					MessageHandler.sendMessage(sender, "&c\"" + getName() + "\" is not in use");
					MessageHandler.sendMessage(sender, "Use this kit any time: &chttp://store.promcgames.com/category/372707");
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
		return "survival_games.crafter";
	}

	@Override
	public void execute() {
		if(getPlayers().size() > 0) {
			used = true;
			MessageHandler.alert("&l&n" + getName() + "&e is in use due to 1 or more players using it");
			MessageHandler.alert("Everyone has access to /craft to open a crafting table");
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
	public void onGameStart(GameStartEvent event) {
		
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(used && event.getInventory().getType() == InventoryType.WORKBENCH && event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			MessageHandler.sendMessage(player, "You can open a crafting table with &e/craft");
			MessageHandler.sendMessage(player, "This is because the \"" + getName() + "\" kit is in use");
			MessageHandler.sendMessage(player, "Use this kit any time: &chttp://store.promcgames.com/category/372707");
		}
	}
}
