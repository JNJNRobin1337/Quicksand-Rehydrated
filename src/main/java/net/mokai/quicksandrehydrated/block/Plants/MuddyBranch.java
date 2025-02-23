package net.mokai.quicksandrehydrated.block.Plants;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.util.ModTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MuddyBranch extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty MODEL = IntegerProperty.create("shape", 0, 4);

    public MuddyBranch(Properties properties) {super(properties);}

    protected static final VoxelShape SHAPE =
            Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)1.0F, (double)16.0F);

    @Override
    public VoxelShape getShape(BlockState State, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState bs = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
        Optional<Integer> modelChoice = Util.getRandomSafe(List.of(0,1,2,3), pContext.getLevel().getRandom());
        if(modelChoice.isPresent()) {
            if(pContext.getPlayer().isLocalPlayer()) {
                return bs.setValue(MODEL, 4);
            } else {
                return bs.setValue(MODEL, modelChoice.get());
            }
        } else {
            return bs;
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(MODEL);
    }

}
