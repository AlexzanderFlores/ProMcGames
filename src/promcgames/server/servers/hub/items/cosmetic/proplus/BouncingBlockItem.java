package promcgames.server.servers.hub.items.cosmetic.proplus;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class BouncingBlockItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public BouncingBlockItem() {
		super(new ItemCreator(Material.EMERALD_BLOCK).setName(Ranks.PRO_PLUS.getColor() + "Bouncing Block"), 1);
		instance = this;
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
				if(player.getVehicle() == null) {
					FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 2, 0), Material.EMERALD_BLOCK, (byte) 0);
					fallingBlock.setPassenger(player);
					player.updateInventory();
					giveOriginalHotBar(player);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		FallingBlock fallingBlock = (FallingBlock) event.getEntity();
		if(fallingBlock.getBlockId() == Material.EMERALD_BLOCK.getId()) {
			if(fallingBlock.getPassenger() != null && fallingBlock.getPassenger() instanceof Player) {
				Player player = (Player) fallingBlock.getPassenger();
				Vector velocity = player.getLocation().getDirection();
				velocity.setY(0.75d);
				fallingBlock.remove();
				fallingBlock = player.getWorld().spawnFallingBlock(player.getLocation(), Material.EMERALD_BLOCK, (byte) 0);
				fallingBlock.setPassenger(player);
				fallingBlock.setVelocity(velocity);
				EffectUtil.playSound(player, Sound.SLIME_WALK);
			} else {
				fallingBlock.remove();
			}
		}
	}
}
