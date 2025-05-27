package net.mokai.quicksandrehydrated.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Feature that generates a quicksand pit with customizable block type and shape.
 * The shape is highly irregular and random, based on noise and configuration parameters.
 */
public class QuicksandPitFeature extends Feature<QuicksandPitConfiguration> {
    public QuicksandPitFeature(Codec<QuicksandPitConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<QuicksandPitConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        QuicksandPitConfiguration config = context.config();

        // Rimuoviamo il controllo che verifica se il blocco sotto l'origine è sabbia
        // Ora la feature può generarsi su qualsiasi tipo di terreno
        BlockState belowState = level.getBlockState(origin.below());
        if (belowState.isAir()) {
            System.out.println("[QuicksandPit] Generation failed: Air below origin at " + origin);
            return false;
        }

        // Get the block to use for the pit from the configuration
        Block pitBlock = config.block;
        BlockState pitBlockState = pitBlock.defaultBlockState();

        // Generate a quicksand pit with random size based on configuration
        // Use a wider range for more variable sizes
        int radius = random.nextIntBetweenInclusive(config.minRadius, config.maxRadius);
        
        // Determine the pit depth based on configuration (minimum of 3)
        int pitDepth = Math.max(3, random.nextIntBetweenInclusive(config.minDepth, config.maxDepth));
        
        // Find the surface level at the origin
        int originSurfaceY = findSurfaceY(level, origin);
        if (originSurfaceY == -1) {
            return false; // Couldn't find a valid surface
        }
        
        // Verifica che l'area sia pianeggiante
        if (!isAreaFlat(level, origin, radius, originSurfaceY)) {
            System.out.println("[QuicksandPit] Area not flat enough at " + origin);
            return false;
        }
        
        // Set the pit level AT the surface level (not affossato)
        int pitLevel = originSurfaceY;
        
        // Non verifichiamo più che l'area sia circondata da sabbia
        // Ora la feature può generarsi in qualsiasi area
        System.out.println("[QuicksandPit] Attempting to generate at " + origin);
        
        boolean placed = false;
        
        // Generate 3D Perlin-like noise for more natural, lake-like shapes
        // The size of the noise map depends on the radius
        int noiseSize = radius * 2 + 5;
        double[][][] noise3D = new double[noiseSize][noiseSize][8]; // 3D noise for more natural shapes
        
        // Generate initial 3D random noise
        for (int x = 0; x < noiseSize; x++) {
            for (int z = 0; z < noiseSize; z++) {
                for (int y = 0; y < 8; y++) {
                    // Generate a random value between 0.0 and 1.0
                    noise3D[x][z][y] = random.nextDouble();
                }
            }
        }
        
        // Smooth the 3D noise
        for (int smoothPass = 0; smoothPass < 2; smoothPass++) {
            double[][][] smoothedNoise = new double[noiseSize][noiseSize][8];
            
            for (int x = 0; x < noiseSize; x++) {
                for (int z = 0; z < noiseSize; z++) {
                    for (int y = 0; y < 8; y++) {
                        double sum = 0;
                        int count = 0;
                        
                        // Average with neighbors for smoothing
                        for (int nx = -1; nx <= 1; nx++) {
                            for (int nz = -1; nz <= 1; nz++) {
                                for (int ny = -1; ny <= 1; ny++) {
                                    int idx = x + nx;
                                    int idz = z + nz;
                                    int idy = y + ny;
                                    
                                    if (idx >= 0 && idx < noiseSize && 
                                        idz >= 0 && idz < noiseSize &&
                                        idy >= 0 && idy < 8) {
                                        sum += noise3D[idx][idz][idy];
                                        count++;
                                    }
                                }
                            }
                        }
                        
                        smoothedNoise[x][z][y] = sum / count;
                    }
                }
            }
            
            noise3D = smoothedNoise;
        }
        
        // Create a 2D noise map for horizontal shape
        double[][] horizontalNoise = new double[noiseSize][noiseSize];
        for (int x = 0; x < noiseSize; x++) {
            for (int z = 0; z < noiseSize; z++) {
                // Use the middle layer of 3D noise for horizontal shape
                horizontalNoise[x][z] = noise3D[x][z][3];
            }
        }
        
        // Apply irregularity factor from configuration
        float irregularity = config.irregularity;
        
        // Second pass: Determine the shape of the pit first (top-down approach)
        // Create a 2D map of the pit shape and depths
        int mapSize = radius * 2 + 11; // Larger to accommodate the extended radius
        int[][] depthMap = new int[mapSize][mapSize];
        boolean[][] pitShape = new boolean[mapSize][mapSize];
        
        // First determine the 2D shape of the pit (flat top layer)
        System.out.println("[QuicksandPit] Determining pit shape with radius " + radius);
        for (int x = -radius - 5; x <= radius + 5; x++) {
            for (int z = -radius - 5; z <= radius + 5; z++) {
                // Get noise value for this position
                double noiseValue = getNoise(x, z, horizontalNoise, radius);
                
                // Add more chaos to the noise value
                noiseValue = noiseValue * 0.6 + random.nextDouble() * 0.4;
                
                // Calculate distance with a more natural influence
                double distortionFactor = 0.8 + noiseValue * 0.4; // Less extreme variation
                double distance = Math.sqrt(x * x + z * z) / (radius * distortionFactor);
                
                // Apply gentle distortion based on angle for slightly non-circular shapes
                double angle = Math.atan2(z, x);
                double angleDistortion = Math.sin(angle * 2 + noiseValue * Math.PI) * 0.15;
                distance *= (1.0 + angleDistortion);
                
                // Determine if this position is part of the pit
                boolean isPitBlock = distance < 1.0 + (noiseValue - 0.5) * 0.2;
                
                // Store in our 2D map
                int mapX = x + radius + 5;
                int mapZ = z + radius + 5;
                
                if (mapX >= 0 && mapX < mapSize && mapZ >= 0 && mapZ < mapSize) {
                    pitShape[mapX][mapZ] = isPitBlock;
                    
                    if (isPitBlock) {
                        // Calculate depth based on distance from center
                        double normalizedDistance = Math.min(1.0, distance);
                        
                        // Invert the distance so center is deepest
                        double depthFactor = 1.0 - normalizedDistance;
                        
                        // Apply a curve to make the depth change more gradual at edges and steeper in center
                        depthFactor = Math.pow(depthFactor, 1.5);
                        
                        // Calculate local depth based on distance from center
                        int localDepth = (int) Math.max(3, Math.round(pitDepth * depthFactor));
                        
                        // Add a small amount of noise to avoid perfectly smooth depth changes
                        if (random.nextFloat() < 0.3) {
                            localDepth += random.nextIntBetweenInclusive(-1, 1);
                            localDepth = Math.max(3, localDepth); // Ensure minimum depth of 3
                        }
                        
                        depthMap[mapX][mapZ] = localDepth;
                    }
                }
            }
        }
        
        // Now generate the pit layer by layer, starting from the top
        System.out.println("[QuicksandPit] Generating pit layer by layer");
        
        // For each layer, from top to bottom
        for (int layer = 0; layer < pitDepth + 1; layer++) {
            System.out.println("[QuicksandPit] Generating layer " + layer);
            
            // For each position in our map
            for (int mapX = 0; mapX < mapSize; mapX++) {
                for (int mapZ = 0; mapZ < mapSize; mapZ++) {
                    // If this position is part of the pit
                    if (pitShape[mapX][mapZ]) {
                        // Calculate the actual world coordinates
                        int worldX = origin.getX() + (mapX - (radius + 5));
                        int worldZ = origin.getZ() + (mapZ - (radius + 5));
                        
                        // Get the depth at this position
                        int localDepth = depthMap[mapX][mapZ];
                        
                        // Only place blocks if we're within the depth range for this position
                        if (layer < localDepth) {
                            // Calculate the Y position for this layer
                            int worldY = pitLevel - layer;
                            
                            // Create the block position
                            BlockPos layerPos = new BlockPos(worldX, worldY, worldZ);
                            
                            // Check if there's solid ground below the bottom layer
                            if (layer == localDepth - 1) {
                                BlockPos belowPos = layerPos.below();
                                BlockState belowLayerState = level.getBlockState(belowPos);
                                
                                // Only place quicksand if there's solid ground below
                                if (belowLayerState.isAir() || !belowLayerState.isSolid()) {
                                    continue; // Skip this position if there's no solid ground below
                                }
                            }
                            
                            // Verifica se il blocco è esposto lateralmente (solo per i blocchi che non sono in superficie)
                            if (layer > 0) {
                                boolean isExposed = false;
                                
                                // Controlla i blocchi adiacenti per vedere se sono esposti all'aria
                                for (int dx = -1; dx <= 1; dx += 2) { // Solo -1 e 1
                                    BlockPos adjacentPos = new BlockPos(worldX + dx, worldY, worldZ);
                                    BlockState adjacentState = level.getBlockState(adjacentPos);
                                    if (adjacentState.isAir()) {
                                        isExposed = true;
                                        break;
                                    }
                                }
                                
                                if (!isExposed) {
                                    for (int dz = -1; dz <= 1; dz += 2) { // Solo -1 e 1
                                        BlockPos adjacentPos = new BlockPos(worldX, worldY, worldZ + dz);
                                        BlockState adjacentState = level.getBlockState(adjacentPos);
                                        if (adjacentState.isAir()) {
                                            isExposed = true;
                                            break;
                                        }
                                    }
                                }
                                
                                // Se il blocco è esposto lateralmente, non lo piazziamo
                                if (isExposed) {
                                    continue;
                                }
                            }
                            
                            // Check if the current block is in the list of replaceable blocks
                            BlockState currentState = level.getBlockState(layerPos);
                            Block currentBlock = currentState.getBlock();
                            
                            // Only replace blocks that are in the replaceable list
                            if (config.replaceableBlocks.contains(currentBlock)) {
                                // Place the quicksand block
                                level.setBlock(layerPos, pitBlockState, 3);
                                placed = true;
                            }
                        }
                    }
                }
            }
        }
        
        // Third pass: Add a smoother transition at the edges (layer by layer approach)
        System.out.println("[QuicksandPit] Adding edge transitions");
        
        // Create a map for the edge blocks
        boolean[][] edgeMap = new boolean[mapSize][mapSize];
        int[][] edgeDepthMap = new int[mapSize][mapSize];
        
        // First determine which blocks are edge blocks
        for (int x = -radius - 3; x <= radius + 3; x++) {
            for (int z = -radius - 3; z <= radius + 3; z++) {
                // Calculate distance from center
                double baseDistance = Math.sqrt(x * x + z * z) / radius;
                
                // Apply a small amount of distortion for natural-looking edges
                double angle = Math.atan2(z, x);
                double distortedDistance = baseDistance * (1.0 + Math.sin(angle * 2) * 0.1);
                
                // Work only on the edge area
                if (distortedDistance >= 0.9 && distortedDistance < 1.1) {
                    // Get noise value for this position
                    double noiseValue = getNoise(x, z, horizontalNoise, radius);
                    
                    // Determine if this is an edge block
                    boolean isEdgeBlock = noiseValue < 0.5 && random.nextFloat() < 0.7;
                    
                    // Store in our edge map
                    int mapX = x + radius + 5;
                    int mapZ = z + radius + 5;
                    
                    if (mapX >= 0 && mapX < mapSize && mapZ >= 0 && mapZ < mapSize) {
                        edgeMap[mapX][mapZ] = isEdgeBlock;
                        
                        if (isEdgeBlock) {
                            // Add a shallow depth at the edges (1-2 blocks)
                            int depth = 1;
                            if (distortedDistance < 1.0 && random.nextFloat() < 0.5) {
                                depth = 2;
                            }
                            
                            edgeDepthMap[mapX][mapZ] = depth;
                        }
                    }
                }
            }
        }
        
        // Now place the edge blocks layer by layer
        for (int layer = 0; layer < 3; layer++) { // Maximum edge depth is 2, but check layer 0 too
            // For each position in our edge map
            for (int mapX = 0; mapX < mapSize; mapX++) {
                for (int mapZ = 0; mapZ < mapSize; mapZ++) {
                    // If this position is an edge block
                    if (edgeMap[mapX][mapZ]) {
                        // Get the depth at this position
                        int edgeDepth = edgeDepthMap[mapX][mapZ];
                        
                        // Only place blocks if we're within the depth range for this position
                        if (layer <= edgeDepth) {
                            // Calculate the actual world coordinates
                            int worldX = origin.getX() + (mapX - (radius + 5));
                            int worldZ = origin.getZ() + (mapZ - (radius + 5));
                            int worldY = pitLevel - layer;
                            
                            // Create the block position
                            BlockPos edgePos = new BlockPos(worldX, worldY, worldZ);
                            
                            // Check if there's solid ground below the bottom layer
                            if (layer == edgeDepth) {
                                BlockPos belowPos = edgePos.below();
                                BlockState belowEdgeState = level.getBlockState(belowPos);
                                
                                // Only place quicksand if there's solid ground below
                                if (belowEdgeState.isAir() || !belowEdgeState.isSolid()) {
                                    continue; // Skip this position if there's no solid ground below
                                }
                            }
                            
                            // Verifica se il blocco è esposto lateralmente (solo per i blocchi che non sono in superficie)
                            if (layer > 0) {
                                boolean isExposed = false;
                                
                                // Controlla i blocchi adiacenti per vedere se sono esposti all'aria
                                for (int dx = -1; dx <= 1; dx += 2) { // Solo -1 e 1
                                    BlockPos adjacentPos = new BlockPos(worldX + dx, worldY, worldZ);
                                    BlockState adjacentState = level.getBlockState(adjacentPos);
                                    if (adjacentState.isAir()) {
                                        isExposed = true;
                                        break;
                                    }
                                }
                                
                                if (!isExposed) {
                                    for (int dz = -1; dz <= 1; dz += 2) { // Solo -1 e 1
                                        BlockPos adjacentPos = new BlockPos(worldX, worldY, worldZ + dz);
                                        BlockState adjacentState = level.getBlockState(adjacentPos);
                                        if (adjacentState.isAir()) {
                                            isExposed = true;
                                            break;
                                        }
                                    }
                                }
                                
                                // Se il blocco è esposto lateralmente, non lo piazziamo
                                if (isExposed) {
                                    continue;
                                }
                            }
                            
                            // Check if the current block is in the list of replaceable blocks
                            BlockState currentState = level.getBlockState(edgePos);
                            Block currentBlock = currentState.getBlock();
                            
                            // Only replace blocks that are in the replaceable list
                            if (config.replaceableBlocks.contains(currentBlock)) {
                                // Place the quicksand block
                                level.setBlock(edgePos, pitBlockState, 3);
                                placed = true;
                            }
                        }
                    }
                }
            }
        }
        
        // Fourth pass: Add a border around the quicksand pit if enabled in configuration
        if (config.hasBorder) {
            System.out.println("[QuicksandPit] Adding border");
            
            // Determine which block to use for the border
            BlockState borderBlockState;
            if (config.borderBlock != null) {
                // Use the specified border block
                borderBlockState = config.borderBlock.defaultBlockState();
            } else {
                // Use the same block as the pit
                borderBlockState = pitBlockState;
            }
            
            // Create a map for the border blocks
            boolean[][] borderMap = new boolean[mapSize][mapSize];
            boolean[][] secondLayerMap = new boolean[mapSize][mapSize];
            
            // First determine which blocks are border blocks
            for (int x = -radius - 4; x <= radius + 4; x++) {
                for (int z = -radius - 4; z <= radius + 4; z++) {
                    // Calculate distance from center
                    double distance = Math.sqrt(x * x + z * z) / radius;
                    
                    // Work only on the border area (just outside the pit)
                    if (distance >= 1.1 && distance < 1.3) {
                        // Get noise value for this position to create a natural-looking border
                        double noiseValue = getNoise(x, z, horizontalNoise, radius);
                        
                        // Determine if this is a border block
                        boolean isBorderBlock = noiseValue < 0.6 && random.nextFloat() < 0.7;
                        
                        // Store in our border map
                        int mapX = x + radius + 5;
                        int mapZ = z + radius + 5;
                        
                        if (mapX >= 0 && mapX < mapSize && mapZ >= 0 && mapZ < mapSize) {
                            borderMap[mapX][mapZ] = isBorderBlock;
                            
                            // Sometimes add a second layer for a more natural look
                            if (isBorderBlock && random.nextFloat() < 0.3) {
                                secondLayerMap[mapX][mapZ] = true;
                            }
                        }
                    }
                }
            }
            
            // Now place the border blocks layer by layer
            // First layer (at pit level)
            for (int mapX = 0; mapX < mapSize; mapX++) {
                for (int mapZ = 0; mapZ < mapSize; mapZ++) {
                    // If this position is a border block
                    if (borderMap[mapX][mapZ]) {
                        // Calculate the actual world coordinates
                        int worldX = origin.getX() + (mapX - (radius + 5));
                        int worldZ = origin.getZ() + (mapZ - (radius + 5));
                        
                        // Create the block position at pit level
                        BlockPos borderPos = new BlockPos(worldX, pitLevel, worldZ);
                        BlockState currentState = level.getBlockState(borderPos);
                        
                        // Only place border blocks on solid ground and if the block is in the replaceable list
                        if (!currentState.isAir() && currentState.isSolid() && config.replaceableBlocks.contains(currentState.getBlock())) {
                            // Place the border block
                            level.setBlock(borderPos, borderBlockState, 3);
                            placed = true;
                        }
                    }
                }
            }
            
            // Second layer (at the same level for some blocks)
            for (int mapX = 0; mapX < mapSize; mapX++) {
                for (int mapZ = 0; mapZ < mapSize; mapZ++) {
                    // If this position should have a second layer
                    if (secondLayerMap[mapX][mapZ]) {
                        // Calculate the actual world coordinates
                        int worldX = origin.getX() + (mapX - (radius + 5));
                        int worldZ = origin.getZ() + (mapZ - (radius + 5));
                        
                        // Create the block position at the same level as the pit (not above)
                        // This prevents blocks from appearing above the surface
                        BlockPos samePos = new BlockPos(worldX, pitLevel, worldZ);
                        BlockState sameState = level.getBlockState(samePos);
                        
                        // Only place if the current position is air or can be replaced
                        // and there's solid ground below
                        BlockState belowSameState = level.getBlockState(samePos.below());
                        if ((sameState.isAir() || (sameState.canBeReplaced() && config.replaceableBlocks.contains(sameState.getBlock()))) 
                            && belowSameState.isSolid()) {
                            level.setBlock(samePos, borderBlockState, 3);
                        }
                    }
                }
            }
            
            // Aggiungi blocchi di bordo anche sotto la superficie per evitare che i lati siano visibili
            for (int layer = 1; layer < 3; layer++) { // Aggiungi fino a 2 strati sotto la superficie
                for (int mapX = 0; mapX < mapSize; mapX++) {
                    for (int mapZ = 0; mapZ < mapSize; mapZ++) {
                        // Se questa posizione è un blocco di bordo
                        if (borderMap[mapX][mapZ]) {
                            // Calcola le coordinate effettive
                            int worldX = origin.getX() + (mapX - (radius + 5));
                            int worldZ = origin.getZ() + (mapZ - (radius + 5));
                            int worldY = pitLevel - layer;
                            
                            // Crea la posizione del blocco
                            BlockPos borderPos = new BlockPos(worldX, worldY, worldZ);
                            
                            // Verifica se il blocco è esposto lateralmente
                            boolean isExposed = false;
                            
                            // Controlla i blocchi adiacenti per vedere se sono esposti all'aria
                            for (int dx = -1; dx <= 1; dx += 2) { // Solo -1 e 1
                                BlockPos adjacentPos = new BlockPos(worldX + dx, worldY, worldZ);
                                BlockState adjacentState = level.getBlockState(adjacentPos);
                                if (adjacentState.isAir()) {
                                    isExposed = true;
                                    break;
                                }
                            }
                            
                            if (!isExposed) {
                                for (int dz = -1; dz <= 1; dz += 2) { // Solo -1 e 1
                                    BlockPos adjacentPos = new BlockPos(worldX, worldY, worldZ + dz);
                                    BlockState adjacentState = level.getBlockState(adjacentPos);
                                    if (adjacentState.isAir()) {
                                        isExposed = true;
                                        break;
                                    }
                                }
                            }
                            
                            // Se il blocco è esposto lateralmente, non lo piazziamo
                            if (isExposed) {
                                continue;
                            }
                            
                            // Verifica se il blocco corrente è nella lista dei blocchi sostituibili
                            BlockState currentState = level.getBlockState(borderPos);
                            Block currentBlock = currentState.getBlock();
                            
                            // Sostituisci solo i blocchi che sono nella lista dei sostituibili
                            if (config.replaceableBlocks.contains(currentBlock)) {
                                // Piazza il blocco di bordo
                                level.setBlock(borderPos, borderBlockState, 3);
                                placed = true;
                            }
                        }
                    }
                }
            }
        }
        
        return placed;
    }
    
