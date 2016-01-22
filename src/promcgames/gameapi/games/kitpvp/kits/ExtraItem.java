package promcgames.gameapi.games.kitpvp.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.kits.KitBase;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

public class ExtraItem extends KitBase {
	public ExtraItem(Material icon, int slot) {
		this(new ItemStack(icon), slot);
	}
	
	public ExtraItem(ItemStack icon, int slot) {
		super(icon, slot);
	}
	
	@Override
	public void use(Player player, boolean defaultKit) {
		int price = getPrice();
		if(EmeraldsHandler.getEmeralds(player) >= price) {
			EmeraldsHandler.addEmeralds(player, price * -1, EmeraldReason.KIT_PURCHASE, false);
			MessageHandler.sendMessage(player, "Purchased extra \"&e" + getName() + "&a\"");
			execute(player);
		} else {
			MessageHandler.sendMessage(player, "&cYou do not have enough emeralds for this extra");
		}
	}

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		String name = StringUtil.getFirstLetterCap(getIcon().getType().toString());
		ItemStack item = new ItemCreator(getIcon()).setName("&b" + name).setLores(new String [] {}).getItemStack();
		player.getInventory().addItem(item);
	}
}
