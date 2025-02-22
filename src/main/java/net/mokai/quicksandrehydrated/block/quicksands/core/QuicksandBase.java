package net.mokai.quicksandrehydrated.block.quicksands.core;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.entity.EntityBubble;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.util.EasingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_DROWNABLE;
import static net.mokai.quicksandrehydrated.util.ModTags.Fluids.QUICKSAND_DROWNABLE_FLUID;
import static org.joml.Math.abs;

/**
 * This is where the <font color=red>M</font>
 * <font color=orange>A</font>
 * <font color=yellow>G</font>
 * <font color=green>I</font>
 * <font color=blue>C</font> is going on!
 * This is the base of Blocks to function as Quicksand blocks.
 * Override the specific methods to achieve non-default behavior when implementing other
 * sinking blocks.
 */
public class QuicksandBase extends Block implements QuicksandInterface {

    private final Random rng = new Random();

    public QuicksandBehavior QSBehavior;
    public QuicksandBehavior getQuicksandBehavior() {return QSBehavior;}

    public QuicksandBase(Properties pProperties, QuicksandBehavior QuicksandBehavior) {
        super(pProperties);
        this.QSBehavior = QuicksandBehavior;
    }


    // ----- OVERRIDE AND MODIFY THESE VALUES FOR YOUR QUICKSAND TYPE ----- //


    /**
     * The relative offset in blocks, the sinking mechanic should respect.
     * This allows not full blocks (think of mud with a water surface) to function with
     * the actual sinking mechanic.
     * @param blockstate The BlockState.
     * @return
     * <p>The offset in blocks, that the sinking mechanic will subtract.</p>
     * <p><code>0</code> (default) means, that the sinking mechanic starts directly at the surface.</p>
     * <p><code>0.5</code> means, the block is considered as half a block, so "sinking" starts only below this level.</p>
     */
    public double getOffset(BlockState blockstate) {
        return 0d;
    }



    public double getDepth(Level pLevel, BlockPos pPos, Entity pEntity) {
        // Are we accounting for player height? e.g. should a player fully under be at 2.0, or 1.0?
        return EasingHandler.getDepth(pEntity, pLevel, pPos, getOffset(null));

    }


    /** the previous position will move towards the player this amount *per tick* !
    * You can think of this as how "sticky" the quicksand is.
    * 1.0 = no effect on player's movement
    * 0.0 = player effectively cannot move unless they manage to stop touching the
    * QS block.
    **/
    public double getTugLerp(double depthRaw) {
        return EasingHandler.doubleListInterpolate(depthRaw / 2, new double[] { 1.0, 1.0 });
    }

    /** how much the player is pulled backwards
    * You can think of this as how *strong* the sticky effect of the quicksand is
    * 1.0 = player *cannot* move away from the previous position, fully locked in
    * place.
    * 0.0 = no effect on player's movement
     */
    public double getTug(double depthRaw) {
        return EasingHandler.doubleListInterpolate(depthRaw / 2, new double[] { 0.0, 0.0 });
    }

    /**
     * whether the player can jump or not.
     * Don't use A RNG value or there will be jittering, as this applies a small
     * amount of friction.
     * this also currently dictates whether the player can step up over block edges
     * (like stairs, 1/2 a block at most)
     * @param depth The depth, the player is currently stuck in.
     * @return <code>true</code>, if the player is allowed to jump. <code>false</code> otherwise.
     */
    public boolean getJump(double depth) {
        return depth < 0.25;
    }

    // Don't override anything below unless you know what you're doing!
    public void quicksandTugMove(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        Vec3 currentPos = pEntity.getPosition(0);

        // Get the Previous Position variable
        Vec3 prevPos = es.getPreviousPosition();

        // move previous pos towards player by set amount
        es.setPreviousPosition(prevPos.lerp(currentPos, QSBehavior.getTugLerp(depth)));

    }

    public void quicksandTug(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        Vec3 Momentum = pEntity.getDeltaMovement();
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        // the difference between the entity position, and the previous position.
        Vec3 differenceVec = es.getPreviousPosition().subtract(pEntity.getPosition(0));

        // apply momentum towards previous pos to entity
        double spd = getTug(depth);
        Vec3 addMomentum = differenceVec.multiply(new Vec3(spd, spd, spd));
        pEntity.setDeltaMovement(Momentum.add(addMomentum));

    }

    public void quicksandMomentum(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        // get quicksand Variables
        double walk = QSBehavior.getWalkSpeed(depth);
        double vert = QSBehavior.getVertSpeed(depth);
        double sink = QSBehavior.getSinkSpeed(depth);

        // sinking is a replacement for gravity.
        Vec3 Momentum = pEntity.getDeltaMovement();
        boolean playerFlying = false;
        if (pEntity instanceof Player) {
            Player p = (Player) pEntity;
            playerFlying = p.getAbilities().flying;
        }
        if (!playerFlying) {
            if (vert != 0.0) {
                sink = sink / vert; // counteract vertical thickness (?)
            }
            Momentum = Momentum.add(0.0, -sink, 0.0);
        }

        Momentum = Momentum.multiply(walk, vert, walk);
        pEntity.setDeltaMovement(Momentum);

    }