    /**
     * Verifica se l'area è circondata da sabbia
     * @return true se l'area è circondata da sabbia
     */
    private boolean isSurroundedBySand(WorldGenLevel level, BlockPos origin, int radius, int surfaceY) {
        // Controlla il perimetro esterno
        int checkRadius = radius + 2; // Controlla un po' più all'esterno
        int sandCount = 0;
        int totalChecks = 0;
        
        for (int x = -checkRadius; x <= checkRadius; x++) {
            for (int z = -checkRadius; z <= checkRadius; z++) {
                // Controlla solo il perimetro
                if (Math.abs(x) == checkRadius || Math.abs(z) == checkRadius) {
                    BlockPos checkPos = new BlockPos(origin.getX() + x, surfaceY, origin.getZ() + z);
                    BlockState state = level.getBlockState(checkPos);
                    totalChecks++;
                    
                    // Conta quanti blocchi di sabbia ci sono sul perimetro
                    if (state.is(BlockTags.SAND)) {
                        sandCount++;
                    }
                }
            }
        }
        
        // Richiedi che almeno il 70% del perimetro sia sabbia
        float sandPercentage = (float) sandCount / totalChecks;
        boolean result = sandPercentage >= 0.7f;
        
        if (!result) {
            System.out.println("[QuicksandPit] Sand percentage too low: " + (sandPercentage * 100) + "% (need 70%)");
        }
        
        return result;
    }
    
