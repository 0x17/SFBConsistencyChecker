package org.andreschnabel.consistencychecker;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args[0].equals("stats")) {
            ConflictStats.showStatsForAllSubprojects();
            return;
        }
        ConflictCollector coll = ConsistencyChecker.checkConsistencyForTargetSubProject(args[0]);
        ConsistencyChecker.serializeConflicts(coll, args[0] + "_Conflicts.txt");
    }

}
