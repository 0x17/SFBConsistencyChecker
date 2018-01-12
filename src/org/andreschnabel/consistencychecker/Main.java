package org.andreschnabel.consistencychecker;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.out.println("Usage:\nConflicts for subproject TP X: java -jar SFBConsistencyChecker.jar X");
            System.out.println("Conflict statistics: java-jar SFBConsistencyChecker.jar stats");
            return;
        }

        if(args[0].equals("stats")) {
            ConflictStats.showStatsForAllSubprojects();
            return;
        }

        ConflictCollector coll = ConsistencyChecker.checkConsistencyForTargetSubProject(args[0]);
        ConsistencyChecker.serializeConflicts(coll, args[0] + "_Conflicts.txt");
    }

}
