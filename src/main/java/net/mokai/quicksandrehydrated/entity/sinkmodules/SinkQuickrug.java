package net.mokai.quicksandrehydrated.entity.sinkmodules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.LivingSlime;

public class SinkQuickrug extends SinkData {

    public Vec3 WobblePosition = new Vec3(0, 0, 0);
    public Vec3 WobbleMomentum = new Vec3(0, 0, 0);
    public Vec3 WobblePosition_2 = new Vec3(0, 0, 0);
    public Vec3 WobbleMomentum_2 = new Vec3(0, 0, 0);

    public double slurpTimer = 0;

    public void non_quicksand_tick(Entity pEntity) {
        super.non_quicksand_tick(pEntity);
    }

}
