package net.mokai.quicksandrehydrated.registry;


import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.worldgen.placement.QuicksandPitPlacement;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> QUICKSAND_PIT_PLACED_KEY = createKey("quicksand_pit_placed");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Register the placed quicksand pit feature with our custom placement logic
        // This will spawn the quicksand pit in appropriate locations based on terrain analysis
        register(context, QUICKSAND_PIT_PLACED_KEY, 
                configuredFeatures.getOrThrow(ModConfiguredFeatures.QUICKSAND_PIT_KEY),
                RarityFilter.onAverageOnceEvery(16), // Aumentiamo la frequenza base perché il nostro filtro è più selettivo
                InSquarePlacement.spread(), // Spreads the feature within the chunk
                PlacementUtils.HEIGHTMAP, // Places on the heightmap
                QuicksandPitPlacement.INSTANCE, // Il nostro placement modifier personalizzato che analizza il terreno
                BiomeFilter.biome() // Filters by biome (will be specified in biome modification)
        );
    }

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(QuicksandRehydrated.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 PlacementModifier... modifiers) {
        register(context, key, configuration, List.of(modifiers));
    }
}
