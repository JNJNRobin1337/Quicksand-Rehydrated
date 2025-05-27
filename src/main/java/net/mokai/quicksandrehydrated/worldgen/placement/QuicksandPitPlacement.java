package net.mokai.quicksandrehydrated.worldgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Placement modifier che determina se una pozza di sabbie mobili dovrebbe essere generata in una posizione specifica.
 * Utilizza una combinazione di criteri per garantire che le pozze si generino in posizioni realistiche.
 */
public class QuicksandPitPlacement extends PlacementFilter {
    // Singleton instance
    public static final QuicksandPitPlacement INSTANCE = new QuicksandPitPlacement();
    // Codec per la serializzazione
    public static final Codec<QuicksandPitPlacement> CODEC = Codec.unit(() -> INSTANCE);

    private QuicksandPitPlacement() {
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        WorldGenLevel level = context.getLevel();
        
        // 1. Trova la superficie
        int surfaceY = findSurfaceY(level, pos);
        if (surfaceY == -1) return false;
        
        BlockPos surfacePos = new BlockPos(pos.getX(), surfaceY, pos.getZ());
        
        // 2. Verifica che il terreno sia sufficientemente pianeggiante
        if (!isTerrainFlat(level, surfacePos, 8, random)) return false;
        
        // 3. Verifica che siamo a un'altitudine appropriata
        double heightProbability = getQuicksandProbabilityByHeight(surfaceY);
        if (random.nextDouble() > heightProbability) return false;
        
        // 4. Usa una noise map per la distribuzione naturale
        if (!shouldGenerateQuicksandPatch(pos.getX(), pos.getZ(), level.getSeed())) return false;
        
        // 5. Bonus di probabilità se siamo in una depressione naturale
        boolean inDepression = isNaturalDepression(level, surfacePos, 8);
        
        // 6. Bonus di probabilità se siamo in un percorso di drenaggio
        boolean inDrainagePath = isWaterDrainagePath(level, surfacePos);
        
        // Decisione finale con bonus per posizioni ideali
        double baseProbability = 0.7; // 70% di probabilità base (aumentata dal 30%)
        if (inDepression) baseProbability += 0.2; // +20% se in una depressione
        if (inDrainagePath) baseProbability += 0.1; // +10% se in un percorso di drenaggio
        
        return random.nextDouble() < baseProbability;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifierTypes.QUICKSAND_PIT_PLACEMENT.get();
    }
    
