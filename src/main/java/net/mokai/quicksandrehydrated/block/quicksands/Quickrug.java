package net.mokai.quicksandrehydrated.block.quicksands;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;

public class Quickrug extends QuicksandBase {
    public Quickrug(Properties pProperties, QuicksandBehavior QuicksandBehavior) {
        super(pProperties, QuicksandBehavior);
    }

    public void playStruggleSound(Entity pEntity, double struggleAmount) {
        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.25F, (pEntity.level().getRandom().nextFloat() * 0.25F) + 0.5F);
    }

}
