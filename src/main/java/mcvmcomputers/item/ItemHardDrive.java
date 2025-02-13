package mcvmcomputers.item;

import mcvmcomputers.MainMod;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemHardDrive extends OrderableItem{
	public ItemHardDrive(Settings settings) {
		super(settings, 6);
	}
	
	@Override
	public boolean shouldSyncTagToClient() {
		return true;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if(world.isClient) {
			MainMod.hardDriveClick.run();
		}
		return super.use(world, user, hand);
	}
	
	@Override
	public Text getName(ItemStack stack) {
		if(stack.getNbt() != null) {
			if(stack.getNbt().contains("vhdfile")) {
				return Text.translatable("mcvmcomputers.hdd_item_name", stack.getNbt().getString("vhdfile")).formatted(Formatting.WHITE);
			}
		}
		return Text.translatable("mcvmcomputers.hdd_item_name", Text.translatable("mcvmcomputers.hdd_right_click")).formatted(Formatting.WHITE);
	}
	
	public static ItemStack createHardDrive(String fileName) {
		ItemStack is = new ItemStack(ItemList.ITEM_HARDDRIVE);
		NbtCompound ct = is.getOrCreateNbt();
		ct.putString("vhdfile", fileName);
		is.setNbt(ct);
		return is;
	}

}
