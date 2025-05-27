package net.mokai.quicksandrehydrated.worldgen;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.registry.ModPlacedFeatures;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_QUICKSAND_PIT = registerKey("add_quicksand_pit");
    
    // Tag for desert biomes
    public static final TagKey<Biome> DESERT_BIOMES = TagKey.create(
            Registries.BIOME, new ResourceLocation("minecraft", "is_desert"));

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // Add quicksand pits to desert biomes
        context.register(ADD_QUICKSAND_PIT, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(DESERT_BIOMES),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.QUICKSAND_PIT_PLACED_KEY)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION
        ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, 
                new ResourceLocation(QuicksandRehydrated.MOD_ID, name));
    }
}