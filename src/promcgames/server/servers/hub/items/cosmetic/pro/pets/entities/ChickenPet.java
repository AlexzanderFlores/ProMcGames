package promcgames.server.servers.hub.items.cosmetic.pro.pets.entities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.EntityChicken;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.util.UnsafeList;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.nms.PathfinderGoalWalkToOwner;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.EntityPet;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ReflectionUtil;

public class ChickenPet extends EntityChicken implements EntityPet {
	public static List<Chicken> flyingChickens = null;
	
	public ChickenPet(World world) {
		super(world);
		try {
			for(String fieldName : new String [] {"b", "c"}) {
				Field field = ReflectionUtil.getDeclaredField(PathfinderGoalSelector.class, fieldName);
				field.setAccessible(true);
				field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
				field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onSpawn(Player player) {
		
	}

	@Override
	public void walkTo(Player player, float speed) {
		this.goalSelector.a(0, new PathfinderGoalWalkToOwner(this, speed, player));
	}

	@Override
	public Inventory getOptionsInventory(Player player, Inventory inventory) {
		inventory.setItem(4, new ItemCreator(Material.FEATHER).setName("&eFly pet").getItemStack());
		inventory.setItem(5, new ItemCreator(Material.TNT).setName("&cRemove your pet").getItemStack());
		return inventory;
	}
	
	@Override
	public void clickedOnCustomOption(Player player, ItemStack clicked) {
		if(clicked.getType() == Material.FEATHER) {
			if(Ranks.ELITE.hasRank(player)) {
				if(flyingChickens == null) {
					flyingChickens = new ArrayList<Chicken>();
				}
				Chicken chicken = (Chicken) getBukkitEntity();
				if(flyingChickens.contains(chicken)) {
					flyingChickens.remove(chicken);
					chicken.eject();
				} else {
					flyingChickens.add(chicken);
					chicken.setPassenger(player);
				}
			} else {
				MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
			}
		}
	}

	@Override
	public void wornBy(Player player) {
		
	}

	@Override
	public Vector tossedBy(Player player) {
		return player.getLocation().getDirection();
	}

	@Override
	public void togglePetStaying(Player player) {
		
	}
	
	@Override
	public void togglePetSounds(Player player) {
		
	}

	@Override
	public void makeSound(Player player) {
		makeSound(super.t(), this.bf(), this.bg());
	}
	
	@Override
	public void makeHurtSound(Player player) {
		makeSound(super.aT(), this.bf(), this.bg());
	}

	@Override
	public void remove(Player player) {
		if(flyingChickens != null) {
			flyingChickens.remove(getBukkitEntity());
		}
	}
	
	@Override
	protected String t() {
		return null;
	}
	
	@Override
	protected String aT() {
		return null;
	}
	
	@Override
	protected String aU() {
		return null;
	}
	
	@Override
	protected void a(int i, int j, int k, Block block) {
		
	}
	
	@Override
	public void aD() {
		super.aD();
		this.getAttributeInstance(GenericAttributes.b).setValue(1000.0D);
	}
	
	@Override
	public void e(float f, float f1) {
		if(this.passenger != null && (this.passenger instanceof EntityLiving)) {
			this.lastYaw = (this.yaw = this.passenger.yaw);
			this.pitch = (this.passenger.pitch * 0.5F);
			b(this.yaw, this.pitch);
			this.aO = (this.aM = this.yaw);
			f = ((EntityLiving) this.passenger).bd * 0.5F;
			f1 = ((EntityLiving) this.passenger).be;
			if(f1 <= 0.0F) {
				f1 *= 0.25F;
			}
			this.W = 1.0F;
			this.aQ = (bl() * 0.1F);
			if(!this.world.isStatic) {
				i((float) getAttributeInstance(GenericAttributes.d).getValue());
				super.e(f, f1);
			}
			this.aE = this.aF;
			double d0 = this.locX - this.lastX;
			double d1 = this.locZ - this.lastZ;
			float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
			if(f4 > 1.0F) {
				f4 = 1.0F;
			}
			this.aF += (f4 - this.aF) * 0.4F;
			this.aG += this.aF;
		} else {
			this.W = 0.5F;
			this.aQ = 0.02F;
			super.e(f, f1);
		}
	}
}
