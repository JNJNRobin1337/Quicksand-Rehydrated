package net.mokai.quicksandrehydrated;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.entity.coverage.WashingSystem;
import net.mokai.quicksandrehydrated.loot.ModLootModifiers;
import net.mokai.quicksandrehydrated.networking.ModMessages;
import net.mokai.quicksandrehydrated.registry.*;
import net.mokai.quicksandrehydrated.screen.MixerScreen;
import net.mokai.quicksandrehydrated.screen.ModMenuTypes;
import net.mokai.quicksandrehydrated.worldgen.placement.ModPlacementModifierTypes;

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_DROWNABLE;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(QuicksandRehydrated.MOD_ID)
public class QuicksandRehydrated {

    public static final String MOD_ID = "qsrehydrated";

    public QuicksandRehydrated() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        QuicksandRegistry.register(modEventBus);
        ModFluids.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeModeTab.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModPlacementModifierTypes.register(modEventBus);


        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        //modEventBus.addListener(this::addCreative);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
            // Register world generation for quicksand pits
            ModFeatures.registerWorldGeneration();
        });
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            //ItemBlockRenderTypes.setRenderLayer(ModFluids.DRY_QUICKSAND.get(), RenderType.solid());
            MenuScreens.register(ModMenuTypes.MIXER_MENU.get(), MixerScreen::new);

        }
    }


    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class GameModEvents {
        @SubscribeEvent
        public static void onLivingBreatheEvent(LivingBreatheEvent event) {

            Entity entity = event.getEntity();
            Vec3 eyePos = entity.getEyePosition();
            BlockPos eyeBlockPos = new BlockPos((int) Math.floor(eyePos.x()), (int) Math.floor(eyePos.y()), (int) Math.floor(eyePos.z()));
            BlockState eyeState = entity.level().getBlockState(eyeBlockPos);

            if (eyeState.is(QUICKSAND_DROWNABLE)) {
                event.setCanBreathe(false);
            }

        }
        
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            // Process at the end of the tick
            if (event.phase == TickEvent.Phase.END) {
                Player player = event.player;
                Level level = player.level();
                
                // Apply washing effect if player is in water
                // This needs to run on both client and server sides
                // - Server side: for actual coverage removal logic
                // - Client side: for particle effects
                WashingSystem.applyWashingEffect(player, level);
            }
        }

        // Commentiamo temporaneamente questo evento per verificare se è la causa del problema
        /*
        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            // Your custom logic goes here
            LivingEntity entity = event.getEntity();

            if (entity.level().isClientSide()) {
                System.out.print("[C] ");
            }
            else {
                System.out.print("[S] ");
            }
            System.out.println(entity.getName() + " jumped!");

            // Verifica se l'entità è effettivamente su un blocco di sabbie mobili
            // Usa la posizione attuale dell'entità invece di getOnPosLegacy()
            BlockPos entityPos = entity.blockPosition().below();
            BlockState blockState = entity.level().getBlockState(entityPos);

            if (blockState.getBlock() instanceof QuicksandBase) {
                QuicksandBase quicksand = (QuicksandBase) blockState.getBlock();
                quicksand.sinkableJumpOff(blockState, entity.level(), entityPos, entity);
            }
        }
        */

    }





}
