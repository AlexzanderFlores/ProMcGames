package promcgames.gameapi.games.skywars.cages.effects;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import promcgames.gameapi.games.skywars.cages.CageHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class BasicColor extends KitBase {
	private int data = 0;
	private String color = null;
	
	public BasicColor(int data, String colorCode, String color) {
		this(data, colorCode, color, getLastSlot() + 1);
	}
	
	public BasicColor(int data, String colorCode, String color, int slot) {
		super(new ItemCreator(Material.STAINED_GLASS, data).setName(colorCode + color + " &fCage").setLores(new String [] {
			"&eRequires " + Ranks.PRO.getPrefix() + "&eor above"
		}).getItemStack(), slot);
		this.data = data;
		this.color = color;
	}

	@Override
	public String getPermission() {
		return "sky_wars_effect_" + color.toLowerCase().replace(" ", "_");
	}
	
	@Override
	public void use(Player player) {
		if(Ranks.PRO.hasRank(player)) {
			super.use(player);
		} else {
			MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
		}
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		List<Block> blocks = CageHandler.getCage(player);
		for(Block block : blocks) {
			block.setType(Material.STAINED_GLASS);
			block.setData((byte) data);
		}
	}
}
