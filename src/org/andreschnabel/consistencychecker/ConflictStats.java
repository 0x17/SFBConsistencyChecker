package org.andreschnabel.consistencychecker;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ConflictStats {

    public static Stream<String> statLinesForAllSubprojects(Path mppFilePath) {
        return statLinesForAllSubprojects(mppFilePath, null);
    }

    public static Stream<String> statLinesForAllSubprojects(Path mppFilePath, Consumer<Integer> progressCallback) {
        try {
            List<String> spnames = Utils.collectSubProjectNames(mppFilePath);

            Map<String, ConflictCollector> spNameToCollector = new HashMap<>();

            if(progressCallback != null) progressCallback.accept(0);

            int ctr = 0;
            for (String spname : spnames) {
                spNameToCollector.put(spname, ConsistencyChecker.checkConsistencyForTargetSubProject(spname, mppFilePath));
                if(progressCallback != null) progressCallback.accept((int)((float)(ctr+1)/(float)spnames.size()*100.0f));
                ctr++;
            }

            return spnames  .stream()
                            .sorted(Comparator.comparingInt(a -> spNameToCollector.get(a).totalCount()))
                            .map(spname -> "Statistics for " + spname + ": " + spNameToCollector.get(spname).statCounts() + " total count = " + spNameToCollector.get(spname).totalCount());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return Stream.empty();
    }
}
