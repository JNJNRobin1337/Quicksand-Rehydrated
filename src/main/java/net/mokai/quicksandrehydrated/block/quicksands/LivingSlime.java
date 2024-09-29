package net.mokai.quicksandrehydrated.block.quicksands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.entity.EntityBubble;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.entity.sinkmodules.SinkLivingSlime;
import net.mokai.quicksandrehydrated.entity.sinkmodules.SinkQuicksand;
import net.mokai.quicksandrehydrated.registry.ModParticles;
import net.mokai.quicksandrehydrated.util.EasingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static org.joml.Math.*;
import static org.joml.Math.abs;

public class LivingSlime extends QuicksandBase {

    public String coverageTexture() {
        return "qsrehydrated:textures/entity/coverage/slime_coverage.png";
    }

    public LivingSlime(Properties pProperties, QuicksandBehavior QSB) {super(pProperties, QSB);}

    public double getSink(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{.001d, .009d, .009d, .009d, .009d}); }

    public double getWalk(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{1d, .9d, .7d, .4d, .2d}); }

    public double getVert(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{.5d, .4d, .4d, .4d, .4d}); }

    public double getTugLerp(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{0.025d, 0.025d, 0.025d, 0.025d, 0.025d}); }

    public double getTug(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{0.08d, 0.07333d, 0.0666d, 0.06d, 0.06d}); }



    public double getSinkSpeed(double depth) {return 0.001d;}
    public double getWalkSpeed(double depth) {return EasingHandler.doubleListInterpolate(depth/2, new double[]{1d, 0.75d});}
    public double getVertSpeed(double depth) {return 0.85d;}


    @Override
    public void quicksandTugMove(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        // Get the Previous Position variable
        Vec3 prevPos = es.getTugPosition();

        Vec3 currentPos = pEntity.getPosition(0);
        BlockState blockAbove = pLevel.getBlockState(pPos.above());


        // how much to move previous pos towards player?
        // the deeper you go, the slower it moves.
        double tugPointSpeed = EasingHandler.doubleListInterpolate(depth / 2, new double[]{0.01d, 0.001d});


        BlockPos tugBPos = new BlockPos((int)floor(prevPos.x), (int)floor(prevPos.y), (int)floor(prevPos.z));
        BlockState tugPosState = pLevel.getBlockState(tugBPos);

        if (!(tugPosState.getBlock() instanceof LivingSlime)) {
            tugPointSpeed = 0.9d;
        }
        else {

            RandomSource randomsource = pLevel.random;

            for (int i = 0; i < 5; i++) {
                Vec3 particlePos = prevPos.lerp(currentPos, randomsource.nextFloat());
                pLevel.addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(), particlePos.x(), particlePos.y(), particlePos.z(), 0.0D, 0.0D, 0.0D);
            }

        }


        prevPos = prevPos.lerp(currentPos, tugPointSpeed);

        // if this block is the top layer of sinkable
        if (!(blockAbove.getBlock() instanceof QuicksandBase)) {

            // if the tug point's y is Above the surface (so - inside of the non-sinkable block)
            if (prevPos.y > pPos.getY() + 1) {
                // limit the tug point to the surface of the sinkable
                prevPos = new Vec3(prevPos.x, pPos.getY() + 0.9, prevPos.z);
            }

        }

        es.setTugPosition(prevPos);

    }

    @Override
    public void quicksandTug(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        Vec3 Momentum = pEntity.getDeltaMovement();
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        // Get the Previous Position variable
        Vec3 tugPos = es.getTugPosition();

        BlockPos tugBPos = new BlockPos((int)floor(tugPos.x), (int)floor(tugPos.y), (int)floor(tugPos.z));
        BlockState tugPosState = pLevel.getBlockState(tugBPos);

        if (tugPosState.getBlock() instanceof LivingSlime) {

            // the difference between the entity position, and the previous position.
            Vec3 differenceVec = tugPos.subtract(pEntity.getPosition(0));

            // how much momentum should be applied towards the tug point
            // here, it's static - it does not change depending on depth
            double hor = 0.1;
            double vert = 0.1;

            if (Momentum.y > 0) {
                // if the player is moving downwards - they are pulled towards the tug point slightly harder
                // adds a little more rebound on bounces (?)
                vert *= 1.1d;
            }

            Vec3 addMomentum = differenceVec.multiply(new Vec3(hor, vert, hor));

            pEntity.addDeltaMovement(addMomentum);

        }

    }




    public boolean canStepOut(double depth) {
        if (depth < 0.125) {
            return false;
        }
        else if (depth < 0.35d) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void applyQuicksandEffects(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
                                      @NotNull Entity pEntity) {

        super.applyQuicksandEffects(pState, pLevel, pPos, pEntity);
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        if (pEntity instanceof EntityBubble) {
            return;
        }

        double depth = getDepth(pLevel, pPos, pEntity);
        if (depth > 0) {

            SinkLivingSlime dataModule;
            if (es.hasSinkModule(SinkLivingSlime.class)) {
                dataModule = (SinkLivingSlime) es.accessSinkModule(SinkLivingSlime.class);
            }
            else {
                dataModule = new SinkLivingSlime();
                es.addSinkModule(dataModule);
            }

            Vec3 Momentum = pEntity.getDeltaMovement();



            // upwards momentum is added to buildup wait ...
            if (Momentum.y > 0) {
                es.multiOrSetQuicksandMultiplier(new Vec3(1.0, 0.9, 1.0));
            }

            if (!dataModule.lastPos.equals(pPos)) {
                dataModule.lastPos = pPos;
            }



            BlockPos maxDepthPos = new BlockPos(pPos.getX(), (int) floor(dataModule.maxDepth), pPos.getZ());
            Block maxDepthBlock = pLevel.getBlockState(maxDepthPos).getBlock();

            boolean depthTargetInsideSlime = maxDepthBlock instanceof LivingSlime;

            if (!depthTargetInsideSlime) {
                dataModule.maxDepth -= 0.001;
            }
            else {
                double depthSink = 0.0001;

                if (depth > 0.4) {

                    dataModule.maxDepth = 2.0;
//
//                    depthSink = lerp(0.001, 0.5, clamp(0, 1, depth / 2));
//                    dataModule.maxDepth += depthSink;
                }

            }

            double maxDepthMaximum = 1.75d;
            dataModule.maxDepth = clamp(0, maxDepthMaximum, dataModule.maxDepth);



            System.out.print("depth: ");
            System.out.print(depth);
            System.out.print("   maxDepth: ");
            System.out.print(dataModule.maxDepth);
            System.out.println("");


            Random rng = new Random();

            if (depth < dataModule.maxDepth) {

                // if the player is above the max depth

                // every tick this much of a chance
                double minSlurp = 0.025;
                double chance = 0.025;


                double depthDifference = dataModule.maxDepth - depth;
                double depthDifFactor = clamp(0, 0.25, depthDifference)/0.25;


                double depthTargetFactor = clamp(0, 2, dataModule.maxDepth);

                chance *= (depthDifFactor * 4) + 1;



                double depthFactor = clamp(0, 1, depth/2);
                minSlurp *= (depthFactor * 7) + 1;
                chance *= (depthFactor * 2) + 1;


                chance = clamp(0, 0.2, chance);

                System.out.print("minslurp: ");
                System.out.print(minSlurp);
                System.out.print("   chance: ");
                System.out.print(chance);
                System.out.println("");

                if (rng.nextDouble() < chance) {
                    double takeAmount = rng.nextDouble(minSlurp, minSlurp * 3);

                    if (depth <= 1.0 && rng.nextDouble() < 0.1) {
                        takeAmount += 0.25d;
                    }

                    slurpEntity(pState, pEntity, takeAmount);
                }

            }
            else {
                // when below the max depth, slowly lerp it down further, to their level

                if (depth >= dataModule.maxDepth) {
                    dataModule.maxDepth = lerp(dataModule.maxDepth, depth, 0.05);
                }

            }

            double tugLerp = 0.02d;
            Vec3 tugM = es.getTugMomentum();
            Vec3 takeAmount = tugM.multiply(new Vec3(tugLerp, tugLerp, tugLerp));
            es.setTugMomentum(tugM.subtract(takeAmount));

//            pEntity.addDeltaMovement(takeAmount);
            es.addQuicksandAdditive(takeAmount);

        }
    }

    public void slurpEntity(@NotNull BlockState pState, @NotNull Entity pEntity, double amount) {

        double depth = getDepth(pEntity.level(), pEntity.blockPosition(), pEntity);
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        float gain = (float) (amount * 0.9F);
        gain = Math.max(gain, 0.0F);

        Vec3 tugM = es.getTugMomentum();
        es.setTugMomentum(tugM.add(new Vec3(0, -amount, 0)));

        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, gain * 1.25F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);
        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.MAGMA_CUBE_JUMP, SoundSource.BLOCKS, gain * 0.25F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);

        BlockPos pPos = pEntity.blockPosition();

        // keep going until the block above is NOT living slime
        while (pEntity.level().getBlockState(pPos.above()).getBlock() instanceof LivingSlime) {
            pPos = pPos.above();
        }

        RandomSource randomsource = pEntity.level().random;
        if (randomsource.nextInt(1) == 0) {
            Vec3 particlePos = new Vec3(pEntity.position().x, pPos.getY(), pEntity.position().z);
            spawnParticles(pEntity.level(), particlePos);
        }

    }

    @Override
    public void struggleAttempt(@NotNull BlockState pState, @NotNull Entity pEntity, double struggleAmount) {

        // block pos has potential to be incorrect?
        double depth = getDepth(pEntity.level(), pEntity.blockPosition(), pEntity);
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        SinkLivingSlime dataModule;
        if (es.hasSinkModule(SinkLivingSlime.class)) {
            dataModule = (SinkLivingSlime) es.accessSinkModule(SinkLivingSlime.class);
        }
        else {
            dataModule = new SinkLivingSlime();
            es.addSinkModule(dataModule);
        }


        dataModule.maxDepth += struggleAmount*0.05;


        Vec3 entityPosition = pEntity.getPosition(0);
        Vec3 prevPos = es.getTugPosition();

//        Vec3 momentumVec3 = pEntity.getDeltaMovement();
//        momentumVec3 = new Vec3(momentumVec3.x, 0, momentumVec3.z);
//        double momentum = momentumVec3.length();

        double momentum = (entityPosition.subtract(prevPos)).length(); // difference between entity and the prev pos

        momentum = 0.3 - momentum;
        momentum = clamp(momentum, -0.3, 0.3);

        double offsetAmount = struggleAmount * (momentum/3);

        pEntity.addDeltaMovement(new Vec3(0.0, offsetAmount*0.75, 0.0));
        entityPosition = entityPosition.add(0.0, offsetAmount*1.5, 0.0);
        es.setTugPosition(entityPosition);

        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_STEP, SoundSource.BLOCKS, 0.25F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);

    }



    @Override
    public void firstTouch(Entity pEntity, Level pLevel) {

        super.firstTouch(pEntity, pLevel);

        entityQuicksandVar pQSEnt = (entityQuicksandVar)pEntity;

        if (!pQSEnt.hasSinkModule(SinkQuicksand.class)) {
            SinkQuicksand qs_module = new SinkQuicksand();
            pQSEnt.addSinkModule(qs_module);
        }

        if (pEntity.getDeltaMovement().y <= -0.333) {
            double mvt = pEntity.getDeltaMovement().y;
            mvt = clamp(mvt, -0.666, 0);
            pLevel.playSound(pEntity, pEntity.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, abs(0.3F+(float) mvt), (pLevel.getRandom().nextFloat() * 0.1F) + 0.5F);
        }

    }




    // normal block things v

    private static void spawnParticles(Level pLevel, BlockPos pPos) {

        RandomSource randomsource = pLevel.random;
        Direction direction = Direction.UP;

        // taken from redstone ore block code
        BlockPos blockpos = pPos.relative(direction);
        if (!pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
            Direction.Axis direction$axis = direction.getAxis();
            double d1 = direction$axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) randomsource.nextFloat();
            double d2 = direction$axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) randomsource.nextFloat();
            double d3 = direction$axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) randomsource.nextFloat();
            pLevel.addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(), (double) pPos.getX() + d1, (double) pPos.getY() + d2, (double) pPos.getZ() + d3, 0.0D, 0.0D, 0.0D);
        }

    }

    private static void singleParticle(Level pLevel, Vec3 pPos) {
        pLevel.addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(), (double) pPos.x(), (double) pPos.y(), (double) pPos.z(), 0.0D, 0.0D, 0.0D);
    }

    private static void spawnParticles(Level pLevel, Vec3 pPos) {

        RandomSource randomsource = pLevel.random;
        Direction direction = Direction.UP;

        // taken from redstone ore block code

        Vec3 aboveVec = pPos.add(0, 1, 0);

        BlockPos bPos = BlockPos.containing(aboveVec);

        if (!pLevel.getBlockState(bPos).isSolidRender(pLevel, bPos)) {
            Direction.Axis direction$axis = direction.getAxis();
            double d1 = direction$axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) randomsource.nextFloat();
            double d2 = direction$axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) randomsource.nextFloat();
            double d3 = direction$axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) randomsource.nextFloat();
            pLevel.addParticle(ModParticles.QUICKSAND_BUBBLE_PARTICLES.get(), (double) pPos.x() + d1, (double) pPos.y() + d2, (double) pPos.z() + d3, 0.0D, 0.0D, 0.0D);
        }

    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {

        if (pRandom.nextInt(50) == 0) {

            BlockState aboveState = pLevel.getBlockState(pPos.above());
            if (aboveState.isAir()) {
                spawnParticles(pLevel, pPos);
            }

        }
    }


    // half transparent block
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) ? true : super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    // copied from stained glass
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    // Doesn't occlude not Ambiently Occlude!
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.empty();
    }

    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }

}
