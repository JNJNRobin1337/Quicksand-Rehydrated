package net.mokai.quicksandrehydrated.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.worldgen.feature.QuicksandPitConfiguration;
import net.mokai.quicksandrehydrated.worldgen.feature.QuicksandPitFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(
            ForgeRegistries.FEATURES, QuicksandRehydrated.MOD_ID);

    public static final RegistryObject<Feature<QuicksandPitConfiguration>> QUICKSAND_PIT = FEATURES.register(
            "quicksand_pit", () -> new QuicksandPitFeature(QuicksandPitConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
    
    /**
     * Registers the quicksand pit feature for world generation.
     * This is called during the common setup phase.
     */
    public static void registerWorldGeneration() {
        // Register the feature for the /place feature command
        registerFeatureForCommand();
        
        // The actual world generation is handled by JSON files in:
        // - data/qsrehydrated/worldgen/configured_feature/quicksand_pit.json
        // - data/qsrehydrated/worldgen/placed_feature/quicksand_pit.json
        // - data/qsrehydrated/tags/worldgen/biome/has_quicksand_pit.json
        // - data/qsrehydrated/forge/biome_modifier/add_quicksand_pit.json
        
        System.out.println("==============================================");
        System.out.println("Quicksand pit feature registered for world generation");
        System.out.println("Feature: " + QUICKSAND_PIT.getId());
        System.out.println("Feature instance: " + QUICKSAND_PIT.get());
        System.out.println("==============================================");
        
        // Ensure the feature is registered with the correct registry
        try {
            // This ensures the feature is available for world generation
            if (!net.minecraft.core.registries.BuiltInRegistries.FEATURE.containsKey(
                    new net.minecraft.resources.ResourceLocation(QuicksandRehydrated.MOD_ID, "quicksand_pit"))) {
                System.out.println("Registering quicksand pit feature for world generation");
                Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE, 
                        new net.minecraft.resources.ResourceLocation(QuicksandRehydrated.MOD_ID, "quicksand_pit"), 
                        QUICKSAND_PIT.get());
            }
        } catch (Exception e) {
            // Log any errors that occur during registration
            System.err.println("Error registering quicksand pit feature for world generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registers the quicksand pit feature for the /place feature command.
     */
    private static void registerFeatureForCommand() {
        // In 1.20.1, we need to use the built-in registries for the /place feature command
        ResourceLocation quicksandPitId = new ResourceLocation(QuicksandRehydrated.MOD_ID, "quicksand_pit");
        
        // Log that we're registering the feature
        System.out.println("Registering quicksand pit feature for /place command: " + quicksandPitId);
        
        try {
            // This ensures the feature is available for the /place feature command
            Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE, 
                    quicksandPitId, QUICKSAND_PIT.get());
        } catch (Exception e) {
            // Log any errors that occur during registration
            System.err.println("Error registering quicksand pit feature: " + e.getMessage());
            e.printStackTrace();
        }
    }
}