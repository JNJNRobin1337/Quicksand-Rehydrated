package net.mokai.quicksandrehydrated.client.render.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mokai.quicksandrehydrated.entity.EntityCaveBlob;
import net.mokai.quicksandrehydrated.registry.ModModelLayers;

@OnlyIn(Dist.CLIENT)
public class CaveBlobRenderer extends MobRenderer<EntityCaveBlob, CaveBlobModel<EntityCaveBlob>> {

    public CaveBlobRenderer(EntityRendererProvider.Context pContext, EntityModel m) {
        super(pContext, new CaveBlobModel<>(pContext.bakeLayer(ModModelLayers.CAVE_BLOB_SOLID_LAYER)), .5f);
        //this.addLayer(new CaveBlobClearLayer<>(this, pContext.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCaveBlob pEntity) {
        return new ResourceLocation("qsrehydrated:textures/entity/cave_blob.png");
    }

    @Override
    public void render(EntityCaveBlob entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

}