    /**
     * Applies multiple smoothing passes to the noise map
     */
    private double[][] smoothNoise(double[][] noiseMap, int passes) {
        double[][] result = noiseMap;
        
        for (int pass = 0; pass < passes; pass++) {
            double[][] smoothed = new double[noiseMap.length][noiseMap[0].length];
            
            for (int x = 0; x < noiseMap.length; x++) {
                for (int z = 0; z < noiseMap[0].length; z++) {
                    // Average with neighbors for smoothing
                    double sum = 0;
                    int count = 0;
                    
                    for (int nx = -1; nx <= 1; nx++) {
                        for (int nz = -1; nz <= 1; nz++) {
                            int idx = x + nx;
                            int idz = z + nz;
                            
                            if (idx >= 0 && idx < noiseMap.length && idz >= 0 && idz < noiseMap[0].length) {
                                sum += result[idx][idz];
                                count++;
                            }
                        }
                    }
                    
                    smoothed[x][z] = sum / count;
                }
            }
            
            result = smoothed;
        }
        
        return result;
    }
    
    /**
     * Gets the noise value at the given position
     */
    private double getNoise(int x, int z, double[][] noiseMap, int radius) {
        int idx = x + radius + 2;
        int idz = z + radius + 2;
        
        if (idx >= 0 && idx < noiseMap.length && idz >= 0 && idz < noiseMap[0].length) {
            return noiseMap[idx][idz];
        }
        
        return 0.5; // Default value if out of bounds
    }
    
