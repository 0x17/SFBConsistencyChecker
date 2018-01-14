package org.andreschnabel.consistencychecker;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ConflictStats {

    public static List<String> statLinesForAllSubprojects() throws Exception {
        List<String> spnames = Utils.collectSubProjectNames(Paths.get("."));
        Map<String, ConflictCollector> spNameToCollector = new HashMap<>();

        for(String spname : spnames) {
            spNameToCollector.put(spname, ConsistencyChecker.checkConsistencyForTargetSubProject(spname));
        }

        return spnames.stream().sorted(Comparator.comparingInt(a -> spNameToCollector.get(a).totalCount()))
                               .map(spname -> "Statistics for " + spname + ": " + spNameToCollector.get(spname).statCounts() + " total count = " + spNameToCollector.get(spname).totalCount())
                               .collect(Collectors.toList());
    }
}
