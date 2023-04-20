package mcvmcomputers.entities;

import mcvmcomputers.MainMod;
import mcvmcomputers.item.ItemList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWallTV extends Entity{
	private static final TrackedData<Float> LOOK_AT_POS_X =
			DataTracker.registerData(EntityWallTV.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LOOK_AT_POS_Y =
			DataTracker.registerData(EntityWallTV.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> LOOK_AT_POS_Z =
			DataTracker.registerData(EntityWallTV.class, TrackedDataHandlerRegistry.FLOAT);
	
	private static final TrackedData<String> OWNER_UUID =
			DataTracker.registerData(EntityWallTV.class, TrackedDataHandlerRegistry.STRING);
	
	public EntityWallTV(EntityType<?> type, World world) {
		super(type, world);
	}
	
	public EntityWallTV(World world, double x, double y, double z) {
		this(EntityList.WALLTV, world);
		this.updatePosition(x, y, z);
	}
	
	public EntityWallTV(World world, Double x, Double y, Double z, Vec3d lookAt, String uuid) {
		this(EntityList.WALLTV, world);
		this.updatePosition(x, y, z);
		this.getDataTracker().set(LOOK_AT_POS_X, (float)lookAt.x);
		this.getDataTracker().set(LOOK_AT_POS_Y, (float)lookAt.y);
		this.getDataTracker().set(LOOK_AT_POS_Z, (float)lookAt.z);
		this.getDataTracker().set(OWNER_UUID, uuid);
	}
	
	public Vec3d getLookAtPos() {
		return new Vec3d(this.getDataTracker().get(LOOK_AT_POS_X),
						 this.getDataTracker().get(LOOK_AT_POS_Y),
						 this.getDataTracker().get(LOOK_AT_POS_Z));
	}

	@Override
	protected void initDataTracker() {
		this.getDataTracker().startTracking(LOOK_AT_POS_X, 0f);
		this.getDataTracker().startTracking(LOOK_AT_POS_Y, 0f);
		this.getDataTracker().startTracking(LOOK_AT_POS_Z, 0f);
		this.getDataTracker().startTracking(OWNER_UUID, "");
	}
	
	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if(!player.world.isClient) {
			if(player.isSneaking()) {
				this.kill();
				player.world.spawnEntity(new ItemEntity(player.world,
						this.getPos().x, this.getPos().y, this.getPos().z,
						new ItemStack(ItemList.ITEM_WALLTV)));
			}
		}else {
			if(!player.isSneaking()) {
				if(this.getOwnerUUID().equals(player.getUuid().toString())) {
					MainMod.focus.run();
				}
			}
		}
		return ActionResult.SUCCESS;
	}
	
	@Override
	public void tick() {
		if(getOwnerUUID().isEmpty()) {
			this.kill();
		}
	}
	
	@Override
	public boolean collides() {
		return true;
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		this.getDataTracker().set(LOOK_AT_POS_X, nbt.getFloat("LookAtX"));
		this.getDataTracker().set(LOOK_AT_POS_Y, nbt.getFloat("LookAtY"));
		this.getDataTracker().set(LOOK_AT_POS_Z, nbt.getFloat("LookAtZ"));
		this.getDataTracker().set(OWNER_UUID, nbt.getString("Owner"));
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putFloat("LookAtX", this.getDataTracker().get(LOOK_AT_POS_X));
		nbt.putFloat("LookAtY", this.getDataTracker().get(LOOK_AT_POS_Y));
		nbt.putFloat("LookAtZ", this.getDataTracker().get(LOOK_AT_POS_Z));
		nbt.putString("Owner", this.getDataTracker().get(OWNER_UUID));
	}

	public String getOwnerUUID() {
		return this.getDataTracker().get(OWNER_UUID);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

}