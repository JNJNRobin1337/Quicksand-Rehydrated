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

public class QuicksandWobbleMEffect extends QuicksandEffect {

    public Vec3 wobbleMomentum;
    public Vec3 wobbleMomentumMomentum;

    public QuicksandWobbleMEffect(BlockPos quicksandPos, Entity pEntity) {
        super(quicksandPos, pEntity);
        this.wobbleMomentum = new Vec3(0, 0, 0);
        this.wobbleMomentumMomentum = new Vec3(0, 0, 0);
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

            double decay = qs.getWobbleDecay(depth);
            double rebound = qs.getWobbleRebound(depth);
            double apply = qs.getWobbleApply(depth);
            double move = qs.getWobbleMove(depth);

            wobbleMomentumMomentum = wobbleMomentumMomentum.multiply(decay, decay, decay);
            wobbleMomentumMomentum = wobbleMomentumMomentum.subtract(wobbleMomentum.multiply(rebound, rebound, rebound));
            wobbleMomentumMomentum = wobbleMomentumMomentum.add(Momentum.multiply(apply, apply, apply));

            wobbleMomentum = wobbleMomentum.add(wobbleMomentumMomentum);

            // Rimuovi i log di sistema per migliorare le prestazioni

            double scale = 10.0;
            double len = wobbleMomentum.length()*scale;
            double sqrtAfter = Math.pow(len, 0.5) / scale;
            double sqrtFactor = sqrtAfter/len;

            if (len == 0) {
                sqrtFactor = 1.0d;
            }

            Vec3 addMomentum = wobbleMomentum.multiply(move*sqrtFactor, move*sqrtFactor, move*sqrtFactor);
            pEntity.setDeltaMovement(Momentum.add(addMomentum));

        }
    }

}
