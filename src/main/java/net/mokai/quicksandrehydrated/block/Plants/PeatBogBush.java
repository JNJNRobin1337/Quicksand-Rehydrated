package net.mokai.quicksandrehydrated.block.Plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.data.ForgeBlockTagsProvider;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.mokai.quicksandrehydrated.registry.ModBlocks;
import net.mokai.quicksandrehydrated.util.ModTags;

public class PeatBogBush extends BushBlock {
    public PeatBogBush(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE = Block.box((double)2.0F, (double)0.0F, (double)2.0F, (double)14.0F, (double)13.0F, (double)14.0F);

    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean mayPlaceOn(BlockState state, BlockGetter getter, BlockPos pos) {
        return state.is(ModTags.Blocks.PEAT_BOG_BUSH) || super.mayPlaceOn(state, getter, pos);
    }
}
