package org.andreschnabel.consistencychecker;

import java.nio.file.Paths;
import java.util.*;

public class ConflictStats {

    public static void showStatsForAllSubprojects() throws Exception {
        List<String> spnames = Utils.collectSubProjectNames(Paths.get("."));
        Map<String, ConflictCollector> spNameToCollector = new HashMap<>();

        for(String spname : spnames) {
            spNameToCollector.put(spname, ConsistencyChecker.checkConsistencyForTargetSubProject(spname));
        }

        spnames.sort(Comparator.comparingInt(a -> spNameToCollector.get(a).totalCount()));

        for(String spname : spnames) {
            ConflictCollector coll = spNameToCollector.get(spname);
            System.out.println("Statistics for " + spname + ": " + coll.statCounts() + " total count = " + coll.totalCount());
        }
    }
}
