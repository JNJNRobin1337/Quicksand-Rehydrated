package net.mokai.quicksandrehydrated.block.quicksands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBehavior;
import net.mokai.quicksandrehydrated.entity.EntityBubble;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.entity.sinkmodules.SinkQuickrug;
import net.mokai.quicksandrehydrated.entity.sinkmodules.SinkQuicksand;
import net.mokai.quicksandrehydrated.registry.ModParticles;
import net.mokai.quicksandrehydrated.util.EasingHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Random;

import static org.joml.Math.*;
import static org.joml.Math.abs;

public class Quickrug extends QuicksandBase {

    public static final IntegerProperty LIQUEFACTION = IntegerProperty.create("liquefaction", 0, 7);

    public Quickrug(Properties pProperties, QuicksandBehavior QSB) {
        super(pProperties, QSB);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIQUEFACTION, 0));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIQUEFACTION);
    }

    public double getSink(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{0.01d, 0.01d, 0.005d, 0.000d, 0.000d, 0.000d}); }
    public double getWalk(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{1d, .9d, .7d, .4d, .2d}); }
    public double getVert(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{.5d, .4d, .4d, .4d, .4d}); }
    public double getTugLerp(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{0.025d, 0.025d, 0.025d, 0.025d, 0.025d}); }
    public double getTug(double depthRaw) { return EasingHandler.doubleListInterpolate(depthRaw/2, new double[]{0.08d, 0.07333d, 0.0666d, 0.06d, 0.06d}); }





    public double getSinkSpeed(double depth) {return 0;}
    public double getWalkSpeed(double depth) {return EasingHandler.doubleListInterpolate(depth/2, new double[]{0.9d, 0.65d, 0.2d});}
    public double getVertSpeed(double depth) {return 0.15d;}



    @Override
    public void quicksandTugMove(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

//        entityQuicksandVar es = (entityQuicksandVar) pEntity;
//        Vec3 prevPos = es.getTugPosition();
//        Vec3 currentPos = pEntity.getPosition(0);
//        SinkQuickrug sinkData = accessDataModule(pEntity);

    }

    @Override
    public void quicksandTug(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        Vec3 Momentum = pEntity.getDeltaMovement();
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        SinkQuickrug sinkData = accessDataModule(pEntity);

        // Get the Previous Position variable
        Vec3 tugPos = sinkData.WobblePosition;
        Vec3 tugPos2 = sinkData.WobblePosition_2;

//        Vector3f RED = Vec3.fromRGB24(16711680).toVector3f();
//        pLevel.addParticle(new DustParticleOptions(RED, 1.0F), tugPos.x(), tugPos.y(), tugPos.z(), 0.0D, 0.0D, 0.0D);
//        Vector3f AQUA = Vec3.fromRGB24(65535).toVector3f();
//        pLevel.addParticle(new DustParticleOptions(AQUA, 1.0F), tugPos2.x(), tugPos2.y(), tugPos2.z(), 0.0D, 0.0D, 0.0D);

        // the difference between the entity position, and the previous position.
        Vec3 playerDifferenceVec = tugPos.subtract(pEntity.getPosition(0));

        Vec3 tugDifferenceVec = tugPos2.subtract(tugPos);

        Vec3 tugDifferenceVec2 = tugPos2.subtract(pEntity.getPosition(0));
        tugDifferenceVec2.multiply(1, 0.5, 1);



        // move player towards tug point 1

        // 0.025
        double multi = EasingHandler.doubleListInterpolate(depth/2, new double[]{0.003d, 0.3d});

        Vec3 addMomentum = playerDifferenceVec.multiply(new Vec3(multi, multi, multi));
        pEntity.addDeltaMovement(addMomentum);

        // move tug point 1 towards tug point 2
        multi = 0.01d;
        addMomentum = tugDifferenceVec.multiply(new Vec3(multi, multi, multi));
        sinkData.WobbleMomentum = sinkData.WobbleMomentum.add(addMomentum);

        if (tugDifferenceVec2.length() > 0.3) {
            // move tug point 2 towards player (linear movement, no momentum)
            multi = clamp(0, 1, depth / 2);
            multi *= multi;
            multi = lerp(0.05, 0.0, multi);

            Vec3 entPos = pEntity.getPosition(0);

            sinkData.WobblePosition_2 = new Vec3(
                    lerp(sinkData.WobblePosition_2.x, entPos.x, multi),
                    lerp(sinkData.WobblePosition_2.y, entPos.y, multi* 0.5),
                    lerp(sinkData.WobblePosition_2.z, entPos.z, multi)
            );
        }


        // depth reaches 1 at 1/2 a block deep
        //
        double sinkFactor = 1 - (depth*2);
        sinkFactor = min(-sinkFactor, 0);
        sinkFactor *= 0.01;
        sinkData.WobblePosition_2 = sinkData.WobblePosition_2.add(0, sinkFactor,0 );


        // add player momentum to point 1
//        multi = 0.05d;
//        sinkData.WobbleMomentum = sinkData.WobbleMomentum.add(Momentum.multiply(multi, multi, multi));


        sinkData.WobblePosition = sinkData.WobblePosition.add(sinkData.WobbleMomentum.multiply(1, 1.2, 1));
        double decay = 0.99d;
        sinkData.WobbleMomentum = sinkData.WobbleMomentum.multiply(decay, decay, decay);



        if (sinkData.WobblePosition.y > pPos.getY()+1) {
            sinkData.WobblePosition = new Vec3(sinkData.WobblePosition.x, pPos.getY()+1, sinkData.WobblePosition.z);
        }
        if (sinkData.WobblePosition_2.y > pPos.getY()+1) {
            sinkData.WobblePosition_2 = new Vec3(sinkData.WobblePosition_2.x, pPos.getY()+1, sinkData.WobblePosition_2.z);
        }

    }



    @Override
    public void quicksandMomentum(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        // get quicksand Variables
        double walk = getWalkSpeed(depth);
        double vert = getVertSpeed(depth);
        double sink = getSinkSpeed(depth);

        // sinking is a replacement for gravity.
        Vec3 Momentum = pEntity.getDeltaMovement();





        SinkQuickrug sinkData = accessDataModule(pEntity);

        Vec3 horizontalMomentum = new Vec3(Momentum.x, 0, Momentum.z);
        double movementFactor = clamp(0, 0.5, horizontalMomentum.length())/0.5;

        movementFactor = max(movementFactor, clamp(0, 1, Momentum.y*3));

        Vec3 playerDifferenceVec = sinkData.WobblePosition_2.subtract(pEntity.getPosition(0));
        playerDifferenceVec = new Vec3(playerDifferenceVec.x, 0, playerDifferenceVec.y);
        double difFactor = 1-clamp(0, 1, playerDifferenceVec.length());

        RandomSource rng = pEntity.level().getRandom();

        if (rng.nextDouble() < 0.2) {
            if (rng.nextDouble() > movementFactor) {
                float gain = (float) movementFactor;
                System.out.println(gain);
                pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, gain * (0.33F + rng.nextFloat() * 0.33F), (rng.nextFloat() * 0.1F) + 0.45F);
            }

            if (rng.nextDouble() * 0.8 > movementFactor) {
                float gain = (float) movementFactor;
                pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, gain * (0.33F + rng.nextFloat() * 0.33F), (rng.nextFloat() * 0.1F) + 0.45F);
            }
        }



        double depthFactor = clamp(0.0, 1.0, depth/2.0);
        double depthFactorCentered = lerp(depthFactor, 0.5, 0.1);
        sinkData.slurpTimer -= difFactor * 0.5;

        double WobblePos2Depth = EasingHandler.getDepthPos(sinkData.WobblePosition_2, pLevel, pPos, 0);

        if (sinkData.slurpTimer <= 0) {
            sinkData.slurpTimer = lerp(20, 60, 1-depthFactor);
            System.out.println(sinkData.slurpTimer);
            double slurpAmount = 0.1 + clamp(0.0, 1.0, WobblePos2Depth*WobblePos2Depth) * 0.1;
            slurpEntity(pState, pEntity, slurpAmount);
        }



        if (Momentum.y >= 0 && depth < 0.125) {
            vert = 0.5d;
        }

        entityQuicksandVar entQS = (entityQuicksandVar) pEntity;

        boolean playerFlying = false;
        if (pEntity instanceof Player) {
            Player p = (Player) pEntity;
            playerFlying = p.getAbilities().flying;
        }
        if (!playerFlying) {
            Vec3 addVec = new Vec3(0, -sink, 0);
            entQS.addQuicksandAdditive(addVec);
        }

        Vec3 thicknessVector = new Vec3(walk, vert, walk);
        entQS.multiOrSetQuicksandMultiplier(thicknessVector);

    }

    public boolean canStepOut(double depth) {
        return (depth < 0.375);
    }

    public SinkQuickrug accessDataModule(Entity pEntity) {

        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        if (es.hasSinkModule(SinkQuickrug.class)) {
            return (SinkQuickrug) es.accessSinkModule(SinkQuickrug.class);
        }
        else {
            SinkQuickrug dataModule = new SinkQuickrug();
            es.addSinkModule(dataModule);
            return dataModule;
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
            // ???
        }
    }

    public void slurpEntity(@NotNull BlockState pState, @NotNull Entity pEntity, double amount) {

        double depth = getDepth(pEntity.level(), pEntity.blockPosition(), pEntity);
        entityQuicksandVar es = (entityQuicksandVar) pEntity;
        SinkQuickrug sinkData = accessDataModule(pEntity);

        float gain = (float) (amount * 2F);
        gain = clamp(0.0F, 1.0F, gain);

        pEntity.addDeltaMovement(new Vec3(0, (-amount) * 0.25, 0));
        sinkData.WobblePosition_2 = sinkData.WobblePosition_2.add(0, -amount, 0);



        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, gain * 0.75F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);
        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, gain * (0.33F + pEntity.level().getRandom().nextFloat() * 0.33F), (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);

        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.SCULK_BLOCK_HIT, SoundSource.BLOCKS, gain * (0.3F + pEntity.level().getRandom().nextFloat() * 0.2F), (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);

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

        struggleAmount *= 1;

        // 0 at surface, *1* at <maxDepth> blocks deep, as the maximum.
        double maxDepth = 1.5;
        double depthFactor = clamp(0, maxDepth, depth)/maxDepth;

        // this curves it downwards ... so it stays small, and approaches 1 quickly towards <maxDepth>
        depthFactor = Math.pow(depthFactor, 2);

        // 1 at surface, 0 at <maxDepth>
        // because of the curve, it stays near 1, but falls towards 0 quickly.
        depthFactor = 1 - depthFactor;
        depthFactor = clamp(0.1, 1, depthFactor);

        // so it can be used to scale struggleAmount by how deep you are.
        struggleAmount *= depthFactor;

        pEntity.addDeltaMovement(new Vec3(0, struggleAmount*0.1, 0));

        SinkQuickrug dataModule = accessDataModule(pEntity);
        dataModule.WobblePosition_2 = dataModule.WobblePosition_2.add(0, struggleAmount*0.5, 0);

        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.HONEY_BLOCK_STEP, SoundSource.BLOCKS, (float)struggleAmount*0.3F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.45F);
        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.WOOL_HIT, SoundSource.BLOCKS, (float)struggleAmount*0.7F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.525F);

    }



    @Override
    public void firstTouch(Entity pEntity, Level pLevel) {

        super.firstTouch(pEntity, pLevel);

        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        SinkQuickrug dataModule;
        if (!es.hasSinkModule(SinkQuickrug.class)) {
            dataModule = new SinkQuickrug();
            dataModule.WobblePosition = pEntity.getPosition(0);
            dataModule.WobblePosition_2 = pEntity.getPosition(0);
            es.addSinkModule(dataModule);
        }

        if (pEntity.getDeltaMovement().y <= -0.5) {
            double mvt = pEntity.getDeltaMovement().y;
            mvt = clamp(mvt, -0.666, 0);
            pLevel.playSound(pEntity, pEntity.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, abs(0.1F+(float) (mvt * 0.2)), (pLevel.getRandom().nextFloat() * 0.3F) + 0.5F);
            pLevel.playSound(pEntity, pEntity.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, abs((float) (mvt * 1.1)), (pLevel.getRandom().nextFloat() * 0.05F) + 0.5F);
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
        BlockState blockstate = pLevel.getBlockState(bPos);

        if (!pLevel.getBlockState(bPos).isSolidRender(pLevel, bPos)) {
            Direction.Axis direction$axis = direction.getAxis();
            double d1 = direction$axis == Direction.Axis.X ? 0.5D + 0.5625D * (double) direction.getStepX() : (double) randomsource.nextFloat();
            double d2 = direction$axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double) direction.getStepY() : (double) randomsource.nextFloat();
            double d3 = direction$axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double) direction.getStepZ() : (double) randomsource.nextFloat();
            pLevel.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(bPos), (double) pPos.x() + d1, (double) pPos.y() + d2, (double) pPos.z() + d3, 0.0D, 0.0D, 0.0D);
        }

    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
//        if (pRandom.nextInt(50) == 0) {
//
//            BlockState aboveState = pLevel.getBlockState(pPos.above());
//            if (aboveState.isAir()) {
//                spawnParticles(pLevel, pPos);
//            }
//
//        }
    }




}
