package net.mokai.quicksandrehydrated.entity.coverage;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.util.EasingHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * WashingSystem - A system that removes coverage from players based on water immersion level
 * The more a player is immersed in water, the more coverage is removed
 */
public class WashingSystem {

    // Constants for washing effectiveness
    private static final double MIN_WASH_THRESHOLD = 0.1; // Minimum water level to start washing
    private static final double FULL_WASH_THRESHOLD = 0.8; // Water level for maximum washing effect
    private static final int WASH_TICKS_REQUIRED = 20; // Number of ticks required for full washing at max immersion
    
    /**
     * Checks if an entity is in water and at what level
     * @param entity The entity to check
     * @param level The world level
     * @return A value between 0.0 (not in water) and 1.0 (fully submerged)
     */
    public static double getWaterImmersionLevel(Entity entity, Level level) {
        if (entity == null || level == null) return 0.0;
        
        // Get entity bounding box
        AABB entityBox = entity.getBoundingBox();
        double entityHeight = entityBox.getYsize();
        
        // Check water level at entity position
        BlockPos entityPos = entity.blockPosition();
        double waterLevel = 0.0;
        
        // Check blocks from feet to head
        int blocksToCheck = (int)Math.ceil(entityHeight) + 1;
        int waterBlocks = 0;
        double partialWaterLevel = 0.0;
        
        // Debug
        // System.out.println("Checking water immersion for entity at " + entityPos + " with height " + entityHeight);
        
        for (int y = 0; y < blocksToCheck; y++) {
            BlockPos checkPos = entityPos.above(y);
            FluidState fluidState = level.getFluidState(checkPos);
            
            if (fluidState.is(FluidTags.WATER)) {
                waterBlocks++;
                
                // For the block containing the water surface, calculate partial immersion
                if (fluidState.getAmount() < 8 && y > 0) {  // Using getAmount() instead of getHeight()
                    partialWaterLevel = fluidState.getAmount() / 8.0;  // Convert to a 0.0-1.0 scale
                } else if (y == 0) {
                    // For the bottom block, consider entity's position within the block
                    double entityYInBlock = entity.getY() - entityPos.getY();
                    partialWaterLevel = Math.max(0, (fluidState.getAmount() / 8.0) - entityYInBlock);
                } else {
                    partialWaterLevel = 1.0;
                }
                
                // Debug
                // System.out.println("Water at Y=" + y + ", amount=" + fluidState.getAmount() + ", partial=" + partialWaterLevel);
            }
        }
        
        // Calculate total immersion level (0.0 to 1.0)
        // Fix calculation when no water blocks are found
        if (waterBlocks == 0) {
            waterLevel = 0.0;
        } else {
            waterLevel = (waterBlocks - 1 + partialWaterLevel) / entityHeight;
        }
        
        // Debug
        // System.out.println("Final water level: " + waterLevel + " (waterBlocks=" + waterBlocks + ", partialWaterLevel=" + partialWaterLevel + ")");
        
        return Math.max(0.0, Math.min(1.0, waterLevel));
    }
    