    /**
     * Trova la coordinata Y della superficie in una data posizione
     */
    private int findSurfaceY(WorldGenLevel level, BlockPos pos) {
        // Ottieni l'altezza della superficie
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        
        // Verifica che ci sia un blocco solido
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), surfaceY, pos.getZ());
        
        // Trova il primo blocco non-aria
        while (mutablePos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(mutablePos);
            if (!state.isAir()) {
                // Verifica che ci sia un blocco solido sotto
                BlockPos belowPos = mutablePos.below();
                BlockState belowState = level.getBlockState(belowPos);
                
                if (!belowState.isAir() && belowState.isSolid()) {
                    return mutablePos.getY();
                }
            }
            mutablePos.move(0, -1, 0);
        }
        
        return -1; // Nessuna superficie valida trovata
    }
    
    /**
     * Verifica se il terreno è sufficientemente pianeggiante
     */
    private boolean isTerrainFlat(WorldGenLevel level, BlockPos pos, int radius, RandomSource random) {
        int centerY = pos.getY();
        
        // Calcola la pendenza massima in direzioni casuali
        double maxSlope = 0.0;
        int numChecks = 8; // Controlla 8 direzioni casuali per efficienza
        
        for (int i = 0; i < numChecks; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int dx = (int) Math.round(Math.cos(angle) * radius);
            int dz = (int) Math.round(Math.sin(angle) * radius);
            
            BlockPos checkPos = new BlockPos(pos.getX() + dx, 0, pos.getZ() + dz);
            int checkY = findSurfaceY(level, checkPos);
            
            if (checkY == -1) continue;
            
            // Calcola la pendenza in questa direzione
            double distance = Math.sqrt(dx*dx + dz*dz);
            double heightDiff = Math.abs(checkY - centerY);
            double slope = heightDiff / distance;
            
            maxSlope = Math.max(maxSlope, slope);
        }
        
        // Genera su terreni con pendenza accettabile
        return maxSlope < 0.35; // Meno del 35% di pendenza (aumentato dal 20%)
    }
    
    /**
     * Calcola la probabilità di generazione in base all'altitudine
     */
    private double getQuicksandProbabilityByHeight(int y) {
        // Definisci un intervallo di altitudine ideale per le sabbie mobili
        int minIdealHeight = 60; // Livello del mare (abbassato)
        int maxIdealHeight = 80; // Aumentato il range ideale
        
        // Nessuna sabbia mobile troppo in alto
        if (y > 120) return 0.0;
        
        // Probabilità massima nell'intervallo ideale
        if (y >= minIdealHeight && y <= maxIdealHeight) {
            return 0.8; // 80% di probabilità nell'intervallo ideale (aumentato)
        }
        
        // Probabilità decrescente all'aumentare dell'altitudine
        return 0.5 * Math.max(0, 1.0 - (y - maxIdealHeight) / 40.0);
    }
    
    /**
     * Usa una funzione di rumore per determinare dove generare le macchie di sabbie mobili
     */
    private boolean shouldGenerateQuicksandPatch(int x, int z, long seed) {
        // Implementazione semplificata usando Math.sin per simulare il rumore
        double regionNoise = Math.sin(x * 0.01 + seed) * Math.cos(z * 0.01 + seed * 2);
        double localNoise = Math.sin(x * 0.1 + seed * 3) * Math.cos(z * 0.1 + seed * 4);
        
        // Combina i due livelli di rumore
        double combinedNoise = (regionNoise + 1) * 0.5 * (localNoise + 1) * 0.5;
        
        // Genera sabbie mobili solo in aree dove il rumore è sopra una certa soglia
        // Soglia abbassata da 0.7 a 0.4 per aumentare la frequenza
        return combinedNoise > 0.4;
    }
    
    /**
     * Verifica se la posizione è in una depressione naturale
     */
    private boolean isNaturalDepression(WorldGenLevel level, BlockPos pos, int radius) {
        // Controlla se la posizione è in una depressione naturale
        int centerY = pos.getY();
        int surroundingAvgY = 0;
        int count = 0;
        
        // Campiona punti circostanti a distanza maggiore del raggio della pozza
        for (int x = -radius*2; x <= radius*2; x += 2) {
            for (int z = -radius*2; z <= radius*2; z += 2) {
                // Salta i punti all'interno del raggio della pozza
                if (Math.sqrt(x*x + z*z) <= radius) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                int surfaceY = findSurfaceY(level, checkPos);
                
                if (surfaceY != -1) {
                    surroundingAvgY += surfaceY;
                    count++;
                }
            }
        }
        
        if (count == 0) return false;
        
        // Calcola l'altezza media del terreno circostante
        surroundingAvgY /= count;
        
        // Verifica se il centro è più basso del terreno circostante
        return centerY < surroundingAvgY - 1; // Almeno 1 blocco più basso
    }
    
    /**
     * Verifica se la posizione è in un percorso di drenaggio dell'acqua
     */
    private boolean isWaterDrainagePath(WorldGenLevel level, BlockPos pos) {
        // Controlla se ci sono blocchi d'acqua nelle vicinanze
        boolean hasWaterNearby = false;
        int waterCheckRadius = 15;
        
        for (int x = -waterCheckRadius; x <= waterCheckRadius; x++) {
            for (int z = -waterCheckRadius; z <= waterCheckRadius; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                int checkY = findSurfaceY(level, checkPos);
                if (checkY == -1) continue;
                
                checkPos = new BlockPos(checkPos.getX(), checkY, checkPos.getZ());
                BlockState state = level.getBlockState(checkPos);
                
                if (state.getBlock() == Blocks.WATER) {
                    hasWaterNearby = true;
                    break;
                }
            }
            if (hasWaterNearby) break;
        }
        
        if (!hasWaterNearby) return false;
        
        // Verifica se la posizione è in un punto basso rispetto ai dintorni
        // (potenziale percorso di drenaggio)
        int lowerPointsCount = 0;
        int higherPointsCount = 0;
        int checkRadius = 8;
        
        for (int x = -checkRadius; x <= checkRadius; x++) {
            for (int z = -checkRadius; z <= checkRadius; z++) {
                if (x == 0 && z == 0) continue;
                
                BlockPos checkPos = pos.offset(x, 0, z);
                int checkY = findSurfaceY(level, checkPos);
                if (checkY == -1) continue;
                
                if (checkY > pos.getY()) {
                    higherPointsCount++;
                } else if (checkY < pos.getY()) {
                    lowerPointsCount++;
                }
            }
        }
        
        // Se ci sono più punti più alti che più bassi, siamo in un potenziale percorso di drenaggio
        return higherPointsCount > lowerPointsCount * 1.5;
    }
}