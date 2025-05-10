package net.mokai.quicksandrehydrated.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.block.*;
import net.mokai.quicksandrehydrated.block.Plants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, QuicksandRehydrated.MOD_ID);

    public static final RegistryObject<Block> MIXER = registerBlock("mixer", () -> new MixerBlock(BlockBehaviour.Properties.of().strength(6f).requiresCorrectToolForDrops().noOcclusion()));


    //public static final RegistryObject<Block> SOFT_COVER = registerBlock("loose_moss", () -> new GoundCover(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET)));






    //Plant and stuff


    public static final RegistryObject<Block> DUCKWEED = registerBlock("duckweed", () -> new Duckweed(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noCollission().replaceable().sound(SoundType.GRASS).instabreak()));
    public static final RegistryObject<Block> DUCKWEED_FLOWERS = registerBlock("duckweed_flowers", () -> new Duckweed(BlockBehaviour.Properties.copy(Blocks.MOSS_CARPET).noCollission().replaceable().sound(SoundType.GRASS).instabreak()));
    public static final RegistryObject<Block> PEAT_BOG_BUSH = registerBlock("peat_bog_bush", () -> new PeatBogBush(BlockBehaviour.Properties.copy(Blocks.GRASS)));
    public static final RegistryObject<Block> FERN_BUSH = registerBlock("fern_bush", () -> new FernBush(BlockBehaviour.Properties.copy(Blocks.GRASS)));
    public static final RegistryObject<Block> CATTAIL_REEDS = registerBlock("cattails", () -> new Cattails(BlockBehaviour.Properties.copy(Blocks.GRASS)));
    public static final RegistryObject<Block> BRANCH = registerBlock("muddy_branch", () -> new MuddyBranch(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noCollission().noOcclusion().instabreak()));
    public static final RegistryObject<Block> CRANBERRY_BUSH = BLOCKS.register("cranberry_bush", () -> new CranBerryBush(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH).noOcclusion()));



    public static Collection<ItemStack> setupCreativeGroups() {
        CREATIVELIST = new ArrayList<>();
        addItem(MIXER);

        //Plant, crops and flowers//

        addItem(PEAT_BOG_BUSH);
        addItem(DUCKWEED);
        addItem(DUCKWEED_FLOWERS);
        addItem(FERN_BUSH);
        addItem(BRANCH);
        addItem(CATTAIL_REEDS);
        System.out.println("NATURAL CREATIVE TAB");
        return CREATIVELIST;
    }





    // ----------------------------------- Done! -----------------------------


    private static Collection<ItemStack> CREATIVELIST;

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
