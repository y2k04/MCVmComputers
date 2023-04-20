package mcvmcomputers.item;

import java.util.List;

import mcvmcomputers.client.ClientMod;
import mcvmcomputers.entities.EntityPC;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemPCCaseSidepanel extends OrderableItem{
	public ItemPCCaseSidepanel(Settings settings) {
		super(settings, 6);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if(!world.isClient && hand == Hand.MAIN_HAND) {
			user.getStackInHand(hand).decrement(1);
			HitResult hr = user.raycast(10, 0f, false);
			EntityPC ek = new EntityPC(world, 
									hr.getPos().getX(),
									hr.getPos().getY(),
									hr.getPos().getZ(),
									new Vec3d(user.getPos().x,
												hr.getPos().getY(),
												user.getPos().z), user.getUuid(), true, user.getStackInHand(hand).getTag());
			world.spawnEntity(ek);
		}
		
		if(world.isClient) {
			world.playSound(ClientMod.thePreviewEntity.getX(),
							ClientMod.thePreviewEntity.getY(),
							ClientMod.thePreviewEntity.getZ(),
							SoundEvents.BLOCK_METAL_PLACE,
							SoundCategory.BLOCKS, 1, 1, true);
		}
		
		return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		if(stack.getTag() != null) {
			if (stack.getTag().contains("MoboInstalled")) {
				if(stack.getTag().getBoolean("MoboInstalled")) {
					tooltip.add(new TranslatableText(stack.getTag().getBoolean("x64") ? "item.mcvmcomputers.motherboard64" : "item.mcvmcomputers.motherboard").formatted(Formatting.GRAY));
					if(stack.getTag().getBoolean("GPUInstalled"))
						tooltip.add(new TranslatableText("mcvmcomputers.pc_item_gpu").formatted(Formatting.GRAY));
					if(stack.getTag().getInt("CPUDividedBy") > 0)
						tooltip.add(new TranslatableText("mcvmcomputers.pc_item_cpu", stack.getTag().getInt("CPUDividedBy")).formatted(Formatting.GRAY));
					if(stack.getTag().getInt("RAMSlot0") > 0) {
						if((stack.getTag().getInt("RAMSlot0") / 1024) < 1) {
							tooltip.add(new TranslatableText("mcvmcomputers.pc_item_ramSlot0Mb", stack.getTag().getInt("RAMSlot0")).formatted(Formatting.GRAY));
						} else if((stack.getTag().getInt("RAMSlot0") / 1024) >= 1) {
							tooltip.add(new TranslatableText("mcvmcomputers.pc_item_ramSlot0", (stack.getTag().getInt("RAMSlot0") / 1024)).formatted(Formatting.GRAY));
					}}
					if(stack.getTag().getInt("RAMSlot1") > 0) {
						if((stack.getTag().getInt("RAMSlot1") / 1024) < 1) {
							tooltip.add(new TranslatableText("mcvmcomputers.pc_item_ramSlot1Mb", stack.getTag().getInt("RAMSlot1")).formatted(Formatting.GRAY));
						} else if((stack.getTag().getInt("RAMSlot1") / 1024) >= 1) {
							tooltip.add(new TranslatableText("mcvmcomputers.pc_item_ramSlot1", (stack.getTag().getInt("RAMSlot1") / 1024)).formatted(Formatting.GRAY));
					}}
					if(!stack.getTag().getString("VHDName").isEmpty())
						tooltip.add(new TranslatableText("mcvmcomputers.pc_item_hdd", stack.getTag().getString("VHDName")).formatted(Formatting.GRAY));
					if(!stack.getTag().getString("ISOName").isEmpty())
						tooltip.add(new TranslatableText("mcvmcomputers.pc_item_iso", stack.getTag().getString("ISOName")).formatted(Formatting.GRAY));
				}
			}
		}
	}
	
	@Override
	public Text getName(ItemStack stack) {
		if(stack.getTag() != null) {
			if (stack.getTag().contains("MoboInstalled")) {
				if(stack.getTag().getBoolean("MoboInstalled")) {
					return new TranslatableText("mcvmcomputers.pc_item_built");
				}
			}
		}
		return new TranslatableText("item.mcvmcomputers.pc_case_sidepanel");
	}
	
	public static ItemStack createPCStackByEntity(EntityPC pc) {
		ItemStack is = new ItemStack(ItemList.PC_CASE_SIDEPANEL);
		if(pc.getMotherboardInstalled()) {
			NbtCompound nc = is.getOrCreateTag();
			nc.putBoolean("x64", pc.get64Bit());
			nc.putBoolean("MoboInstalled", pc.getMotherboardInstalled());
			nc.putBoolean("GPUInstalled", pc.getGpuInstalled());
			nc.putInt("CPUDividedBy", pc.getCpuDividedBy());
			nc.putInt("RAMSlot0", pc.getGigsOfRamInSlot0());
			nc.putInt("RAMSlot1", pc.getGigsOfRamInSlot1());
			nc.putString("VHDName", pc.getHardDriveFileName());
			nc.putString("ISOName", pc.getIsoFileName());
		}
		return is;
	}
}
