package net.mokai.quicksandrehydrated.entity.sinkmodules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.mokai.quicksandrehydrated.block.quicksands.LivingSlime;

public class SinkLivingSlime extends SinkData implements SinkAddonWobble {

    public double maxDepth = 0.0d;
    public BlockPos lastPos = BlockPos.ZERO;

    public void non_quicksand_tick(Entity pEntity) {

        super.non_quicksand_tick(pEntity);

        if (maxDepth > 0.5d) {

            BlockState lastState = pEntity.level().getBlockState(lastPos);

            if (lastState.getBlock() instanceof LivingSlime) {

                LivingSlime livingSlime = (LivingSlime) lastState.getBlock();

                //livingSlime.applyQuicksandEffects(lastState, pEntity.level(), lastPos, pEntity);

                double depth = livingSlime.getDepth(pEntity.level(), lastPos, pEntity);
                System.out.print("oo depth: ");
                System.out.print(depth);
                System.out.println("");

                livingSlime.quicksandTug(lastState, pEntity.level(), lastPos, pEntity, depth);
                livingSlime.quicksandTugMove(lastState, pEntity.level(), lastPos, pEntity, depth);

            }

        }

    }

}
