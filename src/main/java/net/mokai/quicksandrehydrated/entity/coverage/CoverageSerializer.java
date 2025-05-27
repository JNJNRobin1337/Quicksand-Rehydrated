package net.mokai.quicksandrehydrated.entity.coverage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Utility class for serializing and deserializing PlayerCoverage data
 */
public class CoverageSerializer {

    /**
     * Serializes a PlayerCoverage object to a CompoundTag
     * @param coverage The PlayerCoverage to serialize
     * @return A CompoundTag containing the serialized data
     */
    public static CompoundTag serializeCoverage(PlayerCoverage coverage) {
        CompoundTag tag = new CompoundTag();
        
        if (coverage == null || coverage.coverageEntries.isEmpty()) {
            // If there's no coverage, just return an empty tag
            return tag;
        }
        
        // Create a list tag to store all coverage entries
        ListTag entriesTag = new ListTag();
        
        // Add each coverage entry to the list
        for (CoverageEntry entry : coverage.coverageEntries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("begin", entry.begin);
            entryTag.putInt("end", entry.end);
            entryTag.putString("texture", entry.texture.toString());
            entriesTag.add(entryTag);
        }
        
        // Add the list to the main tag
        tag.put("entries", entriesTag);
        
        return tag;
    }
    
    /**
     * Deserializes a CompoundTag into a PlayerCoverage object
     * @param tag The CompoundTag containing the serialized data
     * @return A PlayerCoverage object with the deserialized data
     */
    public static PlayerCoverage deserializeCoverage(CompoundTag tag) {
        PlayerCoverage coverage = new PlayerCoverage();
        
        // If the tag doesn't have entries, return an empty coverage
        if (!tag.contains("entries")) {
            return coverage;
        }
        
        // Get the list of entries
        ListTag entriesTag = tag.getList("entries", 10); // 10 is the ID for CompoundTag
        
        // Process each entry
        for (int i = 0; i < entriesTag.size(); i++) {
            CompoundTag entryTag = entriesTag.getCompound(i);
            
            int begin = entryTag.getInt("begin");
            int end = entryTag.getInt("end");
            String textureString = entryTag.getString("texture");
            
            // Create a new coverage entry and add it to the coverage
            ResourceLocation texture = new ResourceLocation(textureString);
            CoverageEntry entry = new CoverageEntry(begin, end, texture);
            
            // Add the entry to the coverage
            coverage.addCoverageEntry(entry);
        }
        
        // Mark the coverage as needing an update
        coverage.requiresUpdate = true;
        
        return coverage;
    }
}