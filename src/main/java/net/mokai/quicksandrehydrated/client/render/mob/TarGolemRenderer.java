package net.mokai.quicksandrehydrated.client.render.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mokai.quicksandrehydrated.entity.EntityTarGolem;
import net.mokai.quicksandrehydrated.registry.ModModelLayers;

@OnlyIn(Dist.CLIENT)
public class TarGolemRenderer extends MobRenderer<EntityTarGolem, TarGolemModel<EntityTarGolem>> {

    public TarGolemRenderer(EntityRendererProvider.Context pContext, EntityModel m) {
        super(pContext, new TarGolemModel<>(pContext.bakeLayer(ModModelLayers.TAR_GOLEM_LAYER)), .5f);

    }

    @Override
    public ResourceLocation getTextureLocation(EntityTarGolem pEntity) {
        return new ResourceLocation("qsrehydrated:textures/entity/tar_golem.png");
    }

    @Override
    public void render(EntityTarGolem entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

}
