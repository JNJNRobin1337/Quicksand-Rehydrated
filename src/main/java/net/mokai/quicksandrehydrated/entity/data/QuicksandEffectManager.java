package net.mokai.quicksandrehydrated.entity.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuicksandEffectManager {

    /**
     * QuicksandBehavior ... but in the entity (kinda)
     * This class stores, and manages the dynamic values needed for the various quicksand effects that need them.
     * -
     * When an entity steps into a quicksand block, the QS checks if the entity has the effects defined in QSBehavior
     * any effects that are not there are added to the entity, put into the effects list.
     * -
     * QuicksandEffects have timers that determine how long before they are unneeded.
     * Determined by the .isActive() method, the Manager will remove any effects no longer needed. Blub blub blub ...
     */

    public QuicksandEffectManager () {
    }

    public List<QuicksandEffect> effects = new ArrayList<QuicksandEffect>();

    public void addEffect(Class<? extends QuicksandEffect> c, BlockPos pPos, Entity pEntity) {

        // messing with classes like Class<? extends QuicksandEffect> means things can get strange

        try {
            QuicksandEffect effect = c.getDeclaredConstructor(BlockPos.class, Entity.class).newInstance(pPos, pEntity);
            effects.add(effect);
        } catch (Exception e) {
            System.err.println("OOPS. couldn't create this effect: " + c.getName());
        }

    }
    public void clear() {
        effects = new ArrayList<QuicksandEffect>();
    }

}
