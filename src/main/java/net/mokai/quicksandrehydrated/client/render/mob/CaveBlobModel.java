package net.mokai.quicksandrehydrated.client.render.mob;// Made with Blockbench 4.10.2


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveBlobModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	//public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "cave_blob"), "main");
	private final ModelPart root;
	/*
	private final ModelPart core;
	private final ModelPart body;
	private final ModelPart top;
	private final ModelPart sides;
	private final ModelPart front_back;
	private final ModelPart bottom;
	private final ModelPart coins;
	private final ModelPart old_bone_2;
	private final ModelPart old_bone_1;
*/
	public CaveBlobModel(ModelPart rootS) {
		this.root = rootS.getChild("root");
		/*
		this.core = this.root.getChild("core");
		this.body = this.core.getChild("body");
		this.top = this.body.getChild("top");
		this.sides = this.body.getChild("sides");
		this.front_back = this.body.getChild("front_back");
		this.bottom = this.core.getChild("bottom");
		this.coins = this.root.getChild("coins");
		this.old_bone_2 = this.root.getChild("old_bone_2");
		this.old_bone_1 = this.root.getChild("old_bone_1");*/
	}

	public static LayerDefinition createOuterBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition core = root.addOrReplaceChild("core", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition body = core.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -9.5F, -12.0F, 24.0F, 20.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.5F, 0.0F));

		PartDefinition top = body.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 72).addBox(-9.0F, -1.0F, -9.0F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.5F, 0.0F));

		PartDefinition sides = body.addOrReplaceChild("sides", CubeListBuilder.create().texOffs(79, 26).addBox(-14.0F, -8.0F, -9.0F, 2.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(79, 26).mirror().addBox(12.0F, -8.0F, -9.0F, 2.0F, 17.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 1.5F, 0.0F));

		PartDefinition front_back = body.addOrReplaceChild("front_back", CubeListBuilder.create().texOffs(0, 101).addBox(-9.0F, -8.0F, -14.0F, 18.0F, 17.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(96, 70).addBox(-9.0F, -8.0F, 12.0F, 18.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, 0.0F));

		PartDefinition bottom = core.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 44).addBox(-12.0F, -1.5F, -12.0F, 24.0F, 3.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(0, 93).addBox(-12.0F, -1.5F, -16.0F, 24.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(73, 0).addBox(-12.0F, -1.5F, 12.0F, 24.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition bottom_r1 = bottom.addOrReplaceChild("bottom_r1", CubeListBuilder.create().texOffs(73, 8).addBox(-12.0F, -1.5F, -2.0F, 24.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bottom_r2 = bottom.addOrReplaceChild("bottom_r2", CubeListBuilder.create().texOffs(73, 16).addBox(-12.0F, -1.5F, -2.0F, 24.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
		System.out.println("Outer layer");

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	public static LayerDefinition createInnerBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition coins = partdefinition.addOrReplaceChild("coins", CubeListBuilder.create().texOffs(55, 72).addBox(-3.5F, -0.5F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -13.5F, 7.5F, 0.7103F, 0.3508F, -0.8197F));

		PartDefinition old_bone_2 = partdefinition.addOrReplaceChild("old_bone_2", CubeListBuilder.create().texOffs(0, 8).addBox(-4.0F, -0.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 5).addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(2.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -9.5F, -7.5F, -0.0148F, -0.5043F, -1.1454F));

		PartDefinition old_bone_1 = partdefinition.addOrReplaceChild("old_bone_1", CubeListBuilder.create().texOffs(9, 0).addBox(-4.0F, -0.5F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(9, 11).addBox(-2.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(9, 6).addBox(2.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -12.5F, -7.5F, 0.5931F, 0.422F, -0.967F));
		System.out.println("Inner layer");
		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		System.out.println("Setting up anims");
		this.root().getAllParts().forEach(ModelPart::resetPose);

		//this.animate(((EntityCaveBlob) entity).idleAnimationState, CaveBlobAnimations.slime_blob_jiggle, ageInTicks, 1f);
		//this.animate(((EntityCaveBlob) entity).jumpAnimationState, CaveBlobAnimations.slime_blob_bounce, ageInTicks, 1f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {return root;}
}