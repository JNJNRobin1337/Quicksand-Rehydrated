package net.mokai.quicksandrehydrated.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.entity.SinkModules.SinkDataInterface;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_DROWNABLE;

@Mixin(Entity.class)
public abstract class SlowdownMixin implements entityQuicksandVar {

    @Shadow
    protected Vec3 stuckSpeedMultiplier;

    Vec3 quicksandMultiplier = Vec3.ZERO;
    public Vec3 getQuicksandMultiplier() {return this.quicksandMultiplier;}
    public void setQuicksandMultiplier(Vec3 set) {this.quicksandMultiplier = set;}
    public void multiOrSetQuicksandMultiplier(Vec3 set) {
        if (this.quicksandMultiplier.length() > 1.0E-6D) {
            this.quicksandMultiplier = this.quicksandMultiplier.multiply(set);
        }
        else {
            this.quicksandMultiplier = set;
        }
    }

    Vec3 quicksandAdditive = Vec3.ZERO;
    public Vec3 getQuicksandAdditive() {return this.quicksandAdditive;}
    public void setQuicksandAdditive(Vec3 set) {this.quicksandAdditive = set;}
    public void addQuicksandAdditive(Vec3 set) {
        this.quicksandAdditive = quicksandAdditive.add(set);
    }
    public void multiplyQuicksandAdditive(Vec3 set) {
        this.quicksandAdditive = quicksandAdditive.multiply(set);
    }


    @Shadow public abstract boolean shouldRender(double p_20296_, double p_20297_, double p_20298_);

    List<SinkDataInterface> sinkData = new ArrayList<SinkDataInterface>();

    public List<SinkDataInterface> getSinkData() {
        return sinkData;
    }

    public void setSinkData(List<SinkDataInterface> set) {
        sinkData = set;
    }