    public void applyQuicksandEffects(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
            @NotNull Entity pEntity) {

        if (pEntity instanceof EntityBubble) {
            return;
        }

        double depth = getDepth(pLevel, pPos, pEntity);

        if (depth >= 0) {

            // there are three main effects that happen every tick.

            // first, the quicksand's main effects are applied. Thickness and sinking.
            quicksandMomentum(pState, pLevel, pPos, pEntity, depth);

            // the next two deal with the entity's "Previous Position" variable.
            // This can be used in different ways to get different effects.

            // first the player is pushed towards this position a small amount
            quicksandTug(pState, pLevel, pPos, pEntity, depth);

            // then we move that position a little bit towards the player
            quicksandTugMove(pState, pLevel, pPos, pEntity, depth);

            // By default, the tug position is constantly set to the entity's position, no
            // force is applied on the entity towards the position.

            // Some movement stuff. Dealing with whether the entity is "OnGround"
            // whether they can jump, and step out onto a solid block.



            // Okay, so letting people jump on the top layer of the quicksand lets everyone just hold spacebar and jump over it all. I just commented it all out to disable it, and works better (tm)
/*
            if (QSBehavior.canStepOut(depth)) {
                if (pLevel.getBlockState(pPos.above()).isAir()) {

                    boolean playerFlying = false;
                    if (pEntity instanceof Player) {
                        Player p = (Player) pEntity;
                        playerFlying = p.getAbilities().flying;
                    }

                    if (!playerFlying) {
                        // if the player is flying ... they shouldn't be set on ground! cause then they
                        // can't fly.
                        // quicksand effects are still dealt, above, though.
                        pEntity.setOnGround(true);
                    }

                }
            } else {
                // sets to false even if they're at bottom (touching block underneath the QS)
                pEntity.setOnGround(false);
                pEntity.resetFallDistance();
            }

 */
            pEntity.setOnGround(false);
            pEntity.resetFallDistance();

        }

    }

    public void trySetCoverage(Entity pEntity) {
        if (pEntity instanceof Player) {
            playerStruggling pS = (playerStruggling) pEntity;
        if (!Objects.equals(pS.getCoverageTexture(), QSBehavior.getCoverageTexture())) {
                pS.setCoverageTexture(QSBehavior.getCoverageTexture());
            System.out.println(QSBehavior.getCoverageTexture());
                pS.setCoveragePercent(0.0);
            }
        }
    }
    public void firstTouch(Entity pEntity) {
        trySetCoverage(pEntity);
    }

    public void struggleAttempt(@NotNull BlockState pState, @NotNull Entity pEntity, double struggleAmount) {

        // runs when the player struggles in a block of this type.

        // particle should happen at surface in an area around.
        // Vec3 pos = pEntity.position();
        // pEntity.getLevel().addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(),pos.x,
        // pos.y, pos.z, 0, 0, 0);

        // struggleAmount should be 0 .. 1, from 0 to 20 ticks

        double middlePoint = -1 * abs(struggleAmount - 0.5) + 0.5;

        // curve it
        middlePoint = 5.25 * (middlePoint*middlePoint) - 0.15; // ranges -0.5 to 0.5

        pEntity.addDeltaMovement(new Vec3(0.0, middlePoint, 0.0));

        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.SOUL_SOIL_STEP, SoundSource.BLOCKS, 0.25F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.5F);


    }

    public void tryApplyCoverage(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Entity pEntity) {
        double depth = getDepth(pLevel, pPos, pEntity);
        if (depth >= 0) {
            if (pEntity instanceof Player) {

                playerStruggling pS = (playerStruggling) pEntity;
                double currentAmount = pS.getCoveragePercent();
                double shouldBe = depth/1.875;
                if (shouldBe > 1.0) {shouldBe = 1.0;}

                if (shouldBe > currentAmount) {pS.setCoveragePercent(shouldBe);}

            }
        }
    }

    @Override
    public void entityInside(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
            @NotNull Entity pEntity) {

        // when an entity is touching a quicksand block ...

        double depth = getDepth(pLevel, pPos, pEntity);

        if (depth >= 0) {

            // all that needs to be done is set the fact that this entity is in quicksand.
            // Entities choose which quicksand block to run the applyQuicksandEffects
            // function from.

            entityQuicksandVar es = (entityQuicksandVar) pEntity;

            // more movement variables.
            es.setInQuicksand(true);

            if (!es.getquicksandEnterFlag()) {
                // if the enter flag isn't set - this entity entered quicksand
                firstTouch(pEntity);
                es.setquicksandEnterFlag(true);
            }

            pEntity.resetFallDistance();

            tryApplyCoverage(pState, pLevel, pPos, pEntity);

        }

    }


    // @Override
    // public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos,
    // RandomSource pRandom) {}

    public void spawnBubble(BlockState pState, Level pLevel, Vec3 pos, BlockPos pPos, BlockState bs) {
        BlockState upOne = pLevel.getBlockState(pPos.above());
        if (checkDrownable(pState) && !checkDrownable(upOne)) {
            double offset = 0d;
            Block gb = pState.getBlock();
            if (gb instanceof QuicksandInterface) {
                offset = ((QuicksandInterface) gb).getOffset(pState);
            }
            pos = pos.add(new Vec3(0, -offset, 0));
            spawnBubble(pLevel, pos);
        }
    }

    public void spawnBubble(Level pLevel, Vec3 pos) {
        if (!pLevel.isClientSide()) {
            EntityBubble.spawn(pLevel, pos, Blocks.COAL_BLOCK.defaultBlockState());
        }
    }

    // This needs to be set for quicksand blocks that have Ambient Occlusion
    // small detail but important, IMO
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.block();
    }

    public boolean checkDrownable(BlockState pState) {
        return pState.getTags().toList().contains(QUICKSAND_DROWNABLE)
                || pState.getFluidState().getTags().toList().contains(QUICKSAND_DROWNABLE_FLUID);
    }

}
