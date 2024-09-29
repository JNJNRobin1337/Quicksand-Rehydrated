package net.mokai.quicksandrehydrated.entity.coverage;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.clamp;

public class PlayerCoverage {

    private List<CoverageEntry> coverageEntries;

    public PlayerCoverage() {
        this.coverageEntries = new ArrayList<>();
    }

    public void addCoverageEntry(CoverageEntry newEntry) {
        // sorts through list and decides where to put it

        int insertionIndex = -1;
        for (int i = 0; i < coverageEntries.size(); i++) {

            CoverageEntry currentEntry = coverageEntries.get(i);
            if (currentEntry.begin > newEntry.begin) {
                insertionIndex = i;
                break;
            }

        }

        if (insertionIndex >= 0) {
            insertionIndex = 0;
        }

        coverageEntries.add(insertionIndex, newEntry);

    }

    public void addCoverage(int begin, int end, ResourceLocation texture) {
        // TODO this needs to/should combine similar ones
        removeCoverage(begin, end);



    }

    public void removeCoverage(int begin, int end) {

        List<CoverageEntry> entriesToRemove = new ArrayList<>();

        for (CoverageEntry entry : coverageEntries) {

            boolean beginWithin = entry.begin <= end && entry.begin >= begin;
            boolean endWithin = entry.end <= end && entry.end >= begin;

            if (!beginWithin && !endWithin) {
                continue;
            }
            else {

                // if it falls completely in the bound, it needs to be entirely removed.
                if (beginWithin && endWithin) {
                    entriesToRemove.add(entry);
                }
                else {

                    // this coverage is only partially in the range
                    if (beginWithin) {
                        // Begin is inside the range, but the end is above. So this entry should begin further upwards
                        truncateLowerHalf(entry, end);
                    }
                    else {
                        // otherwise, End is in the range, but begin is further below it. So this entry should end further below the range.
                        truncateUpperHalf(entry, begin);
                    }

                }

            }

        }

        coverageEntries.removeAll(entriesToRemove);

    }

    public void truncateLowerHalf(CoverageEntry entry, int pix) {
        // remove the pixels below this. the coordinate at pix IS REMOVED.
        pix = clamp(pix+1, 0, 31);
        entry.begin = pix;
    }
    public void truncateUpperHalf(CoverageEntry entry, int pix) {
        // remove the pixels above this. the coordinate at pix IS REMOVED
        pix = clamp(pix-1, 0, 31);
        entry.end = pix;
    }


}
