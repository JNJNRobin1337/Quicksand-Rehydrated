package net.mokai.quicksandrehydrated.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.block.*;
import net.mokai.quicksandrehydrated.block.quicksands.*;
import net.mokai.quicksandrehydrated.block.quicksands.core.FlowingQuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.entity.data.QuicksandWobbleEffect;
import net.mokai.quicksandrehydrated.util.DepthCurve;


import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, QuicksandRehydrated.MOD_ID);

    public static final RegistryObject<Block> MIXER = registerBlock("mixer", () -> new MixerBlock(BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().noOcclusion()));


    //public static final RegistryObject<Block> SOFT_COVER = registerBlock("loose_moss", () -> new GoundCover(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET)));





    // ----------------------------------- QUICKSAND REGISTRY :O :O :O -----------------------------

    //TODO: When making new substances, make sure to include them in resources/data/qsrehydrated/tags/blocks/quicksand_drownable.json

    private final static BlockBehaviour.Properties baseBehavior = BlockBehaviour.Properties.copy(Blocks.SAND).noCollission().requiresCorrectToolForDrops()
            .noOcclusion().isViewBlocking((A, B, C) -> true);

    // canOcclude

    private final static BlockBehaviour.Properties baseFlowingBehavior = BlockBehaviour.Properties.copy(Blocks.SAND).noCollission().requiresCorrectToolForDrops()
            .noOcclusion().isViewBlocking((A, B, C) -> A.getValue(FlowingQuicksandBase.LEVEL) >= 4);
    private final static BlockBehaviour.Properties slimeBehavior = BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).noCollission()
            .noOcclusion().isViewBlocking((A, B, C) -> true).friction(1.0F).strength(2.5F);

    private final static BlockBehaviour.Properties woolBehavior = BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noCollission()
            .noOcclusion().isViewBlocking((A, B, C) -> true).friction(1.0F).strength(2.5F);



    public static final RegistryObject<Block> QUICKSAND = registerBlock("quicksand", () -> new Quicksand( baseBehavior.randomTicks(), new QuicksandBehavior()
            .setCoverageTexture("quicksand_coverage")
            .setSinkSpeed(.0005d)
            .setVertSpeed(.1d)
            .setWalkSpeed(new DepthCurve(0.9, 0.1))
    ));





//    public static final RegistryObject<Block> LIVING_SLIME = registerBlock("living_slime", () -> new LivingSlime( slimeBehavior, new QuicksandBehavior()
//            .setWobbleMove(0.025d)
//            .setWobbleTugHorizontal(new DepthCurve(0.08d, 0.06d))
//            .setVertSpeed(.4d)
//            .setSinkSpeed(new DepthCurve(new double[]{.001d, .000d, -.001d, .000d, .001d, .003d, .006d, .009d}))
//            .setWalkSpeed(new DepthCurve(1, .2))
//    ));

        public static final RegistryObject<Block> LIVING_SLIME = registerBlock("living_slime", () -> new QuicksandBase( slimeBehavior, new QuicksandBehavior()
            .setSinkSpeed(0.001d)
            .setWalkSpeed(new DepthCurve(new double[]{1d, 0.75d}))
            .setVertSpeed(0.85d)
            .setWobbleMove(new DepthCurve(new double[]{0.01d, 0.001d}))
            .setWobbleTugHorizontal(0.1)
            .setWobbleTugVertical(0.1)

            .addQuicksandEffect(QuicksandWobbleEffect.class)
    ));




    static QuicksandBehavior mudSinkable = new QuicksandBehavior()
            .setVertSpeed(0.4)
            .setWalkSpeed(new DepthCurve(new double[]{0.9, 0.55, 0.15, 0.1, 0.0}))
            .setSinkSpeed(0);

    public static final RegistryObject<Block> THIN_MUD = registerBlock("thin_mud", () -> new DeepMudBlock( baseBehavior.sound(SoundType.MUD), mudSinkable, 0.2d));
    public static final RegistryObject<Block> SHALLOW_MUD = registerBlock("shallow_mud", () -> new DeepMudBlock( baseBehavior.sound(SoundType.MUD), mudSinkable, 0.5d));
    public static final RegistryObject<Block> DEEP_MUD = registerBlock("deep_mud", () -> new DeepMudBlock( baseBehavior.sound(SoundType.MUD), mudSinkable, 1.0d));
    public static final RegistryObject<Block> BOTTOMLESS_MUD = registerBlock("bottomless_mud", () -> new DeepMudBlock( baseBehavior.sound(SoundType.MUD), mudSinkable, 2.5d));


    
    public static final RegistryObject<Block> SOFT_QUICKSAND = registerBlock("soft_quicksand", () -> new FlowingQuicksandBase(baseFlowingBehavior, new QuicksandBehavior()));







    static QuicksandBehavior quickrugSinkable = new QuicksandBehavior()
            .setVertSpeed(new DepthCurve(new double[]{.800d,          .600d,          .200d,          .100d,          .050d,  }))
            .setSinkSpeed(new DepthCurve(new double[]{.001d,  .00133d, .000d,.000d,   .000d,   .000d, .000d, .000d,   .000d,  }))
            .setWalkSpeed(new DepthCurve(new double[]{.900d,   .675d, .450d, .300d,   .100d,   .050d, .040d, .030d,   .020d,  }))

            .setWobbleMove(0.025d)
            .setWobbleTugHorizontal(new DepthCurve(0.08d, 0.06d))

            .setWobbleMove(new DepthCurve(new double[]{0.2d, 0.01d}))
            .setWobbleTugHorizontal(0.1)
            .setWobbleTugVertical(0.1)

            .addQuicksandEffect(QuicksandWobbleEffect.class);





    // ----------------------------------- Done! -----------------------------


    private static Collection<ItemStack> CREATIVELIST;

    public static void addItem(RegistryObject<Block> b) {CREATIVELIST.add(b.get().asItem().getDefaultInstance());}

    public static Collection<ItemStack> setupCreativeGroups() {
        CREATIVELIST = new ArrayList<>();
        addItem(QUICKSAND);

        addItem(THIN_MUD);
        addItem(SHALLOW_MUD);
        addItem(DEEP_MUD);
        addItem(BOTTOMLESS_MUD);

        addItem(SOFT_QUICKSAND);

        addItem(LIVING_SLIME);




        addItem(MIXER);
        return CREATIVELIST;
    }



    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }



}
