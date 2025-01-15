package net.mokai.quicksandrehydrated.block.Plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.util.ModTags;

public class MuddyBranch extends RotatedPillarBlock {
    public MuddyBranch(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)1.0F, (double)16.0F);

    public VoxelShape getShape(BlockState State, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