    /**
     * Applies washing effect to a player based on water immersion level
     * @param player The player to wash
     * @param level The world level
     */
    public static void applyWashingEffect(Player player, Level level) {
        try {
            // Safety checks
            if (player == null || level == null) return;
            if (!(player instanceof playerStruggling)) return;
            
            playerStruggling strugglingPlayer = (playerStruggling) player;
            
            // Safety check for coverage
            PlayerCoverage coverage = strugglingPlayer.getCoverage();
            if (coverage == null || coverage.coverageEntries.isEmpty()) return;
            
            // Get water immersion level
            double immersionLevel = getWaterImmersionLevel(player, level);
            
            // Debug
            // System.out.println("Water immersion level: " + immersionLevel);
            
            // Only apply washing if above minimum threshold
            if (immersionLevel < MIN_WASH_THRESHOLD) return;
            
            // On server side, apply the actual washing logic
            if (!level.isClientSide()) {
                // Apply washing effect based on immersion level
                if (immersionLevel >= FULL_WASH_THRESHOLD) {
                    // Player is significantly immersed, wash more aggressively
                    removeAllCoverage(coverage, immersionLevel);
                } else {
                    // Player is partially immersed, wash from top down
                    // Use a fixed number of pixels for more predictable behavior
                    removeTopCoverage(coverage, 1);
                }
                
                // Play washing sound
                spawnWashingEffects(player, level, immersionLevel);
            }
        } catch (Exception e) {
            // Print the exception for debugging
            System.err.println("Error in WashingSystem.applyWashingEffect: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Removed calculateWashEffectiveness method as it's no longer needed
    // The percentage-based approach handles this calculation directly
    
    /**
     * Removes coverage from the top down based on percentage (washing effect)
     * @param coverage The player's coverage
     * @param pixelsToRemove Number of pixels to remove from top
     */
    private static void removeTopCoverage(PlayerCoverage coverage, int pixelsToRemove) {
        if (pixelsToRemove <= 0 || coverage.coverageEntries.isEmpty()) return;
        
        // Find the highest coverage entry
        CoverageEntry highestEntry = null;
        int highestEnd = -1;
        
        for (CoverageEntry entry : coverage.coverageEntries) {
            if (entry.end > highestEnd) {
                highestEnd = entry.end;
                highestEntry = entry;
            }
        }
        
        if (highestEntry == null) return;
        
        // Simple approach: just remove pixels from the top
        int newEnd = highestEntry.end - pixelsToRemove;
        
        // If we removed all pixels in this entry
        if (newEnd <= highestEntry.begin) {
            coverage.coverageEntries.remove(highestEntry);
        } else {
            highestEntry.end = newEnd;
        }
        
        // Mark coverage for update
        coverage.requiresUpdate = true;
    }
    
    /**
     * Removes coverage based on immersion level
     * When player is fully immersed, coverage is removed completely
     * @param coverage The player's coverage
     * @param immersionLevel The water immersion level
     */
    private static void removeAllCoverage(PlayerCoverage coverage, double immersionLevel) {
        if (coverage.coverageEntries.isEmpty()) return;
        
        // Simplified approach: if immersion is high enough, just clear all coverage
        if (immersionLevel >= 0.9) {
            coverage.coverageEntries.clear();
            coverage.requiresUpdate = true;
            return;
        }
        
        // For lower immersion levels, remove a fixed percentage of each entry
        double removalPercentage = 0.3; // Remove 30% of coverage
        
        if (immersionLevel >= FULL_WASH_THRESHOLD) {
            removalPercentage = 0.5; // Remove 50% of coverage for higher immersion
        }
        
        // Apply the removal to each entry
        List<CoverageEntry> entriesToRemove = new ArrayList<>();
        
        for (CoverageEntry entry : coverage.coverageEntries) {
            int entryHeight = entry.end - entry.begin;
            int pixelsToRemove = (int)(entryHeight * removalPercentage);
            
            // If removing most of the entry, just remove it entirely
            if (pixelsToRemove >= entryHeight - 1) {
                entriesToRemove.add(entry);
            } else {
                // Remove pixels evenly from top and bottom
                int topPixels = pixelsToRemove / 2;
                int bottomPixels = pixelsToRemove - topPixels;
                
                // Apply the changes
                entry.begin += bottomPixels;
                entry.end -= topPixels;
                
                // Safety check
                if (entry.begin >= entry.end) {
                    entriesToRemove.add(entry);
                }
            }
        }
        
        // Remove any entries marked for removal
        coverage.coverageEntries.removeAll(entriesToRemove);
        
        // Mark coverage for update
        coverage.requiresUpdate = true;
    }
    
    /**
     * Spawns washing particles and plays washing sounds
     * @param player The player being washed
     * @param level The world level
     * @param immersionLevel The water immersion level
     */
    public static void spawnWashingEffects(Player player, Level level, double immersionLevel) {
        // Removed particle effects
        
        // Play washing sound occasionally on server side
        // Higher chance with higher immersion
        if (!level.isClientSide() && player.getRandom().nextFloat() < 0.3 * immersionLevel) {
            float volume = 0.3F + (float)(immersionLevel * 0.2F);
            float pitch = 1.2F + (player.getRandom().nextFloat() * 0.6F);
            
            level.playSound(null, player.blockPosition(), 
                SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 
                volume, pitch);
        }
    }
}