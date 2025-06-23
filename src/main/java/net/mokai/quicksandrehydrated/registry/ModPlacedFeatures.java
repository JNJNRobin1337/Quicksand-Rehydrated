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
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.worldgen.placement.QuicksandPitPlacement;

import java.util.List;

public class ModPlacedFeatures {
    // Cambiamo la chiave per corrispondere al file JSON
    public static final ResourceKey<PlacedFeature> QUICKSAND_PIT_PLACED_KEY = createKey("quicksand_pit");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // Registriamo la feature con le stesse impostazioni del file JSON
        register(context, QUICKSAND_PIT_PLACED_KEY, 
                configuredFeatures.getOrThrow(ModConfiguredFeatures.QUICKSAND_PIT_KEY),
                RarityFilter.onAverageOnceEvery(2), // Chance: 2 come nel JSON
                InSquarePlacement.spread(), // Corrisponde a "in_square" nel JSON
                PlacementUtils.HEIGHTMAP, // Corrisponde a "heightmap" nel JSON
                QuicksandPitPlacement.INSTANCE, // Il nostro placement modifier personalizzato
                BiomeFilter.biome(), // Corrisponde a "biome" nel JSON
                CountPlacement.of(3) // Media tra min (2) e max (5) nel JSON
        );
        
        System.out.println("[ModPlacedFeatures] Registrata feature quicksand_pit con chiave: " + QUICKSAND_PIT_PLACED_KEY);
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
