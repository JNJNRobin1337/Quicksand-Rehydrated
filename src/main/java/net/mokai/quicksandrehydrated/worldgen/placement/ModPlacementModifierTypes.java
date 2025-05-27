package net.mokai.quicksandrehydrated.worldgen.placement;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro per i tipi di placement modifier personalizzati
 */
public class ModPlacementModifierTypes {
    // Crea un registro differito per i placement modifier
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = 
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, QuicksandRehydrated.MOD_ID);
    
    // Registra il nostro placement modifier personalizzato
    public static final RegistryObject<PlacementModifierType<QuicksandPitPlacement>> QUICKSAND_PIT_PLACEMENT = 
            PLACEMENT_MODIFIERS.register("quicksand_pit_placement", 
                    () -> () -> QuicksandPitPlacement.CODEC);
    
    /**
     * Registra i placement modifier con l'event bus di Forge
     */
    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIERS.register(eventBus);
    }
}