    /**
     * Calculates a noise-modified distance for more irregular shapes
     * This is used by the old algorithm and kept for compatibility
     */
    private double getDistanceWithNoise(int x, int z, double[][] noiseMap, int radius, float irregularity) {
        // Get the base Euclidean distance
        double baseDistance = Math.sqrt(x * x + z * z);
        
        // Get noise value for this position
        double noise = getNoise(x, z, noiseMap, radius);
        
        // Apply noise to the distance based on irregularity factor
        // Higher irregularity means more influence from the noise
        return baseDistance * (1.0 - irregularity * (noise - 0.5));
    }
    
    /**
     * Finds the Y coordinate of the surface at the given x,z position
     * Ensures we find a solid surface with solid blocks below it
     */
    private int findSurfaceY(WorldGenLevel level, BlockPos pos) {
        // Start from a high position and move down until we find a solid block
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), 
                level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()), 
                pos.getZ());
        
        // Find the highest non-air block
        while (mutablePos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(mutablePos);
            if (!state.isAir()) {
                // Check if there's solid ground below this block
                BlockPos belowPos = mutablePos.below();
                BlockState belowSurfaceState = level.getBlockState(belowPos);
                
                // Make sure there's at least one solid block below
                if (!belowSurfaceState.isAir() && belowSurfaceState.isSolid()) {
                    return mutablePos.getY();
                }
            }
            mutablePos.move(0, -1, 0);
        }
        
        return -1;
    }
    
    /**
     * Verifica se l'area è sufficientemente pianeggiante per generare la fossa
     * @return true se l'area è pianeggiante
     */
    private boolean isAreaFlat(WorldGenLevel level, BlockPos origin, int radius, int centerY) {
        int checkRadius = radius + 2; // Controlla un po' più all'esterno
        int maxHeightDifference = 2; // Differenza massima di altezza consentita
        
        for (int x = -checkRadius; x <= checkRadius; x += 2) { // Campiona ogni 2 blocchi per efficienza
            for (int z = -checkRadius; z <= checkRadius; z += 2) {
                BlockPos checkPos = new BlockPos(origin.getX() + x, 0, origin.getZ() + z);
                int surfaceY = findSurfaceY(level, checkPos);
                
                if (surfaceY == -1) {
                    return false; // Non è stato possibile trovare una superficie valida
                }
                
                // Verifica che la differenza di altezza non sia troppo grande
                if (Math.abs(surfaceY - centerY) > maxHeightDifference) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Reshapes the terrain by removing blocks to create depressions
     * Ensures there are no floating blocks
     */
    private void reshapeTerrain(WorldGenLevel level, BlockPos pos, int currentY, int targetY) {
        if (currentY <= targetY) {
            // We don't want to add blocks to create raised areas anymore
            // Just ensure the top block is sand
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), currentY, pos.getZ());
            
            // Check if there's solid ground below
            BlockPos belowPos = mutablePos.below();
            BlockState belowReshapeState = level.getBlockState(belowPos);
            
            // Only place sand if there's solid ground below
            if (!belowReshapeState.isAir() && belowReshapeState.isSolid()) {
                level.setBlock(mutablePos, Blocks.SAND.defaultBlockState(), 3);
            }
            return;
        }
        
        // If we're here, currentY > targetY, so we need to remove blocks to create a depression
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), 0, pos.getZ());
        
        // Remove blocks to create the depression
        for (int y = currentY; y > targetY; y--) {
            mutablePos.setY(y);
            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 3);
        }
        
        // Ensure the top block is sand
        mutablePos.setY(targetY);
        
        // Check if there's solid ground below
        BlockPos belowPos = mutablePos.below();
        BlockState belowDepressionState = level.getBlockState(belowPos);
        
        // Only place sand if there's solid ground below
        if (!belowDepressionState.isAir() && belowDepressionState.isSolid()) {
            level.setBlock(mutablePos, Blocks.SAND.defaultBlockState(), 3);
        }
    }
}