package mcvmcomputers.client.entities.render;

import mcvmcomputers.entities.EntityKeyboard;
import mcvmcomputers.item.ItemList;
import mcvmcomputers.utils.MVCUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import org.apache.commons.lang3.RandomUtils;

public class KeyboardRender extends EntityRenderer<EntityKeyboard>{

	public KeyboardRender(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public Identifier getTexture(EntityKeyboard entity) {
		return null;
	}

	@Override
	public void render(EntityKeyboard entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.translate(0, 0.5, 0);
		Quaternion look = MVCUtils.lookAt(entity.getPos(), entity.getLookAtPos());
		matrices.multiply(look);
		MinecraftClient.getInstance().getItemRenderer().renderItem(new ItemStack(ItemList.ITEM_KEYBOARD), Mode.NONE, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, RandomUtils.nextInt());
		matrices.pop();
	}

}