    public void addSinkModule(SinkDataInterface addModule) {
        sinkData.add(addModule);
    }
    public boolean hasSinkModule(Class<?> cls) {
        for (SinkDataInterface module: sinkData) {
            if (cls.equals(module.getClass())) {
                return true;
            }
        }
        return false;
    }


//    public SinkDataInterface accessData(Class<?> cls) {
//
//        // in which I fuck around, and find out.
//
//        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)this;
//        List<SinkDataInterface> sinkDataList = QuicksandVarEntity.getSinkData();
//
//        for (SinkDataInterface module: sinkDataList) {
//            if (cls.equals(module.getClass())) {
//                return module;
//            }
//        }
//
//        // it is heavily encouraged that Class<?> should implement SinkDataInterface.
//        try {
//            // attempt to instantiate the SinkDataInterface
//            SinkDataInterface sink = (SinkDataInterface) cls.getDeclaredConstructor().newInstance();
//            sinkDataList.add(sink);
//            return sink;
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException err) {
//            throw new RuntimeException("Failed to instantiate class " + cls.getName(), err);
//        }
//
//    }




    public boolean changed = false;
    public double horizontal = Double.MAX_VALUE;
    public double vertical = Double.MAX_VALUE;

    public Vec3 previousPosition = new Vec3(0.0, 0.0, 0.0);

    public Vec3 getPreviousPosition() {return this.previousPosition;}
    public void setPreviousPosition(Vec3 set) {this.previousPosition = set;}




    public boolean inQuicksand = false;
    public boolean getInQuicksand() {
        return this.inQuicksand;
    }
    public void setInQuicksand(boolean set) {
        this.inQuicksand = set;
    }



    public boolean quicksandEnterFlag = false;
    public boolean getquicksandEnterFlag() {
        return this.quicksandEnterFlag;
    }
    public void setquicksandEnterFlag(boolean set) {
        this.quicksandEnterFlag = set;
    }



//    @Inject(method = "collide", at = @At("HEAD"))
//    private void collide(Vec3 pVec, CallbackInfoReturnable<Vec3> cir) {
//
//        Entity thisEntity = (Entity)(Object)this;
//        BlockState test = thisEntity.getFeetBlockState();
//
//        this.onGround = this.onGround || test.getBlock() instanceof QuicksandBase;
//
//    }







    @Nullable
    public BlockPos getStuckBlock(Entity pEntity) {
        // get the block this entity is stuck in.
        // NOT just the blockpos - it looks around to determine which one

        Level pLevel = pEntity.level();

        Vec3 ePos = pEntity.position();
        Level eLevel = pEntity.level();

        if (eLevel.getBlockState(pEntity.blockPosition()).getBlock() instanceof QuicksandBase) {
            return pEntity.blockPosition();
        }

        // offset into middle of current block.

        ePos = ePos.add(-0.5, 0, -0.5);
        Vec3i vec3i = new Vec3i((int) ePos.x, (int) ePos.y, (int) ePos.z);
        BlockPos blockPosOffset = new BlockPos(vec3i);

        // there is bias towards the southwest block currently
        // Should eventually change to a more sophisticated approach
        // that accurately gets the closest quicksand block, instead.

        if (eLevel.getBlockState(blockPosOffset).getBlock() instanceof QuicksandBase) {
            return blockPosOffset;
        }
        else if (eLevel.getBlockState(blockPosOffset.north()).getBlock() instanceof QuicksandBase) {
            return  blockPosOffset.north();
        }
        else if (eLevel.getBlockState(blockPosOffset.west()).getBlock() instanceof QuicksandBase) {
            return  blockPosOffset.west();
        }
        else if (eLevel.getBlockState(blockPosOffset.north().west()).getBlock() instanceof QuicksandBase) {
            return blockPosOffset.north().west();
        }
        return null;

    }



    @Inject(method = "tick", at = @At("HEAD"))
    private void tickEnd(CallbackInfo ci) {

        Entity thisEntity = (Entity)(Object)this;
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)this;

        List<SinkDataInterface> sinkDataList = QuicksandVarEntity.getSinkData();
        boolean shouldRefresh = false;

        // execute tick function of every module
        for (SinkDataInterface module: sinkDataList) {
            if (!module.isStale()) {
                module.generic_tick(thisEntity);
            }
            else {
                shouldRefresh = true;
            }
        }

        // then refresh and remove stale ones, only when needed
        if (shouldRefresh) {
            List<SinkDataInterface> newList = new ArrayList<SinkDataInterface>();
            for (SinkDataInterface module : sinkDataList) {
                if (!module.isStale()) {
                    newList.add(module);
                }
            }
            QuicksandVarEntity.setSinkData(newList);
        }



        if (!QuicksandVarEntity.getInQuicksand()) {
            // if not in quicksand, move previous position directly to position
            QuicksandVarEntity.setPreviousPosition(thisEntity.getPosition(0));

            if (QuicksandVarEntity.getquicksandEnterFlag()) {
                // set the enter flag to false as well
                QuicksandVarEntity.setquicksandEnterFlag(false);
            }

            for (SinkDataInterface module: sinkDataList) {
                if (!module.isStale()) {
                    module.non_quicksand_tick(thisEntity);
                }
            }

        }
        else {

            // white effect == previous position variable
            //Vec3 PrevPos = QuicksandVarEntity.getPreviousPosition();
            //thisEntity.getLevel().addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(), PrevPos.x(), PrevPos.y(), PrevPos.z(), 0.0D, 0.0D, 0.0D);

            BlockPos bp = getStuckBlock(thisEntity);

            if (bp != null) {
                Level eLevel = thisEntity.level();
                BlockState bs = eLevel.getBlockState(bp);

                for (SinkDataInterface module: sinkDataList) {
                    if (!module.isStale()) {
                        module.quicksand_tick(thisEntity, bp, bs);
                    }
                }

                QuicksandBase qs = (QuicksandBase) bs.getBlock();
                qs.applyQuicksandEffects(bs, eLevel, bp, thisEntity);

            }

        }

        // this is dumb?! why?
        QuicksandVarEntity.setInQuicksand(false);

    }


    /**
     *
     * @param pState
     * @param spd
     * @author Mokai
     * @reason makeStuckInBlock simply overwrote stuckSpeedMultiplier, which lead to instances of directional priority; now, it only accepts the strongest slowdown applied that tick.
     */

    @Overwrite
    public void makeStuckInBlock(BlockState pState, Vec3 spd) {

        // It's not that easy.

        if (horizontal > spd.x() || horizontal > spd.y() || !changed) {

            if (!changed) { horizontal = spd.x(); vertical = spd.y(); }
            if (horizontal > spd.x()) { horizontal = spd.x(); }
            if (vertical > spd.y()) { vertical = spd.y(); }
            changed = true;
            stuckSpeedMultiplier = new Vec3(horizontal, vertical, horizontal);
        }
        Entity e = (Entity)(Object) this;
        e.resetFallDistance();

        // It's that easy.
    }


    @Inject(method = "collide", at = @At("HEAD"))
    private void collide(Vec3 pVec, CallbackInfoReturnable<Vec3> cir) {

        Entity thisEntity = (Entity)(Object)this;
        BlockState test = thisEntity.getFeetBlockState();
        thisEntity.setOnGround(thisEntity.onGround() || test.getTags().toList().contains(QUICKSAND_DROWNABLE));
        // Allows for step-up even while falling if your center is inside a Quicksand_Drownable.

    }

    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    public void emperorsNewMove(MoverType mv, Vec3 spd, final CallbackInfo ci)
    {
        changed = false;
    }

    @Inject(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void quicksandModifier(MoverType p_19973_, Vec3 p_19974_, CallbackInfo ci, @Local LocalRef<MoverType> moverTypeLocalRef, @Local LocalRef<Vec3> Vec3localRef) {



    }

    @ModifyVariable(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "HEAD"),
            index = 2,
            argsOnly = true
    )
    private Vec3 injected(Vec3 momentum) {

        Entity thisEntity = (Entity)(Object)this;
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)this;

        Vec3 quicksandMod = QuicksandVarEntity.getQuicksandMultiplier();
        if (quicksandMod.length() > 1.0E-6D) {
            momentum = momentum.multiply(quicksandMod);
            QuicksandVarEntity.setQuicksandMultiplier(Vec3.ZERO);
        }

        Vec3 quicksandAdd = QuicksandVarEntity.getQuicksandAdditive();
        if (quicksandAdd.length() > 1.0E-6D) {
            momentum = momentum.add(quicksandAdd);
            QuicksandVarEntity.setQuicksandAdditive(Vec3.ZERO);
        }

        thisEntity.setDeltaMovement(momentum);
        return momentum;
    }




    public boolean entityCanStepOut(Entity pEntity) {

        Entity thisEntity = (Entity)(Object)this;
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)this;

        BlockPos stuckBlockPos = QuicksandVarEntity.getStuckBlock(thisEntity);
        if (stuckBlockPos != null) {
            Block stuckBlock = thisEntity.level().getBlockState(stuckBlockPos).getBlock();

            if (stuckBlock instanceof QuicksandBase) {
                QuicksandBase qsBlock = (QuicksandBase) stuckBlock;
                double depth = qsBlock.getDepth(thisEntity.level(), thisEntity.blockPosition(), thisEntity);
                return qsBlock.canStepOut(depth);
            }
        }
        return false;

    }



    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void onMoveAfterSetOnGround(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        Entity thisEntity = (Entity)(Object)this;
        if (entityCanStepOut(thisEntity)) {

            boolean notFlyingPlayer = true;

            if (thisEntity instanceof Player) {
                Player p = (Player) thisEntity;
                notFlyingPlayer = !p.getAbilities().flying;
            }

            if (notFlyingPlayer) {
                thisEntity.setOnGround(true);
            }
        }
    }

}
