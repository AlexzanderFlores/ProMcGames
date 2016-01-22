package promcgames.server.servers.hub.items.cosmetic.pro;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.servers.hub.Parkour;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.ItemCreator;

public class BowTeleporter extends HubItemBase {
	private static HubItemBase instance = null;

	public BowTeleporter() {
		super(new ItemCreator(Material.BOW).setName(Ranks.PRO.getColor() + "Bow Teleporter").addEnchantment(Enchantment.ARROW_INFINITE).addEnchantment(Enchantment.DURABILITY, 100), 4);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		
	}

	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().getItemInHand().getType() == Material.BOW) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(Ranks.PRO.hasRank(player)) {
				event.setCancelled(false);
			} else {
				MessageHandler.sendMessage(player, Ranks.PRO.getNoPermission());
			}
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if(event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			if(arrow.getShooter() instanceof Player) {
				Player player = (Player) arrow.getShooter();
				if(!Parkour.isParkouring(player)) {
					Location location = arrow.getLocation();
					location.setYaw(player.getLocation().getYaw());
					location.setPitch(player.getLocation().getPitch());
					player.teleport(location);
				}
				arrow.remove();
			}
		}
	}
}
