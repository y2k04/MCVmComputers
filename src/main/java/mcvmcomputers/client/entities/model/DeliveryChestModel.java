package mcvmcomputers.client.entities.model;

import java.awt.Color;
import java.io.IOException;
import java.util.Random;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class DeliveryChestModel extends EntityModel<Entity> {
	public final ModelPart model;
	
	private final NativeImage baseTexture;
	private final MinecraftClient mcc = MinecraftClient.getInstance();
	
	public static final Random TEX_RANDOM = new Random();
	
	private NativeImage ni;
	private NativeImageBackedTexture nibt;
	private Identifier texId;
	
	public boolean fireYes = true;

	public DeliveryChestModel(ModelPart modelPart) throws IOException {
		this.baseTexture = NativeImage.read(mcc.getResourceManager().getResource(new Identifier("mcvmcomputers", "textures/entity/delivery_chest.png")).getInputStream());
		int textureWidth = 64;
		int textureHeight = 64;

		model = modelPart;

		ModelData base = new ModelData();
		ModelPartData chestRoot = base.getRoot();
		ModelPartBuilder chest = new ModelPartBuilder();
		chest.cuboid(-6f, -5f, -6f, 12f, 8f, 12f, Dilation.NONE).uv(0, 0).mirrored(false);
		chestRoot.addChild("chest", chest, ModelTransform.pivot(0f, 7f, 0f));

		ModelPartBuilder opening = new ModelPartBuilder();
		opening.cuboid(-6f, -2f, -12f, 12f, 2f, 12f, Dilation.NONE).uv(0, 20).mirrored(false);
		chestRoot.addChild("opening", opening, ModelTransform.of(0f, -5f, 6f, -1.1345f, 0f, 0f));

		ModelPartBuilder upleg0 = new ModelPartBuilder();
		upleg0.cuboid(-1f, 0f, -0.5f, 2f, 7f, 1f, Dilation.NONE).uv(24, 34).mirrored(false);
		chestRoot.addChild("upleg0", upleg0, ModelTransform.of(-6f, 3f, 6f, 0f, 0.7854f, 0f));

		ModelPartBuilder uleg0 = new ModelPartBuilder();
		uleg0.cuboid(-1f, 0f, -0.5f, 2f, 6f, 1f, Dilation.NONE).uv(0, 46).mirrored(false);
		uleg0.cuboid(-1.4868f, 6f, -1.5232f, 3f, 1f, 3f, Dilation.NONE).uv(0, 20).mirrored(false);
		chestRoot.getChild("upleg0").addChild("uleg0", uleg0, ModelTransform.pivot(-0.9828f, 7f, -0.0071f));

		ModelPartBuilder upleg1 = new ModelPartBuilder();
		upleg1.cuboid(-1f, 0f, -0.5f, 2f, 7f, 1f, Dilation.NONE).uv(0, 34).mirrored(false);
		chestRoot.addChild("upleg1", upleg1, ModelTransform.of(-6f, 3f, -6f, 0f, -0.7854f, 0f));

		ModelPartBuilder uleg1 = new ModelPartBuilder();
		uleg1.cuboid(-1f, 0f, -0.5f, 2f, 6f, 1f, Dilation.NONE).uv(44, 44).mirrored(false);
		uleg1.cuboid(-1.4868f, 6f, -1.5232f, 3f, 1f, 3f, Dilation.NONE).uv(0, 8).mirrored(false);
		chestRoot.getChild("upleg1").addChild("uleg1", uleg1, ModelTransform.pivot(-0.9828f, 7f, -0.0071f));

		ModelPartBuilder upleg2 = new ModelPartBuilder();
		upleg2.cuboid(-1f, 0f, -0.5f, 2f, 7f, 1f, Dilation.NONE).uv(6, 24).mirrored(false);
		chestRoot.addChild("upleg2", upleg2, ModelTransform.of(6f, 3f, -6f, 0f, -23562f, 0f));

		ModelPartBuilder uleg2 = new ModelPartBuilder();
		uleg2.cuboid(-1f, 0f, -0.5f, 2f, 6f, 1f, Dilation.NONE).uv(38, 43).mirrored(false);
		uleg2.cuboid(-1.4868f, 6f, -1.5232f, 3f, 1f, 3f, Dilation.NONE).uv(0, 4).mirrored(false);
		chestRoot.getChild("upleg2").addChild("uleg2", uleg2, ModelTransform.pivot(-0.9828f, 7f, -0.0071f));

		ModelPartBuilder upleg3 = new ModelPartBuilder();
		upleg3.cuboid(-1f, 0f, -0.5f, 2f, 7f, 1f, Dilation.NONE).uv(0, 24).mirrored(false);
		chestRoot.addChild("upleg3", upleg3, ModelTransform.of(6f, 3f, 6f, 0f, 2.3562f, 0f));

		ModelPartBuilder uleg3 = new ModelPartBuilder();
		uleg3.cuboid(-1f, 0f, -0.5f, 2f, 6f, 1f, Dilation.NONE).uv(32, 43).mirrored(false);
		uleg3.cuboid(-1.4868f, 6f, -1.5232f, 3f, 1f, 3f, Dilation.NONE).uv(0, 0).mirrored(false);
		chestRoot.getChild("upleg3").addChild("uleg3", uleg3, ModelTransform.pivot(-0.9828f, 7f, -0.0071f));

		ModelPartBuilder engine = new ModelPartBuilder();
		engine.cuboid(-4f, 8f, -4f, 8f, 4f, 8f, Dilation.NONE).uv(0, 34).mirrored(false);
		engine.cuboid(-3f, 5f, -3f, 6f, 3f, 6f, Dilation.NONE).uv(36, 0).mirrored(false);
		engine.cuboid(-2f, 3f, -2f, 4f, 2f, 4f, Dilation.NONE).uv(36, 20).mirrored(false);
		engine.cuboid(-4f, 3f, 3f, 1f, 5f, 1f, Dilation.NONE).uv(36, 26).mirrored(false);
		engine.cuboid(3f, 3f, 3f, 1f, 5f, 1f, Dilation.NONE).uv(30, 34).mirrored(false);
		engine.cuboid(-4f, 3f, -4f, 1f, 5f, 1f, Dilation.NONE).uv(36, 0).mirrored(false);
		engine.cuboid(3f, 3f, -4f, 1f, 5f, 1f, Dilation.NONE).uv(34, 34).mirrored(false);
		chestRoot.addChild("engine", engine, ModelTransform.NONE);

		ModelPartBuilder fire = new ModelPartBuilder();
		fire.cuboid(-3f, -1f, -3f, 6f, 3f, 6f, Dilation.NONE).uv(32, 34).mirrored(false);
		chestRoot.getChild("engine").addChild("fire", fire, ModelTransform.pivot(0f, 13f, 0f));
		model.children.put("chest", chestRoot.createPart(textureWidth, textureHeight));
	}
	
	private void generateTexture() {
		if(ni != null) {ni.close(); ni = null;}
		if(nibt != null) {nibt.close(); nibt = null;}
		if(texId != null) {mcc.getTextureManager().destroyTexture(texId); texId = null;}

		ni = new NativeImage(64, 64, true);
		ni.copyFrom(baseTexture);
		for(int x = 38;x<50;x++) {
			for(int y = 34;y<40;y++) {
				ni.setPixelColor(x, y, randomColor());
			}
		}
		for(int x = 32;x<56;x++) {
			for(int y = 40;y<43;y++) {
				ni.setPixelColor(x, y, randomColor());
			}
		}
		nibt = new NativeImageBackedTexture(ni);
		texId = mcc.getTextureManager().registerDynamicTexture("delivery_chest_fire", nibt);
	}
	
	private int randomColor() {
		if(fireYes) {
			Color r = new Color(0,0,255);
			Color y = new Color(0,128,255);
			Color o = new Color(0, 255, 255);
			r = new Color(r.getRed(), r.getGreen(), r.getBlue(), TEX_RANDOM.nextInt(256));
			y = new Color(y.getRed(), y.getGreen(), y.getBlue(), TEX_RANDOM.nextInt(256));
			o = new Color(o.getRed(), o.getGreen(), o.getBlue(), TEX_RANDOM.nextInt(256));
			int[] sel = new int[] {r.getRGB(), y.getRGB(), o.getRGB()};
			return sel[TEX_RANDOM.nextInt(sel.length)];
		}else {
			return new Color(0f,0f,0f,0f).getRGB();
		}
	}

	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		model.render(matrixStack, buffer, packedLight, packedOverlay);
	}
	
	public void render(MatrixStack matrixStack, VertexConsumerProvider provider, int packedLight, int packedOverlay){
		this.generateTexture();
		model.render(matrixStack, provider.getBuffer(RenderLayer.getText(texId)), packedLight, packedOverlay);
	}
}