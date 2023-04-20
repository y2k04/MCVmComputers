package mcvmcomputers.client.entities.model;

import mcvmcomputers.utils.MVCUtils;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class OrderingTabletModel extends EntityModel<Entity> {
	private final ModelPart model;

	public OrderingTabletModel(ModelPart modelPart) {
		model = modelPart;
		ModelData tablet = new ModelData();
		ModelPartData tabletRoot = tablet.getRoot();
		ModelPartBuilder tabletParts = new ModelPartBuilder();
		tabletParts.cuboid(-6f, -2f, -6f, 12f, 1f, 1f, Dilation.NONE).uv(24, 19).mirrored(false);
		tabletParts.cuboid(-6f, -2f, -5f, 12f, 1f, 1f, Dilation.NONE).uv(24, 24).mirrored(false);
		tabletParts.cuboid(5f, -2f, -5f, 1f, 1f, 10f, Dilation.NONE).uv(12, 20).mirrored(false);
		tabletParts.cuboid(-6f, -2f, -5f, 1f, 1f, 10f, Dilation.NONE).uv(0, 19).mirrored(false);
		tabletParts.cuboid(-6f, -1f, -6f, 12f, 1f, 12f, Dilation.NONE).uv(0, 0).mirrored(false);
		tabletRoot.addChild("tablet", tabletParts, ModelTransform.pivot(0f, 24f, 0f));

		ModelPartBuilder buttons = new ModelPartBuilder();
		buttons.cuboid(-6f, -0.5f, -2.5f, 12f, 1f, 5f, Dilation.NONE).uv(0, 13).mirrored(false);
		tabletRoot.addChild("buttons", buttons, ModelTransform.of(0f, -3.4f, -7.1f, -0.7854f, 0f, 0f));

		ModelPartBuilder up = new ModelPartBuilder();
		up.cuboid(-0.5f, -0.5f, -0.5f, 1f, 1f, 1f, Dilation.NONE).uv(4, 6).mirrored(false);
		tabletRoot.getChild("buttons").addChild("up", up, ModelTransform.pivot(-2f, -0.5f, 1.0657f));

		ModelPartBuilder down = new ModelPartBuilder();
		down.cuboid(-0.5f, -0.5f, -0.5f, 1f, 1f, 1f, Dilation.NONE).uv(0, 6).mirrored(false);
		tabletRoot.getChild("buttons").addChild("down", down, ModelTransform.pivot(-2f, -0.5f, -1.1971f));

		ModelPartBuilder left = new ModelPartBuilder();
		left.cuboid(-0.5f, -0.5f, -0.5f, 1f, 1f, 1f, Dilation.NONE).uv(4, 4).mirrored(false);
		tabletRoot.getChild("buttons").addChild("left", left, ModelTransform.pivot(-3.1f, -0.5f, -0.0657f));

		ModelPartBuilder right = new ModelPartBuilder();
		right.cuboid(-0.5f, -0.5f, -0.5f, 1f, 1f, 1f, Dilation.NONE).uv(0, 4).mirrored(false);
		tabletRoot.getChild("buttons").addChild("right", right, ModelTransform.pivot(-0.9f, -0.5f, -0.0657f));

		ModelPartBuilder enter = new ModelPartBuilder();
		enter.cuboid(-1f, -0.5f, -1.5f, 2f, 1f, 3f, Dilation.NONE).uv(0, 0).mirrored(false);
		tabletRoot.getChild("buttons").addChild("enter", enter, ModelTransform.pivot(2.4f, -0.5f, -0.0657f));
		int textureWidth = 64;
		int textureHeight = 64;
		model.children.put("tablet", tabletRoot.createPart(textureWidth, textureHeight));
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		model.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setButtons(boolean upState, boolean downState, boolean leftState, boolean rightState, boolean enterState, float deltaTime) {
		if(upState) {
			model.getChild("tablet").getChild("buttons").getChild("up").pivotY = MVCUtils.lerp(model.getChild("up").pivotY, -0.5F, deltaTime);
		}else {
			model.getChild("tablet").getChild("buttons").getChild("up").pivotY = MVCUtils.lerp(model.getChild("up").pivotY, -0.9F, deltaTime);
		}

		if(downState) {
			model.getChild("tablet").getChild("buttons").getChild("down").pivotY = MVCUtils.lerp(model.getChild("down").pivotY, -0.4393F, deltaTime);
		}else {
			model.getChild("tablet").getChild("buttons").getChild("down").pivotY = MVCUtils.lerp(model.getChild("down").pivotY, -0.8393F, deltaTime);
		}

		if(leftState) {
			model.getChild("tablet").getChild("buttons").getChild("left").pivotY = MVCUtils.lerp(model.getChild("left").pivotY, -0.4393F, deltaTime);
		}else {
			model.getChild("tablet").getChild("buttons").getChild("left").pivotY = MVCUtils.lerp(model.getChild("left").pivotY, -0.8393F, deltaTime);
		}

		if(rightState) {
			model.getChild("tablet").getChild("buttons").getChild("right").pivotY = MVCUtils.lerp(model.getChild("right").pivotY, -0.4393F, deltaTime);
		}else {
			model.getChild("tablet").getChild("buttons").getChild("right").pivotY = MVCUtils.lerp(model.getChild("right").pivotY, -0.8393F, deltaTime);
		}

		if(enterState) {
			model.getChild("tablet").getChild("buttons").getChild("enter").pivotY = MVCUtils.lerp(model.getChild("enter").pivotY, -0.5F, deltaTime);
		}else {
			model.getChild("tablet").getChild("buttons").getChild("enter").pivotY = MVCUtils.lerp(model.getChild("enter").pivotY, -1.1222F, deltaTime);
		}
	}

	public void rotateButtons(float rotX, float deltaTime) {
		model.getChild("tablet").getChild("buttons").pitch = MVCUtils.lerp(model.getChild("tablet").getChild("buttons").pitch, rotX, deltaTime);
	}

	@Override
	public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}
}