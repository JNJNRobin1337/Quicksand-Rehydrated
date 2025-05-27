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
import net.mokai.quicksandrehydrated.block.quicksands.core.FlowingQuicksandBase;
import net.mokai.quicksandrehydrated.block.quicksands.core.QuicksandBase;
import net.mokai.quicksandrehydrated.entity.data.QuicksandEffectManager;
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

import static net.mokai.quicksandrehydrated.util.ModTags.Blocks.QUICKSAND_DROWNABLE;

@Mixin(Entity.class)
public abstract class SlowdownMixin implements entityQuicksandVar {

    @Shadow
    protected Vec3 stuckSpeedMultiplier;



    final QuicksandEffectManager quicksandEffectManager = new QuicksandEffectManager();
    public QuicksandEffectManager getQuicksandEffectManager() {return this.quicksandEffectManager;}


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
        this.quicksandAdditive = this.quicksandAdditive.add(set);
    }
    public void multiplyQuicksandAdditive(Vec3 set) {
        this.quicksandAdditive = this.quicksandAdditive.multiply(set);
    }

    Vec3 tugPosition = Vec3.ZERO;
    public Vec3 getTugPosition() {return this.tugPosition;}
    public void setTugPosition(Vec3 set) {this.tugPosition = set;}

    Vec3 tugMomentum = Vec3.ZERO;
    public Vec3 getTugMomentum() {return this.tugMomentum;}
    public void setTugMomentum(Vec3 set) {this.tugMomentum = set;}


    @Shadow public abstract boolean shouldRender(double p_20296_, double p_20297_, double p_20298_);





    public boolean changed = false;
    public double horizontal = Double.MAX_VALUE;
    public double vertical = Double.MAX_VALUE;


    public boolean inQuicksand = false;
    public boolean getInQuicksand() {
        return this.inQuicksand;
    }
    public void setInQuicksand(boolean set) {
        this.inQuicksand = set;
    }



    public boolean enterQuicksandFlag = false;
    public boolean getEnterQuicksandFlag() {
        return this.enterQuicksandFlag;
    }
    public void setEnterQuicksandFlag(boolean set) {
        this.enterQuicksandFlag = set;
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

    public boolean stuckBlockValid(BlockPos pPos, Entity pEntity) {
        // Check if the block is quicksand
        BlockState pState = pEntity.level().getBlockState(pPos);
        
        // Verifica che il blocco non sia aria prima di considerarlo come sabbia mobile
        if (pState.isAir() || !(pState.getBlock() instanceof QuicksandBase)) {
            return false;
        }
        
        // Verifica che l'entità non stia saltando
        if (pEntity.getDeltaMovement().y > 0.05) {
            return false;
        }
        
        // Check if the entity is actually in the block
        double entityY = pEntity.getY();
        double entityHeight = pEntity.getBbHeight();
        double entityWidth = pEntity.getBbWidth() / 2.0;
        
        // Get block boundaries
        double blockMinY = pPos.getY();
        double blockMaxY = pPos.getY() + 1.0;
        double blockMinX = pPos.getX();
        double blockMaxX = pPos.getX() + 1.0;
        double blockMinZ = pPos.getZ();
        double blockMaxZ = pPos.getZ() + 1.0;
        
        // Get entity boundaries
        double entityMinY = entityY;
        double entityMaxY = entityY + entityHeight;
        double entityMinX = pEntity.getX() - entityWidth;
        double entityMaxX = pEntity.getX() + entityWidth;
        double entityMinZ = pEntity.getZ() - entityWidth;
        double entityMaxZ = pEntity.getZ() + entityWidth;
        
        // Check if the entity's bounding box intersects with the block
        boolean intersectsX = entityMaxX > blockMinX && entityMinX < blockMaxX;
        boolean intersectsY = entityMaxY > blockMinY && entityMinY < blockMaxY;
        boolean intersectsZ = entityMaxZ > blockMinZ && entityMinZ < blockMaxZ;
        
        // The entity is in the block if all three dimensions intersect
        // Aggiungiamo un controllo speciale per la parte superiore del corpo
        if (intersectsX && intersectsZ) {
            // Se la parte superiore del corpo è nel blocco, consideriamo l'entità come valida
            // anche se la parte inferiore non lo è
            double upperBodyY = entityY + entityHeight * 0.5;
            if (upperBodyY >= blockMinY && upperBodyY <= blockMaxY) {
                return true;
            }
            
            // Altrimenti, usiamo il controllo standard
            return intersectsY;
        }
        
        return false;
    }



    @Nullable
    public BlockPos getStuckBlock(Entity pEntity) {
        // Find the quicksand block the entity is in
        Level level = pEntity.level();
        
        // Get the entity's position and dimensions
        double entityX = pEntity.getX();
        double entityY = pEntity.getY();
        double entityZ = pEntity.getZ();
        double entityHeight = pEntity.getBbHeight();
        double entityWidth = pEntity.getBbWidth() / 2.0;
        
        // Define the area to check for quicksand blocks
        int minX = (int) Math.floor(entityX - entityWidth);
        int maxX = (int) Math.floor(entityX + entityWidth);
        int minY = (int) Math.floor(entityY - 0.1); // Check slightly below feet
        int maxY = (int) Math.floor(entityY + entityHeight); // Check the entire entity's height
        int minZ = (int) Math.floor(entityZ - entityWidth);
        int maxZ = (int) Math.floor(entityZ + entityWidth);
        
        // First, check the block at the entity's feet (most common case)
        BlockPos feetPos = new BlockPos(
            (int) Math.floor(entityX),
            (int) Math.floor(entityY - 0.1),
            (int) Math.floor(entityZ)
        );
        
        // Check if the block at the entity's feet is quicksand
        BlockState feetState = level.getBlockState(feetPos);
        // Verifica che il blocco non sia aria prima di considerarlo come sabbia mobile
        if (!feetState.isAir() && feetState.getBlock() instanceof QuicksandBase && stuckBlockValid(feetPos, pEntity)) {
            return feetPos;
        }
        
        // Then check the block the entity is in (second most common case)
        BlockPos entityPos = new BlockPos(
            (int) Math.floor(entityX),
            (int) Math.floor(entityY),
            (int) Math.floor(entityZ)
        );
        
        // Check if the block the entity is in is quicksand
        BlockState entityState = level.getBlockState(entityPos);
        // Verifica che il blocco non sia aria prima di considerarlo come sabbia mobile
        if (!entityState.isAir() && entityState.getBlock() instanceof QuicksandBase && stuckBlockValid(entityPos, pEntity)) {
            return entityPos;
        }
        
        // Se i casi comuni non hanno funzionato, controlla prima la parte superiore del corpo
        // Questo è importante per far sì che l'effetto delle sabbie mobili si applichi anche alla parte superiore
        
        // Prima controlla la parte superiore del corpo (dalla metà in su)
        int midY = (int) Math.floor(entityY + entityHeight * 0.5);
        for (int y = midY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    // Salta le posizioni già controllate
                    if (pos.equals(feetPos) || pos.equals(entityPos)) {
                        continue;
                    }
                    
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof QuicksandBase && stuckBlockValid(pos, pEntity)) {
                        return pos;
                    }
                }
            }
        }
        
        // Poi controlla la parte inferiore del corpo (dalla metà in giù)
        for (int y = minY; y < midY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    // Salta le posizioni già controllate
                    if (pos.equals(feetPos) || pos.equals(entityPos)) {
                        continue;
                    }
                    
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof QuicksandBase && stuckBlockValid(pos, pEntity)) {
                        return pos;
                    }
                }
            }
        }
        
        return null;
    }



    @Inject(method = "tick", at = @At("HEAD"))
    private void tickEnd(CallbackInfo ci) {
        Entity thisEntity = (Entity)(Object)this;
        entityQuicksandVar qsE = (entityQuicksandVar)(Object)this;

        // Non applicare effetti di sabbie mobili se l'entità sta saltando
        if (thisEntity.getDeltaMovement().y > 0.05) {
            return;
        }

        // Reset the inQuicksand flag at the start of each tick
        boolean wasInQuicksand = qsE.getInQuicksand();
        qsE.setInQuicksand(false);

        // Check if the entity is in quicksand
        BlockPos bp = getStuckBlock(thisEntity);
        
        if (bp != null && stuckBlockValid(bp, thisEntity)) {
            // Entity is in quicksand
            qsE.setInQuicksand(true);
            
            Level eLevel = thisEntity.level();
            BlockState bs = eLevel.getBlockState(bp);
            
            // Verifica che il blocco non sia aria
            if (!bs.isAir() && bs.getBlock() instanceof QuicksandBase qs) {
                // Only trigger firstTouch when entering quicksand for the first time
                if (!qsE.getEnterQuicksandFlag()) {
                    qsE.setEnterQuicksandFlag(true);
                    qs.firstTouch(bp, thisEntity, eLevel);
                }
                
                // Apply quicksand effects
                qs.applyQuicksandEffects(bs, eLevel, bp, thisEntity);
            }
        } else {
            // Not in quicksand
            if (qsE.getEnterQuicksandFlag()) {
                // Reset the enter flag when leaving quicksand
                qsE.setEnterQuicksandFlag(false);
            }
        }
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
        
        // Non modificare onGround se l'entità sta saltando o se sta cadendo
        if (thisEntity.getDeltaMovement().y > 0.05) {
            // Se l'entità sta saltando, non fare nulla
            return;
        }
        
        // Verifica se l'entità è su un blocco di sabbie mobili
        BlockState test = thisEntity.getFeetBlockState();
        if (test.getTags().toList().contains(QUICKSAND_DROWNABLE)) {
            // Imposta onGround a true solo se l'entità è effettivamente a contatto con il blocco
            // e non sta saltando
            if (thisEntity.getDeltaMovement().y <= 0.0) {
                thisEntity.setOnGround(true);
            }
        }
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
        Entity thisEntity = (Entity)(Object)this;
        entityQuicksandVar qsEntity = (entityQuicksandVar)(Object)this;
        
        // Verifica se l'entità è in sabbie mobili - early return se non lo è
        if (!qsEntity.getInQuicksand()) {
            return;
        }
        
        // Ottieni il blocco di sabbie mobili in cui si trova l'entità
        BlockPos stuckBlockPos = qsEntity.getStuckBlock(thisEntity);
        if (stuckBlockPos == null) {
            return;
        }
        
        // Ottieni il blocco e lo stato del blocco
        Level level = thisEntity.level();
        BlockState blockState = level.getBlockState(stuckBlockPos);
        Block block = blockState.getBlock();
        
        // Verifica che sia un blocco di sabbie mobili
        if (!(block instanceof QuicksandBase quicksandBlock)) {
            return;
        }
        
        // Calcola la profondità dell'entità nelle sabbie mobili
        double depth = quicksandBlock.getDepth(level, stuckBlockPos, thisEntity);
        
        // Applica gli effetti di movimento delle sabbie mobili
        quicksandBlock.quicksandMomentum(blockState, level, stuckBlockPos, thisEntity, depth);
        
        // Modifica il vettore di movimento
        Vec3 currentVec = Vec3localRef.get();
        
        // Ottimizzazione: Combina tutte le modifiche in un'unica operazione
        double xMod = currentVec.x;
        double yMod = currentVec.y;
        double zMod = currentVec.z;
        boolean modified = false;
        
        // Applica una resistenza verticale per simulare l'affondamento
        if (yMod < 0) {
            // Fattore di affondamento
            yMod *= 0.7;
            modified = true;
        }
        
        // Applica una resistenza orizzontale solo se c'è un movimento significativo
        boolean hasHorizontalMovement = Math.abs(xMod) > 0.01 || Math.abs(zMod) > 0.01;
        if (hasHorizontalMovement) {
            // Calcola un fattore di resistenza basato sulla profondità
            double resistanceFactor = Math.max(0.1, 1.0 - (depth * 0.5));
            xMod *= resistanceFactor;
            zMod *= resistanceFactor;
            modified = true;
        }
        
        // Se l'entità sta saltando, riduci l'altezza del salto
        if (yMod > 0.1) {
            // Riduci l'altezza del salto in base alla profondità
            double jumpFactor = Math.max(0.1, 1.0 - (depth * 0.7));
            yMod *= jumpFactor;
            modified = true;
        }
        
        // Applica le modifiche solo se necessario
        if (modified) {
            Vec3localRef.set(new Vec3(xMod, yMod, zMod));
        }
    }

    @ModifyVariable(
            method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "HEAD"),
            index = 2,
            argsOnly = true
    )
    private Vec3 injected(Vec3 momentum) {
        // Ottimizzazione: Evita casting e operazioni non necessarie
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)this;

        // Ottimizzazione: Verifica se ci sono modifiche da applicare prima di procedere
        Vec3 quicksandMod = QuicksandVarEntity.getQuicksandMultiplier();
        Vec3 quicksandAdd = QuicksandVarEntity.getQuicksandAdditive();
        
        boolean hasMultiplier = quicksandMod.length() > 1.0E-6D;
        boolean hasAdditive = quicksandAdd.length() > 1.0E-6D;
        
        // Se non ci sono modifiche da applicare, restituisci il momentum originale
        if (!hasMultiplier && !hasAdditive) {
            return momentum;
        }
        
        // Applica il moltiplicatore se presente
        if (hasMultiplier) {
            momentum = momentum.multiply(quicksandMod);
            QuicksandVarEntity.setQuicksandMultiplier(Vec3.ZERO);
        }

        // Applica l'additivo se presente
        if (hasAdditive) {
            momentum = momentum.add(quicksandAdd);
            QuicksandVarEntity.setQuicksandAdditive(Vec3.ZERO);
        }

        // Aggiorna il deltaMovement solo se necessario
        Entity thisEntity = (Entity)(Object)this;
        thisEntity.setDeltaMovement(momentum);
        
        return momentum;
    }



    public boolean entityCanStepOut(Entity pEntity) {
        // Usa l'entità passata come parametro, non thisEntity
        entityQuicksandVar QuicksandVarEntity = (entityQuicksandVar)(Object)pEntity;

        BlockPos stuckBlockPos = QuicksandVarEntity.getStuckBlock(pEntity);
        if (stuckBlockPos != null) {
            Block stuckBlock = pEntity.level().getBlockState(stuckBlockPos).getBlock();

            if (stuckBlock instanceof QuicksandBase) {
                QuicksandBase qsBlock = (QuicksandBase) stuckBlock;
                double depth = qsBlock.getDepth(pEntity.level(), stuckBlockPos, pEntity);
                return qsBlock.canStepOut(depth);
            }
        }
        return false;
    }



    @Inject(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void onMoveAfterSetOnGround(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        Entity thisEntity = (Entity)(Object)this;
        
        // Non modificare onGround se l'entità sta saltando
        if (thisEntity.getDeltaMovement().y > 0.05) {
            return;
        }
        
        if (entityCanStepOut(thisEntity)) {
            boolean notFlyingPlayer = true;

            if (thisEntity instanceof Player) {
                Player p = (Player) thisEntity;
                notFlyingPlayer = !p.getAbilities().flying;
            }

            // Commentato per evitare problemi con il salto
            // if (notFlyingPlayer) {
            //     thisEntity.setOnGround(true);
            // }
        }
    }

}
