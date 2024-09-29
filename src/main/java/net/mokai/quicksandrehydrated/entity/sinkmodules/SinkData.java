package net.mokai.quicksandrehydrated.entity.sinkmodules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.mokai.quicksandrehydrated.block.quicksands.LivingSlime;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;

public class SinkData {
    int cooldown = 20 * 6;
    public int ticks = cooldown;

    public void quicksand_tick(Entity pEntity, BlockPos pPos, BlockState pState) {

//        System.out.println("in quicksand!");

        if (pState.getBlock() instanceof QuicksandBase) {
            if (ticks != cooldown) {
                ticks = cooldown;
            }
        }
        else {
            ticks -= 1;
        }
    }
    public void non_quicksand_tick(Entity pEntity) {

//        System.out.println("out of qs");
//
//        System.out.print("Sinkdata ");
//        System.out.print(ticks);
//        System.out.println();
        ticks -= 1;

    }

    public boolean isStale() {
        return ticks <= 0;
    }


}
