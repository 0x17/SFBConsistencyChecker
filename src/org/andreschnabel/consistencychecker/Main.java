package org.andreschnabel.consistencychecker;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {
        if(showUsage(args)) return;

        Path mppFilePath = Paths.get(".");

        if(args[0].equals("stats")) {
            ConflictStats.statLinesForAllSubprojects(mppFilePath).forEach(System.out::println);
            return;
        }

        ConflictCollector coll = ConsistencyChecker.checkConsistencyForTargetSubProject(args[0], mppFilePath);
        ConsistencyChecker.serializeConflicts(coll, args[0] + "_Conflicts.txt");
    }

    private static boolean showUsage(String[] args) {
        if(args.length == 0) {
            System.out.println("USAGE\nConflicts for subproject TP X: java -jar SFBConsistencyChecker.jar X");
            System.out.println("Conflict statistics: java -jar SFBConsistencyChecker.jar stats");
            return true;
        }
        return false;
    }

}
