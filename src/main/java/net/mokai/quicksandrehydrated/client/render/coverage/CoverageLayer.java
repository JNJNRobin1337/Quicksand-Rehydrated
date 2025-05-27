package net.mokai.quicksandrehydrated.client.render.coverage;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.entity.coverage.CoverageEntry;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.registry.ModModelLayers;

import java.util.ArrayList;
import java.util.List;

public class CoverageLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    PlayerCoverageDefaultModel coverageModel;
    private final DynamicTexture texture;
    TextureManager textureManager;
    ResourceLocation resourcelocation;

    public CoverageLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer, boolean pSlim) {
        super(pRenderer);

        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (!pSlim) {
            this.coverageModel = new PlayerCoverageDefaultModel(modelSet.bakeLayer(ModModelLayers.COVERAGE_LAYER_DEFAULT));
        } else {
            this.coverageModel = new PlayerCoverageSlimModel(modelSet.bakeLayer(ModModelLayers.COVERAGE_LAYER_SLIM));
        }

        this.texture = new DynamicTexture(64, 64, true);
        this.textureManager = Minecraft.getInstance().textureManager;
        this.resourcelocation = Minecraft.getInstance().textureManager.register("coverage", this.texture);

    }

    private void updateTexture(PlayerCoverage cov) {
        // Improved version that tries to be more efficient
        NativeImage img = this.texture.getPixels();

        // Clear the texture to transparent
        for (int i = 0; i < 64; ++i) {
            for (int k = 0; k < 64; ++k) {
                img.setPixelRGBA(i, k, 0);
            }
        }

        int entry_count = cov.coverageEntries.size();
        if (entry_count == 0) {
            this.texture.upload();
            return;
        }

        // Cache the alpha texture since it's used for all entries
        TextureAtlasSprite alphaTex;
        try {
            alphaTex = CoverageAtlasHolder.singleton.get(
                new ResourceLocation(QuicksandRehydrated.MOD_ID, "coverage_mask"));
        } catch (Exception e) {
            // Fallback to a different texture if coverage_mask is not found
            System.out.println("Coverage mask texture not found, using fallback");
            alphaTex = CoverageAtlasHolder.singleton.get(
                new ResourceLocation(QuicksandRehydrated.MOD_ID, "quicksand_coverage"));
        }
        
        // Pre-calculate alpha values for the mask to avoid recalculating for each entry
        float[][] alphaValues = new float[64][64];
        for (int i = 0; i < 64; ++i) {
            for (int k = 0; k < 64; ++k) {
                int alpha_rgba = alphaTex.getPixelRGBA(0, i, k);
                alphaValues[i][k] = (float) FastColor.ARGB32.alpha(alpha_rgba) / 255.0F;
            }
        }
        
        // Sort entries by begin value to ensure proper layering
        // This ensures entries with lower begin values (covering more of the player) are processed first
        List<CoverageEntry> sortedEntries = new ArrayList<>(cov.coverageEntries);
        sortedEntries.sort((a, b) -> Integer.compare(a.begin, b.begin));
        
        // Process entries from bottom to top for proper layering
        for (int c = sortedEntries.size() - 1; c >= 0; --c) {
            CoverageEntry entry = sortedEntries.get(c);
            
            // Skip invalid entries
            if (entry.begin >= entry.end || entry.begin < 0 || entry.end > 32) {
                continue;
            }

            // Modifichiamo leggermente il calcolo per assicurarci che il primo pixel sia coperto
            // Usiamo un piccolo offset per assicurarci che il coverage copra completamente i piedi
            double bot = 1.0 - (entry.end/32.0) - 0.001;
            double top = 1.0 - (entry.begin/32.0) + 0.001;

            // Get the texture for this coverage
            TextureAtlasSprite colorTex;
            try {
                colorTex = CoverageAtlasHolder.singleton.get(entry.texture);
            } catch (Exception e) {
                // If texture can't be loaded, skip this entry
                continue;
            }

            // Process the entire texture to ensure all parts including second layers are covered
            for (int i = 0; i < 64; ++i) {
                for (int k = 0; k < 64; ++k) {
                    float A = alphaValues[i][k];
                    
                    // If the alpha of this pixel is within the bounds, use the pixel from the coverage texture
                    // Modifichiamo la condizione per assicurarci che i pixel ai bordi siano inclusi
                    if (A <= top && A >= bot) {
                        int color_rgba = colorTex.getPixelRGBA(0, i, k);
                        
                        // Only set the pixel if it has some opacity
                        if (FastColor.ARGB32.alpha(color_rgba) > 0) {
                            img.setPixelRGBA(i, k, color_rgba);
                        }
                    }
                }
            }
        }

        this.texture.upload();
        
        // Debug: stampa informazioni sul coverage per aiutare a diagnosticare problemi
        // Decommentare questa sezione se si verificano problemi con il coverage
        /*
        if (!sortedEntries.isEmpty()) {
            System.out.println("Coverage entries: " + sortedEntries.size());
            for (CoverageEntry entry : sortedEntries) {
                System.out.println("Entry: begin=" + entry.begin + ", end=" + entry.end + 
                                  ", texture=" + entry.texture + 
                                  ", bot=" + (1.0 - (entry.end/32.0) - 0.001) + 
                                  ", top=" + (1.0 - (entry.begin/32.0) + 0.001));
            }
        }
        */
    }

//    private void copyYLayer(Resource, int yLayer)

    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pAbstractPlayer, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        try {
            playerStruggling playerqs = (playerStruggling) pAbstractPlayer;
            
            // Check if player has coverage
            PlayerCoverage pC = playerqs.getCoverage();
            if (pC == null || pC.coverageEntries.isEmpty()) {
                return; // No coverage to render
            }
            
            // Update texture if needed
            if (pC.requiresUpdate) {
                pC.requiresUpdate = false;
                this.updateTexture(pC);
            }
            
            // Get the appropriate model
            PlayerCoverageDefaultModel model = this.coverageModel;
            
            // Copy properties from parent model
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(pAbstractPlayer, pLimbSwing, pLimbSwingAmount, pPartialTick);
            model.setupAnim(pAbstractPlayer, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            
            // Ensure visibility of all parts, including all second layers
            model.hat.visible = true;
            model.leftLeg.visible = true;
            model.rightLeg.visible = true;
            model.leftArm.visible = true;
            model.rightArm.visible = true;
            model.body.visible = true;
            model.head.visible = true;
            
            // Use a translucent render type for better blending
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(this.resourcelocation));
            
            // Render the coverage with full opacity
            model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, 
                LivingEntityRenderer.getOverlayCoords(pAbstractPlayer, 0.0F), 
                1.0F, 1.0F, 1.0F, 1.0F);
            
            // Debug: uncomment to print information about the coverage
            /*
            if (!pC.coverageEntries.isEmpty()) {
                System.out.println("Rendering coverage for player: " + pAbstractPlayer.getName().getString());
                System.out.println("Coverage entries: " + pC.coverageEntries.size());
                for (CoverageEntry entry : pC.coverageEntries) {
                    System.out.println("Entry: begin=" + entry.begin + ", end=" + entry.end + 
                                      ", texture=" + entry.texture);
                }
            }
            */
        } catch (Exception e) {
            // Silently catch any exceptions to prevent rendering crashes
            // In a production environment, you might want to log this
            // e.printStackTrace(); // Uncomment for debugging
        }
    }

}
