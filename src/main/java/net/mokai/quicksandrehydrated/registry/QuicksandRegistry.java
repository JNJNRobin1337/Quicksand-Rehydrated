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
import net.mokai.quicksandrehydrated.block.quicksands.DeepMudBlock;
import net.mokai.quicksandrehydrated.block.quicksands.MossyPeatBog;
import net.mokai.quicksandrehydrated.block.quicksands.Quickrug;
import net.mokai.quicksandrehydrated.block.quicksands.Quicksand;
import net.mokai.quicksandrehydrated.block.quicksands.core.FlowingQuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.entity.data.QuicksandWobbleMEffect;
import net.mokai.quicksandrehydrated.entity.data.QuicksandWobblePEffect;
import net.mokai.quicksandrehydrated.util.BodyDepthThreshold;
import net.mokai.quicksandrehydrated.util.DepthCurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static net.mokai.quicksandrehydrated.util.DepthCurve.Vec2;

public class QuicksandRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, QuicksandRehydrated.MOD_ID);

    // --------------------------------- Block properties -----------------------------


    private final static BlockBehaviour.Properties baseBlockBehavior = BlockBehaviour.Properties.copy(Blocks.SAND).noCollission().requiresCorrectToolForDrops()
            .noOcclusion().isViewBlocking((A, B, C) -> true).forceSolidOn();
    private final static BlockBehaviour.Properties muddyBlockBehavior = BlockBehaviour.Properties.copy(Blocks.MUD).noCollission().requiresCorrectToolForDrops()
            .noOcclusion().isViewBlocking((A, B, C) -> true).forceSolidOn();
    private final static BlockBehaviour.Properties baseFlowingBlockBehavior = BlockBehaviour.Properties.copy(Blocks.SAND).noCollission().requiresCorrectToolForDrops()
            .noOcclusion().isViewBlocking((A, B, C) -> A.getValue(FlowingQuicksandBase.LEVEL) >= 4).forceSolidOn();
    private final static BlockBehaviour.Properties slimeBlockBehavior = BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).noCollission()
            .noOcclusion().isViewBlocking((A, B, C) -> true).friction(1.0F).strength(2.5F).forceSolidOn();
    private final static BlockBehaviour.Properties woolBlockBehavior = BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noCollission()
            .noOcclusion().isViewBlocking((A, B, C) -> true).friction(1.0F).strength(2.5F).forceSolidOn();


    // --------------------------------- Quicksand properties -----------------------------

    static QuicksandBehavior MudBehavior = new QuicksandBehavior()
            .setVertSpeed(0.4)
            .setWalkSpeed(new DepthCurve(new double[]{0.9, 0.55, 0.15, 0.1, 0.0}))
            .setSinkSpeed(0)
            .setBuoyancyPoint(BodyDepthThreshold.KNEE.depth) // Valore di buoyancy per le ginocchia
            .setCoverageTexture("mud_coverage")
            .setResurfingForce(0.025); // Moderate resurfing force for mud


    static QuicksandBehavior quickrugSinkable = new QuicksandBehavior()

            .setVertSpeed(new ArrayList<>(List.of(Vec2(0.3, 0.0), Vec2(0.1, 0.35/2.0), Vec2(0.05, 1.0/2.0)
            )))

            .setSinkSpeed(new ArrayList<>(List.of(
                    Vec2(0.002, 0.0),
                    Vec2(0.0014, 0.3/2.0),
                    Vec2(0.0003, 0.4/2.0),
                    Vec2(0.0003, 1.0/2.0)
            )))

            .setWalkSpeed(new ArrayList<>(List.of(
                    Vec2(1.0, 0.0),
                    Vec2(0.9, 0.35/2.0),
                    Vec2(0.1, 0.5/2.0),
                    Vec2(0.0, 1.0/2.0)
            )))

            .setWobbleMove(0.9d)
            .setWobbleDecay(0.94d)
            .setWobbleRebound(0.4d/20.0d)
            .setWobbleApply(-0.5d/20.0d)

            .setStepOutHeight(BodyDepthThreshold.FEET.depth)
            .setBuoyancyPoint(BodyDepthThreshold.FEET.depth) // Valore di buoyancy per le caviglie
            .setResurfingForce(0.06) // High resurfing force for quickrug

            .addQuicksandEffect(QuicksandWobbleMEffect.class);





    // ----------------------------------- QUICKSAND REGISTRY :O :O :O -----------------------------

    //TODO: When making new substances, make sure to include them in resources/data/qsrehydrated/tags/blocks/quicksand_drownable.json


    public static final RegistryObject<Block> QUICKSAND = registerBlock("quicksand", () -> new Quicksand( baseBlockBehavior.randomTicks(), new QuicksandBehavior()
            .setCoverageTexture("quicksand_coverage")
            .setSinkSpeed(.0005d)
            .setVertSpeed(.1d)
            .setWalkSpeed(new DepthCurve(0.9, 0.1))
            .setBuoyancyPoint(BodyDepthThreshold.KNEE.depth) // Valore di buoyancy per le ginocchia (ridotto da CHEST)
            .setResurfingForce(0.03) // Default resurfing force
    ));

    public static final RegistryObject<Block> LIVING_SLIME = registerBlock("living_slime", () -> new QuicksandBase( slimeBlockBehavior, new QuicksandBehavior()
            .setSinkSpeed(0.001d)
            .setWalkSpeed(new DepthCurve(new double[]{1d, 0.75d}))
            .setVertSpeed(0.85d)
            .setWobbleMove(new DepthCurve(new double[]{0.01d, 0.001d}))
            .setWobbleTugHorizontal(0.1)
            .setWobbleTugVertical(0.1)
            .setBuoyancyPoint(BodyDepthThreshold.KNEE.depth) // Valore di buoyancy per le ginocchia (ridotto da WAIST)
            .setResurfingForce(0.05) // Higher resurfing force for slime

            .addQuicksandEffect(QuicksandWobblePEffect.class)
            .setCoverageTexture("slime_coverage")
    ));






    public static final RegistryObject<Block> MOSSY_PEAT_BOG = registerBlock("mossy_peat_bog", () -> new MossyPeatBog(muddyBlockBehavior, new QuicksandBehavior()
            .setSinkSpeed(new DepthCurve(new double[]{0.008, 0, -0.003, -0.003}))
            .setVertSpeed(.1)
            .setWalkSpeed(.4)

            .setWobbleMove(.6)
            .setWobbleTugHorizontal(.4)
            .setWobbleTugVertical(0)
            .setCoverageTexture("peat_bog_coverage")
            .setResurfingForce(0.02) // Lower resurfing force for bog

            .setBuoyancyPoint(BodyDepthThreshold.KNEE.depth) // Valore di buoyancy per le ginocchia
    ));

    public static final RegistryObject<Block> PEAT_BOG = registerBlock("peat_bog", () -> new MossyPeatBog(muddyBlockBehavior, new QuicksandBehavior()
            .setSinkSpeed(new DepthCurve(new double[]{0.008, 0.006, 0.0045, 0.0030, 0.0015, 0.003, 0.001}))
            .setVertSpeed(.08)
            .setWalkSpeed(.4)

            .setWobbleMove(.6)
            .setWobbleTugHorizontal(.4)
            .setBuoyancyPoint(BodyDepthThreshold.KNEE.depth) // Valore di buoyancy per le ginocchia (ridotto da coscia alta)
            .setResurfingForce(0.02) // Lower resurfing force for bog

            .setBubbleChance(.9)
            .setCoverageTexture("peat_bog_coverage")
    ));




    public static final RegistryObject<Block> BOG = registerBlock("bog", () -> new QuicksandBase(muddyBlockBehavior, new QuicksandBehavior()
            .setBuoyancyPoint(BodyDepthThreshold.ABDOMEN.depth) // Valore di buoyancy per le ginocchia (ridotto da WAIST)
            .setResurfingForce(0.005) // Lower resurfing force for bog
    ));


    public static final RegistryObject<Block> THIN_MUD = registerBlock("thin_mud", () -> new DeepMudBlock( muddyBlockBehavior.sound(SoundType.MUD), MudBehavior, 0.2d));
    public static final RegistryObject<Block> SHALLOW_MUD = registerBlock("shallow_mud", () -> new DeepMudBlock( muddyBlockBehavior.sound(SoundType.MUD), MudBehavior, 0.5d));
    public static final RegistryObject<Block> DEEP_MUD = registerBlock("deep_mud", () -> new DeepMudBlock( muddyBlockBehavior.sound(SoundType.MUD), MudBehavior, 1.0d));
    public static final RegistryObject<Block> BOTTOMLESS_MUD = registerBlock("bottomless_mud", () -> new DeepMudBlock( muddyBlockBehavior.sound(SoundType.MUD), MudBehavior, 2.5d));

    public static final RegistryObject<Block> SOFT_QUICKSAND = registerBlock("soft_quicksand", () -> new FlowingQuicksandBase(baseFlowingBlockBehavior, new QuicksandBehavior()
            .setBuoyancyPoint(BodyDepthThreshold.ABDOMEN.depth) // Valore di buoyancy per le ginocchia (ridotto da SHOULDERS)
            .setResurfingForce(0.04) // Higher resurfing force for soft quicksand
    ));


    public static final RegistryObject<Block> WHITE_QUICKRUG = registerBlock("white_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.SNOW), quickrugSinkable));
    public static final RegistryObject<Block> ORANGE_QUICKRUG = registerBlock("orange_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_ORANGE), quickrugSinkable));
    public static final RegistryObject<Block> MAGENTA_QUICKRUG = registerBlock("magenta_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_MAGENTA), quickrugSinkable));
    public static final RegistryObject<Block> LIGHT_BLUE_QUICKRUG = registerBlock("light_blue_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_LIGHT_BLUE), quickrugSinkable));
    public static final RegistryObject<Block> YELLOW_QUICKRUG = registerBlock("yellow_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_YELLOW ), quickrugSinkable));
    public static final RegistryObject<Block> LIME_QUICKRUG = registerBlock("lime_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_LIGHT_GREEN), quickrugSinkable));
    public static final RegistryObject<Block> PINK_QUICKRUG = registerBlock("pink_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_PINK), quickrugSinkable));
    public static final RegistryObject<Block> GRAY_QUICKRUG = registerBlock("gray_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_GRAY), quickrugSinkable));
    public static final RegistryObject<Block> LIGHT_GRAY_QUICKRUG = registerBlock("light_gray_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_LIGHT_GRAY), quickrugSinkable));
    public static final RegistryObject<Block> CYAN_QUICKRUG = registerBlock("cyan_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_CYAN), quickrugSinkable));
    public static final RegistryObject<Block> PURPLE_QUICKRUG = registerBlock("purple_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_PURPLE), quickrugSinkable));
    public static final RegistryObject<Block> BLUE_QUICKRUG = registerBlock("blue_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_BLUE), quickrugSinkable));
    public static final RegistryObject<Block> BROWN_QUICKRUG = registerBlock("brown_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_BROWN), quickrugSinkable));
    public static final RegistryObject<Block> GREEN_QUICKRUG = registerBlock("green_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_GREEN), quickrugSinkable));
    public static final RegistryObject<Block> RED_QUICKRUG = registerBlock("red_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_RED), quickrugSinkable));
    public static final RegistryObject<Block> BLACK_QUICKRUG = registerBlock("black_quickrug", () -> new Quickrug( woolBlockBehavior.mapColor(MapColor.COLOR_BLACK), quickrugSinkable));



    public static Collection<ItemStack> setupCreativeGroups() {
        CREATIVELIST = new ArrayList<>();
        addItem(QUICKSAND);
        addItem(LIVING_SLIME);
        addItem(SOFT_QUICKSAND);
        addItem(BOG);
        addItem(PEAT_BOG);
        addItem(MOSSY_PEAT_BOG);
        addItem(THIN_MUD);
        addItem(SHALLOW_MUD);
        addItem(DEEP_MUD);
        addItem(BOTTOMLESS_MUD);

        addItem(WHITE_QUICKRUG);
        addItem(ORANGE_QUICKRUG);
        addItem(MAGENTA_QUICKRUG);
        addItem(LIGHT_BLUE_QUICKRUG);
        addItem(YELLOW_QUICKRUG);
        addItem(LIME_QUICKRUG);
        addItem(PINK_QUICKRUG);
        addItem(GRAY_QUICKRUG);
        addItem(LIGHT_GRAY_QUICKRUG);
        addItem(CYAN_QUICKRUG);
        addItem(PURPLE_QUICKRUG);
        addItem(BLUE_QUICKRUG);
        addItem(BROWN_QUICKRUG);
        addItem(GREEN_QUICKRUG);
        addItem(RED_QUICKRUG);
        addItem(BLACK_QUICKRUG);
        return CREATIVELIST;
    }





    // ----------------------------------- Done! -----------------------------


    public static Collection<ItemStack> CREATIVELIST;

    public static void addItem(RegistryObject<Block> b) {CREATIVELIST.add(b.get().asItem().getDefaultInstance());}

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
