package net.mokai.quicksandrehydrated.entity.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;

public abstract class QuicksandEffect {

    // this is the base class for a quicksand effect.
    // it holds a value that needs to be dynamic (unique to the entity)
    // and acts upon it.

    public QuicksandEffect(BlockPos quicksandPos, Entity pEntity) {
    }





    public abstract boolean isActive();

    public abstract void tick();

    public abstract void effectEntity(BlockPos quicksandPos, Entity pEntity, QuicksandBehavior quicksandBehavior);

}
