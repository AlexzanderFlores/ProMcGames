package promcgames.server.servers.hub.items.cosmetic.pro.pets.entities;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityOcelot;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.util.UnsafeList;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
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
import promcgames.server.util.StringUtil;

public class OcelotPet extends EntityOcelot implements EntityPet {
	public OcelotPet(World world) {
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
		Inventory newInventory = Bukkit.createInventory(player, 9 * 2, inventory.getTitle());
		for(ItemStack itemStack : inventory.getContents()) {
			if(itemStack != null && itemStack.getType() != Material.AIR) {
				newInventory.addItem(itemStack);
			}
		}
		int counter = 9;
		for(Type type : Type.values()) {
			newInventory.setItem(counter++, new ItemCreator(Material.RAW_FISH).setName("&a" + StringUtil.getFirstLetterCap(type.toString()) + " type").getItemStack());
		}
		return newInventory;
	}
	
	@Override
	public void clickedOnCustomOption(Player player, ItemStack clicked) {
		if(Ranks.PRO_PLUS.hasRank(player)) {
			Ocelot ocelot = (Ocelot) getBukkitEntity();
			ocelot.setCatType(Type.valueOf(ChatColor.stripColor(clicked.getItemMeta().getDisplayName().split(" type")[0].toUpperCase().replace(" ", "_"))));
		} else {
			MessageHandler.sendMessage(player, Ranks.PRO_PLUS.getNoPermission());
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
