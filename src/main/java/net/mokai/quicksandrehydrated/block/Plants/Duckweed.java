package net.mokai.quicksandrehydrated.block.Plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.util.ModTags;

public class Duckweed extends BushBlock {
    public Duckweed(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)1.0F, (double)16.0F);

    public VoxelShape getShape(BlockState State, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected boolean mayPlaceOn(BlockState state, BlockGetter getter, BlockPos pos) {
        return state.is(ModTags.Blocks.DUCKWEED) || super.mayPlaceOn(state, getter, pos);
    }
}
