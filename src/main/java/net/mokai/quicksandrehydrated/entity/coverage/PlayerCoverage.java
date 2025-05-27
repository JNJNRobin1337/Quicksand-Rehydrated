package net.mokai.quicksandrehydrated.entity.coverage;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.clamp;

public class PlayerCoverage {

    public List<CoverageEntry> coverageEntries;
    public boolean requiresUpdate = false;

    public PlayerCoverage() {
        this.coverageEntries = new ArrayList<>();
    }

    public void addCoverageEntry(CoverageEntry newEntry) {
        // Optimized version that tries to combine similar entries and avoid unnecessary updates
        
        newEntry.begin = clamp(newEntry.begin, 0, 32);
        newEntry.end = clamp(newEntry.end, 0, 32);
        
        // If begin and end are the same, no need to add this entry
        if (newEntry.begin >= newEntry.end) {
            return;
        }
        
        // Check if we have a similar entry we can update instead of creating a new one
        boolean entryUpdated = false;
        
        for (CoverageEntry entry : coverageEntries) {
            // If we find an entry with the same texture and similar range
            if (entry.texture.equals(newEntry.texture)) {
                // If the new entry completely contains the existing one, just update the existing one
                if (newEntry.begin <= entry.begin && newEntry.end >= entry.end) {
                    entry.begin = newEntry.begin;
                    entry.end = newEntry.end;
                    entryUpdated = true;
                    requiresUpdate = true;
                    break;
                }
                // If the entries overlap or are adjacent, merge them
                else if ((newEntry.begin <= entry.end && newEntry.end >= entry.begin) ||
                         (newEntry.end == entry.begin - 1) || 
                         (newEntry.begin == entry.end + 1)) {
                    entry.begin = Math.min(entry.begin, newEntry.begin);
                    entry.end = Math.max(entry.end, newEntry.end);
                    entryUpdated = true;
                    requiresUpdate = true;
                    break;
                }
            }
        }
        
        // If we didn't update an existing entry, add the new one
        if (!entryUpdated) {
            // Clear any overlapping coverage before adding
            clearCoverage(newEntry.begin, newEntry.end);
            
            // Find the correct insertion point to maintain order
            int insertionIndex = coverageEntries.size();
            for (int i = 0; i < coverageEntries.size(); i++) {
                if (coverageEntries.get(i).begin > newEntry.begin) {
                    insertionIndex = i;
                    break;
                }
            }
            
            coverageEntries.add(insertionIndex, newEntry);
            requiresUpdate = true;
        }
        
        // Try to merge adjacent entries with the same texture
        mergeSimilarEntries();
    }
    
    /**
     * Attempts to merge adjacent or overlapping entries with the same texture
     */
    private void mergeSimilarEntries() {
        if (coverageEntries.size() < 2) return;
        
        boolean merged;
        do {
            merged = false;
            
            for (int i = 0; i < coverageEntries.size() - 1; i++) {
                CoverageEntry current = coverageEntries.get(i);
                
                for (int j = i + 1; j < coverageEntries.size(); j++) {
                    CoverageEntry next = coverageEntries.get(j);
                    
                    // If entries have the same texture and overlap or are adjacent
                    if (current.texture.equals(next.texture) && 
                        ((current.begin <= next.end && current.end >= next.begin) ||
                         (current.end == next.begin - 1) || 
                         (current.begin == next.end + 1))) {
                        
                        // Merge the entries
                        current.begin = Math.min(current.begin, next.begin);
                        current.end = Math.max(current.end, next.end);
                        
                        // Remove the second entry
                        coverageEntries.remove(j);
                        merged = true;
                        break;
                    }
                }
                
                if (merged) break;
            }
        } while (merged);
    }



    public void clearCoverage(int begin, int end) {
        // Clears the coverage in the given area
        // Bottom coord is Inclusive, Top coord is NOT!
        
        if (begin >= end) return; // Invalid range
        
        List<CoverageEntry> entriesToRemove = new ArrayList<>();
        List<CoverageEntry> entriesToAdd = new ArrayList<>();
        
        for (CoverageEntry entry : coverageEntries) {
            // Check if this entry overlaps with the range to clear
            if (entry.begin < end && entry.end > begin) {
                // Entry overlaps with the range to clear
                
                // Case 1: Entry is completely within the range to clear
                if (entry.begin >= begin && entry.end <= end) {
                    entriesToRemove.add(entry);
                }
                // Case 2: Range to clear is completely within the entry - need to split
                else if (begin > entry.begin && end < entry.end) {
                    // Create two new entries
                    CoverageEntry lowerPart = new CoverageEntry(entry.begin, begin, entry.texture);
                    CoverageEntry upperPart = new CoverageEntry(end, entry.end, entry.texture);
                    
                    entriesToRemove.add(entry);
                    entriesToAdd.add(lowerPart);
                    entriesToAdd.add(upperPart);
                }
                // Case 3: Range to clear overlaps with the lower part of the entry
                else if (begin <= entry.begin && end < entry.end) {
                    entry.begin = end;
                }
                // Case 4: Range to clear overlaps with the upper part of the entry
                else if (begin > entry.begin && end >= entry.end) {
                    entry.end = begin;
                }
            }
        }
        
        // Remove entries marked for removal
        coverageEntries.removeAll(entriesToRemove);
        
        // Add new entries created from splits
        coverageEntries.addAll(entriesToAdd);
        
        // If we made any changes, mark for update
        if (!entriesToRemove.isEmpty() || !entriesToAdd.isEmpty()) {
            requiresUpdate = true;
        }
    }

    public void truncateLowerHalf(CoverageEntry entry, int pix) {
        // remove the pixels below this. the coordinate at pix is *Removed*.
        // moves bottom coordinate UP
        pix = clamp(pix+1, 0, 31);
        entry.begin = pix;
    }
    public void truncateUpperHalf(CoverageEntry entry, int pix) {
        // remove the pixels above this. the coordinate at pix is *Removed*
        // Moves top coordinate DOWN
        pix = clamp(pix-1, 0, 31);
        entry.end = pix;
    }


}
