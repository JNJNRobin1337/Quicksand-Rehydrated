package net.mokai.quicksandrehydrated.util;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> QUICKSAND_DROWNABLE
                = tag("quicksand_drownable");

        public static final TagKey<Block> QUICKSAND_AO_OVERRIDE
                = tag("quicksand_ao_override");

        public static final TagKey<Block> DUCKWEED
                = tag("duckweed");

        public static final TagKey<Block> FERN_BUSH
                = tag("fern_bush");

        public static final TagKey<Block> PEAT_BOG_BUSH
                = tag("peat_bog_bush");


        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(QuicksandRehydrated.MOD_ID, name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> QUICKSAND_DROWNABLE_FLUID = tag("quicksand_drownable_fluid");

        private static TagKey<Fluid> tag(String name) {
            return FluidTags.create(new ResourceLocation(QuicksandRehydrated.MOD_ID, name));
        }

        private static TagKey<Fluid> forgeTag(String name) {
            return FluidTags.create(new ResourceLocation("forge", name));
        }

    }
}
