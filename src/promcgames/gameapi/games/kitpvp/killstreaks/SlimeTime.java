package promcgames.gameapi.games.kitpvp.killstreaks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.kitpvp.SpawnHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class SlimeTime extends Killstreak implements Listener {
	private static SlimeTime instance = null;
	private boolean slimeSpawned = false;
	private World world = null;
	
	public SlimeTime() {
		super(new ItemCreator(Material.SLIME_BALL).setName("Slime Time").getItemStack());
		instance = this;
		world = Bukkit.getWorlds().get(0);
		EventUtil.register(this);
	}
	
	public static SlimeTime getInstance() {
		if(instance == null) {
			new SlimeTime();
		}
		return instance;
	}
	
	@Override
	public void execute(Player player) {
		player.getInventory().addItem(new ItemCreator(Material.SLIME_BALL).setName(getName()).getItemStack());
		MessageHandler.alert(AccountHandler.getPrefix(player) + " &6opened \"&e" + getName() + "&6\" from the killstreak selector");
		MessageHandler.sendMessage(player, "&b" + getName() + ": &aGet a throwable slimeball that spawns an exploding slime");
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(slimeSpawned) {
			int slimesAlive = 0;
			for(Entity entity : world.getEntities()) {
				if(entity instanceof Slime) {
					++slimesAlive;
					Slime slime = (Slime) entity;
					int size = slime.getSize();
					if(++size >= 7) {
						world.createExplosion(slime.getLocation(), 4.0f);
						slime.remove();
						--slimesAlive;
					} else {
						slime.setSize(size);
					}
				}
			}
			if(slimesAlive <= 0) {
				slimeSpawned = false;
			}
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(event.getClickType() == ClickType.LEFT_CLICK) {
			Player player = event.getPlayer();
			if(!SpawnHandler.isAtSpawn(player) && player.getItemInHand().getType() == Material.SLIME_BALL) {
				slimeSpawned = true;
				player.setItemInHand(new ItemStack(Material.AIR));
				Slime slime = (Slime) world.spawnEntity(player.getLocation(), EntityType.SLIME);
				slime.setSize(3);
				slime.setVelocity(player.getLocation().getDirection().multiply(2.5));
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Slime) {
			event.setCancelled(true);
		}
	}
}
