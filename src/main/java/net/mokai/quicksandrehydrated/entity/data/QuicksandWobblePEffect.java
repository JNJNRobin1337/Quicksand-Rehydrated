package net.mokai.quicksandrehydrated.entity.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.util.EasingHandler;

public class QuicksandWobblePEffect extends QuicksandEffect {

    public Vec3 wobblePosition;

    public QuicksandWobblePEffect(BlockPos quicksandPos, Entity pEntity) {
        super(quicksandPos, pEntity);
        this.wobblePosition = pEntity.getPosition(0);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void tick() {

    }

    @Override
    public void effectEntity(BlockPos quicksandPos, Entity pEntity, QuicksandBehavior quicksandBehavior) {
        BlockState pState = pEntity.level().getBlockState(pEntity.blockPosition());
        Block b = pState.getBlock();
        if (b instanceof QuicksandBase qs) {

            Vec3 currentPos = pEntity.getPosition(0);
            double depth = EasingHandler.getDepth(pEntity, pEntity.level(), pEntity.blockPosition(),  0);

            // First, move the entity towards the wobble position
            Vec3 Momentum = pEntity.getDeltaMovement();
            entityQuicksandVar es = (entityQuicksandVar) pEntity;

            // the difference between the entity position, and the wobble pos.
            Vec3 differenceVec = wobblePosition.subtract(pEntity.getPosition(0));

            // apply momentum towards wobble pos to entity

            double hor = qs.getWobbleTugHorizontal(depth);
            double vert = qs.getWobbleTugVertical(depth);

            Vec3 addMomentum = differenceVec.multiply(new Vec3(hor, vert, hor));
            pEntity.setDeltaMovement(Momentum.add(addMomentum));


            // then, move the wobble pos slightly towards the entity
            wobblePosition = wobblePosition.lerp(currentPos, qs.getWobbleMove(depth));

        }
    }

}
