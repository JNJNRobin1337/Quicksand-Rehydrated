package net.mokai.quicksandrehydrated.block.quicksands.core;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.entity.EntityBubble;
import net.mokai.quicksandrehydrated.entity.coverage.CoverageEntry;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;
import net.mokai.quicksandrehydrated.entity.data.QuicksandEffect;
import net.mokai.quicksandrehydrated.entity.data.QuicksandEffectManager;
import net.mokai.quicksandrehydrated.entity.entityQuicksandVar;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.util.EasingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_DROWNABLE;
import static net.mokai.quicksandrehydrated.util.ModTags.Fluids.QUICKSAND_DROWNABLE_FLUID;
import static org.joml.Math.abs;
import static org.joml.Math.clamp;

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



    public String getCoverageTexture() {return QSBehavior.getCoverageTexture();}

    public String getSecretDeathMessage() {return QSBehavior.getSecretDeathMessage();}
    public double getSecretDeathMessageChance() {return QSBehavior.getSecretDeathMessageChance();}

    /**
     * The probability, per tick, of producing a bubble while an entity is in the block.
     * @return The probability. <code>0</code> = No bubbles. <code>1</code> = Always bubbles.
     */
    public double getBubbleChance(double depth) {return QSBehavior.getBubbleChance(depth);}

    /**
     * The sinking speed, depending on the depth.
     * normalized against the vertSpeed, so this value will remain effective - even
     * if the quicksand is very thick.
     * @param depth The depth in blocks. <code>0</code> is exactly on surface level.
     * @return The sinking value. Lower value means slower sinking.
     */
    public double getSinkSpeed(double depth) {
        // Get the configured buoyancy point
        double buoyancyPoint = QSBehavior.getBuoyancyPoint();
        
        // Calculate how much we're beyond the buoyancy point (if at all)
        double beyondBuoyancy = depth - buoyancyPoint;
        
        if (beyondBuoyancy > 0) {
            // We're beyond the buoyancy point
            // Use a smooth curve to gradually reduce sink speed as we go deeper
            
            // This formula creates a gentle curve that approaches zero as depth increases
            // but never quite reaches zero to ensure some minimal movement
            double reductionFactor = 0.05 + 0.15 * Math.exp(-beyondBuoyancy * 3);
            
            return QSBehavior.getSinkSpeed(depth) * reductionFactor;
        }
        else {
            // We're at or above the buoyancy point
            double baseSinkSpeed = QSBehavior.getSinkSpeed(depth);
            
            // Use a smooth curve to gradually reduce sink speed as we approach buoyancy
            // The closer we get to buoyancy, the more we reduce the sink speed
            double distanceFromBuoyancy = Math.abs(beyondBuoyancy);
            
            // This creates a curve that starts at 20% at buoyancy and smoothly increases to 100%
            // as we move away from buoyancy
            double speedFactor = 0.2 + 0.8 * Math.tanh(distanceFromBuoyancy * 3);
            
            return baseSinkSpeed * speedFactor;
        }
    }

    /**
     * Horizontal movement speed depending on the depth.
     * Thickness - but inverse.
     * @param depth The depth of the object. <code>0</code> is exactly on surface level.
     * @return The inverse resistance when walking. <code>0</code> = very thick; <code>1</code> = very thin.
     */
    public double getWalkSpeed(double depth) {return QSBehavior.getWalkSpeed(depth);}

    /**
     * Vertical movement speed depending on the depth.
     * Same as <code>getWalk()</code>
     * @param depth The depth of the object.
     * @return The inverse resistance when moving up/down. <code>0</code> = very thick; <code>1</code> = very thin.
     */
    public double getVertSpeed(double depth) {return QSBehavior.getVertSpeed(depth);} //TODO: invert this back

    /** Used by position based wobble.
     * How strongly the quicksand pulls the player horizontally towards the wobble point. 1 is full strength.
     * @return Horizontal wobble strength. [0, 1]
     */
    public double getWobbleTugHorizontal(double depth) {return QSBehavior.getWobbleTugHorizontal(depth);}

    /** Used by position based wobble.
     * How strongly the quicksand pulls the player vertically towards the wobble point. 1 is full strength.
     * If not set, will default to equal the horizontal wobble strength.
     * @param depth
     * @return Vertical wobble strength. [0, 1]
     */
    public double getWobbleTugVertical(double depth) {return QSBehavior.getWobbleTugVertical(depth);}

    /** Used by both Wobble types.
     * In position based wobble, How quickly the wobble point approaches the player, as a percentage of the distance per tick.
     * In momentum based wobble, how much of the momentum is applied to the player, as a percentage of the total per tick.
     * You can think of this as how "sticky" the quicksand is.
     * 1.0 = no effect on player's movement
     * 0.0 = player effectively cannot move unless they manage to stop touching the QS.
     * @param depth
     * @return Vertical wobble strength. [0, 1]
     **/
    public double getWobbleMove(double depth) {return QSBehavior.getWobbleMove(depth);}

    /** Used by momentum based wobble.
     * How much the wobble momentum decays, percentage per tick.
     * 1.0 = momentum never decays
     * 0.0 = momentum completely decays every tick
     * @param depth
     * @return Vertical tug strength. [0, 1]
     **/
    public double getWobbleDecay(double depth) {return QSBehavior.getWobbleDecay(depth);}

    /** Used by momentum based wobble.
     * How much the wobble actually wobbles back and forth.
     * Mathematically, how much of the actual wobble momentum is added to the wobble momentum's *momentum* as a percentage per tick.
     * 1.0 = flips back and forth every single tick.
     * 0.0 = wobble does not change. just drifts the player in some random direction.
     * @param depth
     * @return Vertical tug strength. [0, 1]
     **/
    public double getWobbleRebound(double depth) {return QSBehavior.getWobbleRebound(depth);}

    /** Used by momentum based wobble.
     * How much of the entity's momentum is added to the wobble momentum.
     * You could think of it as how thick the quicksand is- separate from the Vert and Walk speeds.
     * Mathematically, what percent of the entity's momentum is added to the wobble momentum per tick.
     * 1.0 = 100% of the entity's momentum is added
     * 0.0 = 0% added, no effect.
     * @param depth
     * @return Vertical tug strength. [0, 1]
     **/
    public double getWobbleApply(double depth) {return QSBehavior.getWobbleApply(depth);}



    /** The lowest point the TugPoint will sink to.
     * @return The buoyancy depth.
     */
    public double getBuoyancyPoint() {return QSBehavior.getBuoyancyPoint();}

    /** If the player is above this height, they can simply jump out of the quicksand.
     * @param depth
     * @return true if the player should be allowed to jump out.
     */
    public boolean canStepOut(double depth) {return QSBehavior.canStepOut(depth);}

    /**
     * How far from the top of a block the surface actually is.
     * For example, Mud and Soulsand would use 0.125 (or 1/8th of a block) before sinking begins.
     * @param blockstate The BlockState.
     * @return distance from the top of the block.
     */
    public double getOffset(BlockState blockstate) {return QSBehavior.getOffset();}


    /** The depth, in blocks, that the entity has sunk. Scales based on the entity's height, but is 1:1 for a Player.
     * @return depth, in blocks.
     */
    public double getDepth(Level pLevel, BlockPos pPos, Entity pEntity) {
        return EasingHandler.getDepth(pEntity, pLevel, pPos, getOffset(pLevel.getBlockState(pPos)));
    }


    public void quicksandMomentum(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, double depth) {

        // get quicksand Variables
        double walk = getWalkSpeed(depth);
        double vert = getVertSpeed(depth);
        double sink = getSinkSpeed(depth);

        // sinking is a replacement for gravity.
        Vec3 Momentum = pEntity.getDeltaMovement();

        entityQuicksandVar entQS = (entityQuicksandVar) pEntity;

        boolean playerFlying = false;
        if (pEntity instanceof Player) {
            Player p = (Player) pEntity;
            playerFlying = p.getAbilities().flying;
        }
        if (!playerFlying) {

//            if (vert != 0.0) {
//                sink = sink / vert; // counteract vertical thickness (?)
//            }
            
            // Check if we are close to the buoyancy point
            double buoyancyPoint = QSBehavior.getBuoyancyPoint();
            double distanceToBuoyancy = buoyancyPoint - depth;
            
            // Calculate how much we're beyond the buoyancy point (if at all)
            double beyondBuoyancy = depth - buoyancyPoint;
            
            if (beyondBuoyancy > 0) {
                // We're beyond the buoyancy point - apply resurfing that scales with depth
                
                // Calculate resurfing force with a smooth curve
                // This creates a gradual increase from 0 at buoyancy point
                // The square function makes it very gentle near buoyancy
                double resurfingForce = QSBehavior.getResurfingForce() * Math.pow(beyondBuoyancy, 1.5) * 2.0;
                
                // Apply a minimum force when very close to buoyancy to ensure some movement
                if (beyondBuoyancy < 0.05) {
                    resurfingForce = Math.max(resurfingForce, QSBehavior.getResurfingForce() * 0.01);
                }
                
                Vec3 resurfingVec = new Vec3(0, resurfingForce, 0);
                entQS.addQuicksandAdditive(resurfingVec);
            } 
            else {
                // We're at or above the buoyancy point - apply sinking force
                
                // Calculate sinking force with a smooth curve
                // This creates a gradual decrease as we approach buoyancy
                double distanceFromBuoyancy = Math.abs(beyondBuoyancy);
                double sinkFactor = Math.min(1.0, 0.2 + 0.8 * Math.tanh(distanceFromBuoyancy * 3));
                
                // Apply the scaled sinking force
                double scaledSink = -sink * sinkFactor;
                
                Vec3 addVec = new Vec3(0, scaledSink, 0);
                entQS.addQuicksandAdditive(addVec);
            }
        }

        Vec3 thicknessVector = new Vec3(walk, vert, walk);
        entQS.multiOrSetQuicksandMultiplier(thicknessVector);

    }

    public void applyQuicksandEffects(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
            @NotNull Entity pEntity) {

        // Optimization: Ignore bubbles to avoid unnecessary calculations
        if (pEntity instanceof EntityBubble) {
            return;
        }

        // Calculate the depth only if necessary
        double depth = getDepth(pLevel, pPos, pEntity);
        
        // Even if the depth is 0 or negative, we might have the upper part of the body in the quicksand
        // So we don't do an immediate return, but use a minimum depth
        if (depth <= 0) {
            // Check if the upper part of the body is in the quicksand
            double entityY = pEntity.getY();
            double entityHeight = pEntity.getBbHeight();
            double entityTop = entityY + entityHeight;
            
            // Calculate the surface of the quicksand block with greater precision
            double surfaceY = pPos.getY() + 1.0 - getOffset(pState);
            
            // If the upper part of the body is above the surface, then there is no effect
            if (entityTop <= surfaceY || entityY >= surfaceY + 1.0) {
                return;
            }
            
            // Calculate the depth based on the portion of the body that is actually immersed
            double immersedHeight = Math.max(0, entityTop - surfaceY);
            depth = Math.max(0.05, immersedHeight / entityHeight * 0.2);
        }

        // Apply coverage only for players
        if (pEntity instanceof Player) {
            trySetCoverage(pEntity);
        }

        // Check if we are at the buoyancy point or beyond
        double buoyancyPoint = QSBehavior.getBuoyancyPoint();
        
        // Apply the main effects of quicksand: thickness and sinking
        quicksandMomentum(pState, pLevel, pPos, pEntity, depth);

        // Apply special effects
        entityQuicksandVar qsE = (entityQuicksandVar) pEntity;
        
        // Optimization: Check if there are effects to apply
        if (!qsE.getQuicksandEffectManager().effects.isEmpty()) {
            for (QuicksandEffect e : qsE.getQuicksandEffectManager().effects) {
                e.effectEntity(pPos, pEntity, getQuicksandBehavior());
            }
        }

        // Movement management and "onGround" state
        if (!canStepOut(depth)) {
            // If the entity cannot exit, set onGround to false
            pEntity.setOnGround(false);
        }
        
        // Always reset the fall distance
        pEntity.resetFallDistance();
    }

    public void trySetCoverage(Entity pEntity) {
        // Set a section of coverage at the surface level of the block
        // Now with optimization to avoid unnecessary updates

        if (pEntity instanceof Player) {
            playerStruggling pS = (playerStruggling) pEntity;
            PlayerCoverage pC = pS.getCoverage();
            
            if (pC == null) return;

            // Get the correct depth
            double depth = getDepth(pEntity.level(), pEntity.blockPosition(), pEntity);
            
            // If the depth is too small, don't apply coverage
            if (depth < 0.05) return;
            
            // Calculate the surface of the quicksand block with greater precision
            BlockPos blockPos = pEntity.blockPosition();
            BlockState blockState = pEntity.level().getBlockState(blockPos);
            double offset = getOffset(blockState);
            double surfaceY = blockPos.getY() + 1.0 - offset; // Block surface
            
            double entityY = pEntity.getY();
            double entityHeight = pEntity.getBbHeight();
            
            // Calculate where the block surface intersects the player model (scale 0-32)
            // 0 is the feet, 32 is the head
            // We use a more precise formula that takes into account the actual height of the player
            double surfacePoint = ((surfaceY - entityY) / entityHeight) * 32.0;
            
            // We calculate the surface point more precisely
            // We no longer force a minimum value to allow precise alignment with the block surface
            
            // Limit to a valid range
            int surfacePixel = (int) clamp(surfacePoint, 0, 32);
            
            // Surface-based approach:
            // 1. Always start from the feet (0)
            // 2. End exactly at the surface
            int begin = 0; // Always start from the feet
            int end = surfacePixel; // End exactly at the surface
            
            // We no longer add a margin above the surface
            // to perfectly align the coverage with the block surface
            
            // If the player is completely submerged, cover the entire body
            if (depth >= entityHeight) {
                end = 32; // Up to the head
            }
            
            // Make sure the range is valid
            begin = clamp(begin, 0, 31);
            end = clamp(end, begin + 1, 32);

            // Check if we already have an entry with the same values to avoid unnecessary updates
            ResourceLocation tex = new ResourceLocation(QuicksandRehydrated.MOD_ID, QSBehavior.getCoverageTex());
            
            // Check if an update is needed
            boolean needsUpdate = true;
            
            // Look for an existing entry with the same texture and a similar range
            for (CoverageEntry entry : pC.coverageEntries) {
                if (entry.texture.equals(tex) && entry.begin == begin && Math.abs(entry.end - end) <= 1) {
                    // We already have a very similar entry, no need to update
                    needsUpdate = false;
                    break;
                }
            }
            
            if (needsUpdate) {
                pC.addCoverageEntry(new CoverageEntry(begin, end, tex));
            }
        }
    }
    public void firstTouch(BlockPos pPos, Entity pEntity, Level pLevel) {
        trySetCoverage(pEntity);
        entityQuicksandVar es = (entityQuicksandVar) pEntity;

        QuicksandBehavior qb = getQuicksandBehavior();

        QuicksandEffectManager qEM = es.getQuicksandEffectManager();

        qEM.clear();

        // Remove system logs to improve performance
        for (Class<? extends QuicksandEffect> e : qb.effectsList) {
            qEM.addEffect(e, pPos, pEntity);
        }
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

        playStruggleSound(pEntity, struggleAmount);

    }

    public void playStruggleSound(Entity pEntity, double struggleAmount) {
        pEntity.level().playSound(pEntity, pEntity.blockPosition(), SoundEvents.SOUL_SOIL_STEP, SoundSource.BLOCKS, 0.25F, (pEntity.level().getRandom().nextFloat() * 0.1F) + 0.5F);
    }

    public void tryApplyCoverage(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Entity pEntity) {
        double depth = getDepth(pLevel, pPos, pEntity);
        if (depth > 0) {
            if (pEntity instanceof Player) {
                playerStruggling pS = (playerStruggling) pEntity;
                
                // Calculate coverage height based on depth
                double shouldBe = depth/1.875;
                if (shouldBe > 1.0) {shouldBe = 1.0;}
                
                // Convert to pixel height (0-32)
                int pixelHeight = (int)(shouldBe * 32);
                
                // Create a new coverage entry
                ResourceLocation coverageTexLoc = new ResourceLocation(QuicksandRehydrated.MOD_ID, this.QSBehavior.getCoverageTex());
                CoverageEntry entry = new CoverageEntry(0, pixelHeight, coverageTexLoc);
                
                // Add the coverage entry to the player
                pS.getCoverage().addCoverageEntry(entry);
            }
        }
//        }
    }

    // special function for sinkables that runs when an entity jumps on, or in it.
    public void sinkableJumpOff(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        // Calculate the entity's depth
        double depth = getDepth(pLevel, pPos, pEntity);
        double buoyancyPoint = getBuoyancyPoint();
        
        // If the entity is below the buoyancy point, apply a stronger upward thrust
        if (depth >= buoyancyPoint && pEntity instanceof LivingEntity) {
            // Apply a strong upward thrust
            double jumpForce = 0.15 * (depth - buoyancyPoint + 0.2);
            pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(0, jumpForce, 0));
            
            // Add a sound effect for the jump
            pLevel.playSound(null, pEntity.blockPosition(), 
                    net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 
                    net.minecraft.sounds.SoundSource.BLOCKS, 
                    0.8F, 0.8F + pLevel.getRandom().nextFloat() * 0.4F);
            
            // Add bubble particles
            for (int i = 0; i < 10; i++) {
                double offsetX = pLevel.getRandom().nextDouble() * 0.6 - 0.3;
                double offsetZ = pLevel.getRandom().nextDouble() * 0.6 - 0.3;
                pLevel.addParticle(
                    net.minecraft.core.particles.ParticleTypes.BUBBLE, 
                    pEntity.getX() + offsetX, 
                    pEntity.getY() + 0.1, 
                    pEntity.getZ() + offsetZ, 
                    0, 0.1, 0
                );
            }
        }
    }

    @Override
    public void entityInside(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
            @NotNull Entity pEntity) {

        // Ottimizzazione: Ignora le bolle per evitare calcoli inutili
        if (pEntity instanceof EntityBubble) {
            return;
        }

        // Ottimizzazione: Verifica rapida se l'entità è sopra il blocco
        boolean isAboveBlock = pEntity.getY() > pPos.getY() + 0.9;
        
        // Calcola la profondità solo se necessario
        double depth = getDepth(pLevel, pPos, pEntity);

        if (depth > 0) {
            // Imposta l'entità come "in sabbia mobile"
            entityQuicksandVar es = (entityQuicksandVar) pEntity;
            es.setInQuicksand(true);
            pEntity.resetFallDistance();

            // Applica l'affondamento solo se l'entità è sopra la sabbia mobile
            if (isAboveBlock) {
                // Ottimizzazione: Calcola la velocità di affondamento una sola volta
                // Riduciamo leggermente la velocità di affondamento per un effetto più realistico
                double sinkSpeed = 0.04 * depth;
                
                // Controlla se l'entità ha raggiunto il punto di buoyancy
                double buoyancyPoint = getBuoyancyPoint();
                
                // Se la profondità è maggiore o uguale al punto di buoyancy, imposta la velocità di affondamento a 0
                if (depth >= buoyancyPoint) {
                    sinkSpeed = 0.0;
                    
                    // Se l'entità è un giocatore e sta cercando di muoversi verso l'alto, applica una forza di risalita
                    if (pEntity instanceof Player) {
                        // Check if player has upward momentum (jumping)
                        if (pEntity.getDeltaMovement().y > 0) {
                        // Applica una forza di risalita quando si preme il tasto di salto
                        double resurfingForce = QSBehavior.getResurfingForce();
                        pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(0, resurfingForce, 0));
                        
                        // Aggiungi un effetto sonoro per il resurfing (ogni mezzo secondo)
                        if (pLevel.getGameTime() % 10 == 0) {
                            pLevel.playSound(null, pEntity.blockPosition(), 
                                    net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 
                                    net.minecraft.sounds.SoundSource.BLOCKS, 
                                    0.4F, 0.8F + pLevel.getRandom().nextFloat() * 0.4F);
                        }
                        }
                    }
                }
                // Se siamo vicini al punto di buoyancy (entro 0.3 blocchi), riduci gradualmente la velocità
                else if (buoyancyPoint - depth < 0.3) {
                    // Riduci linearmente la velocità di affondamento man mano che ci avviciniamo al punto di buoyancy
                    double reductionFactor = (buoyancyPoint - depth) / 0.3;
                    sinkSpeed *= reductionFactor;
                }
                
                // Applica solo se la velocità è significativa
                if (sinkSpeed > 0.001) {
                    pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(0, -sinkSpeed, 0));
                }
            }

            // Applica la copertura solo per i giocatori
            if (pEntity instanceof Player) {
                tryApplyCoverage(pState, pLevel, pPos, pEntity);
            }
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



    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        // Rimuovi il log di sistema per migliorare le prestazioni
        return false;
    }

    // This needs to be set for quicksand blocks that have Ambient Occlusion
    // small detail but important, IMO
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.block();
    }
    
    // Ottimizzazione: Crea la forma di collisione una sola volta invece di ricrearla ogni volta
    private static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);
    
    /**
     * Override the collision shape to make it a non-solid block
     * This allows the player to sink into the quicksand
     */
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        // Usa la forma predefinita invece di crearla ogni volta
        return Shapes.empty();
    }
    
    /**
     * Override the shape for consistent rendering
     * This maintains the visual appearance of a full block
     */
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.block();
    }

    public boolean checkDrownable(BlockState pState) {
        return pState.getTags().toList().contains(QUICKSAND_DROWNABLE)
                || pState.getFluidState().getTags().toList().contains(QUICKSAND_DROWNABLE_FLUID);
    }



}
