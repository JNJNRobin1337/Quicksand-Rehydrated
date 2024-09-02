package net.mokai.quicksandrehydrated.entity.SinkModules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface SinkDataInterface {

    void generic_tick(Entity pEntity);
    void quicksand_tick(Entity pEntity, BlockPos pPos, BlockState pState);
    void non_quicksand_tick(Entity pEntity);

//    is this active, or should it be ignored and deleted?
    boolean isStale();


}
