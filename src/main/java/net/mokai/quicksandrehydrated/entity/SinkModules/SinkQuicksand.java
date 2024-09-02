package net.mokai.quicksandrehydrated.entity.SinkModules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.Quicksand;

import static org.joml.Math.clamp;

public class SinkQuicksand implements SinkDataInterface {

    int ticks = 60*5;
    int timer = 0;
    String tag = "";

    boolean active = false;

    double liquefaction = 0.0;

    @Override
    public void generic_tick(Entity pEntity) {
        timer += 1;
        System.out.println("SinkQuicksand Module tick: "+ticks);
        if (!this.isStale()) {
            if (ticks > 0) {
                ticks -= 1;
            }
        }
    }

    @Override
    public void quicksand_tick(Entity pEntity, BlockPos pPos, BlockState pState) {
//        if (!this.isStale()) {
//
//            System.out.println("in quicksand: "+ liquefaction);
//
//            if (pState.getBlock() instanceof Quicksand) {
//                ticks = 60 * 10;
//
//                double hori_movement = pEntity.getDeltaMovement().multiply(new Vec3(1.0, 0.0, 1.0)).length();
//
//                hori_movement = clamp(hori_movement, 0, 0.1)/0.1;
//
//                liquefaction += hori_movement * (0.05/20);
//                liquefaction = clamp(liquefaction, 0.0, 1.0);
//
//                pEntity.addDeltaMovement(new Vec3(0.0, -0.02 * liquefaction, 0.0));
//
//            }
//
//        }
    }

    @Override
    public void non_quicksand_tick(Entity pEntity) {

        if (!this.isStale()) {
            System.out.println("NOT in quicksand: "+ liquefaction);
            liquefaction *= 0.95;
        }

    }

    @Override
    public boolean isStale() {
        return ticks <= 0;
    }
}
